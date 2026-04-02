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
}
