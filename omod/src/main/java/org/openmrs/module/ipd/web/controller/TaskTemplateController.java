package org.openmrs.module.ipd.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.model.Task;
import org.openmrs.module.ipd.api.model.TaskTemplate;
import org.openmrs.module.ipd.api.service.TaskTemplateService;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/ipd/task-templates")
@Validated
@Slf4j
public class TaskTemplateController extends BaseRestController {

    private final TaskTemplateService taskTemplateService;
    private final LocationService locationService;
    private final ConceptService conceptService;

    public TaskTemplateController(TaskTemplateService taskTemplateService, 
                                   LocationService locationService,
                                   ConceptService conceptService) {
        this.taskTemplateService = taskTemplateService;
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
            return new ResponseEntity<>(TaskTemplateResponse.from(saved), OK);
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
                    .map(TaskTemplateResponse::from)
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

            return new ResponseEntity<>(TaskTemplateResponse.from(template), OK);
        } catch (Exception e) {
            log.error("Error while getting task template", e);
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

    private ResponseEntity<Object> forbidden(String privilege) {
        return new ResponseEntity<>(errorPayload("User doesn't have the following privilege: " + privilege), FORBIDDEN);
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
