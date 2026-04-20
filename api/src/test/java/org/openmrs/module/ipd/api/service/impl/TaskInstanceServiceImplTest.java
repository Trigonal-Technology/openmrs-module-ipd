package org.openmrs.module.ipd.api.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.ProviderService;
import org.openmrs.module.ipd.api.dao.TaskInstanceDAO;
import org.openmrs.module.ipd.api.model.TaskInstance;
import org.openmrs.module.ipd.api.model.TaskInstanceStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskInstanceServiceImplTest {

    @InjectMocks
    private TaskInstanceServiceImpl taskInstanceService;

    @Mock
    private TaskInstanceDAO taskInstanceDAO;

    @Mock
    private ProviderService providerService;

    @Test
    public void shouldSaveTaskInstance() {
        TaskInstance instance = new TaskInstance();
        instance.setName("Vital Check");
        instance.setScheduledTime(LocalDateTime.now());
        instance.setStatus(TaskInstanceStatus.SCHEDULED);

        TaskInstance savedInstance = new TaskInstance();
        savedInstance.setId(1);
        savedInstance.setName("Vital Check");

        Mockito.when(taskInstanceDAO.saveTaskInstance(instance)).thenReturn(savedInstance);

        TaskInstance result = taskInstanceService.saveTaskInstance(instance);

        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getId());
        Mockito.verify(taskInstanceDAO, Mockito.times(1)).saveTaskInstance(instance);
    }

    @Test
    public void shouldGetTaskInstanceByUuid() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        TaskInstance instance = new TaskInstance();
        instance.setId(1);
        instance.setUuid(uuid);
        instance.setName("Vital Check");

        Mockito.when(taskInstanceDAO.getTaskInstanceByUuid(uuid)).thenReturn(instance);

        TaskInstance result = taskInstanceService.getTaskInstanceByUuid(uuid);

        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        Mockito.verify(taskInstanceDAO, Mockito.times(1)).getTaskInstanceByUuid(uuid);
    }

    @Test
    public void shouldGetTaskInstancesByPatient() {
        Patient patient = new Patient();
        patient.setId(1);

        TaskInstance instance1 = new TaskInstance();
        instance1.setId(1);
        instance1.setPatient(patient);

        TaskInstance instance2 = new TaskInstance();
        instance2.setId(2);
        instance2.setPatient(patient);

        List<TaskInstance> instances = Arrays.asList(instance1, instance2);
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(1);

        Mockito.when(taskInstanceDAO.getTaskInstancesByPatient(patient, null, from, to))
                .thenReturn(instances);

        List<TaskInstance> result = taskInstanceService.getTaskInstancesByPatient(patient, null, from, to);

        assertNotNull(result);
        assertEquals(2, result.size());
        Mockito.verify(taskInstanceDAO, Mockito.times(1))
                .getTaskInstancesByPatient(patient, null, from, to);
    }

    @Test
    public void shouldStartTask() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        TaskInstance instance = new TaskInstance();
        instance.setId(1);
        instance.setUuid(uuid);
        instance.setStatus(TaskInstanceStatus.SCHEDULED);

        Mockito.when(taskInstanceDAO.getTaskInstanceByUuid(uuid)).thenReturn(instance);
        Mockito.when(taskInstanceDAO.saveTaskInstance(Mockito.any(TaskInstance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskInstance result = taskInstanceService.startTask(uuid);

        assertNotNull(result);
        assertEquals(TaskInstanceStatus.IN_PROGRESS, result.getStatus());
        assertNotNull(result.getStartedTime());
        Mockito.verify(taskInstanceDAO, Mockito.times(1)).saveTaskInstance(instance);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenStartingNonScheduledTask() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        TaskInstance instance = new TaskInstance();
        instance.setId(1);
        instance.setUuid(uuid);
        instance.setStatus(TaskInstanceStatus.COMPLETED);

        Mockito.when(taskInstanceDAO.getTaskInstanceByUuid(uuid)).thenReturn(instance);

        taskInstanceService.startTask(uuid);
    }

    @Test
    public void shouldCompleteTask() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        TaskInstance instance = new TaskInstance();
        instance.setId(1);
        instance.setUuid(uuid);
        instance.setStatus(TaskInstanceStatus.IN_PROGRESS);

        Mockito.when(taskInstanceDAO.getTaskInstanceByUuid(uuid)).thenReturn(instance);
        Mockito.when(taskInstanceDAO.saveTaskInstance(Mockito.any(TaskInstance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskInstance result = taskInstanceService.completeTask(uuid, "Notes", null);

        assertNotNull(result);
        assertEquals(TaskInstanceStatus.COMPLETED, result.getStatus());
        Mockito.verify(taskInstanceDAO, Mockito.times(1)).saveTaskInstance(instance);
    }

    @Test
    public void shouldCancelTask() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        TaskInstance instance = new TaskInstance();
        instance.setId(1);
        instance.setUuid(uuid);
        instance.setStatus(TaskInstanceStatus.SCHEDULED);

        Mockito.when(taskInstanceDAO.getTaskInstanceByUuid(uuid)).thenReturn(instance);
        Mockito.when(taskInstanceDAO.saveTaskInstance(Mockito.any(TaskInstance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskInstance result = taskInstanceService.cancelTask(uuid, "Patient discharged");

        assertNotNull(result);
        assertEquals(TaskInstanceStatus.CANCELLED, result.getStatus());
        assertEquals("Patient discharged", result.getVoidReason());
        Mockito.verify(taskInstanceDAO, Mockito.times(1)).saveTaskInstance(instance);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenCancellingCompletedTask() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        TaskInstance instance = new TaskInstance();
        instance.setId(1);
        instance.setUuid(uuid);
        instance.setStatus(TaskInstanceStatus.COMPLETED);

        Mockito.when(taskInstanceDAO.getTaskInstanceByUuid(uuid)).thenReturn(instance);

        taskInstanceService.cancelTask(uuid, "Should fail");
    }

    @Test
    public void shouldCancelFutureInstances() {
        Patient patient = new Patient();
        patient.setId(1);
        LocalDateTime after = LocalDateTime.now();

        Mockito.when(taskInstanceDAO.cancelFutureInstances(patient, after)).thenReturn(5);

        int result = taskInstanceService.cancelFutureInstances(patient, after);

        assertEquals(5, result);
        Mockito.verify(taskInstanceDAO, Mockito.times(1)).cancelFutureInstances(patient, after);
    }

    @Test
    public void shouldArchiveCancelledInstances() {
        LocalDateTime before = LocalDateTime.now().minusDays(30);

        Mockito.when(taskInstanceDAO.archiveCancelledInstances(before)).thenReturn(10);

        int result = taskInstanceService.archiveCancelledInstances(before);

        assertEquals(10, result);
        Mockito.verify(taskInstanceDAO, Mockito.times(1)).archiveCancelledInstances(before);
    }
}
