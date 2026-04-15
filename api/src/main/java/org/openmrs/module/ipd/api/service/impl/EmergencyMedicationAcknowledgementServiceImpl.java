package org.openmrs.module.ipd.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.ipd.api.dao.EmergencyMedicationAcknowledgementDAO;
import org.openmrs.module.ipd.api.dto.EmergencyMedicationPerformerDetailsDTO;
import org.openmrs.module.ipd.api.dto.EmergencyMedicationToAcknowledgeDTO;
import org.openmrs.module.ipd.api.model.MedicationAdministrationPerformer;
import org.openmrs.module.ipd.api.service.EmergencyMedicationAcknowledgementService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Maps native SQL rows to {@link EmergencyMedicationToAcknowledgeDTO} (15-column SQL preferred; 13-column legacy GP still supported).
 */
public class EmergencyMedicationAcknowledgementServiceImpl implements EmergencyMedicationAcknowledgementService {

	private static final String DEFAULT_WITNESS_NAME = "Witness";

	private EmergencyMedicationAcknowledgementDAO emergencyMedicationAcknowledgementDAO;

	public void setEmergencyMedicationAcknowledgementDAO(EmergencyMedicationAcknowledgementDAO dao) {
		this.emergencyMedicationAcknowledgementDAO = dao;
	}

	@Override
	public List<EmergencyMedicationToAcknowledgeDTO> getEmergencyMedicationsToAcknowledge(String providerUuid,
			String locationUuid) {
		String locale = resolveConceptLocale();
		List<Object[]> rows = emergencyMedicationAcknowledgementDAO.findEmergencyMedicationsToAcknowledge(providerUuid,
				locationUuid, locale, DEFAULT_WITNESS_NAME);
		List<EmergencyMedicationToAcknowledgeDTO> out = new ArrayList<>();
		for (Object[] r : rows) {
			out.add(enrichWitnessPerformer(mapRow(r)));
		}
		return out;
	}

	/**
	 * Fills {@code provider_uuid} / {@code display} when native SQL omits them (legacy 13-column GP, or NULL CONCAT).
	 */
	private EmergencyMedicationToAcknowledgeDTO enrichWitnessPerformer(EmergencyMedicationToAcknowledgeDTO dto) {
		EmergencyMedicationPerformerDetailsDTO w = dto.getWitnessPerformer();
		if (w == null || StringUtils.isBlank(w.getMedicationAdministrationPerformerUuid())) {
			return dto;
		}
		boolean needProvider = StringUtils.isBlank(w.getProviderUuid());
		boolean needDisplay = StringUtils.isBlank(w.getDisplay());
		if (!needProvider && !needDisplay) {
			return dto;
		}
		MedicationAdministrationPerformer perf = emergencyMedicationAcknowledgementDAO
				.getMedicationAdministrationPerformerByUuid(w.getMedicationAdministrationPerformerUuid());
		if (perf == null || perf.getActor() == null) {
			return dto;
		}
		Provider actor = perf.getActor();
		String providerUuid = needProvider ? actor.getUuid() : w.getProviderUuid();
		String display = needDisplay ? formatProviderDisplay(actor) : w.getDisplay();
		dto.setWitnessPerformer(EmergencyMedicationPerformerDetailsDTO.builder()
				.medicationAdministrationPerformerUuid(w.getMedicationAdministrationPerformerUuid())
				.providerUuid(providerUuid)
				.display(trimToNull(display))
				.build());
		return dto;
	}

	private static String formatProviderDisplay(Provider actor) {
		if (actor.getPerson() != null) {
			PersonName pn = actor.getPerson().getPersonName();
			if (pn != null && StringUtils.isNotBlank(pn.getFullName())) {
				return pn.getFullName().trim();
			}
		}
		if (StringUtils.isNotBlank(actor.getIdentifier())) {
			return actor.getIdentifier().trim();
		}
		return null;
	}

	private static String resolveConceptLocale() {
		Locale loc = Context.getLocale();
		if (loc == null) {
			return "en";
		}
		String language = loc.getLanguage();
		return language != null && !language.isEmpty() ? language : "en";
	}

