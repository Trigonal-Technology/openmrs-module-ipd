package org.openmrs.module.ipd.web.resources;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.model.Task;
import org.openmrs.module.ipd.api.service.TaskService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.springframework.stereotype.Component;

@Component
@Resource(name = RestConstants.VERSION_1 + "/tasks", supportedClass = Task.class, supportedOpenmrsVersions = {
        "2.6.* - 9.*" }, order = 3)
public class IPDTaskResource extends DataDelegatingCrudResource<Task> {

	@Override
	public Task getByUniqueId(String uuid) {
		return taskService().getTaskByUuid(uuid);
	}

	@Override
	public Task newDelegate() {
		return new Task();
	}

	@Override
	public Task save(Task task) {
		if (task.getTaskId() == null) {
			User user = Context.getAuthenticatedUser();
			task.setCreator(user);
			task.setDateCreated(new Date());
			task.setVoided(false);
		} else {
			task.setChangedBy(Context.getAuthenticatedUser());
			task.setDateChanged(new Date());
		}
		applyAutoExecutionPeriod(task);
		return taskService().saveTask(task);
	}

	@Override
	protected void delete(Task task, String reason, RequestContext context) {
		throw new UnsupportedOperationException("Delete is not supported for tasks");
	}

	@Override
	public void purge(Task task, RequestContext context) {
		throw new UnsupportedOperationException("Purge is not supported for tasks");
	}

	@Override
	protected PageableResult doSearch(RequestContext context) {
		String patientUuid = context.getParameter("patient");
		if (StringUtils.isBlank(patientUuid)) {
			return new NeedsPaging<Task>(new ArrayList<Task>(), context);
		}
		List<Task.TaskStatus> statuses = parseStatuses(context.getParameter("status"));
		List<Task> tasks = taskService().getTasksByPatientAndStatuses(patientUuid, statuses);
		return new NeedsPaging<Task>(tasks, context);
	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("patient");
		description.addProperty("taskType");
		description.addProperty("name");
		description.addProperty("description");
		description.addProperty("status");
		description.addProperty("intent");
		description.addProperty("priority");
		description.addProperty("notes");
		description.addProperty("executionPeriod");
		return description;
	}

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		if (representation instanceof RefRepresentation) {
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("status");
		} else if (representation instanceof DefaultRepresentation || representation instanceof FullRepresentation) {
			description.addProperty("uuid");
			description.addProperty("patient", Representation.REF);
			description.addProperty("taskType", Representation.REF);
			description.addProperty("name");
			description.addProperty("description");
			description.addProperty("status");
			description.addProperty("intent");
			description.addProperty("priority");
			description.addProperty("notes");
			description.addProperty("executionPeriod");
		}
		return description;
	}

	@PropertySetter("patient")
	public void setPatient(Task task, Object value) {
		String uuid = extractUuid(value);
		if (StringUtils.isBlank(uuid)) {
			throw new IllegalArgumentException("patient.uuid is required");
		}
		Patient patient = patientService().getPatientByUuid(uuid);
		if (patient == null) {
			throw new IllegalArgumentException("Invalid patient uuid");
		}
		task.setPatient(patient);
	}

	@PropertySetter("taskType")
	public void setTaskType(Task task, Object value) {
		String uuid = extractUuid(value);
		if (StringUtils.isBlank(uuid)) {
			task.setTaskType(null);
			return;
		}
		Concept concept = conceptService().getConceptByUuid(uuid);
		if (concept == null) {
			throw new IllegalArgumentException("Invalid taskType uuid");
		}
		task.setTaskType(concept);
	}

	@PropertySetter("status")
	public void setStatus(Task task, String raw) {
		if (StringUtils.isBlank(raw)) {
			return;
		}
		Task.TaskStatus next = Task.TaskStatus.valueOf(raw.trim().toUpperCase());
		validateTransition(task.getStatus(), next);
		task.setStatus(next);
	}

	@PropertySetter("intent")
	public void setIntent(Task task, String raw) {
		if (StringUtils.isBlank(raw)) {
			task.setIntent(Task.TaskIntent.ORDER);
			return;
		}
		task.setIntent(Task.TaskIntent.valueOf(raw.trim().toUpperCase()));
	}

	@PropertySetter("priority")
	public void setPriority(Task task, String raw) {
		if (StringUtils.isBlank(raw)) {
			task.setPriority(Task.TaskPriority.ROUTINE);
			return;
		}
		task.setPriority(Task.TaskPriority.valueOf(raw.trim().toUpperCase()));
	}

	@PropertySetter("executionPeriod")
	public void setExecutionPeriod(Task task, Object value) {
		if (!(value instanceof SimpleObject)) {
			return;
		}
		SimpleObject execution = (SimpleObject) value;
		task.setExecutionStartTime(parseIsoInstant((String) execution.get("start")));
		task.setExecutionEndTime(parseIsoInstant((String) execution.get("end")));
	}

	@PropertyGetter("executionPeriod")
	public SimpleObject getExecutionPeriod(Task task) {
		SimpleObject out = new SimpleObject();
		out.put("start", task.getExecutionStartTime() == null ? null : task.getExecutionStartTime().toInstant().toString());
		out.put("end", task.getExecutionEndTime() == null ? null : task.getExecutionEndTime().toInstant().toString());
		return out;
	}

	private static List<Task.TaskStatus> parseStatuses(String raw) {
		List<Task.TaskStatus> out = new ArrayList<Task.TaskStatus>();
		if (StringUtils.isBlank(raw)) {
			return out;
		}
		for (String chunk : raw.split(",")) {
			if (StringUtils.isBlank(chunk)) {
				continue;
			}
			out.add(Task.TaskStatus.valueOf(chunk.trim().toUpperCase()));
		}
		return out;
	}

	private static String extractUuid(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof String) {
			return (String) value;
		}
		if (value instanceof SimpleObject) {
			Object uuid = ((SimpleObject) value).get("uuid");
			return uuid == null ? null : uuid.toString();
		}
		return null;
	}

	private static Date parseIsoInstant(String raw) {
		if (StringUtils.isBlank(raw)) {
			return null;
		}
		try {
			return Date.from(Instant.parse(raw.trim()));
		} catch (DateTimeParseException ex) {
			throw new IllegalArgumentException("Invalid executionPeriod timestamp: " + raw);
		}
	}

	private static void validateTransition(Task.TaskStatus current, Task.TaskStatus next) {
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

	private static void applyAutoExecutionPeriod(Task task) {
		if (task.getStatus() == Task.TaskStatus.IN_PROGRESS && task.getExecutionStartTime() == null) {
			task.setExecutionStartTime(new Date());
		}
		if (task.getStatus() == Task.TaskStatus.COMPLETED && task.getExecutionEndTime() == null) {
			task.setExecutionEndTime(new Date());
		}
	}

	private static TaskService taskService() {
		return Context.getService(TaskService.class);
	}

	private static PatientService patientService() {
		return Context.getPatientService();
	}

	private static ConceptService conceptService() {
		return Context.getConceptService();
	}
}
