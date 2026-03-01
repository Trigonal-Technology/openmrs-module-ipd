package org.openmrs.module.ipd.web.service.impl;

import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.model.Reference;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.service.ReferenceService;
import org.openmrs.module.ipd.api.service.ScheduleService;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.module.ipd.api.util.DateTimeUtil;
import org.openmrs.module.ipd.web.contract.ScheduleMedicationRequest;
import org.openmrs.module.ipd.web.factory.ScheduleFactory;
import org.openmrs.module.ipd.web.factory.SlotFactory;
import org.openmrs.module.ipd.web.model.PrescribedOrderSlotSummary;
import org.openmrs.module.ipd.web.model.PatientMedicationSummary;
import org.openmrs.module.ipd.web.service.IPDScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.openmrs.module.ipd.api.model.Slot.SlotStatus.SCHEDULED;

/**
 * Implementation of IPDScheduleService.
 *
 * <p>
 * All emrapi (EncounterTransaction) and Bahmni dependencies have been removed
 * and replaced with
 * native OpenMRS APIs. Key changes from the original Bahmni version:
 * <ul>
 * <li>{@code handlePostProcessEncounterTransaction(Encounter, EncounterTransaction)}
 * removed;
 * replaced by {@link #handleDrugOrderStop(Encounter)} which reads stopped
 * orders directly
 * from the native OpenMRS {@link Encounter}.</li>
 * <li>Global property renamed from
 * {@code bahmni-ipd.allowSlotStopOnDrugOrderStop} to
 * {@code nidan.ipd.allowSlotStopOnDrugOrderStop}.</li>
 * <li>{@link AdministrationService} is now injected instead of accessed via
 * {@code Context.getAdministrationService()} to avoid static anti-pattern.</li>
 * <li>NPE guard added to {@link #saveMedicationSchedule} for empty active-visit
 * list.</li>
 * </ul>
 */
@Service
@Transactional
public class IPDScheduleServiceImpl implements IPDScheduleService {

    /**
     * Global property key that controls whether slots are stopped when a drug order
     * is stopped.
     * Renamed from the original "bahmni-ipd.allowSlotStopOnDrugOrderStop" for NIDAN
     * EHR.
     *
     * @see #handleDrugOrderStop(Encounter)
     */
    static final String ALLOW_SLOT_STOP_GP = "nidan.ipd.allowSlotStopOnDrugOrderStop";

    private final ScheduleService scheduleService;
    private final ScheduleFactory scheduleFactory;
    private final SlotFactory slotFactory;
    private final SlotService slotService;
    private final SlotTimeCreationService slotTimeCreationService;
    private final ConceptService conceptService;
    private final ReferenceService referenceService;
    private final VisitService visitService;
    private final PatientService patientService;
    private final OrderService orderService;
    private final AdministrationService administrationService;

    @Autowired
    public IPDScheduleServiceImpl(ScheduleService scheduleService,
            ScheduleFactory scheduleFactory,
            SlotFactory slotFactory,
            SlotService slotService,
            SlotTimeCreationService slotTimeCreationService,
            ConceptService conceptService,
            ReferenceService referenceService,
            VisitService visitService,
            PatientService patientService,
            OrderService orderService,
            AdministrationService administrationService) {
        this.scheduleService = scheduleService;
        this.scheduleFactory = scheduleFactory;
        this.slotFactory = slotFactory;
        this.slotService = slotService;
        this.slotTimeCreationService = slotTimeCreationService;
        this.conceptService = conceptService;
        this.referenceService = referenceService;
        this.visitService = visitService;
        this.patientService = patientService;
        this.orderService = orderService;
        this.administrationService = administrationService;
    }

