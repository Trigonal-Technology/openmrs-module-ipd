package org.openmrs.module.ipd.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.model.Task;
import org.openmrs.module.ipd.api.model.TaskAcknowledgment;
import org.openmrs.module.ipd.api.service.TaskAcknowledgmentService;
import org.openmrs.module.ipd.api.service.TaskService;
import org.openmrs.module.ipd.web.contract.AcknowledgeTaskRequest;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/ipd/tasks")
@Slf4j
public class TaskAcknowledgmentController {

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskAcknowledgmentService taskAcknowledgmentService;

	@Autowired
	private ProviderService providerService;

	@PostMapping("/{taskUuid}/acknowledge")
	public ResponseEntity<?> acknowledgeTask(
			@PathVariable String taskUuid,
			@Valid @RequestBody AcknowledgeTaskRequest request) {

		try {
			request.sanitize();
			
			Task task = taskService.getTaskByUuid(taskUuid);
			if (task == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
			}

			if (task.getStatus() != Task.TaskStatus.COMPLETED) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only completed tasks can be acknowledged");
			}

			// Check if already acknowledged
			if (taskAcknowledgmentService.getTaskAcknowledgmentByTask(task) != null) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body("Task is already acknowledged");
			}

			// Get current provider (the one acknowledging)
			Provider provider = getAuthenticatedProvider();
			if (provider == null) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Current user is not a registered provider");
			}

			TaskAcknowledgment acknowledgment = new TaskAcknowledgment();
			acknowledgment.setTask(task);
			acknowledgment.setAcknowledgedBy(provider);
			acknowledgment.setAcknowledgmentTime(new Date());
			acknowledgment.setAcknowledgmentMethod(request.getAcknowledgmentMethodEnum());
			acknowledgment.setDeviceId(request.getDeviceId());
			acknowledgment.setNotes(request.getNotes());
			acknowledgment.setBillable(true); // Default to billable
			acknowledgment.setLocation(task.getWard());
			stampNewAcknowledgment(acknowledgment);

			TaskAcknowledgment saved = taskAcknowledgmentService.saveTaskAcknowledgment(acknowledgment);

			Map<String, Object> response = new HashMap<>();
			response.put("uuid", saved.getUuid());
			response.put("taskUuid", task.getUuid());
			response.put("acknowledgedBy", provider.getName());
			response.put("acknowledgmentTime", saved.getAcknowledgmentTime());
			response.put("status", "Acknowledged");

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("Error acknowledging task", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
		}
	}

	@GetMapping("/{taskUuid}/acknowledgment")
	public ResponseEntity<?> getAcknowledgment(@PathVariable String taskUuid) {
		try {
			Task task = taskService.getTaskByUuid(taskUuid);
			if (task == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
			}

			TaskAcknowledgment ack = taskAcknowledgmentService.getTaskAcknowledgmentByTask(task);
			if (ack == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No acknowledgment found for this task");
			}

			Map<String, Object> response = new HashMap<>();
			response.put("uuid", ack.getUuid());
			response.put("acknowledgedBy", ack.getAcknowledgedBy().getName());
			response.put("acknowledgmentTime", ack.getAcknowledgmentTime());
			response.put("notes", ack.getNotes());
			response.put("billable", ack.isBillable());

			return ResponseEntity.ok(response);
		} catch (Exception e) {
			log.error("Error loading task acknowledgment", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Error loading acknowledgment: " + e.getMessage());
		}
	}

	private Provider getAuthenticatedProvider() {
		// Standard way to get provider for current user in OpenMRS
		return providerService.getProvidersByPerson(Context.getAuthenticatedUser().getPerson())
				.stream()
				.findFirst()
				.orElse(null);
	}

	/** Required for JPA + NOT NULL uuid/creator on {@link org.openmrs.module.ipd.api.model.TaskAcknowledgment}. */
	private void stampNewAcknowledgment(TaskAcknowledgment data) {
		if (StringUtils.isBlank(data.getUuid())) {
			data.setUuid(UUID.randomUUID().toString());
		}
		User creator = Context.getAuthenticatedUser();
		if (creator == null) {
			creator = Context.getUserService().getUser(1);
		}
		data.setCreator(creator);
		data.setDateCreated(new Date());
		data.setVoided(false);
	}
}