	private static EmergencyMedicationToAcknowledgeDTO mapRow(Object[] r) {
		// V2 SELECT (15 cols): … route, performer_row_uuid, witness_provider_uuid, witness_provider_display, visit_uuid
		// Legacy GP SQL (13 cols): … route, performer_row_uuid, visit_uuid
		int n = r == null ? 0 : r.length;
		if (n >= 15) {
			return mapRowV2(r);
		}
		return mapRowLegacy(r);
	}

	private static EmergencyMedicationToAcknowledgeDTO mapRowV2(Object[] r) {
		return EmergencyMedicationToAcknowledgeDTO.builder()
				.identifier(stringAt(r, 0))
				.name(stringAt(r, 1))
				.gender(stringAt(r, 2))
				.patientUuid(stringAt(r, 3))
				.dateOfBirth(epochMillisAt(r, 4))
				.medicationAdministrationUuid(stringAt(r, 5))
				.administeredDateTime(epochMillisAt(r, 6))
				.administeredDrugName(stringAt(r, 7))
				.administeredDose(doubleAt(r, 8))
				.administeredDoseUnits(stringAt(r, 9))
				.administeredRoute(stringAt(r, 10))
				.witnessPerformer(witnessPerformerAt(r, 11, 12, 13))
				.visitUuid(stringAt(r, 14))
				.build();
	}

	private static EmergencyMedicationToAcknowledgeDTO mapRowLegacy(Object[] r) {
		return EmergencyMedicationToAcknowledgeDTO.builder()
				.identifier(stringAt(r, 0))
				.name(stringAt(r, 1))
				.gender(stringAt(r, 2))
				.patientUuid(stringAt(r, 3))
				.dateOfBirth(epochMillisAt(r, 4))
				.medicationAdministrationUuid(stringAt(r, 5))
				.administeredDateTime(epochMillisAt(r, 6))
				.administeredDrugName(stringAt(r, 7))
				.administeredDose(doubleAt(r, 8))
				.administeredDoseUnits(stringAt(r, 9))
				.administeredRoute(stringAt(r, 10))
				.witnessPerformer(witnessPerformerUuidOnly(r, 11))
				.visitUuid(stringAt(r, 12))
				.build();
	}

	private static String stringAt(Object[] r, int i) {
		if (r == null || i >= r.length || r[i] == null) {
			return null;
		}
		return r[i].toString();
	}

	private static Double doubleAt(Object[] r, int i) {
		if (r == null || i >= r.length || r[i] == null) {
			return null;
		}
		if (r[i] instanceof Number) {
			return ((Number) r[i]).doubleValue();
		}
		try {
			return Double.parseDouble(r[i].toString());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static Long epochMillisAt(Object[] r, int i) {
		if (r == null || i >= r.length || r[i] == null) {
			return null;
		}
		if (r[i] instanceof Date) {
			return ((Date) r[i]).getTime();
		}
		if (r[i] instanceof Timestamp) {
			return ((Timestamp) r[i]).getTime();
		}
		return null;
	}

	private static EmergencyMedicationPerformerDetailsDTO witnessPerformerUuidOnly(Object[] r, int uuidIdx) {
		String rowUuid = stringAt(r, uuidIdx);
		if (rowUuid == null) {
			return null;
		}
		return EmergencyMedicationPerformerDetailsDTO.builder()
				.medicationAdministrationPerformerUuid(rowUuid)
				.build();
	}

	private static EmergencyMedicationPerformerDetailsDTO witnessPerformerAt(Object[] r, int uuidIdx, int providerIdx,
			int displayIdx) {
		String rowUuid = stringAt(r, uuidIdx);
		String providerUuid = stringAt(r, providerIdx);
		String display = stringAt(r, displayIdx);
		if (rowUuid == null && providerUuid == null && display == null) {
			return null;
		}
		return EmergencyMedicationPerformerDetailsDTO.builder()
				.medicationAdministrationPerformerUuid(rowUuid)
				.providerUuid(providerUuid)
				.display(trimToNull(display))
				.build();
	}

	private static String trimToNull(String s) {
		if (s == null) {
			return null;
		}
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}
}