    @Override
    public Schedule saveMedicationSchedule(ScheduleMedicationRequest scheduleMedicationRequest) {
        Patient patient = patientService.getPatientByUuid(scheduleMedicationRequest.getPatientUuid());
        // Bug fix: original code used .get(0) without null/empty check →
        // IndexOutOfBoundsException
        List<Visit> activeVisits = visitService.getActiveVisitsByPatient(patient);
        if (activeVisits == null || activeVisits.isEmpty()) {
            throw new IllegalStateException("No active visit found for patient: " + patient.getUuid());
        }
        Visit visit = activeVisits.get(0);
        Schedule savedSchedule = scheduleService.getScheduleByVisit(visit);
        if (savedSchedule == null || savedSchedule.getId() == null) {
            Schedule schedule = scheduleFactory.createScheduleForMedicationFrom(scheduleMedicationRequest, visit);
            savedSchedule = scheduleService.saveSchedule(schedule);
        }
        DrugOrder order = (DrugOrder) orderService.getOrderByUuid(scheduleMedicationRequest.getOrderUuid());
        ServiceType serviceType = scheduleMedicationRequest.getServiceType() != null
                ? scheduleMedicationRequest.getServiceType()
                : ServiceType.MEDICATION_REQUEST;

        if (serviceType.equals(ServiceType.MEDICATION_REQUEST)) {
            List<Slot> existingSlots = getMedicationSlots(patient.getUuid(), ServiceType.MEDICATION_REQUEST,
                    Collections.singletonList(order.getUuid()));
            if (existingSlots != null && !existingSlots.isEmpty()) {
                throw new RuntimeException("Slots already created for this drug order");
            }
            List<LocalDateTime> slotsStartTime = slotTimeCreationService.createSlotsStartTimeFrom(
                    scheduleMedicationRequest, order);
            slotFactory.createSlotsForMedicationFrom(savedSchedule, slotsStartTime, order, null, SCHEDULED,
                    ServiceType.MEDICATION_REQUEST, scheduleMedicationRequest.getComments())
                    .forEach(slotService::saveSlot);
        } else if (serviceType.equals(ServiceType.AS_NEEDED_PLACEHOLDER)) {
            Slot slot = slotFactory.createAsNeededPlaceholderSlot(savedSchedule, order,
                    scheduleMedicationRequest.getComments());
            slotService.saveSlot(slot);
        }
        return savedSchedule;
    }

    @Override
    public List<Slot> getMedicationSlots(String patientUuid, ServiceType serviceType, LocalDate forDate) {
        Concept concept = conceptService.getConceptByName(serviceType.conceptName());
        Optional<Reference> subjectReference = referenceService.getReferenceByTypeAndTargetUUID(
                Patient.class.getTypeName(), patientUuid);
        if (!subjectReference.isPresent())
            return Collections.emptyList();
        return slotService.getSlotsBySubjectReferenceIdAndForDateAndServiceType(subjectReference.get(), forDate,
                concept);
    }

    @Override
    public List<Slot> getMedicationSlots(String patientUuid, ServiceType serviceType) {
        Concept concept = conceptService.getConceptByName(serviceType.conceptName());
        Optional<Reference> subjectReference = referenceService.getReferenceByTypeAndTargetUUID(
                Patient.class.getTypeName(), patientUuid);
        if (!subjectReference.isPresent())
            return Collections.emptyList();
        return slotService.getSlotsBySubjectReferenceIdAndServiceType(subjectReference.get(), concept);
    }

    @Override
    public List<Slot> getMedicationSlots(String patientUuid, ServiceType serviceType, List<String> orderUuids) {
        Concept concept = conceptService.getConceptByName(serviceType.conceptName());
        Optional<Reference> subjectReference = referenceService.getReferenceByTypeAndTargetUUID(
                Patient.class.getTypeName(), patientUuid);
        if (!subjectReference.isPresent())
            return Collections.emptyList();
        return slotService.getSlotsBySubjectReferenceIdAndServiceTypeAndOrderUuids(subjectReference.get(), concept,
                orderUuids);
    }

    @Override
    public Schedule updateMedicationSchedule(ScheduleMedicationRequest scheduleMedicationRequest) {
        voidExistingMedicationSlotsForOrder(scheduleMedicationRequest.getPatientUuid(),
                scheduleMedicationRequest.getOrderUuid(), "");
        return saveMedicationSchedule(scheduleMedicationRequest);
    }

