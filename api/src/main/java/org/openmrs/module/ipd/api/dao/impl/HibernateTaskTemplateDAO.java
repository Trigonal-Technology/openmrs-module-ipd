package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.Location;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.dao.TaskTemplateDAO;
import org.openmrs.module.ipd.api.model.TaskTemplate;

import java.util.List;

public class HibernateTaskTemplateDAO implements TaskTemplateDAO {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public TaskTemplate saveTaskTemplate(TaskTemplate template) throws DAOException {
        sessionFactory.getCurrentSession().saveOrUpdate(template);
        return template;
    }

    @Override
    public TaskTemplate getTaskTemplateById(Integer templateId) throws DAOException {
        return sessionFactory.getCurrentSession().get(TaskTemplate.class, templateId);
    }

    @Override
    public TaskTemplate getTaskTemplateByUuid(String uuid) throws DAOException {
        Query<TaskTemplate> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskTemplate t where t.uuid = :uuid and t.voided = false", TaskTemplate.class);
        query.setParameter("uuid", uuid);
        return query.uniqueResult();
    }

    @Override
    public List<TaskTemplate> getTaskTemplatesByWard(Location ward) throws DAOException {
        Query<TaskTemplate> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskTemplate t where t.ward = :ward and t.active = true and t.voided = false " +
                        "order by t.name", TaskTemplate.class);
        query.setParameter("ward", ward);
        return query.getResultList();
    }

    @Override
    public List<TaskTemplate> getAllActiveTaskTemplates() throws DAOException {
        Query<TaskTemplate> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskTemplate t where t.active = true and t.voided = false " +
                        "order by t.name", TaskTemplate.class);
        return query.getResultList();
    }

    @Override
    public List<TaskTemplate> getGlobalTaskTemplates() throws DAOException {
        Query<TaskTemplate> query = sessionFactory.getCurrentSession()
                .createQuery("from TaskTemplate t where t.ward is null and t.active = true and t.voided = false " +
                        "order by t.name", TaskTemplate.class);
        return query.getResultList();
    }
}
