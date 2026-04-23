package org.openmrs.module.ipd.api.dao;

import org.openmrs.Location;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.model.TaskTemplate;

import java.util.List;

public interface TaskTemplateDAO {

    TaskTemplate saveTaskTemplate(TaskTemplate template) throws DAOException;

    TaskTemplate getTaskTemplateById(Integer templateId) throws DAOException;

    TaskTemplate getTaskTemplateByUuid(String uuid) throws DAOException;

    List<TaskTemplate> getTaskTemplatesByWard(Location ward) throws DAOException;

    List<TaskTemplate> getTaskTemplates(Location ward, String searchQuery, int offset, int pageSize) throws DAOException;

    long countTaskTemplates(Location ward, String searchQuery) throws DAOException;

    List<TaskTemplate> getAllActiveTaskTemplates() throws DAOException;

    List<TaskTemplate> getGlobalTaskTemplates() throws DAOException;
}
