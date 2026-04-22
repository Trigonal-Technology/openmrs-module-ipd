package org.openmrs.module.ipd.api.scheduler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.model.PatientTaskTemplate;
import org.openmrs.module.ipd.api.service.PatientTaskTemplateService;
import org.openmrs.module.ipd.api.service.TaskService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class TaskGenerationTask extends AbstractTask {

    @Override
    public void execute() {
        log.info("Starting background task generation...");

        try {
            TaskService taskService = Context.getService(TaskService.class);
            PatientTaskTemplateService patientTaskTemplateService = Context.getService(PatientTaskTemplateService.class);

            // Fetch all active patient task templates
            List<PatientTaskTemplate> activeTemplates = patientTaskTemplateService.getAllActivePatientTaskTemplates();
            
            // Define the generation window (e.g., next 48 hours)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime windowEnd = now.plusDays(2);

            int totalGenerated = 0;
            for (PatientTaskTemplate patientTemplate : activeTemplates) {
                int generated = taskService.generateTasksFromTemplate(patientTemplate, now, windowEnd);
                totalGenerated += generated;
            }

            log.info("Background task generation complete. Total tasks generated: {}", totalGenerated);

        } catch (Exception e) {
            log.error("Error during background task generation", e);
        }
    }
}
