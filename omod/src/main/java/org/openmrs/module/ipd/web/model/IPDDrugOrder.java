package org.openmrs.module.ipd.web.model;

import lombok.*;

/**
 * IPD drug order model — wraps the native NIDAN drug order DTO.
 * Replaces the previous Bahmni-coupled version that wrapped BahmniDrugOrder.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IPDDrugOrder {

    private NidanDrugOrderDTO nidanDrugOrder;
    private DrugOrderSchedule drugOrderSchedule;

    public static IPDDrugOrder createFrom(NidanDrugOrderDTO nidanDrugOrderDTO, DrugOrderSchedule drugOrderSchedule) {
        return IPDDrugOrder.builder()
                .nidanDrugOrder(nidanDrugOrderDTO)
                .drugOrderSchedule(drugOrderSchedule)
                .build();
    }
}
