package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.Provider;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ipd.api.dao.TaskCompletionDAO;
import org.openmrs.module.ipd.api.model.TaskCompletion;
import org.openmrs.module.ipd.api.model.TaskInstance;
import org.openmrs.module.ipd.api.service.TaskCompletionService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
public class TaskCompletionServiceImpl extends BaseOpenmrsService implements TaskCompletionService {

    private TaskCompletionDAO taskCompletionDAO;

    public void setTaskCompletionDAO(TaskCompletionDAO taskCompletionDAO) {
        this.taskCompletionDAO = taskCompletionDAO;
    }

    @Override
    public TaskCompletion saveTaskCompletion(TaskCompletion completion) {
        return taskCompletionDAO.saveTaskCompletion(completion);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskCompletion getTaskCompletionByUuid(String uuid) {
        return taskCompletionDAO.getTaskCompletionByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskCompletion getTaskCompletionByInstance(TaskInstance instance) {
        return taskCompletionDAO.getTaskCompletionByInstance(instance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskCompletion> getTaskCompletionsByProvider(Provider provider, LocalDate from, LocalDate to) {
        return taskCompletionDAO.getTaskCompletionsByProvider(provider, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskCompletion> getTaskCompletionsByCompletedBy(Integer userId, LocalDateTime from, LocalDateTime to) {
        return taskCompletionDAO.getTaskCompletionsByCompletedBy(userId, from, to);
    }
}
