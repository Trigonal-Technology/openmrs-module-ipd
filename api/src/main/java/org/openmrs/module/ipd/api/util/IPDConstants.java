package org.openmrs.module.ipd.api.util;

public class IPDConstants {

    public static final String IPD_VIEW_DRUG_CHART = "drugChart";

    /**
     * Global property: native SQL for {@code GET .../ipd/emergencyMedicationsToAcknowledge}.
     * Same key as Bahmni sqlSearch registry. Liquibase upserts via {@code *-mysql-liquibase.sql} / {@code *-postgresql-liquibase.sql};
     * manual copy-paste: {@code emergency-medication-to-acknowledge-global-property.sql}. If unset, DAO uses bundled
     * {@code emergencyMedicationToAcknowledge.sql}.
     * Optional token {@code {{LOCATION_FILTER}}} replaced at runtime when {@code locationUuid} is present.
     * Hibernate binds: {@code :providerUuid}, {@code :locale}, {@code :witnessName}, {@code :locationUuid} (when filter on).
     */
    public static final String GP_EMERGENCY_MEDICATION_TO_ACKNOWLEDGE_SQL = "emrapi.sqlSearch.emergencyMedicationToAcknowledge";

}
