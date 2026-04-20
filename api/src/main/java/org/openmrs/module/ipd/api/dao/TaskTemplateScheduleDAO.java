package org.openmrs.module.ipd.api.dao;

import org.openmrs.api.db.DAOException;
import org.openmrs.module.ipd.api.model.TaskTemplate;
import org.openmrs.module.ipd.api.model.TaskTemplateSchedule;

import java.util.List;

public interface TaskTemplateScheduleDAO {

    TaskTemplateSchedule saveTaskTemplateSchedule(TaskTemplateSchedule schedule) throws DAOException;

    TaskTemplateSchedule getTaskTemplateScheduleById(Integer scheduleId) throws DAOException;

    TaskTemplateSchedule getTaskTemplateScheduleByUuid(String uuid) throws DAOException;

    List<TaskTemplateSchedule> getSchedulesByTemplate(TaskTemplate template) throws DAOException;

    List<TaskTemplateSchedule> getActiveSchedulesByTemplate(TaskTemplate template) throws DAOException;

    List<TaskTemplateSchedule> getAllActiveSchedules() throws DAOException;
}
