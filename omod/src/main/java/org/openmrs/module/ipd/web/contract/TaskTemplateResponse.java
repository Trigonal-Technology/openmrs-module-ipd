package org.openmrs.module.ipd.web.contract;

import lombok.Builder;
import lombok.Getter;
import org.openmrs.module.ipd.api.model.TaskTemplate;

import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
public class TaskTemplateResponse {

    private String uuid;
    private String name;
    private String description;
    private Map<String, Object> taskType;
    private Map<String, Object> ward;
    private String priority;
    private Integer estimatedDurationMinutes;
    private Map<String, Object> defaultAssigneeRole;
    private boolean active;

    public static TaskTemplateResponse from(TaskTemplate template) {
        Map<String, Object> taskTypeRef = null;
        if (template.getTaskType() != null) {
            taskTypeRef = new HashMap<>();
            taskTypeRef.put("uuid", template.getTaskType().getUuid());
            taskTypeRef.put("display", template.getTaskType().getDisplayString());
        }

        Map<String, Object> wardRef = null;
        if (template.getWard() != null) {
            wardRef = new HashMap<>();
            wardRef.put("uuid", template.getWard().getUuid());
            wardRef.put("display", template.getWard().getName());
        }

        Map<String, Object> roleRef = null;
        if (template.getDefaultAssigneeRole() != null) {
            roleRef = new HashMap<>();
            roleRef.put("uuid", template.getDefaultAssigneeRole().getUuid());
            roleRef.put("display", template.getDefaultAssigneeRole().getDisplayString());
        }

        return TaskTemplateResponse.builder()
                .uuid(template.getUuid())
                .name(template.getName())
                .description(template.getDescription())
                .taskType(taskTypeRef)
                .ward(wardRef)
                .priority(template.getPriority() != null ? template.getPriority().name() : null)
                .estimatedDurationMinutes(template.getEstimatedDurationMinutes())
                .defaultAssigneeRole(roleRef)
                .active(template.isActive())
                .build();
    }
}
