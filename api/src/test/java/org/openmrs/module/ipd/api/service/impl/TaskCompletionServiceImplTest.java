package org.openmrs.module.ipd.api.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.module.ipd.api.dao.TaskCompletionDAO;
import org.openmrs.module.ipd.api.model.CompletionMethod;
import org.openmrs.module.ipd.api.model.TaskCompletion;
import org.openmrs.module.ipd.api.model.TaskInstance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskCompletionServiceImplTest {

    @InjectMocks
    private TaskCompletionServiceImpl taskCompletionService;

    @Mock
    private TaskCompletionDAO taskCompletionDAO;

    @Test
    public void shouldSaveTaskCompletion() {
        TaskCompletion completion = new TaskCompletion();
        completion.setCompletionMethod(CompletionMethod.NURSE_ENTRY);
        completion.setCompletionTime(LocalDateTime.now());

        TaskInstance instance = new TaskInstance();
        instance.setId(1);
        completion.setInstance(instance);

        User completedBy = new User();
        completedBy.setId(1);
        completion.setCompletedBy(completedBy);

        TaskCompletion savedCompletion = new TaskCompletion();
        savedCompletion.setId(1);
        savedCompletion.setCompletionMethod(CompletionMethod.NURSE_ENTRY);

        Mockito.when(taskCompletionDAO.saveTaskCompletion(completion)).thenReturn(savedCompletion);

        TaskCompletion result = taskCompletionService.saveTaskCompletion(completion);

        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getId());
        Mockito.verify(taskCompletionDAO, Mockito.times(1)).saveTaskCompletion(completion);
    }

    @Test
    public void shouldGetTaskCompletionByUuid() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        TaskCompletion completion = new TaskCompletion();
        completion.setId(1);
        completion.setUuid(uuid);
        completion.setCompletionMethod(CompletionMethod.NURSE_ENTRY);

        Mockito.when(taskCompletionDAO.getTaskCompletionByUuid(uuid)).thenReturn(completion);

        TaskCompletion result = taskCompletionService.getTaskCompletionByUuid(uuid);

        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        Mockito.verify(taskCompletionDAO, Mockito.times(1)).getTaskCompletionByUuid(uuid);
    }

    @Test
    public void shouldGetTaskCompletionByInstance() {
        TaskInstance instance = new TaskInstance();
        instance.setId(1);

        TaskCompletion completion = new TaskCompletion();
        completion.setId(1);
        completion.setInstance(instance);
        completion.setCompletionMethod(CompletionMethod.NURSE_ENTRY);

        Mockito.when(taskCompletionDAO.getTaskCompletionByInstance(instance)).thenReturn(completion);

        TaskCompletion result = taskCompletionService.getTaskCompletionByInstance(instance);

        assertNotNull(result);
        assertEquals(instance, result.getInstance());
        Mockito.verify(taskCompletionDAO, Mockito.times(1)).getTaskCompletionByInstance(instance);
    }

    @Test
    public void shouldGetTaskCompletionsByProvider() {
        Provider provider = new Provider();
        provider.setId(1);

        TaskCompletion completion1 = new TaskCompletion();
        completion1.setId(1);

        TaskCompletion completion2 = new TaskCompletion();
        completion2.setId(2);

        List<TaskCompletion> completions = Arrays.asList(completion1, completion2);
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        Mockito.when(taskCompletionDAO.getTaskCompletionsByProvider(provider, from, to)).thenReturn(completions);

        List<TaskCompletion> result = taskCompletionService.getTaskCompletionsByProvider(provider, from, to);

        assertNotNull(result);
        assertEquals(2, result.size());
        Mockito.verify(taskCompletionDAO, Mockito.times(1)).getTaskCompletionsByProvider(provider, from, to);
    }

    @Test
    public void shouldGetTaskCompletionsByCompletedBy() {
        Integer userId = 1;

        TaskCompletion completion = new TaskCompletion();
        completion.setId(1);

        List<TaskCompletion> completions = Arrays.asList(completion);
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        LocalDateTime to = LocalDateTime.now();

        Mockito.when(taskCompletionDAO.getTaskCompletionsByCompletedBy(userId, from, to)).thenReturn(completions);

        List<TaskCompletion> result = taskCompletionService.getTaskCompletionsByCompletedBy(userId, from, to);

        assertNotNull(result);
        assertEquals(1, result.size());
        Mockito.verify(taskCompletionDAO, Mockito.times(1)).getTaskCompletionsByCompletedBy(userId, from, to);
    }
}
