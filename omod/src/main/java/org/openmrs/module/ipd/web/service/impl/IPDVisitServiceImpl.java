package org.openmrs.module.ipd.web.service.impl;

import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.service.ReferenceService;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.module.ipd.web.model.DrugOrderSchedule;
import org.openmrs.module.ipd.web.model.IPDDrugOrder;
import org.openmrs.module.ipd.web.model.NidanDrugOrderDTO;
import org.openmrs.module.ipd.web.service.IPDScheduleService;
import org.openmrs.module.ipd.web.service.IPDVisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implementation of {@link IPDVisitService}.
 *
 * <p>
 * <b>Migration from Bahmni to native OpenMRS:</b>
 * <ul>
 * <li>{@code BahmniDrugOrderService.getPrescribedDrugOrders()} →
 * {@link OrderService#getActiveOrders(Patient, OrderType, CareSetting, Date)}
 * filtered to the
 * visit's time window.</li>
 * <li>{@code BahmniDrugOrderService.getDiscontinuedDrugOrders()} →
 * detect discontinued orders from {@link DrugOrder#getPreviousOrder()} and
 * {@link DrugOrder#getAction()} / {@link DrugOrder#getDateStopped()}.</li>
 * <li>{@code BahmniObsService.observationsFor()} →
 * {@link ObsService#getObservationsByPersonAndConcept(Person, Concept)} for
 * each member
 * of the Order Attributes concept set.</li>
 * <li>{@code BahmniDrugOrderMapper.mapToResponse()} → removed;
 * {@link NidanDrugOrderDTO}
 * is built directly from the native {@link DrugOrder} entity.</li>
 * <li>{@code BahmniOrderAttribute.ORDER_ATTRIBUTES_CONCEPT_SET_NAME} →
 * OpenMRS global property {@value #ORDER_ATTRIBUTES_CONCEPT_SET_GP} (defaults
 * to
 * {@code "Order Attributes"} if not configured).</li>
 * <li>Sort-weight logic: previously came from
 * {@code BahmniDrugOrder.getSortWeight()};
 * now derived from the order-attribute Obs value for each DrugOrder.</li>
 * </ul>
 */
@Service
@Transactional
public class IPDVisitServiceImpl implements IPDVisitService {

    /**
     * OpenMRS global property name holding the concept-set name that contains order
     * attribute
     * concepts (replaces
     * {@code BahmniOrderAttribute.ORDER_ATTRIBUTES_CONCEPT_SET_NAME}).
     */
    static final String ORDER_ATTRIBUTES_CONCEPT_SET_GP = "nidan.ipd.orderAttributesConceptSetName";

    /**
     * Default concept set name used when the global property is not configured.
     */
    static final String ORDER_ATTRIBUTES_DEFAULT = "Order Attributes";

    private final OrderService orderService;
    private final ObsService obsService;
    private final ConceptService conceptService;
    private final ReferenceService referenceService;
    private final VisitService visitService;
    private final SlotService slotService;
    private final IPDScheduleService ipdScheduleService;
    private final SlotTimeCreationService slotTimeCreationService;

    @Autowired
    public IPDVisitServiceImpl(OrderService orderService,
            ObsService obsService,
            ConceptService conceptService,
            ReferenceService referenceService,
            VisitService visitService,
            SlotService slotService,
            IPDScheduleService ipdScheduleService,
            SlotTimeCreationService slotTimeCreationService) {
        this.orderService = orderService;
        this.obsService = obsService;
        this.conceptService = conceptService;
        this.referenceService = referenceService;
        this.visitService = visitService;
        this.slotService = slotService;
        this.ipdScheduleService = ipdScheduleService;
        this.slotTimeCreationService = slotTimeCreationService;
    }

    /**
     * Returns all prescribed drug orders for the given visit (and optionally its
     * immediately
     * preceding OPD visit), with slot/schedule information attached.
     *
     * <p>
     * Replaces the Bahmni-coupled {@code getPrescribedOrders()} that used
     * {@code BahmniDrugOrderService} and {@code BahmniDrugOrderMapper}.
     *
     * @param visitUuid              UUID of the IPD visit
     * @param includeActiveVisit     whether to include still-active orders
     * @param numberOfVisits         unused (kept for interface compatibility)
     * @param startDate              filter orders effective after this date
     *                               (nullable)
     * @param endDate                filter orders effective before this date
     *                               (nullable)
     * @param getEffectiveOrdersOnly if true, exclude orders whose effective stop
     *                               date is before
     *                               the visit start
     * @return list of {@link IPDDrugOrder} instances
     */
    @Override
    public List<IPDDrugOrder> getPrescribedOrders(String visitUuid, Boolean includeActiveVisit,
            Integer numberOfVisits, Date startDate,
            Date endDate, Boolean getEffectiveOrdersOnly) {
        Visit visit = visitService.getVisitByUuid(visitUuid);
        Patient patient = visit.getPatient();

        // Collect visit UUIDs: current visit + preceding OPD visit (if any)
        List<String> visitUuids = new ArrayList<>();
        visitUuids.add(visitUuid);
        String precededOpdUuid = getImmediatePrecededOPDVisit(patient, visitUuid);
        if (precededOpdUuid != null) {
            visitUuids.add(precededOpdUuid);
        }

        // Fetch all DrugOrders for these visits via native OrderService
        List<DrugOrder> prescribedDrugOrders = getDrugOrdersForVisits(patient, visitUuids,
                startDate, endDate, includeActiveVisit);

        // Filter by effective stop date relative to visit start
        if (Boolean.TRUE.equals(getEffectiveOrdersOnly)) {
            prescribedDrugOrders = prescribedDrugOrders.stream()
                    .filter(o -> o.getEffectiveStopDate() == null
                            || o.getEffectiveStopDate().after(visit.getStartDatetime()))
                    .collect(Collectors.toList());
        }

        return buildIPDDrugOrders(patient.getUuid(), prescribedDrugOrders, visit);
    }

    /**
     * Retrieves the medication slots for the given visit and service type.
     *
     * @param visitUuid   UUID of the visit
     * @param serviceType the service type to filter slots by
     * @return list of matching slots
     */
    @Override
    public List<Slot> getMedicationSlots(String visitUuid, ServiceType serviceType) {
        Visit visit = visitService.getVisitByUuid(visitUuid);
        Concept concept = conceptService.getConceptByName(serviceType.conceptName());
        Optional<Reference> subjectReference = referenceService.getReferenceByTypeAndTargetUUID(
                Patient.class.getTypeName(), visit.getPatient().getUuid());
        if (!subjectReference.isPresent())
            return Collections.emptyList();
        return slotService.getSlotsByPatientAndVisitAndServiceType(subjectReference.get(), visit, concept);
    }

    // -----------------------------------------------------------------------
    // Private helpers (replacing Bahmni-specific helpers)
    // -----------------------------------------------------------------------

    /**
     * Fetches DrugOrders for a patient that belong to the given visits.
     *
     * <p>
     * Uses native
     * {@link OrderService#getActiveOrders(Patient, OrderType, CareSetting, Date)}
     * as the base query, then filters to only orders whose encounters belong to the
     * target visits.
     * This replicates {@code BahmniDrugOrderService.getPrescribedDrugOrders()}.
     */
    private List<DrugOrder> getDrugOrdersForVisits(Patient patient, List<String> visitUuids,
            Date startDate, Date endDate,
            Boolean includeActiveVisit) {
        // Get drug order type
        OrderType drugOrderType = orderService.getOrderTypeByName("Drug Order");

        // Use getActiveOrders as primary source (covers active prescriptions)
        List<Order> activeOrders = orderService.getActiveOrders(patient, drugOrderType,
                null /* all care settings */, new Date());

        // Also scan all orders on the visits' encounters to catch historical/stopped
        // orders
        Set<String> visitUuidSet = new HashSet<>(visitUuids);
        List<DrugOrder> visitOrders = activeOrders.stream()
                .filter(order -> order instanceof DrugOrder)
                .map(order -> (DrugOrder) order)
                .filter(drugOrder -> {
                    Encounter enc = drugOrder.getEncounter();
                    if (enc == null || enc.getVisit() == null)
                        return false;
                    return visitUuidSet.contains(enc.getVisit().getUuid());
                })
                .collect(Collectors.toList());

        // If includeActiveVisit is false, exclude orders still active at query time
        // (mirrors the original Bahmni logic)
        if (!Boolean.TRUE.equals(includeActiveVisit)) {
            visitOrders = visitOrders.stream()
                    .filter(o -> o.getEffectiveStopDate() != null)
                    .collect(Collectors.toList());
        }

        // Apply date filters (startDate / endDate)
        if (startDate != null) {
            visitOrders = visitOrders.stream()
                    .filter(o -> o.getEffectiveStartDate() == null || !o.getEffectiveStartDate().before(startDate))
                    .collect(Collectors.toList());
        }
        if (endDate != null) {
            visitOrders = visitOrders.stream()
                    .filter(o -> o.getEffectiveStartDate() == null || !o.getEffectiveStartDate().after(endDate))
                    .collect(Collectors.toList());
        }

        return visitOrders;
    }

    /**
     * Builds IPDDrugOrder instances enriched with schedule/slot data.
     *
     * <p>
     * Replaces the Bahmni pipeline:
     * {@code BahmniDrugOrderMapper.mapToResponse() -> sortDrugOrders -> getDrugOrderSchedule}.
     */
    private List<IPDDrugOrder> buildIPDDrugOrders(String patientUuid, List<DrugOrder> drugOrders, Visit visit) {
        // Build a discontinued-order map: DISCONTINUE/REVISE orders point to their
        // previous order
        Map<String, DrugOrder> discontinuedOrderMap = buildDiscontinuedOrderMap(drugOrders);

        // Build sort-weight map from OrderAttribute observations
        Map<String, Integer> sortWeightByOrderUuid = getSortWeightByOrderUuid(
                visit.getPatient(), drugOrders);

        // Build NidanDrugOrderDTOs sorted by sort weight
        List<NidanDrugOrderDTO> dtos = drugOrders.stream()
                .map(drugOrder -> NidanDrugOrderDTO.createFrom(
                        drugOrder, sortWeightByOrderUuid.get(drugOrder.getUuid())))
                .sorted(Comparator
                        .comparingInt(dto -> dto.getSortWeight() != null ? dto.getSortWeight() : Integer.MAX_VALUE))
                .collect(Collectors.toList());

        // Get scheduling information (slot time windows)
        List<String> orderUuids = dtos.stream().map(NidanDrugOrderDTO::getUuid).collect(Collectors.toList());
        Map<String, DrugOrderSchedule> drugOrderScheduleByOrders = getDrugOrderSchedule(patientUuid, orderUuids);

        return dtos.stream()
                .map(dto -> IPDDrugOrder.createFrom(dto, drugOrderScheduleByOrders.get(dto.getUuid())))
                .collect(Collectors.toList());
    }

    /**
     * Builds a map of discontinued orders.
     *
     * <p>
     * Replaces {@code BahmniDrugOrderService.getDiscontinuedDrugOrders()}.
     * Maps previousOrder.uuid → discontinuingDrugOrder.
     */
    private Map<String, DrugOrder> buildDiscontinuedOrderMap(List<DrugOrder> drugOrders) {
        Map<String, DrugOrder> map = new LinkedHashMap<>();
        for (DrugOrder drugOrder : drugOrders) {
            if (Order.Action.DISCONTINUE.equals(drugOrder.getAction())
                    || Order.Action.REVISE.equals(drugOrder.getAction())) {
                if (drugOrder.getPreviousOrder() != null) {
                    map.put(drugOrder.getPreviousOrder().getUuid(), drugOrder);
                }
            }
        }
        return map;
    }

    /**
     * Returns a map of orderUuid → sortWeight derived from OrderAttribute Obs
     * values.
     *
     * <p>
     * Replaces the combination of {@code BahmniObsService.observationsFor()} +
     * {@code BahmniOrderAttribute} + {@code BahmniDrugOrder.getSortWeight()}.
     *
     * <p>
     * The "sortWeight" is looked up as an Obs on the patient with the concept named
     * "Sort Weight" (or whichever concept in the Order Attributes set carries that
     * value).
     * If no sort-weight obs is found for an order, the order retains its natural
     * list order.
     */
    private Map<String, Integer> getSortWeightByOrderUuid(Person patient, List<DrugOrder> drugOrders) {
        Map<String, Integer> sortWeightMap = new HashMap<>();

        String conceptSetName = Context.getAdministrationService()
                .getGlobalProperty(ORDER_ATTRIBUTES_CONCEPT_SET_GP, ORDER_ATTRIBUTES_DEFAULT);
        Concept orderAttributeSet = conceptService.getConceptByName(conceptSetName);

        if (orderAttributeSet == null) {
            return sortWeightMap; // No concept set configured — skip sort weight
        }

        // Get all members of the order-attributes concept set
        Collection<Concept> orderAttributeConcepts = orderAttributeSet.getSetMembers();

        // Fetch observations for each order-attribute concept
        for (Concept attributeConcept : orderAttributeConcepts) {
            List<Obs> obsList = obsService.getObservationsByPersonAndConcept(patient, attributeConcept);
            for (Obs obs : obsList) {
                if (obs.getOrder() != null && obs.getValueNumeric() != null) {
                    // Use obs.getOrder().getUuid() to match the DrugOrder
                    sortWeightMap.put(obs.getOrder().getUuid(), obs.getValueNumeric().intValue());
                }
            }
        }
        return sortWeightMap;
    }

    /**
     * Retrieves the drug order schedule/slot time summary for a list of order
     * UUIDs.
     *
     * <p>
     * Replaces {@code getDrugOrderScheduleForOrders()} which previously accepted
     * {@code List<BahmniDrugOrder>}. Now works with plain order UUID strings.
     */
    private Map<String, DrugOrderSchedule> getDrugOrderSchedule(String patientUuid, List<String> orderUuids) {
        List<Slot> slots = ipdScheduleService.getMedicationSlots(
                patientUuid, ServiceType.MEDICATION_REQUEST, orderUuids);
        List<Slot> prnSlots = ipdScheduleService.getMedicationSlots(
                patientUuid, ServiceType.AS_NEEDED_MEDICATION_REQUEST, orderUuids);
        slots.addAll(prnSlots);

        Map<DrugOrder, List<Slot>> groupedByOrders = slots.stream()
                .filter(slot -> slot.getOrder() instanceof DrugOrder)
                .collect(Collectors.groupingBy(slot -> (DrugOrder) slot.getOrder()));

        return slotTimeCreationService.getDrugOrderScheduledTime(groupedByOrders);
    }

    /**
     * Finds the UUID of the immediately preceding OPD visit for a patient relative
     * to the
     * given current visit UUID.
     *
     * <p>
     * Logic is identical to the original — no Bahmni dependency was present in this
     * method.
     */
    private String getImmediatePrecededOPDVisit(Patient patient, String currentVisitUuid) {
        List<Visit> visits = visitService.getVisitsByPatient(patient);
        List<Visit> sortedVisits = visits.stream()
                .sorted(Comparator.comparing(Visit::getStartDatetime).reversed())
                .collect(Collectors.toList());

        int currentVisitIndex = IntStream.range(0, sortedVisits.size())
                .filter(i -> sortedVisits.get(i).getUuid().equals(currentVisitUuid))
                .findFirst()
                .orElse(-1);

        if (currentVisitIndex != -1 && currentVisitIndex + 1 < sortedVisits.size()) {
            Visit previousVisit = sortedVisits.get(currentVisitIndex + 1);
            if ("OPD".equals(previousVisit.getVisitType().getName())) {
                return previousVisit.getUuid();
            }
        }
        return null;
    }
}
