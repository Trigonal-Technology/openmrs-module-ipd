package org.openmrs.module.ipd.web.model;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.Provider;

import java.util.Date;

/**
 * Native NIDAN DTO that replaces
 * org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder.
 * Built from the standard OpenMRS {@link DrugOrder} entity — no Bahmni
 * dependency.
 */
public class NidanDrugOrderDTO {

    // --- Core order identity ---
    private String uuid;
    private String orderNumber; // e.g. "ORD-5"
    private String action;
    private String careSetting; // INPATIENT / OUTPATIENT
    private String previousOrderUuid; // for REVISE/DISCONTINUE chain
    private String commentToFulfiller; // pharmacy instructions
    private String orderReasonText; // clinical reason

    // --- Drug / Concept info (mirrors Bahmni's drug + concept sub-objects) ---
    private String drugUuid;
    private String drugName;
    private String drugForm; // e.g. "Tablet", "Injection"
    private String drugStrength; // e.g. "40 mg"
    private String conceptUuid; // generic drug concept UUID
    private String conceptName; // generic drug concept name

    // --- Dosing instructions (mirrors Bahmni's dosingInstructions) ---
    private Double dose;
    private String doseUnitsName;
    private String routeName;
    private String frequency;
    private Boolean asNeeded;
    private String asNeededCondition; // free-text condition for PRN (as-needed) orders, e.g. "for diarrhoea"
    private Double quantity;
    private String quantityUnitsName;
    private String administrationInstructions; // e.g. "As directed" or plain-text dosing note
    private String additionalInstructions; // e.g. "take before food"

    // --- Duration ---
    private Integer duration;
    private String durationUnitsName;

    // --- Dates ---
    private Date dateActivated; // when the order was placed
    private Date scheduledDate;
    private Date effectiveStartDate;
    private Date effectiveStopDate;
    private Date autoExpireDate; // mirrors Bahmni's autoExpireDate
    private Date dateStopped;

    // --- Metadata ---
    private String orderSetUuid;
    private Integer sortWeight;
    private String providerUuid;
    private String providerName;

    /**
     * Factory method: builds a NidanDrugOrderDTO from a native OpenMRS DrugOrder.
     *
     * @param drugOrder  the OpenMRS DrugOrder entity
     * @param sortWeight optional sort weight from ObsService order-attribute obs;
     *                   null if not found
     * @return populated DTO
     */
    public static NidanDrugOrderDTO createFrom(DrugOrder drugOrder, Integer sortWeight) {
        return createFrom(drugOrder, sortWeight, null);
    }

    /**
     * Factory method that also accepts the discontinuing order, if any.
     *
     * <p>
     * When {@code discontinuingOrder} is non-null the original order was
     * discontinued;
     * its {@code action} field is overridden to {@code "DISCONTINUE"} and
     * {@code dateStopped} is taken from the discontinuing order so the frontend can
     * render the correct state.
     *
     * @param drugOrder          the OpenMRS DrugOrder entity (NEW/RENEW action)
     * @param sortWeight         optional sort weight; null if not found
     * @param discontinuingOrder the DISCONTINUE action order that stopped this
     *                           order, or null
     * @return populated DTO
     */
    public static NidanDrugOrderDTO createFrom(DrugOrder drugOrder, Integer sortWeight, DrugOrder discontinuingOrder) {
        NidanDrugOrderDTO dto = new NidanDrugOrderDTO();

        // --- Core order identity ---
        dto.uuid = drugOrder.getUuid();
        dto.orderNumber = drugOrder.getOrderNumber();
        dto.careSetting = drugOrder.getCareSetting() != null ? drugOrder.getCareSetting().getName() : null;
        dto.commentToFulfiller = drugOrder.getCommentToFulfiller();
        dto.orderReasonText = drugOrder.getOrderReasonNonCoded();
        dto.previousOrderUuid = drugOrder.getPreviousOrder() != null
                ? drugOrder.getPreviousOrder().getUuid()
                : null;

        // --- Drug / Concept ---
        if (drugOrder.getDrug() != null) {
            dto.drugUuid = drugOrder.getDrug().getUuid();
            dto.drugName = drugOrder.getDrug().getDisplayName();
            dto.drugForm = drugOrder.getDrug().getDosageForm() != null
                    ? drugOrder.getDrug().getDosageForm().getDisplayString()
                    : null;
            dto.drugStrength = drugOrder.getDrug().getStrength();
        }
        if (drugOrder.getConcept() != null) {
            dto.conceptUuid = drugOrder.getConcept().getUuid();
            dto.conceptName = drugOrder.getConcept().getDisplayString();
        }

        // --- Dosing instructions ---
        dto.dose = drugOrder.getDose();
        dto.doseUnitsName = conceptName(drugOrder.getDoseUnits());
        dto.routeName = conceptName(drugOrder.getRoute());
        dto.frequency = drugOrder.getFrequency() != null ? drugOrder.getFrequency().getName() : null;
        dto.asNeeded = drugOrder.getAsNeeded();
        dto.asNeededCondition = drugOrder.getAsNeededCondition(); // e.g. "for diarrhoea"
        dto.quantity = drugOrder.getQuantity();
        dto.quantityUnitsName = conceptName(drugOrder.getQuantityUnits());
        // dosing_instructions column may hold:
        //   (a) Bahmni JSON: {"instructions":"...","additionalInstructions":"..."}
        //   (b) Plain text:  "take throughout the day, mix 1 pack with 1 litre"
        // parseAdministrationInstructions handles both cases.
        parseAdministrationInstructions(dto, drugOrder.getDosingInstructions());

        // --- Duration ---
        dto.duration = drugOrder.getDuration();
        dto.durationUnitsName = conceptName(drugOrder.getDurationUnits());

        // --- Dates ---
        dto.dateActivated = drugOrder.getDateActivated();
        dto.scheduledDate = drugOrder.getScheduledDate();
        dto.effectiveStartDate = drugOrder.getEffectiveStartDate();
        dto.effectiveStopDate = drugOrder.getEffectiveStopDate();
        dto.autoExpireDate = drugOrder.getAutoExpireDate();
        dto.dateStopped = drugOrder.getDateStopped();
        dto.sortWeight = sortWeight;

        // --- Action (with discontinued override) ---
        if (discontinuingOrder != null) {
            dto.action = Order.Action.DISCONTINUE.name();
            if (discontinuingOrder.getDateStopped() != null) {
                dto.dateStopped = discontinuingOrder.getDateStopped();
            }
        } else {
            dto.action = drugOrder.getAction() != null ? drugOrder.getAction().name() : null;
        }

        // --- Order group / set ---
        OrderGroup og = drugOrder.getOrderGroup();
        if (og != null && og.getOrderSet() != null) {
            dto.orderSetUuid = og.getOrderSet().getUuid();
        }

        // --- Provider ---
        if (drugOrder.getOrderer() != null) {
            Provider orderer = drugOrder.getOrderer();
            dto.providerUuid = orderer.getUuid();
            dto.providerName = orderer.getName();
        }

        return dto;
    }

