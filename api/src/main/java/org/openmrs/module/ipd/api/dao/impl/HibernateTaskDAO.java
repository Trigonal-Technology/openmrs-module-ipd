package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.dao.TaskDAO;
import org.openmrs.module.ipd.api.model.Task;

import java.util.List;

public class HibernateTaskDAO implements TaskDAO {

	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public Task saveTask(Task task) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(task);
		return task;
	}

	@Override
	public Task getTaskByUuid(String uuid) throws DAOException {
		Query<Task> query = sessionFactory.getCurrentSession().createQuery(
		    "from Task t where t.uuid = :uuid and t.voided = false", Task.class);
		query.setParameter("uuid", uuid);
		return query.uniqueResult();
	}

	@Override
	public List<Task> getTasksByPatientAndStatuses(String patientUuid, List<Task.TaskStatus> statuses) throws DAOException {
		String hql = "from Task t where t.voided = false and t.patient.uuid = :patientUuid";
		if (statuses != null && !statuses.isEmpty()) {
			hql += " and t.status in (:statuses)";
		}
		hql += " order by t.dateCreated desc";
		Query<Task> query = sessionFactory.getCurrentSession().createQuery(hql, Task.class);
		query.setParameter("patientUuid", patientUuid);
		if (statuses != null && !statuses.isEmpty()) {
			query.setParameterList("statuses", statuses);
		}
		return query.getResultList();
	}

	@Override
	public List<Task> getFutureTasksByPatient(org.openmrs.Patient patient, java.util.Date from) throws DAOException {
		Query<Task> query = sessionFactory.getCurrentSession().createQuery(
		    "from Task t where t.patient = :patient and t.executionStartTime >= :from and t.voided = false", Task.class);
		query.setParameter("patient", patient);
		query.setParameter("from", from);
		return query.getResultList();
	}

	@Override
	public int archiveTasks(Task.TaskStatus status, java.util.Date beforeDate) throws DAOException {
		// For now, we'll just mark them as voided with a reason "Archived"
		// or if there's a specific 'ARCHIVED' status, we use that.
		// Since TaskStatus doesn't have ARCHIVED, we'll just use a bulk update if needed.
		// But the service requested ARCHIVED? Let's check Task.TaskStatus.
		// TaskStatus only has REQUESTED, IN_PROGRESS, COMPLETED, CANCELLED.
		
		Query<?> query = sessionFactory.getCurrentSession().createQuery(
		    "update Task t set t.voided = true, t.voidReason = 'Archived' " +
		    "where t.status = :status and t.executionStartTime < :beforeDate and t.voided = false");
		query.setParameter("status", status);
		query.setParameter("beforeDate", beforeDate);
		return query.executeUpdate();
	}
}
