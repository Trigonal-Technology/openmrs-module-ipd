package org.openmrs.module.ipd.web.contract;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.module.ipd.api.model.Task;
import org.springframework.web.util.HtmlUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class TaskTemplateRequest {

    @NotBlank(message = "Template name is required")
    @Size(max = 100, message = "Template name must be less than 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-_.,()\\[\\]]+$", message = "Template name contains invalid characters")
    private String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid task type UUID format")
    private String taskTypeUuid;

    @NotNull(message = "Priority is required")
    private Task.TaskPriority priority;

    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid ward UUID format")
    private String wardUuid;

    private boolean active = true;

    @Valid
    private RecurrenceRequest recurrence;

    private String defaultAssigneeRoleUuid;

    private Integer estimatedDurationMinutes;

    /**
     * Sanitizes input fields to prevent XSS attacks
     */
    public void sanitize() {
        if (this.name != null) {
            this.name = HtmlUtils.htmlEscape(this.name.trim());
        }
        if (this.description != null) {
            this.description = HtmlUtils.htmlEscape(this.description.trim());
        }
    }

    @Getter
    @Setter
    public static class RecurrenceRequest {
        private String type;
        private Integer interval;
        private String startDate;
        private String endDate;
        private String[] activeTimes;
        private Integer[] daysOfWeek;
    }
}
