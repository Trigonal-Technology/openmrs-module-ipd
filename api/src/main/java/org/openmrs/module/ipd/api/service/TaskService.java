package org.openmrs.module.ipd.api.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.ipd.api.model.PatientTaskTemplate;
import org.openmrs.module.ipd.api.model.Task;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService extends OpenmrsService {

	@Authorized({ "Add Tasks", "Edit Tasks" })
	Task saveTask(Task task);

	@Authorized({ "Get Tasks" })
	Task getTaskByUuid(String uuid);

	@Authorized({ "Get Tasks" })
	List<Task> getTasksByPatientAndStatuses(String patientUuid, List<Task.TaskStatus> statuses);

	@Authorized({ "Apply Task Templates" })
	int generateTasksFromTemplate(PatientTaskTemplate patientTemplate, LocalDateTime from, LocalDateTime to);

	@Authorized({ "Edit Tasks" })
	void voidStaleTasks(PatientTaskTemplate patientTemplate, String reason);

	@Authorized({ "Edit Tasks" })
	int cancelFutureTasks(Patient patient, LocalDateTime from);

	@Authorized({ "Edit Tasks" })
	int archiveCancelledTasks(LocalDateTime archiveBefore);
}
