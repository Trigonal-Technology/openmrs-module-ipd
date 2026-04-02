package org.openmrs.module.ipd.web.contract;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {

	private String name;

	private String description;

	private String intent;

	private String status;

	private String priority;

	private String notes;

	private Reference patient;

	private Reference taskType;

	private ExecutionPeriod executionPeriod;

	@Getter
	@Setter
	public static class Reference {
		private String uuid;
	}

	@Getter
	@Setter
	public static class ExecutionPeriod {
		private String start;
		private String end;
	}
}
