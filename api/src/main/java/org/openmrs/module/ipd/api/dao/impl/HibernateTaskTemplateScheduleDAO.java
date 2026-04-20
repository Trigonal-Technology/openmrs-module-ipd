package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.dao.TaskTemplateScheduleDAO;
import org.openmrs.module.ipd.api.model.TaskTemplate;
import org.openmrs.module.ipd.api.model.TaskTemplateSchedule;

import java.util.List;

public class HibernateTaskTemplateScheduleDAO implements TaskTemplateScheduleDAO {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public TaskTemplateSchedule saveTaskTemplateSchedule(TaskTemplateSchedule schedule) throws DAOException {
        sessionFactory.getCurrentSession().saveOrUpdate(schedule);
        return schedule;
    }

    @Override
    public TaskTemplateSchedule getTaskTemplateScheduleById(Integer scheduleId) throws DAOException {
        return sessionFactory.getCurrentSession().get(TaskTemplateSchedule.class, scheduleId);
    }

    @Override
    public TaskTemplateSchedule getTaskTemplateScheduleByUuid(String uuid) throws DAOException {
        Query<TaskTemplateSchedule> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskTemplateSchedule s where s.uuid = :uuid and s.voided = false", 
                        TaskTemplateSchedule.class);
        query.setParameter("uuid", uuid);
        return query.uniqueResult();
    }

    @Override
    public List<TaskTemplateSchedule> getSchedulesByTemplate(TaskTemplate template) throws DAOException {
        Query<TaskTemplateSchedule> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskTemplateSchedule s where s.template = :template and s.voided = false", 
                        TaskTemplateSchedule.class);
        query.setParameter("template", template);
        return query.getResultList();
    }

    @Override
    public List<TaskTemplateSchedule> getActiveSchedulesByTemplate(TaskTemplate template) throws DAOException {
        Query<TaskTemplateSchedule> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskTemplateSchedule s where s.template = :template and s.active = true " +
                        "and s.voided = false", TaskTemplateSchedule.class);
        query.setParameter("template", template);
        return query.getResultList();
    }

    @Override
    public List<TaskTemplateSchedule> getAllActiveSchedules() throws DAOException {
        Query<TaskTemplateSchedule> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskTemplateSchedule s where s.active = true and s.voided = false", 
                        TaskTemplateSchedule.class);
        return query.getResultList();
    }
}
