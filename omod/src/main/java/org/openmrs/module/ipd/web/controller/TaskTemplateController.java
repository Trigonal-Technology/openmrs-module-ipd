package org.openmrs.module.ipd.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.Patient;
import org.openmrs.module.ipd.api.model.PatientTaskTemplate;
import org.openmrs.module.ipd.api.model.RecurrenceType;
import org.openmrs.module.ipd.api.model.Task;
import org.openmrs.module.ipd.api.model.TaskTemplate;
import org.openmrs.module.ipd.api.model.TaskTemplateSchedule;
import org.openmrs.module.ipd.api.service.PatientTaskTemplateService;
import org.openmrs.module.ipd.api.service.TaskService;
import org.openmrs.module.ipd.api.service.TaskTemplateScheduleService;
import org.openmrs.module.ipd.api.service.TaskTemplateService;
import org.openmrs.module.ipd.web.contract.ApplyTemplateRequest;
import org.openmrs.module.ipd.web.contract.TaskTemplateRequest;
import org.openmrs.module.ipd.web.contract.TaskTemplateResponse;
import org.openmrs.module.ipd.web.util.PrivilegeConstants;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/ipd/task-templates")
@Validated
@Slf4j
public class TaskTemplateController extends BaseRestController {

    private final TaskTemplateService taskTemplateService;
    private final PatientTaskTemplateService patientTaskTemplateService;
    private final TaskTemplateScheduleService taskTemplateScheduleService;
    private final TaskService taskService;
    private final LocationService locationService;
    private final ConceptService conceptService;

