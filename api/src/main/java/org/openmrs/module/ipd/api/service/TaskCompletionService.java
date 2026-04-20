package org.openmrs.module.ipd.api.service;

import org.openmrs.Provider;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.ipd.api.model.TaskCompletion;
import org.openmrs.module.ipd.api.model.TaskInstance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskCompletionService extends OpenmrsService {

    @Authorized({ "Complete Tasks" })
    TaskCompletion saveTaskCompletion(TaskCompletion completion);

    @Authorized({ "Get Task Instances", "Complete Tasks" })
    TaskCompletion getTaskCompletionByUuid(String uuid);

    @Authorized({ "Get Task Instances", "Complete Tasks" })
    TaskCompletion getTaskCompletionByInstance(TaskInstance instance);

    @Authorized({ "View Task Reports" })
    List<TaskCompletion> getTaskCompletionsByProvider(Provider provider, LocalDate from, LocalDate to);

    @Authorized({ "View Task Reports" })
    List<TaskCompletion> getTaskCompletionsByCompletedBy(Integer userId, LocalDateTime from, LocalDateTime to);
}
