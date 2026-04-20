package org.openmrs.module.ipd.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Location;

import javax.persistence.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "ipd_task_template")
public class TaskTemplate extends BaseChangeableOpenmrsData {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Integer templateId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 1024)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_type_concept_id")
    private Concept taskType;

    @ManyToOne(optional = true)
    @JoinColumn(name = "ward_id")
    private Location ward;

    @Column(name = "priority", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Task.TaskPriority priority = Task.TaskPriority.ROUTINE;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @ManyToOne(optional = true)
    @JoinColumn(name = "default_assignee_role_concept_id")
    private Concept defaultAssigneeRole;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Override
    public Integer getId() {
        return templateId;
    }

    @Override
    public void setId(Integer id) {
        this.templateId = id;
    }
}
