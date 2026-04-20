package org.openmrs.module.ipd.api.service;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.ipd.api.model.TaskInstance;
import org.openmrs.module.ipd.api.model.TaskInstanceStatus;
import org.openmrs.module.ipd.api.model.TaskTemplate;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskInstanceService extends OpenmrsService {

    @Authorized({ "Manage Task Instances", "Apply Task Templates" })
    TaskInstance saveTaskInstance(TaskInstance instance);

    @Authorized({ "Manage Task Instances", "Get Task Instances", "Complete Tasks" })
    TaskInstance getTaskInstanceByUuid(String uuid);

    @Authorized({ "Manage Task Instances", "Get Task Instances", "Complete Tasks" })
    List<TaskInstance> getTaskInstancesByPatient(Patient patient, List<TaskInstanceStatus> statuses,
                                                  LocalDateTime from, LocalDateTime to);

    @Authorized({ "Manage Task Instances", "Get Task Instances", "Complete Tasks" })
    List<TaskInstance> getTaskInstancesByWard(Location ward, List<TaskInstanceStatus> statuses,
                                               LocalDateTime from, LocalDateTime to);

    @Authorized({ "Manage Task Instances", "Get Task Instances" })
    List<TaskInstance> getTaskInstancesByTemplate(TaskTemplate template, LocalDateTime from, LocalDateTime to);

    @Authorized({ "Complete Tasks" })
    TaskInstance startTask(String instanceUuid);

    @Authorized({ "Complete Tasks" })
    TaskInstance completeTask(String instanceUuid, String notes, String completedOnBehalfOfProviderUuid);

    @Authorized({ "Manage Task Instances" })
    TaskInstance cancelTask(String instanceUuid, String reason);

    @Authorized({ "Manage Task Cleanup" })
    int cancelFutureInstances(Patient patient, LocalDateTime after);

    @Authorized({ "Manage Task Cleanup" })
    int archiveCancelledInstances(LocalDateTime before);

    @Authorized({ "Apply Task Templates" })
    List<TaskInstance> generateInstancesFromTemplate(TaskTemplate template, Patient patient, 
                                                      Location ward, LocalDateTime startDate, LocalDateTime endDate);
}
