package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.Provider;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ipd.api.dao.TaskAcknowledgmentDAO;
import org.openmrs.module.ipd.api.model.TaskAcknowledgment;
import org.openmrs.module.ipd.api.model.Task;
import org.openmrs.module.ipd.api.service.TaskAcknowledgmentService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
	public TaskAcknowledgment getTaskAcknowledgmentByTask(Task task) {
		return taskAcknowledgmentDAO.getTaskAcknowledgmentByTask(task);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TaskAcknowledgment> getTaskAcknowledgmentsByProvider(Provider provider, Date from, Date to) {
		return taskAcknowledgmentDAO.getTaskAcknowledgmentsByProvider(provider, from, to);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TaskAcknowledgment> getUnbilledAcknowledgments(Date from, Date to) {
		return taskAcknowledgmentDAO.getUnbilledAcknowledgments(from, to);
	}

	@Override
	public TaskAcknowledgment markAsBilled(String acknowledgmentUuid, String billingReferenceId) {
		TaskAcknowledgment acknowledgment = getTaskAcknowledgmentByUuid(acknowledgmentUuid);
		if (acknowledgment == null) {
			throw new IllegalArgumentException("Task acknowledgment not found");
		}
		acknowledgment.setBilledAt(new Date());
		acknowledgment.setBillingReferenceId(billingReferenceId);
		return taskAcknowledgmentDAO.saveTaskAcknowledgment(acknowledgment);
	}
}
