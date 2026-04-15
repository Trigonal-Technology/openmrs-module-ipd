package org.openmrs.module.ipd.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Emergency / witness-linked medication administrations pending clinician acknowledgement
 * (backed by {@code emrapi.sqlSearch.emergencyMedicationToAcknowledge} SQL; IPD REST adds epoch millis + witness performer details).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyMedicationToAcknowledgeDTO {

	private String identifier;

	private String name;

	private String gender;

	private String patientUuid;

	private Long dateOfBirth;

	private String medicationAdministrationUuid;

	/** Epoch milliseconds (UTC), same convention as {@link #dateOfBirth}. */
	private Long administeredDateTime;

	private String administeredDrugName;

	private Double administeredDose;

	private String administeredDoseUnits;

	private String administeredRoute;

	private EmergencyMedicationPerformerDetailsDTO witnessPerformer;

	private String visitUuid;
}
