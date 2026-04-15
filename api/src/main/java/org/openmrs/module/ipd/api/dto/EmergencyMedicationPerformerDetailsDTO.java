package org.openmrs.module.ipd.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Witness (or other) performer row on a medication administration, with linked provider identity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyMedicationPerformerDetailsDTO {

	private String medicationAdministrationPerformerUuid;

	private String providerUuid;

	/** Preferred person name for the provider (given + family). */
	private String display;
}
