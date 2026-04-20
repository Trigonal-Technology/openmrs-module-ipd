package org.openmrs.module.ipd.api.events.factory.impl;

import org.openmrs.module.ipd.api.events.handler.impl.*;
import org.openmrs.module.ipd.api.events.model.IPDEventType;
import org.openmrs.module.ipd.api.events.factory.IPDEventFactory;
import org.openmrs.module.ipd.api.events.handler.IPDEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IPDEventFactoryImpl implements IPDEventFactory {

    @Autowired
    PatientAdmitEventHandler patientAdmitEventHandler;

    @Autowired
    ShiftStartTaskEventHandler shiftStartTaskEventHandler;

    @Autowired
    RolloverTaskEventHandler rolloverTaskEventHandler;

    @Autowired
    GenerateTaskInstancesEventHandler generateTaskInstancesEventHandler;

    @Autowired
    PatientDischargeEventHandler patientDischargeEventHandler;

    @Autowired
    ArchiveCancelledTasksEventHandler archiveCancelledTasksEventHandler;

    @Override
    public IPDEventHandler createEventHandler(IPDEventType eventType) {
        switch (eventType) {
            case PATIENT_ADMIT:
                return patientAdmitEventHandler;
            case SHIFT_START_TASK:
                return shiftStartTaskEventHandler;
            case ROLLOVER_TASK:
                return rolloverTaskEventHandler;
            case GENERATE_TASK_INSTANCES:
                return generateTaskInstancesEventHandler;
            case PATIENT_DISCHARGE:
                return patientDischargeEventHandler;
            case ARCHIVE_CANCELLED_TASKS:
                return archiveCancelledTasksEventHandler;
            default:
                throw new IllegalArgumentException("Unsupported event type: " + eventType);
        }
    }

}
