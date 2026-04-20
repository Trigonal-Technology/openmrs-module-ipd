package org.openmrs.module.ipd.api.events.handler.impl;

import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.ipd.api.events.handler.IPDEventHandler;
import org.openmrs.module.ipd.api.events.model.IPDEvent;
import org.openmrs.module.ipd.api.service.PatientTaskTemplateService;
import org.openmrs.module.ipd.api.service.TaskInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Event handler to clean up future tasks when a patient is discharged.
 * - Cancels all SCHEDULED and IN_PROGRESS tasks for the patient
 * - Deactivates all active PatientTaskTemplate assignments
 */
@Component
public class PatientDischargeEventHandler implements IPDEventHandler {

    private static final Logger log = LoggerFactory.getLogger(PatientDischargeEventHandler.class);

    @Autowired
    private TaskInstanceService taskInstanceService;

    @Autowired
    private PatientTaskTemplateService patientTaskTemplateService;

    @Autowired
    private PatientService patientService;

    @Override
    @Transactional
    public void handleEvent(IPDEvent event) {
        String patientUuid = event.getPatientUuid();
        if (patientUuid == null) {
            log.warn("Patient discharge event received without patient UUID");
            return;
        }
        
        log.info("Processing discharge for patient: {}", patientUuid);
        
        try {
            // Note: We need to get the Patient object from the patient service
            // For now, we'll use the UUID to find the patient in the service layer
            Patient patient = patientService.getPatientByUuid(patientUuid);
            if (patient == null) {
                log.warn("Patient not found for UUID: {}", patientUuid);
                return;
            }
            
            LocalDateTime now = LocalDateTime.now();
            
            // Cancel all future task instances for the patient
            int cancelledCount = taskInstanceService.cancelFutureInstances(patient, now);
            log.info("Cancelled {} future task instances for patient {}", cancelledCount, patientUuid);
            
            // Deactivate all patient-task template assignments
            patientTaskTemplateService.deactivateForPatient(patient);
            log.info("Deactivated all task template assignments for patient {}", patientUuid);
            
        } catch (Exception e) {
            log.error("Error processing discharge for patient: {}" + patientUuid, e);
        }
    }
}