    public TaskTemplateController(TaskTemplateService taskTemplateService,
                                   PatientTaskTemplateService patientTaskTemplateService,
                                   TaskTemplateScheduleService taskTemplateScheduleService,
                                   TaskService taskService,
                                   LocationService locationService,
                                   ConceptService conceptService) {
        this.taskTemplateService = taskTemplateService;
        this.patientTaskTemplateService = patientTaskTemplateService;
        this.taskService = taskService;
        this.taskTemplateScheduleService = taskTemplateScheduleService;
        this.locationService = locationService;
        this.conceptService = conceptService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> createTaskTemplate(@Valid @RequestBody TaskTemplateRequest request) {
        try {
            if (!hasPrivilege(PrivilegeConstants.MANAGE_TASK_TEMPLATES)) {
                return forbidden(PrivilegeConstants.MANAGE_TASK_TEMPLATES);
            }
            
            // Sanitize inputs to prevent XSS
            request.sanitize();

            TaskTemplate template = new TaskTemplate();
            template.setName(request.getName());
            template.setDescription(StringUtils.trimToNull(request.getDescription()));
            
            Concept taskType = conceptService.getConceptByUuid(request.getTaskTypeUuid());
            if (taskType == null) {
                return new ResponseEntity<>(errorPayload("Invalid taskTypeUuid"), BAD_REQUEST);
            }
            template.setTaskType(taskType);

            if (StringUtils.isNotBlank(request.getWardUuid())) {
                Location ward = locationService.getLocationByUuid(request.getWardUuid());
                if (ward == null) {
                    return new ResponseEntity<>(errorPayload("Invalid wardUuid"), BAD_REQUEST);
                }
                template.setWard(ward);
            }

            if (request.getPriority() != null) {
                template.setPriority(request.getPriority());
            }

            template.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());

            if (StringUtils.isNotBlank(request.getDefaultAssigneeRoleUuid())) {
                Concept role = conceptService.getConceptByUuid(request.getDefaultAssigneeRoleUuid());
                if (role != null) {
                    template.setDefaultAssigneeRole(role);
                }
            }

            template.setCreator(Context.getAuthenticatedUser());
            template.setDateCreated(new Date());
            template.setActive(true);

            TaskTemplate saved = taskTemplateService.saveTaskTemplate(template);

            // Create schedule if recurrence is provided
            TaskTemplateSchedule schedule = null;
            if (request.getRecurrence() != null) {
                schedule = createScheduleFromRequest(saved, request.getRecurrence());
                if (schedule != null) {
                    taskTemplateScheduleService.saveTaskTemplateSchedule(schedule);
                }
            }

            return new ResponseEntity<>(TaskTemplateResponse.from(saved, schedule), OK);
        } catch (Exception e) {
            log.error("Error while creating task template", e);
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getTaskTemplates(@RequestParam(value = "wardUuid", required = false) String wardUuid) {
        try {
            if (!hasPrivilege(PrivilegeConstants.GET_TASK_TEMPLATES)) {
                return forbidden(PrivilegeConstants.GET_TASK_TEMPLATES);
            }

            List<TaskTemplate> templates;
            if (StringUtils.isNotBlank(wardUuid)) {
                Location ward = locationService.getLocationByUuid(wardUuid);
                if (ward == null) {
                    return new ResponseEntity<>(errorPayload("Invalid wardUuid"), BAD_REQUEST);
                }
                templates = taskTemplateService.getTaskTemplatesByWard(ward);
            } else {
                templates = taskTemplateService.getAllActiveTaskTemplates();
            }

            List<TaskTemplateResponse> results = templates.stream()
                    .map(t -> TaskTemplateResponse.from(t, getScheduleForTemplate(t)))
                    .collect(Collectors.toList());
            
            SimpleObject response = new SimpleObject();
            response.put("results", results);
            return new ResponseEntity<>(response, OK);
        } catch (Exception e) {
            log.error("Error while listing task templates", e);
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/{templateUuid}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getTaskTemplate(
            @PathVariable("templateUuid") 
            @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid UUID format") 
            String templateUuid) {
        try {
            if (!hasPrivilege(PrivilegeConstants.GET_TASK_TEMPLATES)) {
                return forbidden(PrivilegeConstants.GET_TASK_TEMPLATES);
            }

            TaskTemplate template = taskTemplateService.getTaskTemplateByUuid(templateUuid);
            if (template == null) {
                return new ResponseEntity<>(errorPayload("Task template not found"), NOT_FOUND);
            }

            return new ResponseEntity<>(TaskTemplateResponse.from(template, getScheduleForTemplate(template)), OK);
        } catch (Exception e) {
            log.error("Error while getting task template", e);
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/{templateUuid}", method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Object> toggleTaskTemplateActive(
            @PathVariable("templateUuid")
            @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid UUID format")
            String templateUuid,
            @RequestBody Map<String, Object> payload) {
        try {
            if (!hasPrivilege(PrivilegeConstants.MANAGE_TASK_TEMPLATES)) {
                return forbidden(PrivilegeConstants.MANAGE_TASK_TEMPLATES);
            }

            TaskTemplate template = taskTemplateService.getTaskTemplateByUuid(templateUuid);
            if (template == null) {
                return new ResponseEntity<>(errorPayload("Task template not found"), NOT_FOUND);
            }

            Object activeValue = payload.get("active");
            if (activeValue == null) {
                return new ResponseEntity<>(errorPayload("'active' field is required"), BAD_REQUEST);
            }

            boolean active = Boolean.parseBoolean(activeValue.toString());
            template.setActive(active);
            template.setDateChanged(new Date());
            template.setChangedBy(Context.getAuthenticatedUser());

            TaskTemplate saved = taskTemplateService.saveTaskTemplate(template);
            return new ResponseEntity<>(TaskTemplateResponse.from(saved, getScheduleForTemplate(saved)), OK);
        } catch (Exception e) {
            log.error("Error while toggling task template active status", e);
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/{templateUuid}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Object> voidTaskTemplate(
            @PathVariable("templateUuid") 
            @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid UUID format") 
            String templateUuid,
            @RequestParam(value = "reason", required = false) 
            @Size(min = 5, max = 255, message = "Void reason must be between 5 and 255 characters") 
            String reason) {
        try {
            if (!hasPrivilege(PrivilegeConstants.MANAGE_TASK_TEMPLATES)) {
                return forbidden(PrivilegeConstants.MANAGE_TASK_TEMPLATES);
            }

            TaskTemplate template = taskTemplateService.getTaskTemplateByUuid(templateUuid);
            if (template == null) {
                return new ResponseEntity<>(errorPayload("Task template not found"), NOT_FOUND);
            }

            String voidReason = StringUtils.isNotBlank(reason) ? reason : "Voided by user";
            taskTemplateService.voidTaskTemplate(template, voidReason);
            
            SimpleObject response = new SimpleObject();
            response.put("message", "Task template voided successfully");
            return new ResponseEntity<>(response, OK);
        } catch (Exception e) {
            log.error("Error while voiding task template", e);
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/{templateUuid}/apply", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> applyTemplate(
            @PathVariable("templateUuid")
            @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid UUID format")
            String templateUuid,
            @Valid @RequestBody ApplyTemplateRequest request) {
        try {
            if (!hasPrivilege(PrivilegeConstants.APPLY_TASK_TEMPLATES)) {
                return forbidden(PrivilegeConstants.APPLY_TASK_TEMPLATES);
            }

            TaskTemplate template = taskTemplateService.getTaskTemplateByUuid(templateUuid);
            if (template == null) {
                return new ResponseEntity<>(errorPayload("Task template not found"), NOT_FOUND);
            }

            Patient patient = Context.getPatientService().getPatientByUuid(request.getPatientUuid());
            if (patient == null) {
                return new ResponseEntity<>(errorPayload("Patient not found"), BAD_REQUEST);
            }

            Location ward = locationService.getLocationByUuid(request.getWardUuid());
            if (ward == null) {
                return new ResponseEntity<>(errorPayload("Ward not found"), BAD_REQUEST);
            }

            LocalDateTime startDate = LocalDateTime.now();
            if (request.getStartDate() != null) {
                startDate = LocalDateTime.parse(request.getStartDate());
            }
            
            LocalDateTime endDate = null;
            if (request.getEndDate() != null) {
                endDate = LocalDateTime.parse(request.getEndDate());
            }
            
            PatientTaskTemplate patientTemplate = patientTaskTemplateService.applyTemplateToPatient(
                    template, patient, ward, startDate, endDate);

            // Generate scheduled tasks immediately for the remainder of the current day (plus 1 day)
            // The background scheduler will handle the rest.
            LocalDateTime generateUntil = startDate.plusDays(1);
            int generatedCount = taskService.generateTasksFromTemplate(patientTemplate, startDate, generateUntil);

            SimpleObject response = new SimpleObject();
            response.put("message", "Template applied successfully");
            response.put("generatedTasks", generatedCount);
            response.put("patientTaskTemplateUuid", patientTemplate.getUuid());
            response.put("patientUuid", patient.getUuid());
            response.put("wardUuid", ward.getUuid());
            return new ResponseEntity<>(response, OK);
        } catch (Exception e) {
            log.error("Error while applying template", e);
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        }
    }

    private ResponseEntity<Object> forbidden(String privilege) {
        return new ResponseEntity<>(errorPayload("User doesn't have the following privilege: " + privilege), FORBIDDEN);
    }

    private TaskTemplateSchedule getScheduleForTemplate(TaskTemplate template) {
        List<TaskTemplateSchedule> schedules = taskTemplateScheduleService.getSchedulesByTemplate(template);
        return schedules.isEmpty() ? null : schedules.get(0);
    }

    private TaskTemplateSchedule createScheduleFromRequest(TaskTemplate template, TaskTemplateRequest.RecurrenceRequest recurrence) {
        try {
            TaskTemplateSchedule schedule = new TaskTemplateSchedule();
            schedule.setTemplate(template);
            schedule.setRecurrenceType(RecurrenceType.valueOf(recurrence.getType()));
            schedule.setRecurrenceInterval(recurrence.getInterval() != null ? recurrence.getInterval() : 1);
            schedule.setStartDate(LocalDateTime.parse(recurrence.getStartDate() != null ? recurrence.getStartDate() : LocalDateTime.now().toString()));
            if (recurrence.getEndDate() != null) {
                schedule.setEndDate(LocalDateTime.parse(recurrence.getEndDate()));
            }
            if (recurrence.getActiveTimes() != null && recurrence.getActiveTimes().length > 0) {
                schedule.setActiveTimes(String.join(",", recurrence.getActiveTimes()));
            }
            if (recurrence.getDaysOfWeek() != null && recurrence.getDaysOfWeek().length > 0) {
                schedule.setDaysOfWeek(String.join(",", java.util.Arrays.stream(recurrence.getDaysOfWeek())
                    .map(String::valueOf).collect(java.util.stream.Collectors.toList())));
            }
            schedule.setActive(true);
            schedule.setCreator(Context.getAuthenticatedUser());
            schedule.setDateCreated(new Date());
            return schedule;
        } catch (Exception e) {
            log.error("Error creating schedule from request", e);
            return null;
        }
    }

    private SimpleObject errorPayload(String message) {
        SimpleObject out = new SimpleObject();
        out.put("error", message);
        return out;
    }

    public boolean hasPrivilege(String privilege) {
        return Context.getUserContext().hasPrivilege(privilege);
    }
}
