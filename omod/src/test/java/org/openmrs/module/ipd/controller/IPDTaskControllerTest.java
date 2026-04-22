package org.openmrs.module.ipd.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.module.ipd.api.model.Task;
import org.openmrs.module.ipd.api.service.TaskService;
import org.openmrs.module.ipd.web.contract.TaskRequest;
import org.openmrs.module.ipd.web.controller.IPDTaskController;
import org.openmrs.module.ipd.web.util.PrivilegeConstants;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IPDTaskControllerTest {

	@Mock
	private TaskService taskService;

	@Mock
	private PatientService patientService;

	@Mock
	private ConceptService conceptService;

	@Mock
	private User authenticatedUser;

	private IPDTaskController controller;

	@Before
	public void setUp() {
		controller = Mockito.spy(new IPDTaskController(taskService, patientService, conceptService));
		Mockito.lenient().doReturn(authenticatedUser).when(controller).getAuthenticatedUser();
	}

	@Test
	public void createTask_shouldReturnForbiddenWhenNoAddPrivilege() {
		Mockito.doReturn(false).when(controller).hasPrivilege(PrivilegeConstants.ADD_TASKS);
		ResponseEntity<Object> response = controller.createTask(new TaskRequest());
		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
	}

	@Test
	public void getTasks_shouldReturnBadRequestWhenPatientMissing() {
		ResponseEntity<Object> response = controller.getTasks(null, null);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		response = controller.getTasks("  ", null);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	@Test
	public void getTasks_shouldReturnWrappedResults() {
		Mockito.doReturn(true).when(controller).hasPrivilege(PrivilegeConstants.GET_TASKS);
		Task task = sampleTask(Task.TaskStatus.REQUESTED);
		when(taskService.getTasksByPatientAndStatuses(Mockito.eq("patient-uuid"), any())).thenReturn(
		    Collections.singletonList(task));
		ResponseEntity<Object> response = controller.getTasks("patient-uuid", "REQUESTED,IN_PROGRESS");
		assertEquals(HttpStatus.OK, response.getStatusCode());
		SimpleObject payload = (SimpleObject) response.getBody();
		assertTrue(payload.containsKey("results"));
	}

	@Test
	public void updateTask_shouldReturnBadRequestForInvalidTransition() {
		Mockito.doReturn(true).when(controller).hasPrivilege(PrivilegeConstants.EDIT_TASKS);
		Task task = sampleTask(Task.TaskStatus.COMPLETED);
		when(taskService.getTaskByUuid("task-uuid")).thenReturn(task);

		org.openmrs.module.ipd.web.contract.TaskUpdateRequest request = new org.openmrs.module.ipd.web.contract.TaskUpdateRequest();
		request.setStatus("IN_PROGRESS");

		ResponseEntity<Object> response = controller.updateTask("task-uuid", request);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
	}

	private Task sampleTask(Task.TaskStatus status) {
		Task t = new Task();
		t.setUuid("task-uuid");
		t.setName("Vitals");
		t.setDescription("Do vitals");
		t.setIntent(Task.TaskIntent.ORDER);
		t.setPriority(Task.TaskPriority.ROUTINE);
		t.setStatus(status);
		t.setDateCreated(new Date());
		Patient patient = new Patient();
		patient.setUuid("patient-uuid");
		patient.addName(new PersonName("Jane", "", "Doe"));
		t.setPatient(patient);
		return t;
	}
}
