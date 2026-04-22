package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ipd.api.dao.PatientTaskTemplateDAO;
import org.openmrs.module.ipd.api.model.PatientTaskTemplate;
import org.openmrs.module.ipd.api.model.TaskTemplate;
import org.openmrs.module.ipd.api.service.PatientTaskTemplateService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class PatientTaskTemplateServiceImpl extends BaseOpenmrsService implements PatientTaskTemplateService {

    private PatientTaskTemplateDAO patientTaskTemplateDAO;

    public void setPatientTaskTemplateDAO(PatientTaskTemplateDAO patientTaskTemplateDAO) {
        this.patientTaskTemplateDAO = patientTaskTemplateDAO;
    }

    @Override
    public PatientTaskTemplate savePatientTaskTemplate(PatientTaskTemplate patientTaskTemplate) {
        return patientTaskTemplateDAO.savePatientTaskTemplate(patientTaskTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientTaskTemplate getPatientTaskTemplateByUuid(String uuid) {
        return patientTaskTemplateDAO.getPatientTaskTemplateByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientTaskTemplate> getPatientTaskTemplatesByPatient(Patient patient) {
        return patientTaskTemplateDAO.getPatientTaskTemplatesByPatient(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientTaskTemplate> getActivePatientTaskTemplatesByPatient(Patient patient) {
        return patientTaskTemplateDAO.getActivePatientTaskTemplatesByPatient(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientTaskTemplate> getActivePatientTaskTemplatesByWard(Location ward) {
        return patientTaskTemplateDAO.getActivePatientTaskTemplatesByWard(ward);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientTaskTemplate> getAllActivePatientTaskTemplates() {
        return patientTaskTemplateDAO.getAllActivePatientTaskTemplates();
    }

    @Override
    public PatientTaskTemplate applyTemplateToPatient(TaskTemplate template, Patient patient, Location ward,
                                                        LocalDateTime startDate, LocalDateTime endDate) {
        PatientTaskTemplate patientTaskTemplate = new PatientTaskTemplate();
        patientTaskTemplate.setPatient(patient);
        patientTaskTemplate.setTemplate(template);
        patientTaskTemplate.setWard(ward);
        patientTaskTemplate.setStartDate(startDate != null ? startDate : LocalDateTime.now());
        patientTaskTemplate.setEndDate(endDate);
        patientTaskTemplate.setActive(true);
        return patientTaskTemplateDAO.savePatientTaskTemplate(patientTaskTemplate);
    }

    @Override
    public void deactivateForPatient(Patient patient) {
        patientTaskTemplateDAO.deactivateForPatient(patient);
    }
}
