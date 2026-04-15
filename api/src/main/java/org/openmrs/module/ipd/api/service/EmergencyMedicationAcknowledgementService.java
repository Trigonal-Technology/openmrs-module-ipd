package org.openmrs.module.ipd.api.service;

import org.openmrs.module.ipd.api.dto.EmergencyMedicationToAcknowledgeDTO;

import java.util.List;

public interface EmergencyMedicationAcknowledgementService {

	/**
	 * @param providerUuid  required OpenMRS provider UUID (witness on the medication administration)
	 * @param locationUuid  optional location UUID (visit or IPD slot location)
	 */
	List<EmergencyMedicationToAcknowledgeDTO> getEmergencyMedicationsToAcknowledge(String providerUuid,
			String locationUuid);
}
