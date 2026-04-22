package org.openmrs.module.ipd.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "ipd_task")
public class Task extends BaseChangeableOpenmrsData {

	public enum TaskStatus {
		REQUESTED,
		IN_PROGRESS,
		COMPLETED,
		CANCELLED
	}

	public enum TaskIntent {
		ORDER,
		PROPOSAL
	}

	public enum TaskPriority {
		ROUTINE,
		URGENT,
		STAT
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "task_id")
	private Integer taskId;

	@ManyToOne(optional = false)
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@ManyToOne(optional = true)
	@JoinColumn(name = "task_type_concept_id")
	private Concept taskType;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "description", length = 1024)
	private String description;

	@Column(name = "status", nullable = false, length = 30)
	@Enumerated(EnumType.STRING)
	private TaskStatus status = TaskStatus.REQUESTED;

	@Column(name = "intent", nullable = false, length = 30)
	@Enumerated(EnumType.STRING)
	private TaskIntent intent = TaskIntent.ORDER;

	@Column(name = "priority", nullable = false, length = 30)
	@Enumerated(EnumType.STRING)
	private TaskPriority priority = TaskPriority.ROUTINE;

	@Column(name = "execution_start_time")
	private Date executionStartTime;

	@Column(name = "execution_end_time")
	private Date executionEndTime;

	@Column(name = "notes", length = 2048)
	private String notes;

	@ManyToOne(optional = true)
	@JoinColumn(name = "task_template_id")
	private TaskTemplate taskTemplate;

	@ManyToOne(optional = true)
	@JoinColumn(name = "ward_id")
	private Location ward;

	@Override
	public Integer getId() {
		return taskId;
	}

	@Override
	public void setId(Integer id) {
		this.taskId = id;
	}
}
