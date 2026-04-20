package org.openmrs.module.ipd.api.events.model;

public enum IPDEventType {
    PATIENT_ADMIT,
    SHIFT_START_TASK,
    ROLLOVER_TASK,
    GENERATE_TASK_INSTANCES,
    PATIENT_DISCHARGE,
    ARCHIVE_CANCELLED_TASKS;
}
