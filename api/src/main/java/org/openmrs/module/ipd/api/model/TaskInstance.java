package org.openmrs.module.ipd.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.Location;
import org.openmrs.Patient;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "ipd_task_instance")
public class TaskInstance extends BaseChangeableOpenmrsData {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "instance_id")
    private Integer instanceId;

    @ManyToOne(optional = true)
    @JoinColumn(name = "template_id")
    private TaskTemplate template;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ward_id")
    private Location ward;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private TaskInstanceStatus status = TaskInstanceStatus.SCHEDULED;

    @Column(name = "priority", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Task.TaskPriority priority = Task.TaskPriority.ROUTINE;

    @Column(name = "started_time")
    private LocalDateTime startedTime;

    @Override
    public Integer getId() {
        return instanceId;
    }

    @Override
    public void setId(Integer id) {
        this.instanceId = id;
    }
}
