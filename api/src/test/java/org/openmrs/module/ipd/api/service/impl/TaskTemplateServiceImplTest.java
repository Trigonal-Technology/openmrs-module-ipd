package org.openmrs.module.ipd.api.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.module.ipd.api.dao.TaskTemplateDAO;
import org.openmrs.module.ipd.api.model.Task;
import org.openmrs.module.ipd.api.model.TaskTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskTemplateServiceImplTest {

    @InjectMocks
    private TaskTemplateServiceImpl taskTemplateService;

    @Mock
    private TaskTemplateDAO taskTemplateDAO;

    @Test
    public void shouldSaveTaskTemplate() {
        TaskTemplate template = new TaskTemplate();
        template.setName("Vital Signs Check");
        template.setTaskType(new Concept());
        template.setPriority(Task.TaskPriority.ROUTINE);

        TaskTemplate savedTemplate = new TaskTemplate();
        savedTemplate.setId(1);
        savedTemplate.setName("Vital Signs Check");

        Mockito.when(taskTemplateDAO.saveTaskTemplate(template)).thenReturn(savedTemplate);

        TaskTemplate result = taskTemplateService.saveTaskTemplate(template);

        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getId());
        Mockito.verify(taskTemplateDAO, Mockito.times(1)).saveTaskTemplate(template);
    }

    @Test
    public void shouldGetTaskTemplateByUuid() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        TaskTemplate template = new TaskTemplate();
        template.setId(1);
        template.setUuid(uuid);
        template.setName("Vital Signs Check");

        Mockito.when(taskTemplateDAO.getTaskTemplateByUuid(uuid)).thenReturn(template);

        TaskTemplate result = taskTemplateService.getTaskTemplateByUuid(uuid);

        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        Mockito.verify(taskTemplateDAO, Mockito.times(1)).getTaskTemplateByUuid(uuid);
    }

    @Test
    public void shouldGetTaskTemplatesByWard() {
        Location ward = new Location();
        ward.setId(1);
        ward.setName("Ward A");

        TaskTemplate template1 = new TaskTemplate();
        template1.setId(1);
        template1.setName("Ward A Task 1");

        TaskTemplate template2 = new TaskTemplate();
        template2.setId(2);
        template2.setName("Ward A Task 2");

        List<TaskTemplate> templates = Arrays.asList(template1, template2);

        Mockito.when(taskTemplateDAO.getTaskTemplatesByWard(ward)).thenReturn(templates);

        List<TaskTemplate> result = taskTemplateService.getTaskTemplatesByWard(ward);

        assertNotNull(result);
        assertEquals(2, result.size());
        Mockito.verify(taskTemplateDAO, Mockito.times(1)).getTaskTemplatesByWard(ward);
    }

    @Test
    public void shouldGetAllActiveTaskTemplates() {
        TaskTemplate template1 = new TaskTemplate();
        template1.setId(1);
        template1.setName("Template 1");
        template1.setActive(true);

        TaskTemplate template2 = new TaskTemplate();
        template2.setId(2);
        template2.setName("Template 2");
        template2.setActive(true);

        List<TaskTemplate> templates = Arrays.asList(template1, template2);

        Mockito.when(taskTemplateDAO.getAllActiveTaskTemplates()).thenReturn(templates);

        List<TaskTemplate> result = taskTemplateService.getAllActiveTaskTemplates();

        assertNotNull(result);
        assertEquals(2, result.size());
        Mockito.verify(taskTemplateDAO, Mockito.times(1)).getAllActiveTaskTemplates();
    }

    @Test
    public void shouldGetGlobalTaskTemplates() {
        TaskTemplate template = new TaskTemplate();
        template.setId(1);
        template.setName("Global Template");
        template.setActive(true);

        List<TaskTemplate> templates = Arrays.asList(template);

        Mockito.when(taskTemplateDAO.getGlobalTaskTemplates()).thenReturn(templates);

        List<TaskTemplate> result = taskTemplateService.getGlobalTaskTemplates();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getWard());
        Mockito.verify(taskTemplateDAO, Mockito.times(1)).getGlobalTaskTemplates();
    }

    @Test
    public void shouldVoidTaskTemplate() {
        TaskTemplate template = new TaskTemplate();
        template.setId(1);
        template.setName("Template to Void");
        template.setActive(true);

        Mockito.when(taskTemplateDAO.saveTaskTemplate(Mockito.any(TaskTemplate.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        taskTemplateService.voidTaskTemplate(template, "No longer needed");

        assertTrue(template.getVoided());
        assertEquals("No longer needed", template.getVoidReason());
        Mockito.verify(taskTemplateDAO, Mockito.times(1)).saveTaskTemplate(template);
    }
}
