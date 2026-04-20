package org.openmrs.module.ipd.api.service.impl;

import org.openmrs.Location;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ipd.api.dao.TaskTemplateDAO;
import org.openmrs.module.ipd.api.model.TaskTemplate;
import org.openmrs.module.ipd.api.service.TaskTemplateService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional
public class TaskTemplateServiceImpl extends BaseOpenmrsService implements TaskTemplateService {

    private TaskTemplateDAO taskTemplateDAO;

    public void setTaskTemplateDAO(TaskTemplateDAO taskTemplateDAO) {
        this.taskTemplateDAO = taskTemplateDAO;
    }

    @Override
    public TaskTemplate saveTaskTemplate(TaskTemplate template) {
        return taskTemplateDAO.saveTaskTemplate(template);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskTemplate getTaskTemplateByUuid(String uuid) {
        return taskTemplateDAO.getTaskTemplateByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskTemplate> getTaskTemplatesByWard(Location ward) {
        return taskTemplateDAO.getTaskTemplatesByWard(ward);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskTemplate> getAllActiveTaskTemplates() {
        return taskTemplateDAO.getAllActiveTaskTemplates();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskTemplate> getGlobalTaskTemplates() {
        return taskTemplateDAO.getGlobalTaskTemplates();
    }

    @Override
    public void voidTaskTemplate(TaskTemplate template, String reason) {
        template.setVoided(true);
        template.setVoidReason(reason);
        template.setDateVoided(new Date());
        taskTemplateDAO.saveTaskTemplate(template);
    }
}
