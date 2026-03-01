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

    /** Core drug order information from OpenMRS DrugOrder entity. */
    private String orderUuid;
    private String drugName;
    private Double dose;
    private String doseUnitsName;
    private String routeName;
    private String durationUnitsName;
    private Integer duration;
    private Double quantity;
    private String quantityUnitsName;
    private String frequency;
    private Boolean asNeeded;
    private String action;
    private String orderSetUuid;

    /** Prescribing provider */
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
                .orderUuid(dto.getUuid())
                .drugName(dto.getDrugName())
                .dose(dto.getDose())
                .doseUnitsName(dto.getDoseUnitsName())
                .routeName(dto.getRouteName())
                .durationUnitsName(dto.getDurationUnitsName())
                .duration(dto.getDuration())
                .quantity(dto.getQuantity())
                .quantityUnitsName(dto.getQuantityUnitsName())
                .frequency(dto.getFrequency())
                .asNeeded(dto.getAsNeeded())
                .action(dto.getAction())
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
