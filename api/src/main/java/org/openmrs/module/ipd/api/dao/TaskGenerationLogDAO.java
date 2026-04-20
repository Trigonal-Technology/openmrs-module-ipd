package org.openmrs.module.ipd.api.dao;

import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.model.PatientTaskTemplate;
import org.openmrs.module.ipd.api.model.TaskGenerationLog;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskGenerationLogDAO {

    TaskGenerationLog saveTaskGenerationLog(TaskGenerationLog log) throws DAOException;

    TaskGenerationLog getTaskGenerationLogById(Integer logId) throws DAOException;

    List<TaskGenerationLog> getLogsByPatientTemplate(PatientTaskTemplate patientTemplate) throws DAOException;

    List<TaskGenerationLog> getPendingGenerationLogs(LocalDateTime before) throws DAOException;

    boolean isAlreadyGenerated(PatientTaskTemplate patientTemplate, LocalDateTime scheduledTime) throws DAOException;

    TaskGenerationLog getLogByPatientTemplateAndTime(PatientTaskTemplate patientTemplate, LocalDateTime scheduledTime) throws DAOException;
}
