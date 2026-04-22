package org.openmrs.module.ipd.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.Location;
import org.openmrs.Provider;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "ipd_task_acknowledgment")
public class TaskAcknowledgment extends BaseChangeableOpenmrsData {

	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "acknowledgment_id")
	private Integer acknowledgmentId;

	@OneToOne(optional = false)
	@JoinColumn(name = "task_id")
	private Task task;

	@ManyToOne(optional = false)
	@JoinColumn(name = "acknowledged_by")
	private Provider acknowledgedBy;

	@Column(name = "acknowledgment_time", nullable = false)
	private Date acknowledgmentTime;

	@Column(name = "acknowledgment_method", nullable = false, length = 30)
	@Enumerated(EnumType.STRING)
	private AcknowledgmentMethod acknowledgmentMethod;

	@Column(name = "device_id", length = 100)
	private String deviceId;

	@ManyToOne(optional = true)
	@JoinColumn(name = "location_id")
	private Location location;

	@Column(name = "notes", length = 2048)
	private String notes;

	@Column(name = "billable")
	private boolean billable = true;

	@Column(name = "billing_code", length = 50)
	private String billingCode;

	@Column(name = "billing_amount", precision = 10, scale = 2)
	private BigDecimal billingAmount;

	@Column(name = "billed_at")
	private Date billedAt;

	@Column(name = "billing_reference_id", length = 100)
	private String billingReferenceId;

	@Override
	public Integer getId() {
		return acknowledgmentId;
	}

	@Override
	public void setId(Integer id) {
		this.acknowledgmentId = id;
	}
}
