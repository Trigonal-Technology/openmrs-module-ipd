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
@Table(name = "ipd_patient_task_template")
public class PatientTaskTemplate extends BaseChangeableOpenmrsData {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_template_id")
    private Integer patientTemplateId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(optional = false)
    @JoinColumn(name = "template_id")
    private TaskTemplate template;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ward_id")
    private Location ward;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Override
    public Integer getId() {
        return patientTemplateId;
    }

    @Override
    public void setId(Integer id) {
        this.patientTemplateId = id;
    }
}
