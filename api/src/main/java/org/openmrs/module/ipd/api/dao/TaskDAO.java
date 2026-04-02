package org.openmrs.module.ipd.api.dao;

import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.model.Task;

import java.util.List;

public interface TaskDAO {

	Task saveTask(Task task) throws DAOException;

	Task getTaskByUuid(String uuid) throws DAOException;

	List<Task> getTasksByPatientAndStatuses(String patientUuid, List<Task.TaskStatus> statuses) throws DAOException;
}
