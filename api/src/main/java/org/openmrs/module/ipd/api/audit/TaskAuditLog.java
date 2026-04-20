package org.openmrs.module.ipd.api.audit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity to store audit logs for task-related actions.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "ipd_task_audit_log")
public class TaskAuditLog extends BaseOpenmrsData {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Integer auditId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_uuid", length = 38)
    private String entityUuid;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime;

    @Column(name = "details", length = 4000)
    private String details;

    @Column(name = "patient_uuid", length = 38)
    private String patientUuid;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Override
    public Integer getId() {
        return auditId;
    }

    @Override
    public void setId(Integer id) {
        this.auditId = id;
    }
}
