package org.openmrs.module.ipd.api.dao;

import org.openmrs.Provider;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.model.TaskAcknowledgment;
import org.openmrs.module.ipd.api.model.TaskInstance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskAcknowledgmentDAO {

    TaskAcknowledgment saveTaskAcknowledgment(TaskAcknowledgment acknowledgment) throws DAOException;

    TaskAcknowledgment getTaskAcknowledgmentById(Integer acknowledgmentId) throws DAOException;

    TaskAcknowledgment getTaskAcknowledgmentByUuid(String uuid) throws DAOException;

    TaskAcknowledgment getTaskAcknowledgmentByInstance(TaskInstance instance) throws DAOException;

    List<TaskAcknowledgment> getTaskAcknowledgmentsByProvider(Provider provider, LocalDate from, LocalDate to) throws DAOException;

    List<TaskAcknowledgment> getUnbilledAcknowledgments(LocalDateTime from, LocalDateTime to) throws DAOException;

    List<TaskAcknowledgment> getAcknowledgmentsByBillingReference(String billingReferenceId) throws DAOException;
}
