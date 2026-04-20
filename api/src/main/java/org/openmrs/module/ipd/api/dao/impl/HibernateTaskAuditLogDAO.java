package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.audit.TaskAuditLog;
import org.openmrs.module.ipd.api.dao.TaskAuditLogDAO;

import java.time.LocalDateTime;
import java.util.List;

public class HibernateTaskAuditLogDAO implements TaskAuditLogDAO {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public TaskAuditLog saveAuditLog(TaskAuditLog auditLog) throws DAOException {
        sessionFactory.getCurrentSession().save(auditLog);
        return auditLog;
    }

    @Override
    public TaskAuditLog getAuditLogById(Integer auditId) throws DAOException {
        return sessionFactory.getCurrentSession().get(TaskAuditLog.class, auditId);
    }

    @Override
    public List<TaskAuditLog> getAuditLogsForEntity(String entityType, String entityUuid, 
                                                     LocalDateTime from, LocalDateTime to) throws DAOException {
        String hql = "from TaskAuditLog a where a.entityType = :entityType and a.entityUuid = :entityUuid";
        
        if (from != null) {
            hql += " and a.actionTime >= :from";
        }
        if (to != null) {
            hql += " and a.actionTime <= :to";
        }
        hql += " order by a.actionTime desc";
        
        Query<TaskAuditLog> query = sessionFactory.getCurrentSession().createQuery(hql, TaskAuditLog.class);
        query.setParameter("entityType", entityType);
        query.setParameter("entityUuid", entityUuid);
        
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        
        return query.getResultList();
    }

    @Override
    public List<TaskAuditLog> getAuditLogsForUser(Integer userId, LocalDateTime from, LocalDateTime to) throws DAOException {
        String hql = "from TaskAuditLog a where a.user.id = :userId";
        
        if (from != null) {
            hql += " and a.actionTime >= :from";
        }
        if (to != null) {
            hql += " and a.actionTime <= :to";
        }
        hql += " order by a.actionTime desc";
        
        Query<TaskAuditLog> query = sessionFactory.getCurrentSession().createQuery(hql, TaskAuditLog.class);
        query.setParameter("userId", userId);
        
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        
        return query.getResultList();
    }

    @Override
    public List<TaskAuditLog> getAuditLogsByAction(String action, LocalDateTime from, LocalDateTime to) throws DAOException {
        String hql = "from TaskAuditLog a where a.action = :action";
        
        if (from != null) {
            hql += " and a.actionTime >= :from";
        }
        if (to != null) {
            hql += " and a.actionTime <= :to";
        }
        hql += " order by a.actionTime desc";
        
        Query<TaskAuditLog> query = sessionFactory.getCurrentSession().createQuery(hql, TaskAuditLog.class);
        query.setParameter("action", action);
        
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        
        return query.getResultList();
    }

    @Override
    public List<TaskAuditLog> getAuditLogsByPatient(String patientUuid, LocalDateTime from, LocalDateTime to) throws DAOException {
        String hql = "from TaskAuditLog a where a.patientUuid = :patientUuid";
        
        if (from != null) {
            hql += " and a.actionTime >= :from";
        }
        if (to != null) {
            hql += " and a.actionTime <= :to";
        }
        hql += " order by a.actionTime desc";
        
        Query<TaskAuditLog> query = sessionFactory.getCurrentSession().createQuery(hql, TaskAuditLog.class);
        query.setParameter("patientUuid", patientUuid);
        
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        
        return query.getResultList();
    }

    @Override
    public List<TaskAuditLog> getAllAuditLogs(LocalDateTime from, LocalDateTime to, int limit) throws DAOException {
        String hql = "from TaskAuditLog a where 1=1";
        
        if (from != null) {
            hql += " and a.actionTime >= :from";
        }
        if (to != null) {
            hql += " and a.actionTime <= :to";
        }
        hql += " order by a.actionTime desc";
        
        Query<TaskAuditLog> query = sessionFactory.getCurrentSession().createQuery(hql, TaskAuditLog.class);
        
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        
        query.setMaxResults(limit);
        return query.getResultList();
    }
}
