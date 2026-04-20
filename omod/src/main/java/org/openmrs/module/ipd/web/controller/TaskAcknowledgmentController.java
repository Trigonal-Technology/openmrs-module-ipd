package org.openmrs.module.ipd.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.model.AcknowledgmentMethod;
import org.openmrs.module.ipd.api.model.TaskAcknowledgment;
import org.openmrs.module.ipd.api.model.TaskInstance;
import org.openmrs.module.ipd.api.service.TaskAcknowledgmentService;
import org.openmrs.module.ipd.api.service.TaskInstanceService;
import org.openmrs.module.ipd.web.contract.AcknowledgeTaskRequest;
import org.openmrs.module.ipd.web.util.PrivilegeConstants;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/ipd/tasks")
@Validated
@Slf4j
public class TaskAcknowledgmentController extends BaseRestController {

    private final TaskInstanceService taskInstanceService;
    private final TaskAcknowledgmentService taskAcknowledgmentService;
    private final ProviderService providerService;

    public TaskAcknowledgmentController(TaskInstanceService taskInstanceService,
                                         TaskAcknowledgmentService taskAcknowledgmentService,
                                         ProviderService providerService) {
        this.taskInstanceService = taskInstanceService;
        this.taskAcknowledgmentService = taskAcknowledgmentService;
        this.providerService = providerService;
    }

    @RequestMapping(value = "/{instanceUuid}/acknowledge", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> acknowledgeTask(
            @PathVariable("instanceUuid") 
            @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid UUID format") 
            String instanceUuid,
            @Valid @RequestBody AcknowledgeTaskRequest request) {
        try {
            if (!hasPrivilege(PrivilegeConstants.ACKNOWLEDGE_TASKS)) {
                return forbidden(PrivilegeConstants.ACKNOWLEDGE_TASKS);
            }
            
            // Sanitize inputs to prevent XSS
            request.sanitize();

            TaskInstance instance = taskInstanceService.getTaskInstanceByUuid(instanceUuid);
            if (instance == null) {
                return new ResponseEntity<>(errorPayload("Task instance not found"), NOT_FOUND);
            }

            // Get the current user as the acknowledging provider
            Provider provider = getCurrentProvider();
            if (provider == null) {
                return new ResponseEntity<>(errorPayload("No provider associated with current user"), BAD_REQUEST);
            }

            TaskAcknowledgment acknowledgment = new TaskAcknowledgment();
            acknowledgment.setInstance(instance);
            acknowledgment.setAcknowledgedBy(provider);
            acknowledgment.setAcknowledgmentTime(LocalDateTime.now());
            
            // Use the helper method for case-insensitive enum conversion
            if (request.getAcknowledgmentMethod() != null) {
                acknowledgment.setAcknowledgmentMethod(request.getAcknowledgmentMethodEnum());
            } else {
                acknowledgment.setAcknowledgmentMethod(AcknowledgmentMethod.MANUAL_ENTRY);
            }
            
            acknowledgment.setDeviceId(StringUtils.trimToNull(request.getDeviceId()));
            acknowledgment.setNotes(StringUtils.trimToNull(request.getNotes()));
            acknowledgment.setCreator(Context.getAuthenticatedUser());
            acknowledgment.setDateCreated(new Date());
            acknowledgment.setBillable(true);

            TaskAcknowledgment saved = taskAcknowledgmentService.saveTaskAcknowledgment(acknowledgment);
            return new ResponseEntity<>(convertToResponse(saved), OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error while acknowledging task", e);
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/acknowledgments/{acknowledgmentUuid}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getAcknowledgment(
            @PathVariable("acknowledgmentUuid") 
            @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid UUID format") 
            String acknowledgmentUuid) {
        try {
            if (!hasPrivilege(PrivilegeConstants.GET_TASK_INSTANCES)) {
                return forbidden(PrivilegeConstants.GET_TASK_INSTANCES);
            }

            TaskAcknowledgment acknowledgment = taskAcknowledgmentService.getTaskAcknowledgmentByUuid(acknowledgmentUuid);
            if (acknowledgment == null) {
                return new ResponseEntity<>(errorPayload("Task acknowledgment not found"), NOT_FOUND);
            }

            return new ResponseEntity<>(convertToResponse(acknowledgment), OK);
        } catch (Exception e) {
            log.error("Error while getting task acknowledgment", e);
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/{instanceUuid}/acknowledgment", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getAcknowledgmentByInstance(@PathVariable("instanceUuid") String instanceUuid) {
        try {
            if (!hasPrivilege(PrivilegeConstants.GET_TASK_INSTANCES)) {
                return forbidden(PrivilegeConstants.GET_TASK_INSTANCES);
            }

            TaskInstance instance = taskInstanceService.getTaskInstanceByUuid(instanceUuid);
            if (instance == null) {
                return new ResponseEntity<>(errorPayload("Task instance not found"), NOT_FOUND);
            }

            TaskAcknowledgment acknowledgment = taskAcknowledgmentService.getTaskAcknowledgmentByInstance(instance);
            if (acknowledgment == null) {
                return new ResponseEntity<>(errorPayload("No acknowledgment found for this task"), NOT_FOUND);
            }

            return new ResponseEntity<>(convertToResponse(acknowledgment), OK);
        } catch (Exception e) {
            log.error("Error while getting task acknowledgment", e);
            return new ResponseEntity<>(errorPayload(e.getMessage()), BAD_REQUEST);
        }
    }

    private Map<String, Object> convertToResponse(TaskAcknowledgment acknowledgment) {
        Map<String, Object> response = new HashMap<>();
        response.put("uuid", acknowledgment.getUuid());
        response.put("instanceUuid", acknowledgment.getInstance() != null ? acknowledgment.getInstance().getUuid() : null);
        
        Map<String, Object> providerRef = new HashMap<>();
        if (acknowledgment.getAcknowledgedBy() != null) {
            providerRef.put("uuid", acknowledgment.getAcknowledgedBy().getUuid());
            providerRef.put("display", acknowledgment.getAcknowledgedBy().getName());
        }
        response.put("acknowledgedBy", providerRef);
        
        response.put("acknowledgmentTime", acknowledgment.getAcknowledgmentTime() != null 
                ? acknowledgment.getAcknowledgmentTime().toString() : null);
        response.put("acknowledgmentMethod", acknowledgment.getAcknowledgmentMethod() != null 
                ? acknowledgment.getAcknowledgmentMethod().name() : null);
        response.put("deviceId", acknowledgment.getDeviceId());
        response.put("notes", acknowledgment.getNotes());
        response.put("billable", acknowledgment.isBillable());
        response.put("billingCode", acknowledgment.getBillingCode());
        response.put("billingAmount", acknowledgment.getBillingAmount());
        response.put("billedAt", acknowledgment.getBilledAt() != null 
                ? acknowledgment.getBilledAt().toString() : null);
        response.put("billingReferenceId", acknowledgment.getBillingReferenceId());
        
        return response;
    }

    private Provider getCurrentProvider() {
        // Try to find a provider linked to the current user
        return providerService.getProvidersByPerson(Context.getAuthenticatedUser().getPerson())
                .stream()
                .findFirst()
                .orElse(null);
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
