package org.openmrs.module.ipd.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseChangeableOpenmrsData;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public boolean matchesDate(LocalDate date) {
        if (startDate != null && date.isBefore(startDate.toLocalDate())) {
            return false;
        }

        if (recurrenceType == RecurrenceType.DAILY) {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate.toLocalDate(), date);
            return daysBetween >= 0 && daysBetween % recurrenceInterval == 0;
        }

        if (recurrenceType == RecurrenceType.WEEKLY) {
            if (daysOfWeek == null || daysOfWeek.isEmpty()) {
                return true; 
            }
            
            String currentDayName = date.getDayOfWeek().name();
            String currentDayNum = String.valueOf(date.getDayOfWeek().getValue());
            
            return Arrays.stream(daysOfWeek.split(","))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .anyMatch(d -> d.equals(currentDayName) || d.equals(currentDayNum));
        }

        return true;
    }

    public List<LocalTime> getActiveTimesAsList() {
        List<LocalTime> times = new ArrayList<>();
        if (activeTimes == null || activeTimes.isEmpty()) {
            return times;
        }
        for (String timeStr : activeTimes.split(",")) {
            timeStr = timeStr.trim();
            if (!timeStr.isEmpty()) {
                try {
                    // Handle formats HH:mm, HH:mm:ss or H:mm
                    if (timeStr.length() == 4 && timeStr.indexOf(':') == 1) {
                        timeStr = "0" + timeStr;
                    }
                    times.add(LocalTime.parse(timeStr));
                } catch (Exception e) {
                    // Log and continue
                }
            }
        }
        return times;
    }
}
