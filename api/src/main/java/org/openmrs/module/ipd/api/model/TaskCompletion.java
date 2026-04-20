package org.openmrs.module.ipd.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.Provider;
import org.openmrs.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "ipd_task_completion")
public class TaskCompletion extends BaseChangeableOpenmrsData {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "completion_id")
    private Integer completionId;

    @OneToOne(optional = false)
    @JoinColumn(name = "instance_id")
    private TaskInstance instance;

    @ManyToOne(optional = false)
    @JoinColumn(name = "completed_by")
    private User completedBy;

    @ManyToOne(optional = true)
    @JoinColumn(name = "completed_on_behalf_of")
    private Provider completedOnBehalfOf;

    @Column(name = "completion_time", nullable = false)
    private LocalDateTime completionTime;

    @Column(name = "completion_method", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private CompletionMethod completionMethod;

    @Column(name = "notes", length = 2048)
    private String notes;

    @Column(name = "evidence_document_id")
    private Integer evidenceDocumentId;

    @Override
    public Integer getId() {
        return completionId;
    }

    @Override
    public void setId(Integer id) {
        this.completionId = id;
    }
}
