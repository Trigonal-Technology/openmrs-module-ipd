package org.openmrs.module.ipd.api.events.handler.impl;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.ipd.api.dao.TaskGenerationLogDAO;
import org.openmrs.module.ipd.api.events.handler.IPDEventHandler;
import org.openmrs.module.ipd.api.events.model.IPDEvent;
import org.openmrs.module.ipd.api.model.*;
import org.openmrs.module.ipd.api.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * Event handler to dynamically generate task instances from templates.
 * Processes all active PatientTaskTemplate records and generates upcoming task instances
 * based on the recurrence schedule defined in TaskTemplateSchedule.
 */
@Component
public class GenerateTaskInstancesEventHandler implements IPDEventHandler {

    private static final Logger log = LoggerFactory.getLogger(GenerateTaskInstancesEventHandler.class);
    private static final int GENERATION_WINDOW_HOURS = 48; // Generate tasks 48 hours ahead (includes tomorrow)
    private static final int MINIMUM_DAYS_FOR_DAILY_TASKS = 2; // Ensure at least 2 days of daily tasks are generated

    @Autowired
    private PatientTaskTemplateService patientTaskTemplateService;

    @Autowired
    private TaskTemplateScheduleService taskTemplateScheduleService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskGenerationLogDAO taskGenerationLogDAO;

    @Override
    @Transactional
    public void handleEvent(IPDEvent event) {
        log.info("Starting task instance generation...");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime generationWindowEnd = now.plusHours(GENERATION_WINDOW_HOURS);
        
        List<PatientTaskTemplate> activeTemplates = patientTaskTemplateService.getAllActivePatientTaskTemplates();
        log.info("Found {} active patient-task templates", activeTemplates.size());
        
        int generatedCount = 0;
        for (PatientTaskTemplate patientTemplate : activeTemplates) {
            try {
                generatedCount += generateInstancesForTemplate(patientTemplate, now, generationWindowEnd);
            } catch (Exception e) {
                log.error("Error generating instances for patient template: {}" + 
                        patientTemplate.getUuid(), e);
            }
        }
        
        log.info("Task instance generation completed. Generated {} instances", generatedCount);
    }

    private int generateInstancesForTemplate(PatientTaskTemplate patientTemplate, 
                                              LocalDateTime from, LocalDateTime to) {
        int count = 0;
        TaskTemplate template = patientTemplate.getTemplate();
        Patient patient = patientTemplate.getPatient();
        Location ward = patientTemplate.getWard();
        
        List<TaskTemplateSchedule> schedules = taskTemplateScheduleService.getActiveSchedulesByTemplate(template);
        
        for (TaskTemplateSchedule schedule : schedules) {
            List<LocalDateTime> scheduledTimes = calculateScheduledTimes(schedule, from, to);
            
            for (LocalDateTime scheduledTime : scheduledTimes) {
                // Check if already generated
                if (taskGenerationLogDAO.isAlreadyGenerated(patientTemplate, scheduledTime)) {
                    continue;
                }
                
                // Create scheduled task
                Task task = new Task();
                task.setPatient(patient);
                task.setTaskTemplate(template);
                task.setWard(ward);
                task.setName(template.getName());
                task.setDescription(template.getDescription());
                task.setExecutionStartTime(Date.from(scheduledTime.atZone(java.time.ZoneId.systemDefault()).toInstant()));
                task.setStatus(Task.TaskStatus.REQUESTED);
                task.setIntent(Task.TaskIntent.ORDER);
                task.setPriority(template.getPriority());
                task.setTaskType(template.getTaskType());
                task.setDateCreated(new Date());
                
                Task saved = taskService.saveTask(task);
                
                // Log the generation
                TaskGenerationLog logEntry = new TaskGenerationLog();
                logEntry.setPatientTemplate(patientTemplate);
                logEntry.setScheduledTime(scheduledTime);
                logEntry.setGenerated(true);
                logEntry.setTaskUuid(saved.getUuid());
                logEntry.setDateCreated(new Date());
                taskGenerationLogDAO.saveTaskGenerationLog(logEntry);
                
                count++;
            }
        }
        
        return count;
    }

    private List<LocalDateTime> calculateScheduledTimes(TaskTemplateSchedule schedule, 
                                                         LocalDateTime from, LocalDateTime to) {
        List<LocalDateTime> times = new java.util.ArrayList<>();
        
        LocalDateTime current = schedule.getStartDate();
        if (current.isBefore(from)) {
            // Adjust start to within the generation window
            current = adjustToNextOccurrence(current, schedule.getRecurrenceType(), 
                    schedule.getRecurrenceInterval(), from);
        }
        
        LocalDateTime endDate = schedule.getEndDate() != null ? schedule.getEndDate() : to;
        LocalDateTime actualEnd = endDate.isBefore(to) ? endDate : to;
        
        while (!current.isAfter(actualEnd)) {
            times.add(current);
            current = getNextOccurrence(current, schedule.getRecurrenceType(), 
                    schedule.getRecurrenceInterval());
        }
        
        return times;
    }

    private LocalDateTime adjustToNextOccurrence(LocalDateTime start, RecurrenceType type, 
                                                  int interval, LocalDateTime target) {
        LocalDateTime current = start;
        while (current.isBefore(target)) {
            current = getNextOccurrence(current, type, interval);
        }
        return current;
    }

    private LocalDateTime getNextOccurrence(LocalDateTime current, RecurrenceType type, int interval) {
        switch (type) {
            case HOURLY:
                return current.plusHours(interval);
            case DAILY:
                return current.plusDays(interval);
            case WEEKLY:
                return current.plusWeeks(interval);
            default:
                return current.plusDays(interval);
        }
    }
}
