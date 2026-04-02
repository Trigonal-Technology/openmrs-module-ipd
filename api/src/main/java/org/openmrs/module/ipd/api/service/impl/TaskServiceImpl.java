package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ipd.api.dao.TaskDAO;
import org.openmrs.module.ipd.api.model.Task;
import org.openmrs.module.ipd.api.service.TaskService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class TaskServiceImpl extends BaseOpenmrsService implements TaskService {

	private TaskDAO taskDAO;

	public void setTaskDAO(TaskDAO taskDAO) {
		this.taskDAO = taskDAO;
	}

	@Override
	public Task saveTask(Task task) {
		return taskDAO.saveTask(task);
	}

	@Override
	@Transactional(readOnly = true)
	public Task getTaskByUuid(String uuid) {
		return taskDAO.getTaskByUuid(uuid);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Task> getTasksByPatientAndStatuses(String patientUuid, List<Task.TaskStatus> statuses) {
		return taskDAO.getTasksByPatientAndStatuses(patientUuid, statuses);
	}
}
