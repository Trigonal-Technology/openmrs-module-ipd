package org.openmrs.module.ipd.api.scheduler.tasks;

import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.events.IPDEventManager;
import org.openmrs.module.ipd.api.events.model.IPDEvent;
import org.openmrs.module.ipd.api.events.model.IPDEventType;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler task to archive cancelled tasks older than a retention period.
 * This runs periodically (e.g., daily) to clean up old cancelled tasks.
 */
public class ArchiveCancelledTasksTask extends AbstractTask {

    private static final Logger log = LoggerFactory.getLogger(ArchiveCancelledTasksTask.class);

    @Override
    public void execute() {
        log.info("Starting ArchiveCancelledTasksTask...");
        try {
            IPDEventManager eventManager = Context.getRegisteredComponents(IPDEventManager.class).get(0);
            IPDEventType eventType = eventManager.getEventTypeForEncounter(String.valueOf(IPDEventType.ARCHIVE_CANCELLED_TASKS));
            if (eventType != null) {
                IPDEvent ipdEvent = new IPDEvent(null, null, eventType);
                eventManager.processEvent(ipdEvent);
                log.info("ArchiveCancelledTasksTask completed successfully");
            } else {
                log.warn("No event handler registered for ARCHIVE_CANCELLED_TASKS");
            }
        } catch (Exception e) {
            log.error("Error while executing ArchiveCancelledTasksTask", e);
        }
    }
}