    /**
     * Populates {@code administrationInstructions} and {@code additionalInstructions}
     * from the {@code dosing_instructions} DB column, which may contain either:
     * <ul>
     *   <li><b>Bahmni JSON</b>: {@code {"instructions":"As directed","additionalInstructions":"take before food"}}</li>
     *   <li><b>Plain text</b>: {@code "take throughout the day, mix 1 pack with 1 litre"}</li>
     * </ul>
     * For plain text, the whole string is used as {@code administrationInstructions}.
     */
    private static void parseAdministrationInstructions(NidanDrugOrderDTO dto, String dosingInstructions) {
        if (dosingInstructions == null || dosingInstructions.trim().isEmpty()) {
            return;
        }
        String trimmed = dosingInstructions.trim();
        if (trimmed.startsWith("{")) {
            // Bahmni JSON format: extract individual keys
            dto.administrationInstructions = extractJsonStringValue(trimmed, "instructions");
            dto.additionalInstructions = extractJsonStringValue(trimmed, "additionalInstructions");
        } else {
            // Plain-text format: use the whole string as the primary instruction
            dto.administrationInstructions = trimmed;
        }
    }

    /**
     * Extracts a string value from a flat JSON object without external
     * dependencies.
     */
    private static String extractJsonStringValue(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx < 0)
            return null;
        int colonIdx = json.indexOf(':', keyIdx + search.length());
        if (colonIdx < 0)
            return null;
        int quoteStart = json.indexOf('"', colonIdx + 1);
        if (quoteStart < 0)
            return null;
        int quoteEnd = json.indexOf('"', quoteStart + 1);
        if (quoteEnd < 0)
            return null;
        String value = json.substring(quoteStart + 1, quoteEnd).trim();
        return value.isEmpty() ? null : value;
    }

    private static String conceptName(Concept concept) {
        return concept != null ? concept.getDisplayString() : null;
    }

    // --- Getters ---

    public String getUuid() {
        return uuid;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getAction() {
        return action;
    }

    public String getCareSetting() {
        return careSetting;
    }

    public String getPreviousOrderUuid() {
        return previousOrderUuid;
    }

    public String getCommentToFulfiller() {
        return commentToFulfiller;
    }

    public String getOrderReasonText() {
        return orderReasonText;
    }

    public String getDrugUuid() {
        return drugUuid;
    }

    public String getDrugName() {
        return drugName;
    }

    public String getDrugForm() {
        return drugForm;
    }

    public String getDrugStrength() {
        return drugStrength;
    }

    public String getConceptUuid() {
        return conceptUuid;
    }

    public String getConceptName() {
        return conceptName;
    }

    public Double getDose() {
        return dose;
    }

    public String getDoseUnitsName() {
        return doseUnitsName;
    }

    public String getRouteName() {
        return routeName;
    }

    public String getFrequency() {
        return frequency;
    }

    public Boolean getAsNeeded() {
        return asNeeded;
    }

    public String getAsNeededCondition() {
        return asNeededCondition;
    }

    public Double getQuantity() {
        return quantity;
    }

    public String getQuantityUnitsName() {
        return quantityUnitsName;
    }

    public String getAdministrationInstructions() {
        return administrationInstructions;
    }

    public String getAdditionalInstructions() {
        return additionalInstructions;
    }

    public Integer getDuration() {
        return duration;
    }

    public String getDurationUnitsName() {
        return durationUnitsName;
    }

    public Date getDateActivated() {
        return dateActivated;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public Date getEffectiveStartDate() {
        return effectiveStartDate;
    }

    public Date getEffectiveStopDate() {
        return effectiveStopDate;
    }

    public Date getAutoExpireDate() {
        return autoExpireDate;
    }

    public Date getDateStopped() {
        return dateStopped;
    }

    public String getOrderSetUuid() {
        return orderSetUuid;
    }

    public Integer getSortWeight() {
        return sortWeight;
    }

    public String getProviderUuid() {
        return providerUuid;
    }

    public String getProviderName() {
        return providerName;
    }
}
