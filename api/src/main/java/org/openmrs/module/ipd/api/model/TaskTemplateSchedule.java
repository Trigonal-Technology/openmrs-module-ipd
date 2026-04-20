package org.openmrs.module.ipd.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseChangeableOpenmrsData;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "ipd_task_template_schedule")
public class TaskTemplateSchedule extends BaseChangeableOpenmrsData {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Integer scheduleId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "template_id")
    private TaskTemplate template;

    @Column(name = "recurrence_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private RecurrenceType recurrenceType;

    @Column(name = "recurrence_interval", nullable = false)
    private Integer recurrenceInterval = 1;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "active_times", length = 500)
    private String activeTimes;

    @Column(name = "days_of_week", length = 50)
    private String daysOfWeek;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Override
    public Integer getId() {
        return scheduleId;
    }

    @Override
    public void setId(Integer id) {
        this.scheduleId = id;
    }
}
