package org.openmrs.module.ipd.api.audit;

import org.openmrs.User;
import org.openmrs.module.ipd.api.model.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for task-related audit logging.
 * Tracks all important actions for compliance and debugging.
 */
public interface TaskAuditService {

    /**
     * Log when a task template is created
     */
    void logTemplateCreated(TaskTemplate template, User createdBy);

    /**
     * Log when a task template is updated
     */
    void logTemplateUpdated(TaskTemplate template, User updatedBy, String changes);

    /**
     * Log when a task template is voided
     */
    void logTemplateVoided(TaskTemplate template, User voidedBy, String reason);

    /**
     * Log when a task is created
     */
    void logTaskCreated(Task task, User createdBy);

    /**
     * Log when a task status changes
     */
    void logTaskStatusChanged(Task task, Task.TaskStatus oldStatus, 
                                   Task.TaskStatus newStatus, User changedBy, String reason);

    /**
     * Log when a doctor acknowledges a task
     */
    void logTaskAcknowledged(TaskAcknowledgment acknowledgment, User acknowledgedBy);

    /**
     * Log when tasks are cancelled (e.g., on patient discharge)
     */
    void logTasksCancelled(String patientUuid, int count, User cancelledBy, String reason);

    /**
     * Log when cancelled tasks are archived
     */
    void logTasksArchived(int count, User archivedBy);

    /**
     * Log when a task template is applied to a patient
     */
    void logTemplateApplied(PatientTaskTemplate patientTemplate, User appliedBy);

    /**
     * Log when patient task templates are deactivated (e.g., on discharge)
     */
    void logTemplatesDeactivated(String patientUuid, User deactivatedBy);

    /**
     * Get audit logs for a specific task
     */
    List<TaskAuditLog> getAuditLogsForTask(String taskUuid, LocalDateTime from, LocalDateTime to);

    /**
     * Get audit logs for a specific user
     */
    List<TaskAuditLog> getAuditLogsForUser(Integer userId, LocalDateTime from, LocalDateTime to);

    /**
     * Get audit logs by action type
     */
    List<TaskAuditLog> getAuditLogsByAction(String action, LocalDateTime from, LocalDateTime to);
}