    private void voidExistingMedicationSlotsForOrder(String patientUuid, String orderUuid, String voidReason) {
        List<Slot> existingSlots = getMedicationSlots(patientUuid, ServiceType.MEDICATION_REQUEST,
                Collections.singletonList(orderUuid));
        existingSlots.forEach(slot -> slotService.voidSlot(slot, voidReason));
    }

    @Override
    public List<Slot> getMedicationSlotsForTheGivenTimeFrame(String patientUuid, LocalDateTime localStartDate,
            LocalDateTime localEndDate,
            Boolean considerAdministeredTime, Visit visit) {
        Optional<Reference> subjectReference = referenceService.getReferenceByTypeAndTargetUUID(
                Patient.class.getTypeName(), patientUuid);
        if (!subjectReference.isPresent())
            return Collections.emptyList();
        if (considerAdministeredTime) {
            return slotService.getSlotsBySubjectReferenceIncludingAdministeredTimeFrame(
                    subjectReference.get(), localStartDate, localEndDate, visit);
        }
        return slotService.getSlotsBySubjectReferenceIdAndForTheGivenTimeFrame(
                subjectReference.get(), localStartDate, localEndDate, visit);
    }

    /**
     * Handles drug-order stop logic triggered by an encounter save event.
     *
     * <p>
     * This method replaces the old emrapi-coupled
     * {@code handlePostProcessEncounterTransaction(Encounter, EncounterTransaction)}.
     * It reads {@link DrugOrder}s directly from the native OpenMRS
     * {@link Encounter} — no
     * emrapi dependency required.
     *
     * <p>
     * Only runs when global property {@value #ALLOW_SLOT_STOP_GP} is {@code true}.
     *
     * @param encounter the just-saved OpenMRS Encounter
     */
    @Override
    public void handleDrugOrderStop(Encounter encounter) {
        boolean allowSlotStop = Boolean.parseBoolean(
                administrationService.getGlobalProperty(ALLOW_SLOT_STOP_GP, "false"));
        if (!allowSlotStop)
            return;

        // Find DrugOrders on this encounter that are being stopped/discontinued/revised
        List<DrugOrder> stoppedDrugOrders = encounter.getOrders().stream()
                .filter(order -> order instanceof DrugOrder)
                .map(order -> (DrugOrder) order)
                .filter(drugOrder -> drugOrder.getDateStopped() != null
                        || Order.Action.DISCONTINUE.equals(drugOrder.getAction())
                        || Order.Action.REVISE.equals(drugOrder.getAction()))
                .collect(Collectors.toList());

        String patientUuid = encounter.getPatient().getUuid();
        for (DrugOrder drugOrder : stoppedDrugOrders) {
            // For REVISE/DISCONTINUE, the previous order holds the original UUID
            Order previousOrder = drugOrder.getPreviousOrder();
            String orderUuidToStop = previousOrder != null ? previousOrder.getUuid() : drugOrder.getUuid();

            List<Slot> existingSlots = getMedicationSlots(patientUuid, ServiceType.MEDICATION_REQUEST,
                    Collections.singletonList(orderUuidToStop));
            if (existingSlots == null || existingSlots.isEmpty())
                continue;

            boolean atLeastOneMedicationAdministered = existingSlots.stream()
                    .anyMatch(slot -> slot.getMedicationAdministration() != null);

            if (atLeastOneMedicationAdministered) {
                // Mark future un-administered slots as STOPPED
                Date effectiveStopDate = drugOrder.getEffectiveStopDate();
                existingSlots.forEach(slot -> {
                    if (slot.getMedicationAdministration() == null
                            && !slot.isStopped()
                            && effectiveStopDate != null
                            && DateTimeUtil.convertDateToLocalDateTime(effectiveStopDate)
                                    .compareTo(slot.getStartDateTime()) < 0) {
                        slot.setStatus(Slot.SlotStatus.STOPPED);
                        slotService.saveSlot(slot);
                    }
                });
            } else {
                // No medication has been administered yet — void all future slots
                existingSlots.forEach(slot -> slotService.voidSlot(slot, ""));
            }
        }
    }

