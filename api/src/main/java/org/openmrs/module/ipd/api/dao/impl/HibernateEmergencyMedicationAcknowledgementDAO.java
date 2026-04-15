package org.openmrs.module.ipd.api.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.dao.EmergencyMedicationAcknowledgementDAO;
import org.openmrs.module.ipd.api.model.MedicationAdministrationPerformer;
import org.openmrs.module.ipd.api.util.IPDConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Loads SQL from global property {@link IPDConstants#GP_EMERGENCY_MEDICATION_TO_ACKNOWLEDGE_SQL} (Bahmni-compatible key),
 * else bundled {@code org/openmrs/module/ipd/db/emergencyMedicationToAcknowledge.sql}.
 */
public class HibernateEmergencyMedicationAcknowledgementDAO implements EmergencyMedicationAcknowledgementDAO {

	public static final String LOCATION_FILTER_TOKEN = "{{LOCATION_FILTER}}";

	private static final Logger log = LoggerFactory.getLogger(HibernateEmergencyMedicationAcknowledgementDAO.class);

	private static final String BUNDLED_SQL = "org/openmrs/module/ipd/db/emergencyMedicationToAcknowledge.sql";

	private static final String LOCATION_FILTER = " AND ( "
			+ " v.location_id = (SELECT loc.location_id FROM location loc WHERE loc.uuid = :locationUuid AND loc.retired = 0 LIMIT 1) "
			+ " OR slot.location_id = (SELECT loc2.location_id FROM location loc2 WHERE loc2.uuid = :locationUuid AND loc2.retired = 0 LIMIT 1) "
			+ " ) ";

	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Object[]> findEmergencyMedicationsToAcknowledge(String providerUuid, String locationUuid, String locale,
			String witnessConceptName) {
		if (providerUuid == null || providerUuid.trim().isEmpty()) {
			log.debug("findEmergencyMedicationsToAcknowledge: providerUuid is blank");
			return Collections.emptyList();
		}
		String sqlTemplate = resolveSqlTemplate();
		boolean hasLocation = StringUtils.isNotBlank(locationUuid);
		String sql = sqlTemplate.replace(LOCATION_FILTER_TOKEN, hasLocation ? LOCATION_FILTER : "");
		if (sql.contains(LOCATION_FILTER_TOKEN)) {
			log.warn("SQL still contains {} after replace — check global_property {}", LOCATION_FILTER_TOKEN,
					IPDConstants.GP_EMERGENCY_MEDICATION_TO_ACKNOWLEDGE_SQL);
		}
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		query.setString("providerUuid", providerUuid);
		query.setString("locale", locale);
		query.setString("witnessName", witnessConceptName);
		if (hasLocation) {
			query.setString("locationUuid", locationUuid);
		}
		return query.list();
	}

	@Override
	public MedicationAdministrationPerformer getMedicationAdministrationPerformerByUuid(String uuid) {
		if (StringUtils.isBlank(uuid)) {
			return null;
		}
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(MedicationAdministrationPerformer.class);
		criteria.add(Restrictions.eq("uuid", uuid.trim()));
		criteria.add(Restrictions.eq("voided", false));
		return (MedicationAdministrationPerformer) criteria.uniqueResult();
	}

	private String resolveSqlTemplate() {
		// Module Spring context has no core "administrationService" bean — use OpenMRS Context (same pattern as other module DAOs).
		String fromGp = Context.getAdministrationService()
				.getGlobalProperty(IPDConstants.GP_EMERGENCY_MEDICATION_TO_ACKNOWLEDGE_SQL);
		if (StringUtils.isNotBlank(fromGp)) {
			return fromGp.trim();
		}
		return readBundledSql();
	}

	private String readBundledSql() {
		ClassLoader cl = HibernateEmergencyMedicationAcknowledgementDAO.class.getClassLoader();
		try (InputStream in = cl.getResourceAsStream(BUNDLED_SQL)) {
			if (in == null) {
				throw new IllegalStateException("Bundled resource missing: " + BUNDLED_SQL);
			}
			StringBuilder sb = new StringBuilder();
			try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
				String line;
				while ((line = r.readLine()) != null) {
					sb.append(line).append('\n');
				}
			}
			return sb.toString().trim();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read " + BUNDLED_SQL, e);
		}
	}
}
