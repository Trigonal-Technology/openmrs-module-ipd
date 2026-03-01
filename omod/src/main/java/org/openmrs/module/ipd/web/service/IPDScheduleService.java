package org.openmrs.module.ipd.web.service;

import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.web.contract.ScheduleMedicationRequest;
import org.openmrs.module.ipd.web.model.PatientMedicationSummary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for IPD medication scheduling operations.
 * All emrapi (EncounterTransaction) dependencies have been replaced with native
 * OpenMRS types.
 */
public interface IPDScheduleService {

    Schedule saveMedicationSchedule(ScheduleMedicationRequest scheduleMedicationRequest);

    List<Slot> getMedicationSlots(String patientUuid, ServiceType serviceType, LocalDate forDate);

    List<Slot> getMedicationSlots(String patientUuid, ServiceType serviceType);

    List<Slot> getMedicationSlots(String patientUuid, ServiceType serviceType, List<String> orderUuids);

    Schedule updateMedicationSchedule(ScheduleMedicationRequest scheduleMedicationRequest);

    List<Slot> getMedicationSlotsForTheGivenTimeFrame(String patientUuid, LocalDateTime localStartDate,
            LocalDateTime localEndDate, Boolean considerAdministeredTime,
            Visit visit);

    /**
     * Handles drug-order stop events triggered from a saved encounter.
     * Replaces the previous emrapi-coupled
     * handlePostProcessEncounterTransaction(Encounter, EncounterTransaction).
     * Reads stopped DrugOrders directly from the native OpenMRS {@link Encounter}.
     *
     * @param encounter the just-saved OpenMRS Encounter; stopped DrugOrders are
     *                  read via encounter.getOrders()
     */
    void handleDrugOrderStop(Encounter encounter);

    List<PatientMedicationSummary> getSlotsForPatientListByTime(List<String> patientUuidList,
            LocalDateTime localStartDate,
            LocalDateTime localEndDate,
            Boolean includePreviousSlot,
            Boolean includeSlotDuration);
}
