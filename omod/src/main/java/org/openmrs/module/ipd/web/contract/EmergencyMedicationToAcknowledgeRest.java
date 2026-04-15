package org.openmrs.module.ipd.web.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openmrs.module.ipd.api.dto.EmergencyMedicationToAcknowledgeDTO;

/**
 * JSON for IPD {@code GET .../emergencyMedicationsToAcknowledge}. Administered time = epoch millis (like date_of_birth).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyMedicationToAcknowledgeRest {

	private String identifier;

	private String name;

	private String gender;

	@JsonProperty("patient_uuid")
	private String patientUuid;

	@JsonProperty("date_of_birth")
	private Long dateOfBirth;

	@JsonProperty("medication_administration_uuid")
	private String medicationAdministrationUuid;

	@JsonProperty("administered_date_time")
	private Long administeredDateTime;

	@JsonProperty("administered_drug_name")
	private String administeredDrugName;

	@JsonProperty("administered_dose")
	private Double administeredDose;

	@JsonProperty("administered_dose_units")
	private String administeredDoseUnits;

	@JsonProperty("administered_route")
	private String administeredRoute;

	@JsonProperty("performer")
	private EmergencyMedicationPerformerDetailsRest performer;

	@JsonProperty("visit_uuid")
	private String visitUuid;

	public static EmergencyMedicationToAcknowledgeRest from(EmergencyMedicationToAcknowledgeDTO d) {
		if (d == null) {
			return null;
		}
		return EmergencyMedicationToAcknowledgeRest.builder()
				.identifier(d.getIdentifier())
				.name(d.getName())
				.gender(d.getGender())
				.patientUuid(d.getPatientUuid())
				.dateOfBirth(d.getDateOfBirth())
				.medicationAdministrationUuid(d.getMedicationAdministrationUuid())
				.administeredDateTime(d.getAdministeredDateTime())
				.administeredDrugName(d.getAdministeredDrugName())
				.administeredDose(d.getAdministeredDose())
				.administeredDoseUnits(d.getAdministeredDoseUnits())
				.administeredRoute(d.getAdministeredRoute())
				.performer(EmergencyMedicationPerformerDetailsRest.from(d.getWitnessPerformer()))
				.visitUuid(d.getVisitUuid())
				.build();
	}
}
