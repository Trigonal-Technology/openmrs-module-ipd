package org.openmrs.module.ipd.api.events;

import org.openmrs.module.fhir2.model.FhirTask;
// import org.openmrs.module.fhirExtension.web.contract.TaskRequest;
import org.openmrs.module.ipd.api.events.model.IPDEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class IPDEventUtils {
    private static final Logger log = LoggerFactory.getLogger(IPDEventUtils.class);

    public static Object createNonMedicationTaskRequest(IPDEvent ipdEvent, String name, String taskType,
            Boolean isSystemGenerated) {
        log.warn(
                "createNonMedicationTaskRequest called, but Bahmni fhirExtension is disabled. Task not created for: {}",
                name);
        return null;
    }
}
