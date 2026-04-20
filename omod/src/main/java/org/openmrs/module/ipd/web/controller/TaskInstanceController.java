package org.openmrs.module.ipd.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.LocationService;
import org.openmrs.module.ipd.api.model.Task;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.model.TaskInstance;
import org.openmrs.module.ipd.api.model.TaskInstanceStatus;
import org.openmrs.module.ipd.api.service.TaskInstanceService;
import org.openmrs.module.ipd.web.contract.CompleteTaskRequest;
import org.openmrs.module.ipd.web.contract.TaskInstanceRequest;
import org.openmrs.module.ipd.web.contract.TaskInstanceResponse;
import org.openmrs.module.ipd.web.util.PrivilegeConstants;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/ipd/task-instances")
@Validated
@Slf4j
public class TaskInstanceController extends BaseRestController {

    private final TaskInstanceService taskInstanceService;
    private final PatientService patientService;
    private final LocationService locationService;

    public TaskInstanceController(TaskInstanceService taskInstanceService,
                                   PatientService patientService,
                                   LocationService locationService) {
        this.taskInstanceService = taskInstanceService;
        this.patientService = patientService;
        this.locationService = locationService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> createTaskInstance(@Valid @RequestBody TaskInstanceRequest request) {
        try {
            if (!hasPrivilege(PrivilegeConstants.MANAGE_TASK_INSTANCES)) {
                return forbidden(PrivilegeConstants.MANAGE_TASK_INSTANCES);
            }
            
            // Sanitize inputs to prevent XSS
            request.sanitize();

            TaskInstance instance = new TaskInstance();
            instance.setName(request.getName());
            instance.setDescription(StringUtils.trimToNull(request.getDescription()));

            Patient patient = patientService.getPatientByUuid(request.getPatientUuid());
            if (patient == null) {
                return new ResponseEntity<>(errorPayload("Invalid patientUuid"), BAD_REQUEST);
            }
            instance.setPatient(patient);

            Location ward = locationService.getLocationByUuid(request.getWardUuid());
            if (ward == null) {
                return new ResponseEntity<>(errorPayload("Invalid wardUuid"), BAD_REQUEST);
            }
            instance.setWard(ward);

            if (StringUtils.isNotBlank(request.getScheduledTime())) {
                instance.setScheduledTime(LocalDateTime.parse(request.getScheduledTime()));
            } else {
                instance.setScheduledTime(LocalDateTime.now());
            }

            if (StringUtils.isNotBlank(request.getPriority())) {
                instance.setPriority(Task.TaskPriority.valueOf(request.getPriority().toUpperCase()));
            }

            instance.setStatus(TaskInstanceStatus.SCHEDULED);
            instance.setCreator(Context.getAuthenticatedUser());
            instance.setDateCreated(new Date());

            TaskInstance saved = taskInstanceService.saveTaskInstance(instance);
            return new ResponseEntity<>(TaskInstanceResponse.from(saved), OK);
        } catch (Exception e) {
            log.error("Error while creating task instance", e);
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getTaskInstances(
            @RequestParam(value = "patientUuid", required = false) String patientUuid,
            @RequestParam(value = "wardUuid", required = false) String wardUuid,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        try {
            if (!hasPrivilege(PrivilegeConstants.GET_TASK_INSTANCES)) {
                return forbidden(PrivilegeConstants.GET_TASK_INSTANCES);
            }

            List<TaskInstanceStatus> statuses = parseStatuses(status);
            List<TaskInstance> instances = new ArrayList<>();

            if (StringUtils.isNotBlank(patientUuid)) {
                Patient patient = patientService.getPatientByUuid(patientUuid);
                if (patient == null) {
                    return new ResponseEntity<>(errorPayload("Invalid patientUuid"), BAD_REQUEST);
                }
                instances = taskInstanceService.getTaskInstancesByPatient(patient, statuses, from, to);
            } else if (StringUtils.isNotBlank(wardUuid)) {
                Location ward = locationService.getLocationByUuid(wardUuid);
                if (ward == null) {
                    return new ResponseEntity<>(errorPayload("Invalid wardUuid"), BAD_REQUEST);
                }
                instances = taskInstanceService.getTaskInstancesByWard(ward, statuses, from, to);
            } else {
                return new ResponseEntity<>(errorPayload("Either patientUuid or wardUuid is required"), BAD_REQUEST);
            }

            List<TaskInstanceResponse> results = instances.stream()
                    .map(TaskInstanceResponse::from)
                    .collect(Collectors.toList());

            SimpleObject response = new SimpleObject();
            response.put("results", results);
            return new ResponseEntity<>(response, OK);
        } catch (Exception e) {
            log.error("Error while listing task instances", e);
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/{instanceUuid}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getTaskInstance(
            @PathVariable("instanceUuid") 
            @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid UUID format") 
            String instanceUuid) {
        try {
            if (!hasPrivilege(PrivilegeConstants.GET_TASK_INSTANCES)) {
                return forbidden(PrivilegeConstants.GET_TASK_INSTANCES);
            }

            TaskInstance instance = taskInstanceService.getTaskInstanceByUuid(instanceUuid);
            if (instance == null) {
                return new ResponseEntity<>(errorPayload("Task instance not found"), NOT_FOUND);
            }

            return new ResponseEntity<>(TaskInstanceResponse.from(instance), OK);
        } catch (Exception e) {
            log.error("Error while getting task instance", e);
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/{instanceUuid}/start", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> startTask(
            @PathVariable("instanceUuid") 
            @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid UUID format") 
            String instanceUuid) {
        try {
            if (!hasPrivilege(PrivilegeConstants.COMPLETE_TASKS)) {
                return forbidden(PrivilegeConstants.COMPLETE_TASKS);
            }

            TaskInstance instance = taskInstanceService.startTask(instanceUuid);
            return new ResponseEntity<>(TaskInstanceResponse.from(instance), OK);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error while starting task", e);
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/{instanceUuid}/complete", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> completeTask(
            @PathVariable("instanceUuid") 
            @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid UUID format") 
            String instanceUuid,
            @Valid @RequestBody(required = false) CompleteTaskRequest request) {
        try {
            if (!hasPrivilege(PrivilegeConstants.COMPLETE_TASKS)) {
                return forbidden(PrivilegeConstants.COMPLETE_TASKS);
            }
            
            // Sanitize inputs if request provided
            if (request != null) {
                request.sanitize();
            }

            String notes = request != null ? request.getNotes() : null;
            String providerUuid = request != null ? request.getCompletedOnBehalfOfProviderUuid() : null;

            TaskInstance instance = taskInstanceService.completeTask(instanceUuid, notes, providerUuid);
            return new ResponseEntity<>(TaskInstanceResponse.from(instance), OK);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error while completing task", e);
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/{instanceUuid}/cancel", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> cancelTask(
            @PathVariable("instanceUuid") 
            @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid UUID format") 
            String instanceUuid,
            @RequestParam(value = "reason", required = false) 
            @Size(min = 5, max = 255, message = "Cancel reason must be between 5 and 255 characters") 
            String reason) {
        try {
            if (!hasPrivilege(PrivilegeConstants.MANAGE_TASK_INSTANCES)) {
                return forbidden(PrivilegeConstants.MANAGE_TASK_INSTANCES);
            }
            
            // Ensure reason is provided
            String cancelReason = StringUtils.isNotBlank(reason) ? reason : "Cancelled by user";

            TaskInstance instance = taskInstanceService.cancelTask(instanceUuid, cancelReason);
            return new ResponseEntity<>(TaskInstanceResponse.from(instance), OK);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error while cancelling task", e);
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        }
    }

    private List<TaskInstanceStatus> parseStatuses(String raw) {
        List<TaskInstanceStatus> out = new ArrayList<>();
        if (StringUtils.isBlank(raw)) {
            return out;
        }
        String[] chunks = raw.split(",");
        for (String chunk : chunks) {
            if (StringUtils.isBlank(chunk)) {
                continue;
            }
            out.add(TaskInstanceStatus.valueOf(chunk.trim().toUpperCase()));
        }
        return out;
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
