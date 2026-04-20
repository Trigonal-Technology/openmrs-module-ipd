package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.Provider;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.dao.TaskAcknowledgmentDAO;
import org.openmrs.module.ipd.api.model.TaskAcknowledgment;
import org.openmrs.module.ipd.api.model.TaskInstance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class HibernateTaskAcknowledgmentDAO implements TaskAcknowledgmentDAO {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public TaskAcknowledgment saveTaskAcknowledgment(TaskAcknowledgment acknowledgment) throws DAOException {
        sessionFactory.getCurrentSession().saveOrUpdate(acknowledgment);
        return acknowledgment;
    }

    @Override
    public TaskAcknowledgment getTaskAcknowledgmentById(Integer acknowledgmentId) throws DAOException {
        return sessionFactory.getCurrentSession().get(TaskAcknowledgment.class, acknowledgmentId);
    }

    @Override
    public TaskAcknowledgment getTaskAcknowledgmentByUuid(String uuid) throws DAOException {
        Query<TaskAcknowledgment> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskAcknowledgment a where a.uuid = :uuid and a.voided = false", 
                        TaskAcknowledgment.class);
        query.setParameter("uuid", uuid);
        return query.uniqueResult();
    }

    @Override
    public TaskAcknowledgment getTaskAcknowledgmentByInstance(TaskInstance instance) throws DAOException {
        Query<TaskAcknowledgment> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskAcknowledgment a where a.instance = :instance and a.voided = false", 
                        TaskAcknowledgment.class);
        query.setParameter("instance", instance);
        return query.uniqueResult();
    }

    @Override
    public List<TaskAcknowledgment> getTaskAcknowledgmentsByProvider(Provider provider, LocalDate from, LocalDate to) throws DAOException {
        String hql = "from TaskAcknowledgment a where a.acknowledgedBy = :provider and a.voided = false";
        
        if (from != null && to != null) {
            hql += " and a.acknowledgmentTime >= :from and a.acknowledgmentTime <= :to";
        }
        hql += " order by a.acknowledgmentTime desc";
        
        Query<TaskAcknowledgment> query = sessionFactory.getCurrentSession().createQuery(hql, TaskAcknowledgment.class);
        query.setParameter("provider", provider);
        
        if (from != null && to != null) {
            query.setParameter("from", LocalDateTime.of(from, LocalTime.MIN));
            query.setParameter("to", LocalDateTime.of(to, LocalTime.MAX));
        }
        
        return query.getResultList();
    }

    @Override
    public List<TaskAcknowledgment> getUnbilledAcknowledgments(LocalDateTime from, LocalDateTime to) throws DAOException {
        String hql = "from TaskAcknowledgment a where a.billable = true and a.billedAt is null " +
                "and a.voided = false";
        
        if (from != null) {
            hql += " and a.acknowledgmentTime >= :from";
        }
        if (to != null) {
            hql += " and a.acknowledgmentTime <= :to";
        }
        hql += " order by a.acknowledgmentTime";
        
        Query<TaskAcknowledgment> query = sessionFactory.getCurrentSession().createQuery(hql, TaskAcknowledgment.class);
        
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        
        return query.getResultList();
    }

    @Override
    public List<TaskAcknowledgment> getAcknowledgmentsByBillingReference(String billingReferenceId) throws DAOException {
        Query<TaskAcknowledgment> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskAcknowledgment a where a.billingReferenceId = :billingReferenceId " +
                        "and a.voided = false", TaskAcknowledgment.class);
        query.setParameter("billingReferenceId", billingReferenceId);
        return query.getResultList();
    }
}
