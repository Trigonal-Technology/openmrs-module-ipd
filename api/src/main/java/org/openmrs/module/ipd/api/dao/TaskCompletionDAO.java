package org.openmrs.module.ipd.api.dao;

import org.openmrs.Provider;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.model.TaskCompletion;
import org.openmrs.module.ipd.api.model.TaskInstance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskCompletionDAO {

    TaskCompletion saveTaskCompletion(TaskCompletion completion) throws DAOException;

    TaskCompletion getTaskCompletionById(Integer completionId) throws DAOException;

    TaskCompletion getTaskCompletionByUuid(String uuid) throws DAOException;

    TaskCompletion getTaskCompletionByInstance(TaskInstance instance) throws DAOException;

    List<TaskCompletion> getTaskCompletionsByProvider(Provider provider, LocalDate from, LocalDate to) throws DAOException;

    List<TaskCompletion> getTaskCompletionsByCompletedBy(Integer userId, LocalDateTime from, LocalDateTime to) throws DAOException;
}
