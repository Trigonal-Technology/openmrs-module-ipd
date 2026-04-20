package org.openmrs.module.ipd.api.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.ipd.api.dao.PatientTaskTemplateDAO;
import org.openmrs.module.ipd.api.model.PatientTaskTemplate;
import org.openmrs.module.ipd.api.model.TaskTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PatientTaskTemplateServiceImplTest {

    @InjectMocks
    private PatientTaskTemplateServiceImpl patientTaskTemplateService;

    @Mock
    private PatientTaskTemplateDAO patientTaskTemplateDAO;

    @Test
    public void shouldSavePatientTaskTemplate() {
        PatientTaskTemplate patientTemplate = new PatientTaskTemplate();
        patientTemplate.setActive(true);
        patientTemplate.setStartDate(LocalDateTime.now());

        PatientTaskTemplate savedTemplate = new PatientTaskTemplate();
        savedTemplate.setId(1);
        savedTemplate.setActive(true);

        Mockito.when(patientTaskTemplateDAO.savePatientTaskTemplate(patientTemplate)).thenReturn(savedTemplate);

        PatientTaskTemplate result = patientTaskTemplateService.savePatientTaskTemplate(patientTemplate);

        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getId());
        Mockito.verify(patientTaskTemplateDAO, Mockito.times(1)).savePatientTaskTemplate(patientTemplate);
    }

    @Test
    public void shouldGetPatientTaskTemplateByUuid() {
        String uuid = "123e4567-e89b-12d3-a456-426614174000";
        PatientTaskTemplate patientTemplate = new PatientTaskTemplate();
        patientTemplate.setId(1);
        patientTemplate.setUuid(uuid);
        patientTemplate.setActive(true);

        Mockito.when(patientTaskTemplateDAO.getPatientTaskTemplateByUuid(uuid)).thenReturn(patientTemplate);

        PatientTaskTemplate result = patientTaskTemplateService.getPatientTaskTemplateByUuid(uuid);

        assertNotNull(result);
        assertEquals(uuid, result.getUuid());
        Mockito.verify(patientTaskTemplateDAO, Mockito.times(1)).getPatientTaskTemplateByUuid(uuid);
    }

    @Test
    public void shouldGetActivePatientTaskTemplatesByPatient() {
        Patient patient = new Patient();
        patient.setId(1);

        PatientTaskTemplate template1 = new PatientTaskTemplate();
        template1.setId(1);
        template1.setActive(true);

        PatientTaskTemplate template2 = new PatientTaskTemplate();
        template2.setId(2);
        template2.setActive(true);

        List<PatientTaskTemplate> templates = Arrays.asList(template1, template2);

        Mockito.when(patientTaskTemplateDAO.getActivePatientTaskTemplatesByPatient(patient)).thenReturn(templates);

        List<PatientTaskTemplate> result = patientTaskTemplateService.getActivePatientTaskTemplatesByPatient(patient);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(PatientTaskTemplate::isActive));
        Mockito.verify(patientTaskTemplateDAO, Mockito.times(1)).getActivePatientTaskTemplatesByPatient(patient);
    }

    @Test
    public void shouldApplyTemplateToPatient() {
        TaskTemplate template = new TaskTemplate();
        template.setId(1);
        template.setName("Vital Signs Check");

        Patient patient = new Patient();
        patient.setId(1);

        Location ward = new Location();
        ward.setId(1);
        ward.setName("Ward A");

        PatientTaskTemplate savedTemplate = new PatientTaskTemplate();
        savedTemplate.setId(1);
        savedTemplate.setTemplate(template);
        savedTemplate.setPatient(patient);
        savedTemplate.setWard(ward);
        savedTemplate.setStartDate(LocalDateTime.now());
        savedTemplate.setActive(true);

        Mockito.when(patientTaskTemplateDAO.savePatientTaskTemplate(Mockito.any(PatientTaskTemplate.class)))
                .thenReturn(savedTemplate);

        PatientTaskTemplate result = patientTaskTemplateService.applyTemplateToPatient(template, patient, ward);

        assertNotNull(result);
        assertTrue(result.isActive());
        assertNotNull(result.getStartDate());
        Mockito.verify(patientTaskTemplateDAO, Mockito.times(1)).savePatientTaskTemplate(Mockito.any(PatientTaskTemplate.class));
    }

    @Test
    public void shouldDeactivateForPatient() {
        Patient patient = new Patient();
        patient.setId(1);

        Mockito.doNothing().when(patientTaskTemplateDAO).deactivateForPatient(patient);

        patientTaskTemplateService.deactivateForPatient(patient);

        Mockito.verify(patientTaskTemplateDAO, Mockito.times(1)).deactivateForPatient(patient);
    }

    @Test
    public void shouldGetAllActivePatientTaskTemplates() {
        PatientTaskTemplate template1 = new PatientTaskTemplate();
        template1.setId(1);
        template1.setActive(true);

        PatientTaskTemplate template2 = new PatientTaskTemplate();
        template2.setId(2);
        template2.setActive(true);

        List<PatientTaskTemplate> templates = Arrays.asList(template1, template2);

        Mockito.when(patientTaskTemplateDAO.getAllActivePatientTaskTemplates()).thenReturn(templates);

        List<PatientTaskTemplate> result = patientTaskTemplateService.getAllActivePatientTaskTemplates();

        assertNotNull(result);
        assertEquals(2, result.size());
        Mockito.verify(patientTaskTemplateDAO, Mockito.times(1)).getAllActivePatientTaskTemplates();
    }
}
