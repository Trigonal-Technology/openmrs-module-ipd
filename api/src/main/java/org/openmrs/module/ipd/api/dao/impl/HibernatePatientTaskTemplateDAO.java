package org.openmrs.module.ipd.api.dao.impl;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.dao.PatientTaskTemplateDAO;
import org.openmrs.module.ipd.api.model.PatientTaskTemplate;
import org.openmrs.module.ipd.api.model.TaskTemplate;

import java.util.List;

public class HibernatePatientTaskTemplateDAO implements PatientTaskTemplateDAO {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public PatientTaskTemplate savePatientTaskTemplate(PatientTaskTemplate patientTaskTemplate) throws DAOException {
        sessionFactory.getCurrentSession().saveOrUpdate(patientTaskTemplate);
        return patientTaskTemplate;
    }

    @Override
    public PatientTaskTemplate getPatientTaskTemplateById(Integer patientTemplateId) throws DAOException {
        return sessionFactory.getCurrentSession().get(PatientTaskTemplate.class, patientTemplateId);
    }

    @Override
    public PatientTaskTemplate getPatientTaskTemplateByUuid(String uuid) throws DAOException {
        Query<PatientTaskTemplate> query = sessionFactory.getCurrentSession()
                .createQuery("from PatientTaskTemplate pt where pt.uuid = :uuid and pt.voided = false", 
                        PatientTaskTemplate.class);
        query.setParameter("uuid", uuid);
        return query.uniqueResult();
    }

    @Override
    public List<PatientTaskTemplate> getPatientTaskTemplatesByPatient(Patient patient) throws DAOException {
        Query<PatientTaskTemplate> query = sessionFactory.getCurrentSession()
                .createQuery("from PatientTaskTemplate pt where pt.patient = :patient and pt.voided = false", 
                        PatientTaskTemplate.class);
        query.setParameter("patient", patient);
        return query.getResultList();
    }

    @Override
    public List<PatientTaskTemplate> getActivePatientTaskTemplatesByPatient(Patient patient) throws DAOException {
        Query<PatientTaskTemplate> query = sessionFactory.getCurrentSession()
                .createQuery("from PatientTaskTemplate pt where pt.patient = :patient and pt.active = true " +
                        "and pt.voided = false", PatientTaskTemplate.class);
        query.setParameter("patient", patient);
        return query.getResultList();
    }

    @Override
    public List<PatientTaskTemplate> getPatientTaskTemplatesByTemplate(TaskTemplate template) throws DAOException {
        Query<PatientTaskTemplate> query = sessionFactory.getCurrentSession()
                .createQuery("from PatientTaskTemplate pt where pt.template = :template and pt.voided = false", 
                        PatientTaskTemplate.class);
        query.setParameter("template", template);
        return query.getResultList();
    }

    @Override
    public List<PatientTaskTemplate> getActivePatientTaskTemplatesByWard(Location ward) throws DAOException {
        Query<PatientTaskTemplate> query = sessionFactory.getCurrentSession()
                .createQuery("from PatientTaskTemplate pt where pt.ward = :ward and pt.active = true " +
                        "and pt.voided = false", PatientTaskTemplate.class);
        query.setParameter("ward", ward);
        return query.getResultList();
    }

    @Override
    public List<PatientTaskTemplate> getAllActivePatientTaskTemplates() throws DAOException {
        Query<PatientTaskTemplate> query = sessionFactory.getCurrentSession()
                .createQuery("from PatientTaskTemplate pt where pt.active = true and pt.voided = false", 
                        PatientTaskTemplate.class);
        return query.getResultList();
    }

    @Override
    public void deactivateForPatient(Patient patient) throws DAOException {
        String hql = "update PatientTaskTemplate pt set pt.active = false where pt.patient = :patient " +
                "and pt.active = true and pt.voided = false";
        Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("patient", patient);
        query.executeUpdate();
    }
}
