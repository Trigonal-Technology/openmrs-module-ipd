package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ipd.api.dao.TaskInstanceDAO;
import org.openmrs.module.ipd.api.model.*;
import org.openmrs.module.ipd.api.service.TaskInstanceService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Transactional
public class TaskInstanceServiceImpl extends BaseOpenmrsService implements TaskInstanceService {

    private TaskInstanceDAO taskInstanceDAO;
    private ProviderService providerService;

    public void setTaskInstanceDAO(TaskInstanceDAO taskInstanceDAO) {
        this.taskInstanceDAO = taskInstanceDAO;
    }

    public void setProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }

    @Override
    public TaskInstance saveTaskInstance(TaskInstance instance) {
        return taskInstanceDAO.saveTaskInstance(instance);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskInstance getTaskInstanceByUuid(String uuid) {
        return taskInstanceDAO.getTaskInstanceByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskInstance> getTaskInstancesByPatient(Patient patient, List<TaskInstanceStatus> statuses,
                                                       LocalDateTime from, LocalDateTime to) {
        return taskInstanceDAO.getTaskInstancesByPatient(patient, statuses, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskInstance> getTaskInstancesByWard(Location ward, List<TaskInstanceStatus> statuses,
                                                       LocalDateTime from, LocalDateTime to) {
        return taskInstanceDAO.getTaskInstancesByWard(ward, statuses, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskInstance> getTaskInstancesByTemplate(TaskTemplate template, LocalDateTime from, LocalDateTime to) {
        return taskInstanceDAO.getTaskInstancesByTemplate(template, from, to);
    }

    @Override
    public TaskInstance startTask(String instanceUuid) {
        TaskInstance instance = getTaskInstanceByUuid(instanceUuid);
        if (instance == null) {
            throw new IllegalArgumentException("Task instance not found");
        }
        if (instance.getStatus() != TaskInstanceStatus.SCHEDULED) {
            throw new IllegalStateException("Task can only be started from SCHEDULED status");
        }
        instance.setStatus(TaskInstanceStatus.IN_PROGRESS);
        instance.setStartedTime(LocalDateTime.now());
        return taskInstanceDAO.saveTaskInstance(instance);
    }

    @Override
    public TaskInstance completeTask(String instanceUuid, String notes, String completedOnBehalfOfProviderUuid) {
        TaskInstance instance = getTaskInstanceByUuid(instanceUuid);
        if (instance == null) {
            throw new IllegalArgumentException("Task instance not found");
        }
        if (instance.getStatus() != TaskInstanceStatus.IN_PROGRESS && instance.getStatus() != TaskInstanceStatus.SCHEDULED) {
            throw new IllegalStateException("Task can only be completed from SCHEDULED or IN_PROGRESS status");
        }
        
        instance.setStatus(TaskInstanceStatus.COMPLETED);
        return taskInstanceDAO.saveTaskInstance(instance);
    }

    @Override
    public TaskInstance cancelTask(String instanceUuid, String reason) {
        TaskInstance instance = getTaskInstanceByUuid(instanceUuid);
        if (instance == null) {
            throw new IllegalArgumentException("Task instance not found");
        }
        if (instance.getStatus() == TaskInstanceStatus.COMPLETED || instance.getStatus() == TaskInstanceStatus.ARCHIVED) {
            throw new IllegalStateException("Cannot cancel completed or archived tasks");
        }
        instance.setStatus(TaskInstanceStatus.CANCELLED);
        instance.setVoidReason(reason);
        return taskInstanceDAO.saveTaskInstance(instance);
    }

    @Override
    public int cancelFutureInstances(Patient patient, LocalDateTime after) {
        return taskInstanceDAO.cancelFutureInstances(patient, after);
    }

    @Override
    public int archiveCancelledInstances(LocalDateTime before) {
        return taskInstanceDAO.archiveCancelledInstances(before);
    }

    @Override
    public List<TaskInstance> generateInstancesFromTemplate(TaskTemplate template, Patient patient, 
                                                              Location ward, LocalDateTime startDate, LocalDateTime endDate) {
        // This is a simplified version - full implementation would use the recurrence schedule
        List<TaskInstance> instances = new ArrayList<>();
        
        TaskInstance instance = new TaskInstance();
        instance.setTemplate(template);
        instance.setPatient(patient);
        instance.setWard(ward);
        instance.setName(template.getName());
        instance.setDescription(template.getDescription());
        instance.setScheduledTime(startDate);
        instance.setStatus(TaskInstanceStatus.SCHEDULED);
        instance.setPriority(template.getPriority());
        instance.setCreator(Context.getAuthenticatedUser());
        instance.setDateCreated(new Date());
        
        instances.add(taskInstanceDAO.saveTaskInstance(instance));
        
        return instances;
    }
}
