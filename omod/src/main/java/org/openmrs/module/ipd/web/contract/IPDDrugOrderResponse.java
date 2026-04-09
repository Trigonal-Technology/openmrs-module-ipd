package org.openmrs.module.ipd.web.contract;

import lombok.*;
import org.openmrs.module.ipd.web.model.IPDDrugOrder;
import org.openmrs.module.ipd.web.model.NidanDrugOrderDTO;

/**
 * REST response DTO for a prescribed drug order on an IPD visit.
 * Previously coupled to emrapi EncounterTransaction.DrugOrder — now uses native
 * OpenMRS data
 * via NidanDrugOrderDTO.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IPDDrugOrderResponse {

    // --- Core order identity ---
    private String orderUuid;
    private String orderNumber;
    private String action;
    private String careSetting;
    private String previousOrderUuid;
    private String commentToFulfiller;
    private String orderReasonText;

    // --- Drug / concept info (mirrors Bahmni's drug + concept sub-objects) ---
    private String drugUuid;
    private String drugName;
    private String drugForm; // e.g. "Tablet", "Injection"
    private String drugStrength; // e.g. "40 mg"
    private String conceptUuid;
    private String conceptName;

    // --- Dosing instructions ---
    private Double dose;
    private String doseUnitsName;
    private String routeName;
    private String frequency;
    private Boolean asNeeded;
    private String asNeededCondition; // free-text PRN condition, e.g. "for diarrhoea"
    private Double quantity;
    private String quantityUnitsName;
    private String administrationInstructions; // e.g. "As directed" or plain-text note
    private String additionalInstructions; // e.g. "take before food"

    // --- Duration ---
    private Integer duration;
    private String durationUnitsName;

    // --- Dates ---
    private java.util.Date dateActivated;
    private java.util.Date scheduledDate;
    private java.util.Date effectiveStartDate;
    private java.util.Date effectiveStopDate;
    private java.util.Date autoExpireDate;
    private java.util.Date dateStopped;

    // --- Metadata ---
    private String orderSetUuid;
    private String providerUuid;
    private String providerName;

    /** Schedule/slot information */
    private DrugOrderScheduleResponse drugOrderSchedule;

    /**
     * Builds response from the wrapped IPDDrugOrder model.
     *
     * @param ipdDrugOrder the IPD drug order model
     * @return populated response DTO
     */
    public static IPDDrugOrderResponse createFrom(IPDDrugOrder ipdDrugOrder) {
        NidanDrugOrderDTO dto = ipdDrugOrder.getNidanDrugOrder();
        IPDDrugOrderResponse response = IPDDrugOrderResponse.builder()
                // identity
                .orderUuid(dto.getUuid())
                .orderNumber(dto.getOrderNumber())
                .action(dto.getAction())
                .careSetting(dto.getCareSetting())
                .previousOrderUuid(dto.getPreviousOrderUuid())
                .commentToFulfiller(dto.getCommentToFulfiller())
                .orderReasonText(dto.getOrderReasonText())
                // drug / concept
                .drugUuid(dto.getDrugUuid())
                .drugName(dto.getDrugName())
                .drugForm(dto.getDrugForm())
                .drugStrength(dto.getDrugStrength())
                .conceptUuid(dto.getConceptUuid())
                .conceptName(dto.getConceptName())
                // dosing
                .dose(dto.getDose())
                .doseUnitsName(dto.getDoseUnitsName())
                .routeName(dto.getRouteName())
                .frequency(dto.getFrequency())
                .asNeeded(dto.getAsNeeded())
                .asNeededCondition(dto.getAsNeededCondition())
                .quantity(dto.getQuantity())
                .quantityUnitsName(dto.getQuantityUnitsName())
                .administrationInstructions(dto.getAdministrationInstructions())
                .additionalInstructions(dto.getAdditionalInstructions())
                // duration
                .duration(dto.getDuration())
                .durationUnitsName(dto.getDurationUnitsName())
                // dates
                .dateActivated(dto.getDateActivated())
                .scheduledDate(dto.getScheduledDate())
                .effectiveStartDate(dto.getEffectiveStartDate())
                .effectiveStopDate(dto.getEffectiveStopDate())
                .autoExpireDate(dto.getAutoExpireDate())
                .dateStopped(dto.getDateStopped())
                // metadata
                .orderSetUuid(dto.getOrderSetUuid())
                .providerUuid(dto.getProviderUuid())
                .providerName(dto.getProviderName())
                .build();
        if (ipdDrugOrder.getDrugOrderSchedule() != null) {
            response.setDrugOrderSchedule(DrugOrderScheduleResponse.createFrom(ipdDrugOrder.getDrugOrderSchedule()));
        }
        return response;
    }

    @Override
    public boolean equals(Object otherOrder) {
        if (otherOrder == null)
            return false;
        if (!(otherOrder instanceof IPDDrugOrderResponse))
            return false;
        return super.equals(otherOrder);
    }
}
