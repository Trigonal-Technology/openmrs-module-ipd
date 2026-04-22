package org.openmrs.module.ipd.api.dao;

import org.openmrs.Provider;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.model.TaskAcknowledgment;
import org.openmrs.module.ipd.api.model.Task;

import java.util.Date;
import java.util.List;

public interface TaskAcknowledgmentDAO {

	TaskAcknowledgment saveTaskAcknowledgment(TaskAcknowledgment acknowledgment) throws DAOException;

	TaskAcknowledgment getTaskAcknowledgmentById(Integer acknowledgmentId) throws DAOException;

	TaskAcknowledgment getTaskAcknowledgmentByUuid(String uuid) throws DAOException;

	TaskAcknowledgment getTaskAcknowledgmentByTask(Task task) throws DAOException;

	List<TaskAcknowledgment> getTaskAcknowledgmentsByProvider(Provider provider, Date from, Date to) throws DAOException;

	List<TaskAcknowledgment> getUnbilledAcknowledgments(Date from, Date to) throws DAOException;

	List<TaskAcknowledgment> getAcknowledgmentsByBillingReference(String billingReferenceId) throws DAOException;
}
