package org.openmrs.module.ipd.web.contract;

import lombok.Getter;
import lombok.Setter;
import org.openmrs.module.ipd.api.model.AcknowledgmentMethod;
import org.springframework.web.util.HtmlUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class AcknowledgeTaskRequest {

    @NotBlank(message = "Acknowledgment method is required")
    @Pattern(regexp = "^(?i)(QR_SCAN|NFC_TAP|MANUAL_ENTRY|BIOMETRIC)$", message = "Invalid acknowledgment method. Must be: QR_SCAN, NFC_TAP, MANUAL_ENTRY, or BIOMETRIC")
    private String acknowledgmentMethod;
    
    @Size(max = 50, message = "Device ID must be less than 50 characters")
    private String deviceId;
    
    @Size(max = 500, message = "Notes must be less than 500 characters")
    private String notes;
    
    /**
     * Sanitizes input fields to prevent XSS attacks
     */
    public void sanitize() {
        if (this.notes != null) {
            this.notes = HtmlUtils.htmlEscape(this.notes.trim());
        }
    }
    
    /**
     * Returns the acknowledgment method as enum, case-insensitive
     */
    public AcknowledgmentMethod getAcknowledgmentMethodEnum() {
        if (this.acknowledgmentMethod == null) {
            return null;
        }
        return AcknowledgmentMethod.valueOf(this.acknowledgmentMethod.toUpperCase());
    }
}
