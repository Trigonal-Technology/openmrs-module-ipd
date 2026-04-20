package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.dao.TaskGenerationLogDAO;
import org.openmrs.module.ipd.api.model.PatientTaskTemplate;
import org.openmrs.module.ipd.api.model.TaskGenerationLog;

import java.time.LocalDateTime;
import java.util.List;

public class HibernateTaskGenerationLogDAO implements TaskGenerationLogDAO {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public TaskGenerationLog saveTaskGenerationLog(TaskGenerationLog log) throws DAOException {
        sessionFactory.getCurrentSession().saveOrUpdate(log);
        return log;
    }

    @Override
    public TaskGenerationLog getTaskGenerationLogById(Integer logId) throws DAOException {
        return sessionFactory.getCurrentSession().get(TaskGenerationLog.class, logId);
    }

    @Override
    public List<TaskGenerationLog> getLogsByPatientTemplate(PatientTaskTemplate patientTemplate) throws DAOException {
        Query<TaskGenerationLog> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskGenerationLog l where l.patientTemplate = :patientTemplate", 
                        TaskGenerationLog.class);
        query.setParameter("patientTemplate", patientTemplate);
        return query.getResultList();
    }

    @Override
    public List<TaskGenerationLog> getPendingGenerationLogs(LocalDateTime before) throws DAOException {
        Query<TaskGenerationLog> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskGenerationLog l where l.generated = false " +
                        "and l.scheduledTime <= :before order by l.scheduledTime", TaskGenerationLog.class);
        query.setParameter("before", before);
        return query.getResultList();
    }

    @Override
    public boolean isAlreadyGenerated(PatientTaskTemplate patientTemplate, LocalDateTime scheduledTime) throws DAOException {
        Query<Long> query = sessionFactory.getCurrentSession()
                .createQuery("select count(l) from TaskGenerationLog l where l.patientTemplate = :patientTemplate " +
                        "and l.scheduledTime = :scheduledTime and l.generated = true", Long.class);
        query.setParameter("patientTemplate", patientTemplate);
        query.setParameter("scheduledTime", scheduledTime);
        Long count = query.uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public TaskGenerationLog getLogByPatientTemplateAndTime(PatientTaskTemplate patientTemplate, LocalDateTime scheduledTime) throws DAOException {
        Query<TaskGenerationLog> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskGenerationLog l where l.patientTemplate = :patientTemplate " +
                        "and l.scheduledTime = :scheduledTime", TaskGenerationLog.class);
        query.setParameter("patientTemplate", patientTemplate);
        query.setParameter("scheduledTime", scheduledTime);
        return query.uniqueResult();
    }
}
