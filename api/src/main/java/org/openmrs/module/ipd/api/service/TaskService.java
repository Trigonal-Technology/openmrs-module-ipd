package org.openmrs.module.ipd.api.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.ipd.api.model.Task;

import java.util.List;

public interface TaskService extends OpenmrsService {

	@Authorized({ "Add Tasks", "Edit Tasks" })
	Task saveTask(Task task);

	@Authorized({ "Get Tasks" })
	Task getTaskByUuid(String uuid);

	@Authorized({ "Get Tasks" })
	List<Task> getTasksByPatientAndStatuses(String patientUuid, List<Task.TaskStatus> statuses);
}
