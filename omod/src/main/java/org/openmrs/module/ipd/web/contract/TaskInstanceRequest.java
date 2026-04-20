package org.openmrs.module.ipd.web.contract;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.util.HtmlUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class TaskInstanceRequest {

    @NotBlank(message = "Task name is required")
    @Size(max = 100, message = "Task name must be less than 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;
    
    @NotBlank(message = "Patient UUID is required")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid patient UUID format")
    private String patientUuid;
    
    @NotBlank(message = "Ward UUID is required")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid ward UUID format")
    private String wardUuid;
    
    @NotBlank(message = "Scheduled time is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$", message = "Invalid date format. Expected: yyyy-MM-ddTHH:mm:ss")
    private String scheduledTime;
    
    @NotNull(message = "Priority is required")
    private String priority;
    
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid template UUID format")
    private String templateUuid;
    
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
}
