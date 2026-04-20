package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.Provider;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ipd.api.dao.TaskAcknowledgmentDAO;
import org.openmrs.module.ipd.api.model.TaskAcknowledgment;
import org.openmrs.module.ipd.api.model.TaskInstance;
import org.openmrs.module.ipd.api.service.TaskAcknowledgmentService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class TaskAcknowledgmentServiceImpl extends BaseOpenmrsService implements TaskAcknowledgmentService {

    private TaskAcknowledgmentDAO taskAcknowledgmentDAO;

    public void setTaskAcknowledgmentDAO(TaskAcknowledgmentDAO taskAcknowledgmentDAO) {
        this.taskAcknowledgmentDAO = taskAcknowledgmentDAO;
    }

    @Override
    public TaskAcknowledgment saveTaskAcknowledgment(TaskAcknowledgment acknowledgment) {
        return taskAcknowledgmentDAO.saveTaskAcknowledgment(acknowledgment);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskAcknowledgment getTaskAcknowledgmentByUuid(String uuid) {
        return taskAcknowledgmentDAO.getTaskAcknowledgmentByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskAcknowledgment getTaskAcknowledgmentByInstance(TaskInstance instance) {
        return taskAcknowledgmentDAO.getTaskAcknowledgmentByInstance(instance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskAcknowledgment> getTaskAcknowledgmentsByProvider(Provider provider, LocalDate from, LocalDate to) {
        return taskAcknowledgmentDAO.getTaskAcknowledgmentsByProvider(provider, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskAcknowledgment> getUnbilledAcknowledgments(LocalDateTime from, LocalDateTime to) {
        return taskAcknowledgmentDAO.getUnbilledAcknowledgments(from, to);
    }

    @Override
    public TaskAcknowledgment markAsBilled(String acknowledgmentUuid, String billingReferenceId) {
        TaskAcknowledgment acknowledgment = getTaskAcknowledgmentByUuid(acknowledgmentUuid);
        if (acknowledgment == null) {
            throw new IllegalArgumentException("Task acknowledgment not found");
        }
        acknowledgment.setBilledAt(LocalDateTime.now());
        acknowledgment.setBillingReferenceId(billingReferenceId);
        return taskAcknowledgmentDAO.saveTaskAcknowledgment(acknowledgment);
    }
}
