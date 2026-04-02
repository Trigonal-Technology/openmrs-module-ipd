package org.openmrs.module.ipd.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.model.Task;
import org.openmrs.module.ipd.api.service.TaskService;
import org.openmrs.module.ipd.web.contract.TaskRequest;
import org.openmrs.module.ipd.web.contract.TaskResponse;
import org.openmrs.module.ipd.web.contract.TaskUpdateRequest;
import org.openmrs.module.ipd.web.util.PrivilegeConstants;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/tasks")
@Slf4j
public class IPDTaskController extends BaseRestController {

	private final TaskService taskService;
	private final PatientService patientService;
	private final ConceptService conceptService;

	public IPDTaskController(TaskService taskService, PatientService patientService, ConceptService conceptService) {
		this.taskService = taskService;
		this.patientService = patientService;
		this.conceptService = conceptService;
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> createTask(@RequestBody TaskRequest request) {
		try {
			if (!hasPrivilege(PrivilegeConstants.ADD_TASKS)) {
				return forbidden(PrivilegeConstants.ADD_TASKS);
			}
			if (request == null || StringUtils.isBlank(request.getName()) || request.getPatient() == null
			        || StringUtils.isBlank(request.getPatient().getUuid()) || StringUtils.isBlank(request.getStatus())) {
				return new ResponseEntity<>(errorPayload("name, patient.uuid and status are required"), BAD_REQUEST);
			}
			Task task = new Task();
			applyRequestToTask(task, request);
			initializeAudit(task);
			Task saved = taskService.saveTask(task);
			return new ResponseEntity<>(TaskResponse.from(saved), OK);
		} catch (Exception e) {
			log.error("Error while creating task", e);
			return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
		}
	}

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Object> getTasks(@RequestParam(value = "patient") String patientUuid,
	        @RequestParam(value = "status", required = false) String statuses) {
		try {
			if (!hasPrivilege(PrivilegeConstants.GET_TASKS)) {
				return forbidden(PrivilegeConstants.GET_TASKS);
			}
			List<Task.TaskStatus> parsedStatuses = parseStatuses(statuses);
			List<Task> tasks = taskService.getTasksByPatientAndStatuses(patientUuid, parsedStatuses);
			List<TaskResponse> results = tasks.stream().map(TaskResponse::from).collect(Collectors.toList());
			SimpleObject response = new SimpleObject();
			response.put("results", results);
			return new ResponseEntity<>(response, OK);
		} catch (Exception e) {
			log.error("Error while listing tasks", e);
			return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
		}
	}

	@RequestMapping(value = "/{taskUuid}", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Object> updateTask(@PathVariable("taskUuid") String taskUuid,
	        @RequestBody TaskUpdateRequest request) {
		try {
			if (!hasPrivilege(PrivilegeConstants.EDIT_TASKS)) {
				return forbidden(PrivilegeConstants.EDIT_TASKS);
			}
			Task task = taskService.getTaskByUuid(taskUuid);
			if (task == null) {
				return new ResponseEntity<>(errorPayload("Task not found"), NOT_FOUND);
			}
			if (StringUtils.isNotBlank(request.getStatus())) {
				Task.TaskStatus next = Task.TaskStatus.valueOf(request.getStatus().trim().toUpperCase());
				validateTransition(task.getStatus(), next);
				task.setStatus(next);
			}
			if (StringUtils.isNotBlank(request.getNotes())) {
				task.setNotes(request.getNotes());
			}
			if (request.getExecutionPeriod() != null) {
				task.setExecutionStartTime(parseIsoInstant(request.getExecutionPeriod().getStart()));
				task.setExecutionEndTime(parseIsoInstant(request.getExecutionPeriod().getEnd()));
			} else {
				applyAutoExecutionPeriod(task);
			}
			task.setChangedBy(getAuthenticatedUser());
			task.setDateChanged(new Date());
			Task saved = taskService.saveTask(task);
			return new ResponseEntity<>(TaskResponse.from(saved), OK);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
		} catch (Exception e) {
			log.error("Error while updating task", e);
			return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
		}
	}

	private void applyRequestToTask(Task task, TaskRequest request) {
		Patient patient = patientService.getPatientByUuid(request.getPatient().getUuid());
		if (patient == null) {
			throw new IllegalArgumentException("Invalid patient uuid");
		}
		task.setPatient(patient);
		task.setName(request.getName().trim());
		task.setDescription(StringUtils.trimToNull(request.getDescription()));
		task.setStatus(Task.TaskStatus.valueOf(request.getStatus().trim().toUpperCase()));
		task.setIntent(parseIntent(request.getIntent()));
		task.setPriority(parsePriority(request.getPriority()));
		task.setNotes(StringUtils.trimToNull(request.getNotes()));
		if (request.getTaskType() != null && StringUtils.isNotBlank(request.getTaskType().getUuid())) {
			Concept concept = conceptService.getConceptByUuid(request.getTaskType().getUuid());
			if (concept == null) {
				throw new IllegalArgumentException("Invalid taskType uuid");
			}
			task.setTaskType(concept);
		}
		if (request.getExecutionPeriod() != null) {
			task.setExecutionStartTime(parseIsoInstant(request.getExecutionPeriod().getStart()));
			task.setExecutionEndTime(parseIsoInstant(request.getExecutionPeriod().getEnd()));
		}
		applyAutoExecutionPeriod(task);
	}

	private void initializeAudit(Task task) {
		User user = getAuthenticatedUser();
		task.setCreator(user);
		task.setDateCreated(new Date());
		task.setVoided(false);
	}

	private Task.TaskIntent parseIntent(String raw) {
		if (StringUtils.isBlank(raw)) {
			return Task.TaskIntent.ORDER;
		}
		return Task.TaskIntent.valueOf(raw.trim().toUpperCase());
	}

	private Task.TaskPriority parsePriority(String raw) {
		if (StringUtils.isBlank(raw)) {
			return Task.TaskPriority.ROUTINE;
		}
		return Task.TaskPriority.valueOf(raw.trim().toUpperCase());
	}

	private List<Task.TaskStatus> parseStatuses(String raw) {
		List<Task.TaskStatus> out = new ArrayList<>();
		if (StringUtils.isBlank(raw)) {
			return out;
		}
		String[] chunks = raw.split(",");
		for (String chunk : chunks) {
			if (StringUtils.isBlank(chunk)) {
				continue;
			}
			out.add(Task.TaskStatus.valueOf(chunk.trim().toUpperCase()));
		}
		return out;
	}

	private Date parseIsoInstant(String raw) {
		if (StringUtils.isBlank(raw)) {
			return null;
		}
		return Date.from(Instant.parse(raw.trim()));
	}

	private void validateTransition(Task.TaskStatus current, Task.TaskStatus next) {
		if (next == null || current == null || current == next) {
			return;
		}
		if (current == Task.TaskStatus.REQUESTED
		        && (next == Task.TaskStatus.IN_PROGRESS || next == Task.TaskStatus.CANCELLED)) {
			return;
		}
		if (current == Task.TaskStatus.IN_PROGRESS
		        && (next == Task.TaskStatus.COMPLETED || next == Task.TaskStatus.CANCELLED)) {
			return;
		}
		throw new IllegalArgumentException("Invalid status transition: " + current + " -> " + next);
	}

	private void applyAutoExecutionPeriod(Task task) {
		if (task.getStatus() == Task.TaskStatus.IN_PROGRESS && task.getExecutionStartTime() == null) {
			task.setExecutionStartTime(new Date());
		}
		if (task.getStatus() == Task.TaskStatus.COMPLETED && task.getExecutionEndTime() == null) {
			task.setExecutionEndTime(new Date());
		}
	}

	private ResponseEntity<Object> forbidden(String privilege) {
		return new ResponseEntity<>(errorPayload("User doesn't have the following privilege " + privilege), FORBIDDEN);
	}

	private SimpleObject errorPayload(String message) {
		SimpleObject out = new SimpleObject();
		out.put("error", message);
		return out;
	}

	public boolean hasPrivilege(String privilege) {
		return Context.getUserContext().hasPrivilege(privilege);
	}

	public User getAuthenticatedUser() {
		return Context.getAuthenticatedUser();
	}
}
