package org.openmrs.module.ipd.api.service;

import org.openmrs.Provider;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.ipd.api.model.TaskAcknowledgment;
import org.openmrs.module.ipd.api.model.Task;

import java.util.Date;
import java.util.List;

public interface TaskAcknowledgmentService extends OpenmrsService {

	@Authorized({ "Acknowledge Tasks" })
	TaskAcknowledgment saveTaskAcknowledgment(TaskAcknowledgment acknowledgment);

	@Authorized({ "Get Task Instances", "Acknowledge Tasks", "View Task Reports" })
	TaskAcknowledgment getTaskAcknowledgmentByUuid(String uuid);

	@Authorized({ "Get Task Instances", "Acknowledge Tasks" })
	TaskAcknowledgment getTaskAcknowledgmentByTask(Task task);

	@Authorized({ "View Task Reports" })
	List<TaskAcknowledgment> getTaskAcknowledgmentsByProvider(Provider provider, Date from, Date to);

	@Authorized({ "View Task Reports", "Manage Billing" })
	List<TaskAcknowledgment> getUnbilledAcknowledgments(Date from, Date to);

	@Authorized({ "Manage Billing" })
	TaskAcknowledgment markAsBilled(String acknowledgmentUuid, String billingReferenceId);
}
