package org.openmrs.module.ipd.web.contract;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskUpdateRequest {

	private String status;

	private String notes;

	private TaskRequest.ExecutionPeriod executionPeriod;
}
