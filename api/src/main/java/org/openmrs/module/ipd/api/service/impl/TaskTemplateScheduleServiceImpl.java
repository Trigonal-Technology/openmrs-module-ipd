package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ipd.api.dao.TaskTemplateScheduleDAO;
import org.openmrs.module.ipd.api.model.TaskTemplate;
import org.openmrs.module.ipd.api.model.TaskTemplateSchedule;
import org.openmrs.module.ipd.api.service.TaskTemplateScheduleService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional
public class TaskTemplateScheduleServiceImpl extends BaseOpenmrsService implements TaskTemplateScheduleService {

    private TaskTemplateScheduleDAO taskTemplateScheduleDAO;

    public void setTaskTemplateScheduleDAO(TaskTemplateScheduleDAO taskTemplateScheduleDAO) {
        this.taskTemplateScheduleDAO = taskTemplateScheduleDAO;
    }

    @Override
    public TaskTemplateSchedule saveTaskTemplateSchedule(TaskTemplateSchedule schedule) {
        return taskTemplateScheduleDAO.saveTaskTemplateSchedule(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskTemplateSchedule getTaskTemplateScheduleByUuid(String uuid) {
        return taskTemplateScheduleDAO.getTaskTemplateScheduleByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskTemplateSchedule> getSchedulesByTemplate(TaskTemplate template) {
        return taskTemplateScheduleDAO.getSchedulesByTemplate(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskTemplateSchedule> getActiveSchedulesByTemplate(TaskTemplate template) {
        return taskTemplateScheduleDAO.getActiveSchedulesByTemplate(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskTemplateSchedule> getAllActiveSchedules() {
        return taskTemplateScheduleDAO.getAllActiveSchedules();
    }

    @Override
    public void voidTaskTemplateSchedule(TaskTemplateSchedule schedule, String reason) {
        schedule.setVoided(true);
        schedule.setVoidReason(reason);
        schedule.setDateVoided(new Date());
        taskTemplateScheduleDAO.saveTaskTemplateSchedule(schedule);
    }
}
