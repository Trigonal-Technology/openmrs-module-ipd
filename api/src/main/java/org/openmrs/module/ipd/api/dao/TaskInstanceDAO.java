package org.openmrs.module.ipd.api.dao;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.model.TaskInstance;
import org.openmrs.module.ipd.api.model.TaskInstanceStatus;
import org.openmrs.module.ipd.api.model.TaskTemplate;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskInstanceDAO {

    TaskInstance saveTaskInstance(TaskInstance instance) throws DAOException;

    TaskInstance getTaskInstanceById(Integer instanceId) throws DAOException;

    TaskInstance getTaskInstanceByUuid(String uuid) throws DAOException;

    List<TaskInstance> getTaskInstancesByPatient(Patient patient, List<TaskInstanceStatus> statuses, 
                                                    LocalDateTime from, LocalDateTime to) throws DAOException;

    List<TaskInstance> getTaskInstancesByWard(Location ward, List<TaskInstanceStatus> statuses,
                                                 LocalDateTime from, LocalDateTime to) throws DAOException;

    List<TaskInstance> getTaskInstancesByTemplate(TaskTemplate template, LocalDateTime from, LocalDateTime to) throws DAOException;

    List<TaskInstance> getTaskInstancesByStatus(TaskInstanceStatus status) throws DAOException;

    List<TaskInstance> getFutureTaskInstances(Patient patient, LocalDateTime after) throws DAOException;

    int cancelFutureInstances(Patient patient, LocalDateTime after) throws DAOException;

    int archiveCancelledInstances(LocalDateTime before) throws DAOException;
}
