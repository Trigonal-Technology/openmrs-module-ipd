package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.dao.TaskInstanceDAO;
import org.openmrs.module.ipd.api.model.TaskInstance;
import org.openmrs.module.ipd.api.model.TaskInstanceStatus;
import org.openmrs.module.ipd.api.model.TaskTemplate;

import java.time.LocalDateTime;
import java.util.List;

public class HibernateTaskInstanceDAO implements TaskInstanceDAO {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public TaskInstance saveTaskInstance(TaskInstance instance) throws DAOException {
        sessionFactory.getCurrentSession().saveOrUpdate(instance);
        return instance;
    }

    @Override
    public TaskInstance getTaskInstanceById(Integer instanceId) throws DAOException {
        return sessionFactory.getCurrentSession().get(TaskInstance.class, instanceId);
    }

    @Override
    public TaskInstance getTaskInstanceByUuid(String uuid) throws DAOException {
        Query<TaskInstance> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskInstance i where i.uuid = :uuid and i.voided = false", TaskInstance.class);
        query.setParameter("uuid", uuid);
        return query.uniqueResult();
    }

    @Override
    public List<TaskInstance> getTaskInstancesByPatient(Patient patient, List<TaskInstanceStatus> statuses,
                                                         LocalDateTime from, LocalDateTime to) throws DAOException {
        String hql = "from TaskInstance i where i.patient = :patient and i.voided = false";
        
        if (statuses != null && !statuses.isEmpty()) {
            hql += " and i.status in (:statuses)";
        }
        if (from != null) {
            hql += " and i.scheduledTime >= :from";
        }
        if (to != null) {
            hql += " and i.scheduledTime <= :to";
        }
        hql += " order by i.scheduledTime";
        
        Query<TaskInstance> query = sessionFactory.getCurrentSession().createQuery(hql, TaskInstance.class);
        query.setParameter("patient", patient);
        
        if (statuses != null && !statuses.isEmpty()) {
            query.setParameterList("statuses", statuses);
        }
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        
        return query.getResultList();
    }

    @Override
    public List<TaskInstance> getTaskInstancesByWard(Location ward, List<TaskInstanceStatus> statuses,
                                                        LocalDateTime from, LocalDateTime to) throws DAOException {
        String hql = "from TaskInstance i where i.ward = :ward and i.voided = false";
        
        if (statuses != null && !statuses.isEmpty()) {
            hql += " and i.status in (:statuses)";
        }
        if (from != null) {
            hql += " and i.scheduledTime >= :from";
        }
        if (to != null) {
            hql += " and i.scheduledTime <= :to";
        }
        hql += " order by i.scheduledTime";
        
        Query<TaskInstance> query = sessionFactory.getCurrentSession().createQuery(hql, TaskInstance.class);
        query.setParameter("ward", ward);
        
        if (statuses != null && !statuses.isEmpty()) {
            query.setParameterList("statuses", statuses);
        }
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        
        return query.getResultList();
    }

    @Override
    public List<TaskInstance> getTaskInstancesByTemplate(TaskTemplate template, LocalDateTime from, LocalDateTime to) throws DAOException {
        String hql = "from TaskInstance i where i.template = :template and i.voided = false";
        
        if (from != null) {
            hql += " and i.scheduledTime >= :from";
        }
        if (to != null) {
            hql += " and i.scheduledTime <= :to";
        }
        hql += " order by i.scheduledTime";
        
        Query<TaskInstance> query = sessionFactory.getCurrentSession().createQuery(hql, TaskInstance.class);
        query.setParameter("template", template);
        
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        
        return query.getResultList();
    }

    @Override
    public List<TaskInstance> getTaskInstancesByStatus(TaskInstanceStatus status) throws DAOException {
        Query<TaskInstance> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskInstance i where i.status = :status and i.voided = false", TaskInstance.class);
        query.setParameter("status", status);
        return query.getResultList();
    }

    @Override
    public List<TaskInstance> getFutureTaskInstances(Patient patient, LocalDateTime after) throws DAOException {
        Query<TaskInstance> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskInstance i where i.patient = :patient and i.scheduledTime > :after " +
                        "and i.status in (:statuses) and i.voided = false", TaskInstance.class);
        query.setParameter("patient", patient);
        query.setParameter("after", after);
        query.setParameterList("statuses", new TaskInstanceStatus[]{TaskInstanceStatus.SCHEDULED, TaskInstanceStatus.IN_PROGRESS});
        return query.getResultList();
    }

    @Override
    public int cancelFutureInstances(Patient patient, LocalDateTime after) throws DAOException {
        String hql = "update TaskInstance i set i.status = :cancelledStatus " +
                "where i.patient = :patient and i.scheduledTime > :after " +
                "and i.status in (:statuses) and i.voided = false";
        
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("cancelledStatus", TaskInstanceStatus.CANCELLED);
        query.setParameter("patient", patient);
        query.setParameter("after", after);
        query.setParameterList("statuses", new TaskInstanceStatus[]{TaskInstanceStatus.SCHEDULED, TaskInstanceStatus.IN_PROGRESS});
        
        return query.executeUpdate();
    }

    @Override
    public int archiveCancelledInstances(LocalDateTime before) throws DAOException {
        String hql = "update TaskInstance i set i.status = :archivedStatus " +
                "where i.status = :cancelledStatus and i.dateCreated < :before and i.voided = false";
        
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("archivedStatus", TaskInstanceStatus.ARCHIVED);
        query.setParameter("cancelledStatus", TaskInstanceStatus.CANCELLED);
        query.setParameter("before", before);
        
        return query.executeUpdate();
    }
}
