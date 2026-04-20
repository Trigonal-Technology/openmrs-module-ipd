package org.openmrs.module.ipd.api.dao;

import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.audit.TaskAuditLog;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskAuditLogDAO {

    TaskAuditLog saveAuditLog(TaskAuditLog auditLog) throws DAOException;

    TaskAuditLog getAuditLogById(Integer auditId) throws DAOException;

    List<TaskAuditLog> getAuditLogsForEntity(String entityType, String entityUuid, 
                                                LocalDateTime from, LocalDateTime to) throws DAOException;

    List<TaskAuditLog> getAuditLogsForUser(Integer userId, LocalDateTime from, LocalDateTime to) throws DAOException;

    List<TaskAuditLog> getAuditLogsByAction(String action, LocalDateTime from, LocalDateTime to) throws DAOException;

    List<TaskAuditLog> getAuditLogsByPatient(String patientUuid, LocalDateTime from, LocalDateTime to) throws DAOException;

    List<TaskAuditLog> getAllAuditLogs(LocalDateTime from, LocalDateTime to, int limit) throws DAOException;
}
