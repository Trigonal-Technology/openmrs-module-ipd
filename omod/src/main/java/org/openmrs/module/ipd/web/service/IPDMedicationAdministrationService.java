package org.openmrs.module.ipd.web.service;

import org.openmrs.module.ipd.api.model.MedicationAdministration;
import org.openmrs.module.ipd.web.contract.MedicationAdministrationRequest;

public interface IPDMedicationAdministrationService {

    MedicationAdministration saveScheduledMedicationAdministration(
            MedicationAdministrationRequest medicationAdministrationRequest);

    MedicationAdministration updateAdhocMedicationAdministration(String uuid,
            MedicationAdministrationRequest medicationAdministrationRequest);

    MedicationAdministration saveAdhocMedicationAdministration(
            MedicationAdministrationRequest medicationAdministrationRequest);

}
