package org.openmrs.module.ipd.api.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.ipd.api.model.TaskTemplate;
import org.openmrs.module.ipd.api.model.TaskTemplateSchedule;

import java.util.List;

public interface TaskTemplateScheduleService extends OpenmrsService {

    @Authorized({ "Manage Task Templates" })
    TaskTemplateSchedule saveTaskTemplateSchedule(TaskTemplateSchedule schedule);

    @Authorized({ "Manage Task Templates", "Get Task Templates" })
    TaskTemplateSchedule getTaskTemplateScheduleByUuid(String uuid);

    @Authorized({ "Manage Task Templates", "Get Task Templates" })
    List<TaskTemplateSchedule> getSchedulesByTemplate(TaskTemplate template);

    @Authorized({ "Manage Task Templates", "Get Task Templates" })
    List<TaskTemplateSchedule> getActiveSchedulesByTemplate(TaskTemplate template);

    @Authorized({ "Manage Task Templates", "Get Task Templates" })
    List<TaskTemplateSchedule> getAllActiveSchedules();

    @Authorized({ "Manage Task Templates" })
    void voidTaskTemplateSchedule(TaskTemplateSchedule schedule, String reason);
}
