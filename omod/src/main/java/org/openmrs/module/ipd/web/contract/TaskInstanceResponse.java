package org.openmrs.module.ipd.web.contract;

import lombok.Builder;
import lombok.Getter;
import org.openmrs.module.ipd.api.model.TaskInstance;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class TaskInstanceResponse {

    private String uuid;
    private String name;
    private String description;
    private String status;
    private String priority;
    private String scheduledTime;
    private String startedTime;
    private Map<String, Object> patient;
    private Map<String, Object> ward;
    private Map<String, Object> template;
    private Map<String, Object> createdBy;
    private String dateCreated;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static TaskInstanceResponse from(TaskInstance instance) {
        Map<String, Object> patientRef = null;
        if (instance.getPatient() != null) {
            patientRef = new HashMap<>();
            patientRef.put("uuid", instance.getPatient().getUuid());
            patientRef.put("display", instance.getPatient().getPersonName() != null 
                    ? instance.getPatient().getPersonName().toString() : null);
        }

        Map<String, Object> wardRef = null;
        if (instance.getWard() != null) {
            wardRef = new HashMap<>();
            wardRef.put("uuid", instance.getWard().getUuid());
            wardRef.put("display", instance.getWard().getName());
        }

        Map<String, Object> templateRef = null;
        if (instance.getTemplate() != null) {
            templateRef = new HashMap<>();
            templateRef.put("uuid", instance.getTemplate().getUuid());
            templateRef.put("name", instance.getTemplate().getName());
        }

        Map<String, Object> createdByRef = null;
        if (instance.getCreator() != null) {
            createdByRef = new HashMap<>();
            createdByRef.put("uuid", instance.getCreator().getUuid());
            createdByRef.put("display", instance.getCreator().getUsername());
        }

        return TaskInstanceResponse.builder()
                .uuid(instance.getUuid())
                .name(instance.getName())
                .description(instance.getDescription())
                .status(instance.getStatus() != null ? instance.getStatus().name() : null)
                .priority(instance.getPriority() != null ? instance.getPriority().name() : null)
                .scheduledTime(instance.getScheduledTime() != null 
                        ? instance.getScheduledTime().format(ISO_FORMATTER) : null)
                .startedTime(instance.getStartedTime() != null 
                        ? instance.getStartedTime().format(ISO_FORMATTER) : null)
                .patient(patientRef)
                .ward(wardRef)
                .template(templateRef)
                .createdBy(createdByRef)
                .dateCreated(instance.getDateCreated() != null 
                        ? instance.getDateCreated().toInstant().toString() : null)
                .build();
    }
}
