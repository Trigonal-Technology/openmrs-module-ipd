package org.openmrs.module.ipd.api.service;

import org.openmrs.Location;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.ipd.api.model.TaskTemplate;

import java.util.List;

public interface TaskTemplateService extends OpenmrsService {

    @Authorized({ "Manage Task Templates" })
    TaskTemplate saveTaskTemplate(TaskTemplate template);

    @Authorized({ "Manage Task Templates", "Get Task Templates" })
    TaskTemplate getTaskTemplateByUuid(String uuid);

    @Authorized({ "Manage Task Templates", "Get Task Templates" })
    List<TaskTemplate> getTaskTemplatesByWard(Location ward);

    @Authorized({ "Manage Task Templates", "Get Task Templates" })
    List<TaskTemplate> getAllActiveTaskTemplates();

    @Authorized({ "Manage Task Templates", "Get Task Templates" })
    List<TaskTemplate> getGlobalTaskTemplates();

    @Authorized({ "Manage Task Templates" })
    void voidTaskTemplate(TaskTemplate template, String reason);
}
