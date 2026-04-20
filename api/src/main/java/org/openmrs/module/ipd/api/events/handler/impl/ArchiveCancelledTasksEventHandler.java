package org.openmrs.module.ipd.api.events.handler.impl;

import org.openmrs.module.ipd.api.events.handler.IPDEventHandler;
import org.openmrs.module.ipd.api.events.model.IPDEvent;
import org.openmrs.module.ipd.api.service.TaskInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Event handler to archive cancelled tasks older than a retention period.
 * This moves cancelled tasks to ARCHIVED status after they have been cancelled
 * for a specified period (default: 30 days).
 */
@Component
public class ArchiveCancelledTasksEventHandler implements IPDEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ArchiveCancelledTasksEventHandler.class);
    private static final int DEFAULT_RETENTION_DAYS = 30;

    @Autowired
    private TaskInstanceService taskInstanceService;

    @Value("${nidan.ipd.archiveRetentionDays:30}")
    private int retentionDays;

    @Override
    @Transactional
    public void handleEvent(IPDEvent event) {
        log.info("Starting archive of cancelled tasks older than {} days...", retentionDays);
        
        try {
            LocalDateTime archiveBefore = LocalDateTime.now().minusDays(retentionDays);
            
            int archivedCount = taskInstanceService.archiveCancelledInstances(archiveBefore);
            
            log.info("Archived {} cancelled task instances older than {}", archivedCount, archiveBefore);
        } catch (Exception e) {
            log.error("Error while archiving cancelled tasks", e);
        }
    }
}
