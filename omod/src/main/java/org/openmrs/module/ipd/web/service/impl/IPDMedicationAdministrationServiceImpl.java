package org.openmrs.module.ipd.web.service.impl;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.model.MedicationAdministration;
import org.openmrs.module.ipd.api.model.Schedule;
import org.openmrs.module.ipd.api.model.ServiceType;
import org.openmrs.module.ipd.api.model.Slot;
import org.openmrs.module.ipd.api.service.MedicationAdministrationService;
import org.openmrs.module.ipd.api.service.ScheduleService;
import org.openmrs.module.ipd.api.service.SlotService;
import org.openmrs.module.ipd.api.translators.MedicationAdministrationToSlotStatusTranslator;
import org.openmrs.module.ipd.api.util.DateTimeUtil;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationRequest;
import org.openmrs.module.ipd.web.contract.ScheduleMedicationRequest;
import org.openmrs.module.ipd.web.factory.MedicationAdministrationFactory;
import org.openmrs.module.ipd.web.factory.ScheduleFactory;
import org.openmrs.module.ipd.web.factory.SlotFactory;
import org.openmrs.module.ipd.web.service.IPDMedicationAdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class IPDMedicationAdministrationServiceImpl implements IPDMedicationAdministrationService {

    private MedicationAdministrationService medicationAdministrationService;
    private MedicationAdministrationFactory medicationAdministrationFactory;
    private SlotFactory slotFactory;
    private SlotService slotService;
    private ScheduleService scheduleService;
    private MedicationAdministrationToSlotStatusTranslator medicationAdministrationToSlotStatusTranslator;
    private ScheduleFactory scheduleFactory;

    @Autowired
    public IPDMedicationAdministrationServiceImpl(MedicationAdministrationService medicationAdministrationService,
            MedicationAdministrationFactory medicationAdministrationFactory,
            SlotFactory slotFactory, SlotService slotService, ScheduleService scheduleService,
            MedicationAdministrationToSlotStatusTranslator medicationAdministrationToSlotStatusTranslator,
            ScheduleFactory scheduleFactory) {
        this.medicationAdministrationService = medicationAdministrationService;
        this.medicationAdministrationFactory = medicationAdministrationFactory;
        this.slotFactory = slotFactory;
        this.slotService = slotService;
        this.scheduleService = scheduleService;
        this.medicationAdministrationToSlotStatusTranslator = medicationAdministrationToSlotStatusTranslator;
        this.scheduleFactory = scheduleFactory;
    }

    private MedicationAdministration createMedicationAdministration(
            MedicationAdministrationRequest medicationAdministrationRequest) {
        MedicationAdministration medicationAdministration = medicationAdministrationFactory
                .mapRequestToMedicationAdministration(medicationAdministrationRequest, new MedicationAdministration());
        return medicationAdministrationService.saveMedicationAdministration(medicationAdministration);
    }

    @Override
    public MedicationAdministration saveScheduledMedicationAdministration(
            MedicationAdministrationRequest medicationAdministrationRequest) {
        Slot slot = slotService.getSlotByUUID(medicationAdministrationRequest.getSlotUuid());
        if (slot == null) {
            throw new RuntimeException("Slot not found");
        } else {
            if (slot.getMedicationAdministration() != null) {
                return medicationAdministrationService
                        .getMedicationAdministrationByUuid(slot.getMedicationAdministration().getUuid());
            }
            if (!StringUtils.isBlank(medicationAdministrationRequest.getUuid())) {
                return medicationAdministrationService
                        .getMedicationAdministrationByUuid(medicationAdministrationRequest.getUuid());
            }
            MedicationAdministration medicationAdministration = createMedicationAdministration(
                    medicationAdministrationRequest);
            slot.setStatus(
                    medicationAdministrationToSlotStatusTranslator.toSlotStatus(medicationAdministration.getStatus()));
            slot.setMedicationAdministration(medicationAdministration);
            slotService.saveSlot(slot);
            return medicationAdministration;
        }
    }

    @Override
    public MedicationAdministration updateAdhocMedicationAdministration(String uuid,
            MedicationAdministrationRequest medicationAdministrationRequest) {
        MedicationAdministration existing = medicationAdministrationService.getMedicationAdministrationByUuid(uuid);
        MedicationAdministration medicationAdministration = medicationAdministrationFactory
                .mapRequestToMedicationAdministration(medicationAdministrationRequest, existing);
        return medicationAdministrationService.saveMedicationAdministration(medicationAdministration);
    }

    @Override
    public MedicationAdministration saveAdhocMedicationAdministration(
            MedicationAdministrationRequest medicationAdministrationRequest) {
        Patient patient = Context.getPatientService()
                .getPatientByUuid(medicationAdministrationRequest.getPatientUuid());
        List<Visit> activeVisits = Context.getVisitService().getActiveVisitsByPatient(patient);
        if (activeVisits.isEmpty()) {
            throw new RuntimeException("No active visit found for patient");
        }
        Visit visit = activeVisits.get(0);
        Schedule schedule = scheduleService.getScheduleByVisit(visit);
        if (schedule == null) {
            ScheduleMedicationRequest scheduleMedicationRequest = new ScheduleMedicationRequest();
            scheduleMedicationRequest.setPatientUuid(medicationAdministrationRequest.getPatientUuid());
            scheduleMedicationRequest
                    .setProviderUuid(medicationAdministrationRequest.getProviders().get(0).getProviderUuid());
            schedule = scheduleService
                    .saveSchedule(scheduleFactory.createScheduleForMedicationFrom(scheduleMedicationRequest, visit));
        }
        MedicationAdministration medicationAdministration = createMedicationAdministration(
                medicationAdministrationRequest);
        List<LocalDateTime> slotsStartTime = new ArrayList<>();
        slotsStartTime.add(
                DateTimeUtil.convertEpocUTCToLocalTimeZone(medicationAdministrationRequest.getAdministeredDateTime()));
        ServiceType serviceType = medicationAdministration.getDrugOrder() == null
                ? ServiceType.EMERGENCY_MEDICATION_REQUEST
                : ServiceType.AS_NEEDED_MEDICATION_REQUEST;
        slotFactory.createSlotsForMedicationFrom(schedule, slotsStartTime, medicationAdministration.getDrugOrder(),
                medicationAdministration, Slot.SlotStatus.COMPLETED, serviceType, "")
                .forEach(slotService::saveSlot);
        return medicationAdministration;
    }

}
