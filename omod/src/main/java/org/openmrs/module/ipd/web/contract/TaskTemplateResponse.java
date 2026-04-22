package org.openmrs.module.ipd.web.contract;

import lombok.Builder;
import lombok.Getter;
import org.openmrs.module.ipd.api.model.TaskTemplate;
import org.openmrs.module.ipd.api.model.TaskTemplateSchedule;

import java.util.Arrays;
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
    private RecurrenceResponse recurrence;

    public static TaskTemplateResponse from(TaskTemplate template, TaskTemplateSchedule schedule) {
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

        RecurrenceResponse recurrenceResponse = null;
        if (schedule != null) {
            recurrenceResponse = RecurrenceResponse.builder()
                    .type(schedule.getRecurrenceType() != null ? schedule.getRecurrenceType().name() : null)
                    .interval(schedule.getRecurrenceInterval())
                    .startDate(schedule.getStartDate() != null ? schedule.getStartDate().toString() : null)
                    .endDate(schedule.getEndDate() != null ? schedule.getEndDate().toString() : null)
                    .activeTimes(schedule.getActiveTimes() != null ? schedule.getActiveTimes().split(",") : null)
                    .daysOfWeek(schedule.getDaysOfWeek() != null ? 
                            Arrays.stream(schedule.getDaysOfWeek().split(",")).map(Integer::parseInt).toArray(Integer[]::new) : null)
                    .build();
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
                .recurrence(recurrenceResponse)
                .build();
    }

    @Getter
    @Builder
    public static class RecurrenceResponse {
        private String type;
        private Integer interval;
        private String startDate;
        private String endDate;
        private String[] activeTimes;
        private Integer[] daysOfWeek;
    }
}