    @Override
    public List<PatientMedicationSummary> getSlotsForPatientListByTime(List<String> patientUuidList,
            LocalDateTime localStartDate,
            LocalDateTime localEndDate,
            Boolean includePreviousSlot,
            Boolean includeSlotDuration) {
        List<Slot> slots = slotService.getSlotsForPatientListByTime(patientUuidList, localStartDate, localEndDate);

        List<Slot> previousSlots = null;
        if (Boolean.TRUE.equals(includePreviousSlot)) {
            previousSlots = slotService.getImmediatePreviousSlotsForPatientListByTime(patientUuidList, localStartDate);
        }

        List<Object[]> slotsDuration = null;
        if (Boolean.TRUE.equals(includeSlotDuration)) {
            List<Order> orders = slots.stream()
                    .map(Slot::getOrder)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            List<Concept> serviceTypes = new ArrayList<>();
            serviceTypes.add(Context.getConceptService().getConceptByName(
                    ServiceType.MEDICATION_REQUEST.conceptName()));
            serviceTypes.add(Context.getConceptService().getConceptByName(
                    ServiceType.AS_NEEDED_MEDICATION_REQUEST.conceptName()));
            slotsDuration = slotService.getSlotDurationForPatientsByOrder(orders, serviceTypes);
        }

        return groupSlotsByMedicationsAndPatients(slots, previousSlots, slotsDuration);
    }

    private List<PatientMedicationSummary> groupSlotsByMedicationsAndPatients(List<Slot> currentSlots,
            List<Slot> previousSlots,
            List<Object[]> slotsDuration) {
        Map<String, Map<String, List<Slot>>> groupedSlots = currentSlots.stream()
                .collect(Collectors.groupingBy(
                        slot -> slot.getSchedule().getSubject().getTargetUuid(),
                        Collectors.groupingBy(
                                slot -> slot.getOrder() != null ? slot.getOrder().getUuid() : "emergencyMedications")));

        return groupedSlots.entrySet().stream()
                .map(entry -> {
                    PatientMedicationSummary patientMedicationSummary = new PatientMedicationSummary();
                    patientMedicationSummary.setPatientUuid(entry.getKey());
                    List<PrescribedOrderSlotSummary> prescribedOrderSlotsSummaryList = entry.getValue()
                            .entrySet().stream()
                            .filter(subEntry -> subEntry.getKey() != null
                                    && !subEntry.getKey().equals("emergencyMedications"))
                            .map(subEntry -> {
                                PrescribedOrderSlotSummary prescribedOrderSlotSummary = new PrescribedOrderSlotSummary();
                                prescribedOrderSlotSummary.setOrderUuid(subEntry.getKey());
                                prescribedOrderSlotSummary.setCurrentSlots(subEntry.getValue());
                                if (previousSlots != null) {
                                    prescribedOrderSlotSummary.setPreviousSlot(previousSlots.stream()
                                            .filter(slot -> slot.getOrder().getUuid().equals(subEntry.getKey()))
                                            .findFirst()
                                            .orElse(null));
                                }
                                if (slotsDuration != null) {
                                    Object[] durationObj = slotsDuration.stream()
                                            .filter(item -> ((Order) item[0]).getUuid().equals(subEntry.getKey()))
                                            .findFirst()
                                            .orElse(null);
                                    if (durationObj != null) {
                                        prescribedOrderSlotSummary.setInitialSlotStartTime(
                                                DateTimeUtil
                                                        .convertLocalDateTimeToUTCEpoc((LocalDateTime) durationObj[1]));
                                        prescribedOrderSlotSummary.setFinalSlotStartTime(
                                                DateTimeUtil
                                                        .convertLocalDateTimeToUTCEpoc((LocalDateTime) durationObj[2]));
                                    }
                                }
                                return prescribedOrderSlotSummary;
                            })
                            .collect(Collectors.toList());
                    patientMedicationSummary.setPrescribedOrderSlots(prescribedOrderSlotsSummaryList);
                    patientMedicationSummary.setEmergencyMedicationSlots(entry.getValue().get("emergencyMedications"));
                    return patientMedicationSummary;
                })
                .collect(Collectors.toList());
    }
}
