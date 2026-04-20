package org.openmrs.module.ipd.api.scheduler.tasks;

import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.events.IPDEventManager;
import org.openmrs.module.ipd.api.events.model.IPDEvent;
import org.openmrs.module.ipd.api.events.model.IPDEventType;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler task to dynamically generate task instances from templates.
 * This task runs periodically (e.g., every hour) to generate upcoming task instances
 * for all active patient-task template assignments.
 */
public class GenerateTaskInstancesTask extends AbstractTask {

    private static final Logger log = LoggerFactory.getLogger(GenerateTaskInstancesTask.class);

    @Override
    public void execute() {
        log.info("Starting GenerateTaskInstancesTask...");
        try {
            IPDEventManager eventManager = Context.getRegisteredComponents(IPDEventManager.class).get(0);
            IPDEventType eventType = eventManager.getEventTypeForEncounter(String.valueOf(IPDEventType.GENERATE_TASK_INSTANCES));
            if (eventType != null) {
                IPDEvent ipdEvent = new IPDEvent(null, null, eventType);
                eventManager.processEvent(ipdEvent);
                log.info("GenerateTaskInstancesTask completed successfully");
            } else {
                log.warn("No event handler registered for GENERATE_TASK_INSTANCES");
            }
        } catch (Exception e) {
            log.error("Error while executing GenerateTaskInstancesTask", e);
        }
    }
}
