package org.openmrs.module.ipd.api.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ipd.api.dao.TaskDAO;
import org.openmrs.module.ipd.api.dao.TaskGenerationLogDAO;
import org.openmrs.module.ipd.api.model.*;
import org.openmrs.module.ipd.api.service.TaskService;
import org.openmrs.module.ipd.api.service.TaskTemplateScheduleService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Transactional
@Slf4j
public class TaskServiceImpl extends BaseOpenmrsService implements TaskService {

	private TaskDAO taskDAO;
	private TaskTemplateScheduleService taskTemplateScheduleService;
	private TaskGenerationLogDAO taskGenerationLogDAO;

	public void setTaskDAO(TaskDAO taskDAO) {
		this.taskDAO = taskDAO;
	}

	public void setTaskTemplateScheduleService(TaskTemplateScheduleService taskTemplateScheduleService) {
		this.taskTemplateScheduleService = taskTemplateScheduleService;
	}

	public void setTaskGenerationLogDAO(TaskGenerationLogDAO taskGenerationLogDAO) {
		this.taskGenerationLogDAO = taskGenerationLogDAO;
	}

	@Override
	public Task saveTask(Task task) {
		return taskDAO.saveTask(task);
	}

	@Override
	@Transactional(readOnly = true)
	public Task getTaskByUuid(String uuid) {
		return taskDAO.getTaskByUuid(uuid);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Task> getTasksByPatientAndStatuses(String patientUuid, List<Task.TaskStatus> statuses) {
		return taskDAO.getTasksByPatientAndStatuses(patientUuid, statuses);
	}

	@Override
	public int generateTasksFromTemplate(PatientTaskTemplate patientTemplate, LocalDateTime from, LocalDateTime to) {
		try {
			TaskTemplate template = patientTemplate.getTemplate();
			List<TaskTemplateSchedule> schedules = taskTemplateScheduleService.getActiveSchedulesByTemplate(template);
			if (schedules == null || schedules.isEmpty()) {
				log.warn("No active schedule found for template: {}", template.getUuid());
				return 0;
			}
			TaskTemplateSchedule schedule = schedules.get(0);
			int count = 0;

			if (schedule.getRecurrenceType() == RecurrenceType.HOURLY) {
				LocalDateTime current = from;
				int interval = schedule.getRecurrenceInterval() != null ? schedule.getRecurrenceInterval() : 1;
				while (!current.isAfter(to)) {
					if (createTaskIfMissing(patientTemplate, schedule, current)) {
						count++;
					}
					current = current.plusHours(interval);
				}
			} else {
				LocalDate currentDate = from.toLocalDate();
				LocalDate endDate = to.toLocalDate();
				List<LocalTime> times = schedule.getActiveTimesAsList();
				if (times.isEmpty()) {
					times.add(from.toLocalTime());
				}

				while (!currentDate.isAfter(endDate)) {
					if (schedule.matchesDate(currentDate)) {
						for (LocalTime time : times) {
							LocalDateTime scheduledDateTime = LocalDateTime.of(currentDate, time);
							if (!scheduledDateTime.isBefore(from) && !scheduledDateTime.isAfter(to)) {
								if (createTaskIfMissing(patientTemplate, schedule, scheduledDateTime)) {
									count++;
								}
							}
						}
					}
					currentDate = currentDate.plusDays(1);
				}
			}
			return count;
		} catch (Exception e) {
			log.error("Error generating tasks from template", e);
			return 0;
		}
	}

	private boolean createTaskIfMissing(PatientTaskTemplate patientTemplate, TaskTemplateSchedule schedule, LocalDateTime scheduledTime) {
		if (taskGenerationLogDAO.isAlreadyGenerated(patientTemplate, scheduledTime)) {
			return false;
		}

		TaskTemplate template = patientTemplate.getTemplate();
		Task task = new Task();
		task.setPatient(patientTemplate.getPatient());
		task.setTaskTemplate(template);
		task.setWard(patientTemplate.getWard());
		task.setName(template.getName());
		task.setDescription(template.getDescription());
		task.setExecutionStartTime(Date.from(scheduledTime.atZone(ZoneId.systemDefault()).toInstant()));
		task.setStatus(Task.TaskStatus.REQUESTED);
		task.setIntent(Task.TaskIntent.ORDER);
		task.setPriority(template.getPriority());
		task.setTaskType(template.getTaskType());
		Date now = new Date();
		task.setDateCreated(now);
		stampDataCreatorAndUuidIfNeeded(task);

		Task saved = taskDAO.saveTask(task);

		TaskGenerationLog log = new TaskGenerationLog();
		log.setPatientTemplate(patientTemplate);
		log.setScheduledTime(scheduledTime);
		log.setGenerated(true);
		log.setTaskUuid(saved.getUuid());
		log.setDateCreated(now);
		stampDataCreatorAndUuidIfNeeded(log);
		taskGenerationLogDAO.saveTaskGenerationLog(log);

		return true;
	}

	/**
	 * BaseOpenmrsData requires creator, uuid, and voided; ensure Hibernate can INSERT without missing column errors.
	 */
	private void stampDataCreatorAndUuidIfNeeded(BaseOpenmrsData data) {
		if (StringUtils.isBlank(data.getUuid())) {
			data.setUuid(UUID.randomUUID().toString());
		}
		if (data.getCreator() == null) {
			User u = Context.getAuthenticatedUser();
			if (u == null) {
				try {
					u = Context.getUserService().getUser(1);
				} catch (Exception e) {
					log.warn("Could not set creator for task row (no authenticated user, user(1) missing)");
				}
			}
			if (u != null) {
				data.setCreator(u);
			}
		}
		if (data.getVoided() == null) {
			data.setVoided(false);
		}
	}

	@Override
	public void voidStaleTasks(PatientTaskTemplate patientTemplate, String reason) {
		List<Task> tasks = taskDAO.getTasksByPatientAndStatuses(patientTemplate.getPatient().getUuid(), 
				java.util.Arrays.asList(Task.TaskStatus.REQUESTED));
		for (Task task : tasks) {
			if (task.getTaskTemplate() != null && task.getTaskTemplate().equals(patientTemplate.getTemplate())) {
				// Only void future tasks or all requested tasks? Typically all requested tasks that haven't been started.
				task.setVoided(true);
				task.setVoidReason(reason);
				task.setDateVoided(new Date());
				taskDAO.saveTask(task);
			}
		}
	}

	@Override
	public int cancelFutureTasks(org.openmrs.Patient patient, LocalDateTime from) {
		Date fromDate = Date.from(from.atZone(ZoneId.systemDefault()).toInstant());
		List<Task> tasks = taskDAO.getFutureTasksByPatient(patient, fromDate);
		int count = 0;
		for (Task task : tasks) {
			if (task.getStatus() == Task.TaskStatus.REQUESTED) {
				task.setStatus(Task.TaskStatus.CANCELLED);
				taskDAO.saveTask(task);
				count++;
			}
		}
		return count;
	}

	@Override
	public int archiveCancelledTasks(LocalDateTime archiveBefore) {
		Date beforeDate = Date.from(archiveBefore.atZone(ZoneId.systemDefault()).toInstant());
		return taskDAO.archiveTasks(Task.TaskStatus.CANCELLED, beforeDate);
	}
}
