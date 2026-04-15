package org.openmrs.module.ipd.web.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openmrs.module.ipd.api.dto.EmergencyMedicationPerformerDetailsDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyMedicationPerformerDetailsRest {

	@JsonProperty("medication_administration_performer_uuid")
	private String medicationAdministrationPerformerUuid;

	@JsonProperty("provider_uuid")
	private String providerUuid;

	@JsonProperty("display")
	private String display;

	public static EmergencyMedicationPerformerDetailsRest from(EmergencyMedicationPerformerDetailsDTO d) {
		if (d == null) {
			return null;
		}
		return EmergencyMedicationPerformerDetailsRest.builder()
				.medicationAdministrationPerformerUuid(d.getMedicationAdministrationPerformerUuid())
				.providerUuid(d.getProviderUuid())
				.display(d.getDisplay())
				.build();
	}
}
