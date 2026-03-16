/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ipd.fhir2.dao.impl;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Join;

import java.util.Optional;

import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import org.openmrs.annotation.Authorized;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.dao.impl.BaseFhirDao;
import org.openmrs.module.fhir2.api.dao.internals.OpenmrsFhirCriteriaContext;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.ipd.api.model.CareTeam;
import org.openmrs.module.ipd.fhir2.dao.FhirCareTeamDao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class FhirCareTeamDaoImpl extends BaseFhirDao<CareTeam> implements FhirCareTeamDao {

	@Override
	@Transactional(readOnly = true)
	@Authorized(PrivilegeConstants.GET_VISITS)
	public CareTeam get(@Nonnull String uuid) {
		return super.get(uuid);
	}

	@Override
	@Authorized("Manage Care Teams")
	public CareTeam createOrUpdate(@Nonnull CareTeam newEntry) {
		return super.createOrUpdate(newEntry);
	}

	@Override
	@Authorized(PrivilegeConstants.DELETE_VISITS)
	public CareTeam delete(@Nonnull String uuid) {
		return super.delete(uuid);
	}

	@Override
	protected <U> void setupSearchParams(@Nonnull OpenmrsFhirCriteriaContext<CareTeam, U> criteriaContext,
			@Nonnull SearchParameterMap theParams) {
		theParams.getParameters().forEach(entry -> {
			switch (entry.getKey()) {
				case FhirConstants.PATIENT_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(patientReference -> getSearchQueryHelper().handlePatientReference(
							criteriaContext, (ReferenceAndListParam) patientReference.getParam(), "patient"));
					break;
				case FhirConstants.ENCOUNTER_REFERENCE_SEARCH_HANDLER:
					entry.getValue().forEach(e -> handleEncounterReference(criteriaContext,
							(ReferenceAndListParam) e.getParam()).ifPresent(criteriaContext::addPredicate));
					break;
				case FhirConstants.COMMON_SEARCH_HANDLER:
					handleCommonSearchParameters(criteriaContext, entry.getValue()).ifPresent(criteriaContext::addPredicate);
					break;
				default:
					break;
			}
		});
	}

	private <U> Optional<javax.persistence.criteria.Predicate> handleEncounterReference(
			OpenmrsFhirCriteriaContext<CareTeam, U> criteriaContext, ReferenceAndListParam encounterReference) {
		if (encounterReference == null) {
			return Optional.empty();
		}
		Join<?, ?> visitJoin = criteriaContext.addJoin("visit", "v");
		Join<?, ?> encountersJoin = criteriaContext.addJoin(visitJoin, "encounters", "e");
		return handleAndListParam(criteriaContext.getCriteriaBuilder(), encounterReference, token -> {
			if (token.getValue() != null) {
				String encounterUuid = token.getIdPart();
				if (encounterUuid != null) {
					return Optional.of(criteriaContext.getCriteriaBuilder()
							.equal(encountersJoin.get("uuid"), encounterUuid));
				}
			}
			return Optional.empty();
		});
	}
}
