package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.Provider;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.dao.TaskCompletionDAO;
import org.openmrs.module.ipd.api.model.TaskCompletion;
import org.openmrs.module.ipd.api.model.TaskInstance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class HibernateTaskCompletionDAO implements TaskCompletionDAO {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public TaskCompletion saveTaskCompletion(TaskCompletion completion) throws DAOException {
        sessionFactory.getCurrentSession().saveOrUpdate(completion);
        return completion;
    }

    @Override
    public TaskCompletion getTaskCompletionById(Integer completionId) throws DAOException {
        return sessionFactory.getCurrentSession().get(TaskCompletion.class, completionId);
    }

    @Override
    public TaskCompletion getTaskCompletionByUuid(String uuid) throws DAOException {
        Query<TaskCompletion> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskCompletion c where c.uuid = :uuid and c.voided = false", TaskCompletion.class);
        query.setParameter("uuid", uuid);
        return query.uniqueResult();
    }

    @Override
    public TaskCompletion getTaskCompletionByInstance(TaskInstance instance) throws DAOException {
        Query<TaskCompletion> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskCompletion c where c.instance = :instance and c.voided = false", 
                        TaskCompletion.class);
        query.setParameter("instance", instance);
        return query.uniqueResult();
    }

    @Override
    public List<TaskCompletion> getTaskCompletionsByProvider(Provider provider, LocalDate from, LocalDate to) throws DAOException {
        String hql = "from TaskCompletion c where c.completedOnBehalfOf = :provider and c.voided = false";
        
        if (from != null && to != null) {
            hql += " and c.completionTime >= :from and c.completionTime <= :to";
        }
        hql += " order by c.completionTime desc";
        
        Query<TaskCompletion> query = sessionFactory.getCurrentSession().createQuery(hql, TaskCompletion.class);
        query.setParameter("provider", provider);
        
        if (from != null && to != null) {
            query.setParameter("from", LocalDateTime.of(from, LocalTime.MIN));
            query.setParameter("to", LocalDateTime.of(to, LocalTime.MAX));
        }
        
        return query.getResultList();
    }

    @Override
    public List<TaskCompletion> getTaskCompletionsByCompletedBy(Integer userId, LocalDateTime from, LocalDateTime to) throws DAOException {
        String hql = "from TaskCompletion c where c.completedBy.id = :userId and c.voided = false";
        
        if (from != null) {
            hql += " and c.completionTime >= :from";
        }
        if (to != null) {
            hql += " and c.completionTime <= :to";
        }
        hql += " order by c.completionTime desc";
        
        Query<TaskCompletion> query = sessionFactory.getCurrentSession().createQuery(hql, TaskCompletion.class);
        query.setParameter("userId", userId);
        
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        
        return query.getResultList();
    }
}
