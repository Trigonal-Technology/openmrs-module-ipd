package org.openmrs.module.ipd.api.dao;

import org.openmrs.module.ipd.api.model.MedicationAdministrationPerformer;

import java.util.List;

/**
 * Read-side DAO for emergency / witness-linked medication administrations pending clinician acknowledgement.
 */
public interface EmergencyMedicationAcknowledgementDAO {

	/**
	 * @param providerUuid   required; OpenMRS {@link org.openmrs.Provider} UUID (the witness on the administration)
	 * @param locationUuid   optional; when set, restricts to visits or IPD slots at this {@link org.openmrs.Location}
	 * @param locale         concept_name locale (e.g. {@code en})
	 * @param witnessConceptName performer function concept name, typically {@code Witness}
	 */
	List<Object[]> findEmergencyMedicationsToAcknowledge(String providerUuid, String locationUuid, String locale,
			String witnessConceptName);

	/** @return non-voided row, or null */
	MedicationAdministrationPerformer getMedicationAdministrationPerformerByUuid(String uuid);
}
