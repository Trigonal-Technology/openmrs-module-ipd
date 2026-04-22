package org.openmrs.module.ipd.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseOpenmrsData;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "ipd_task_generation_log")
public class TaskGenerationLog extends BaseOpenmrsData {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_template_id")
    private PatientTaskTemplate patientTemplate;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Column(name = "generated", nullable = false)
    private boolean generated = false;

    @Column(name = "task_uuid", length = 38)
    private String taskUuid;

    @Override
    public Integer getId() {
        return logId;
    }

    @Override
    public void setId(Integer id) {
        this.logId = id;
    }
}
