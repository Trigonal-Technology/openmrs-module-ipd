package org.openmrs.module.ipd.web.contract;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.util.HtmlUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CompleteTaskRequest {

    @Size(max = 1000, message = "Notes must be less than 1000 characters")
    private String notes;
    
    @NotBlank(message = "Provider UUID is required")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid provider UUID format")
    private String completedOnBehalfOfProviderUuid;
    
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$", message = "Invalid date format. Expected: yyyy-MM-ddTHH:mm:ss")
    private String completionTime;
    
    /**
     * Sanitizes input fields to prevent XSS attacks
     */
    public void sanitize() {
        if (this.notes != null) {
            this.notes = HtmlUtils.htmlEscape(this.notes.trim());
        }
    }
}
