package org.openmrs.module.ipd.api.service;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.ipd.api.model.PatientTaskTemplate;
import org.openmrs.module.ipd.api.model.TaskTemplate;

import java.util.List;

public interface PatientTaskTemplateService extends OpenmrsService {

    @Authorized({ "Apply Task Templates" })
    PatientTaskTemplate savePatientTaskTemplate(PatientTaskTemplate patientTaskTemplate);

    @Authorized({ "Apply Task Templates", "Get Task Templates" })
    PatientTaskTemplate getPatientTaskTemplateByUuid(String uuid);

    @Authorized({ "Apply Task Templates", "Get Task Templates" })
    List<PatientTaskTemplate> getPatientTaskTemplatesByPatient(Patient patient);

    @Authorized({ "Apply Task Templates", "Get Task Templates" })
    List<PatientTaskTemplate> getActivePatientTaskTemplatesByPatient(Patient patient);

    @Authorized({ "Apply Task Templates", "Get Task Templates" })
    List<PatientTaskTemplate> getActivePatientTaskTemplatesByWard(Location ward);

    @Authorized({ "Apply Task Templates", "Get Task Templates" })
    List<PatientTaskTemplate> getAllActivePatientTaskTemplates();

    @Authorized({ "Apply Task Templates" })
    PatientTaskTemplate applyTemplateToPatient(TaskTemplate template, Patient patient, Location ward);

    @Authorized({ "Apply Task Templates", "Manage Task Cleanup" })
    void deactivateForPatient(Patient patient);
}
