package org.openmrs.module.ipd.web.contract;

import lombok.Builder;
import lombok.Getter;
import org.openmrs.module.ipd.api.model.Task;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class TaskResponse {

	private String uuid;
	private String name;
	private String description;
	private String intent;
	private String status;
	private Map<String, Object> patient;
	private Map<String, Object> taskType;
	private Map<String, String> executionPeriod;

	public static TaskResponse from(Task task) {
		Map<String, Object> patientRef = new HashMap<>();
		patientRef.put("uuid", task.getPatient().getUuid());
		patientRef.put("display", task.getPatient().getPersonName() != null ? task.getPatient().getPersonName().toString() : null);

		Map<String, Object> taskTypeRef = null;
		if (task.getTaskType() != null) {
			taskTypeRef = new HashMap<>();
			taskTypeRef.put("uuid", task.getTaskType().getUuid());
			taskTypeRef.put("display", task.getTaskType().getDisplayString());
		}

		Map<String, String> executionPeriod = null;
		if (task.getExecutionStartTime() != null || task.getExecutionEndTime() != null) {
			executionPeriod = new HashMap<>();
			executionPeriod.put("start", task.getExecutionStartTime() != null ? task.getExecutionStartTime().toInstant().toString() : null);
			executionPeriod.put("end", task.getExecutionEndTime() != null ? task.getExecutionEndTime().toInstant().toString() : null);
		}

		return TaskResponse.builder()
		    .uuid(task.getUuid())
		    .name(task.getName())
		    .description(task.getDescription())
		    .intent(task.getIntent() != null ? task.getIntent().name().toLowerCase() : null)
		    .status(task.getStatus() != null ? task.getStatus().name() : null)
		    .patient(patientRef)
		    .taskType(taskTypeRef)
		    .executionPeriod(executionPeriod)
		    .build();
	}
}
