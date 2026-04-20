package org.openmrs.module.ipd.api.audit.impl;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.audit.TaskAuditLog;
import org.openmrs.module.ipd.api.audit.TaskAuditService;
import org.openmrs.module.ipd.api.dao.TaskAuditLogDAO;
import org.openmrs.module.ipd.api.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class TaskAuditServiceImpl implements TaskAuditService {

    @Autowired
    private TaskAuditLogDAO taskAuditLogDAO;

    public void setTaskAuditLogDAO(TaskAuditLogDAO taskAuditLogDAO) {
        this.taskAuditLogDAO = taskAuditLogDAO;
    }

    private TaskAuditLog createAuditLog(String action, String entityType, String entityUuid, 
                                         User user, String details, String patientUuid) {
        TaskAuditLog log = new TaskAuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityUuid(entityUuid);
        log.setUser(user);
        log.setUserName(user != null ? user.getUsername() : "system");
        log.setActionTime(LocalDateTime.now());
        log.setDetails(details);
        log.setPatientUuid(patientUuid);
        log.setDateCreated(new java.util.Date());
        
        // Try to capture IP and session info if available
        try {
            if (Context.getUserContext() != null) {
                // Note: IP address and session ID would need to be captured from the request context
                // This is a placeholder for the actual implementation
            }
        } catch (Exception e) {
            // Ignore - audit logging should not fail the main operation
        }
        
        return taskAuditLogDAO.saveAuditLog(log);
    }

    @Override
    public void logTemplateCreated(TaskTemplate template, User createdBy) {
        String details = String.format("Template created: name=%s, taskType=%s, ward=%s",
                template.getName(),
                template.getTaskType() != null ? template.getTaskType().getName() : "null",
                template.getWard() != null ? template.getWard().getName() : "global");
        createAuditLog("TEMPLATE_CREATED", "TaskTemplate", template.getUuid(), createdBy, details, null);
    }

    @Override
    public void logTemplateUpdated(TaskTemplate template, User updatedBy, String changes) {
        String details = String.format("Template updated: %s", changes);
        createAuditLog("TEMPLATE_UPDATED", "TaskTemplate", template.getUuid(), updatedBy, details, null);
    }

    @Override
    public void logTemplateVoided(TaskTemplate template, User voidedBy, String reason) {
        String details = String.format("Template voided: name=%s, reason=%s", template.getName(), reason);
        createAuditLog("TEMPLATE_VOIDED", "TaskTemplate", template.getUuid(), voidedBy, details, null);
    }

    @Override
    public void logInstanceCreated(TaskInstance instance, User createdBy) {
        String patientUuid = instance.getPatient() != null ? instance.getPatient().getUuid() : null;
        String details = String.format("Instance created: name=%s, scheduledTime=%s, priority=%s",
                instance.getName(), instance.getScheduledTime(), instance.getPriority());
        createAuditLog("INSTANCE_CREATED", "TaskInstance", instance.getUuid(), createdBy, details, patientUuid);
    }

    @Override
    public void logInstanceStatusChanged(TaskInstance instance, TaskInstanceStatus oldStatus,
                                          TaskInstanceStatus newStatus, User changedBy, String reason) {
        String patientUuid = instance.getPatient() != null ? instance.getPatient().getUuid() : null;
        String details = String.format("Status changed: %s -> %s, reason=%s", oldStatus, newStatus, reason);
        createAuditLog("STATUS_CHANGED", "TaskInstance", instance.getUuid(), changedBy, details, patientUuid);
    }

    @Override
    public void logTaskCompleted(TaskCompletion completion, User completedBy) {
        TaskInstance instance = completion.getInstance();
        String patientUuid = instance != null && instance.getPatient() != null 
                ? instance.getPatient().getUuid() : null;
        String details = String.format("Task completed by %s at %s, method=%s",
                completedBy.getUsername(), completion.getCompletionTime(), completion.getCompletionMethod());
        createAuditLog("TASK_COMPLETED", "TaskInstance", 
                instance != null ? instance.getUuid() : null, completedBy, details, patientUuid);
    }

    @Override
    public void logTaskAcknowledged(TaskAcknowledgment acknowledgment, User acknowledgedBy) {
        TaskInstance instance = acknowledgment.getInstance();
        String patientUuid = instance != null && instance.getPatient() != null 
                ? instance.getPatient().getUuid() : null;
        String details = String.format("Task acknowledged by doctor %s at %s, method=%s, billable=%s",
                acknowledgment.getAcknowledgedBy() != null ? acknowledgment.getAcknowledgedBy().getName() : "unknown",
                acknowledgment.getAcknowledgmentTime(),
                acknowledgment.getAcknowledgmentMethod(),
                acknowledgment.isBillable());
        createAuditLog("TASK_ACKNOWLEDGED", "TaskInstance",
                instance != null ? instance.getUuid() : null, acknowledgedBy, details, patientUuid);
    }

    @Override
    public void logTasksCancelled(String patientUuid, int count, User cancelledBy, String reason) {
        String details = String.format("Cancelled %d future tasks for patient. Reason: %s", count, reason);
        createAuditLog("TASKS_CANCELLED", "Patient", patientUuid, cancelledBy, details, patientUuid);
    }

    @Override
    public void logTasksArchived(int count, User archivedBy) {
        String details = String.format("Archived %d cancelled task instances", count);
        createAuditLog("TASKS_ARCHIVED", "TaskInstance", null, archivedBy, details, null);
    }

    @Override
    public void logTemplateApplied(PatientTaskTemplate patientTemplate, User appliedBy) {
        String patientUuid = patientTemplate.getPatient() != null ? patientTemplate.getPatient().getUuid() : null;
        String templateName = patientTemplate.getTemplate() != null ? patientTemplate.getTemplate().getName() : "unknown";
        String details = String.format("Template '%s' applied to patient starting %s",
                templateName, patientTemplate.getStartDate());
        createAuditLog("TEMPLATE_APPLIED", "PatientTaskTemplate", patientTemplate.getUuid(), appliedBy, details, patientUuid);
    }

    @Override
    public void logTemplatesDeactivated(String patientUuid, User deactivatedBy) {
        String details = "All task template assignments deactivated (patient discharged)";
        createAuditLog("TEMPLATES_DEACTIVATED", "Patient", patientUuid, deactivatedBy, details, patientUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskAuditLog> getAuditLogsForInstance(String instanceUuid, LocalDateTime from, LocalDateTime to) {
        return taskAuditLogDAO.getAuditLogsForEntity("TaskInstance", instanceUuid, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskAuditLog> getAuditLogsForUser(Integer userId, LocalDateTime from, LocalDateTime to) {
        return taskAuditLogDAO.getAuditLogsForUser(userId, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskAuditLog> getAuditLogsByAction(String action, LocalDateTime from, LocalDateTime to) {
        return taskAuditLogDAO.getAuditLogsByAction(action, from, to);
    }
}
