package org.openmrs.module.ipd.web.contract;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class ApplyTemplateRequest {

    @NotBlank(message = "Patient UUID is required")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid patient UUID format")
    private String patientUuid;
    
    @NotBlank(message = "Ward UUID is required")
    @Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", message = "Invalid ward UUID format")
    private String wardUuid;
    
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$", message = "Invalid start date format. Expected: yyyy-MM-ddTHH:mm:ss")
    private String startDate;
    
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$", message = "Invalid end date format. Expected: yyyy-MM-ddTHH:mm:ss")
    private String endDate;
}
