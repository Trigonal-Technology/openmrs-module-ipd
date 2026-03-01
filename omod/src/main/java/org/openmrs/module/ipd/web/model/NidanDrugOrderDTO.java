package org.openmrs.module.ipd.web.model;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
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

    private String uuid;
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
    private Date scheduledDate;
    private Date effectiveStartDate;
    private Date effectiveStopDate;
    private Date dateStopped;
    private String action;
    private String orderSetUuid; // replaces BahmniDrugOrder.getOrderGroup().getOrderSet().getUuid()
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
        NidanDrugOrderDTO dto = new NidanDrugOrderDTO();
        dto.uuid = drugOrder.getUuid();
        dto.drugName = drugOrder.getDrug() != null ? drugOrder.getDrug().getDisplayName() : null;
        dto.dose = drugOrder.getDose();
        dto.doseUnitsName = conceptName(drugOrder.getDoseUnits());
        dto.routeName = conceptName(drugOrder.getRoute());
        dto.durationUnitsName = conceptName(drugOrder.getDurationUnits());
        dto.duration = drugOrder.getDuration();
        dto.quantity = drugOrder.getQuantity();
        dto.quantityUnitsName = conceptName(drugOrder.getQuantityUnits());
        dto.frequency = drugOrder.getFrequency() != null ? drugOrder.getFrequency().getName() : null;
        dto.asNeeded = drugOrder.getAsNeeded();
        dto.scheduledDate = drugOrder.getScheduledDate();
        dto.effectiveStartDate = drugOrder.getEffectiveStartDate();
        dto.effectiveStopDate = drugOrder.getEffectiveStopDate();
        dto.dateStopped = drugOrder.getDateStopped();
        dto.action = drugOrder.getAction() != null ? drugOrder.getAction().name() : null;
        dto.sortWeight = sortWeight;

        OrderGroup og = drugOrder.getOrderGroup();
        if (og != null && og.getOrderSet() != null) {
            dto.orderSetUuid = og.getOrderSet().getUuid();
        }

        if (drugOrder.getOrderer() != null) {
            Provider orderer = drugOrder.getOrderer();
            dto.providerUuid = orderer.getUuid();
            dto.providerName = orderer.getName();
        }

        return dto;
    }

    private static String conceptName(Concept concept) {
        return concept != null ? concept.getDisplayString() : null;
    }

    // --- Getters ---

    public String getUuid() {
        return uuid;
    }

    public String getDrugName() {
        return drugName;
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

    public String getDurationUnitsName() {
        return durationUnitsName;
    }

    public Integer getDuration() {
        return duration;
    }

    public Double getQuantity() {
        return quantity;
    }

    public String getQuantityUnitsName() {
        return quantityUnitsName;
    }

    public String getFrequency() {
        return frequency;
    }

    public Boolean getAsNeeded() {
        return asNeeded;
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

    public Date getDateStopped() {
        return dateStopped;
    }

    public String getAction() {
        return action;
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
