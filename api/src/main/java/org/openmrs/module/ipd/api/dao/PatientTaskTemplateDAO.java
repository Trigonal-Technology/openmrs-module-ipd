package org.openmrs.module.ipd.api.dao;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.model.PatientTaskTemplate;
import org.openmrs.module.ipd.api.model.TaskTemplate;

import java.util.List;

public interface PatientTaskTemplateDAO {

    PatientTaskTemplate savePatientTaskTemplate(PatientTaskTemplate patientTaskTemplate) throws DAOException;

    PatientTaskTemplate getPatientTaskTemplateById(Integer patientTemplateId) throws DAOException;

    PatientTaskTemplate getPatientTaskTemplateByUuid(String uuid) throws DAOException;

    List<PatientTaskTemplate> getPatientTaskTemplatesByPatient(Patient patient) throws DAOException;

    List<PatientTaskTemplate> getActivePatientTaskTemplatesByPatient(Patient patient) throws DAOException;

    List<PatientTaskTemplate> getPatientTaskTemplatesByTemplate(TaskTemplate template) throws DAOException;

    List<PatientTaskTemplate> getActivePatientTaskTemplatesByWard(Location ward) throws DAOException;

    List<PatientTaskTemplate> getAllActivePatientTaskTemplates() throws DAOException;

    void deactivateForPatient(Patient patient) throws DAOException;
}
