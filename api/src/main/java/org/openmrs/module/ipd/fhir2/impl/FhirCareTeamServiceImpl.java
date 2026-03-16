/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ipd.fhir2.impl;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.openmrs.module.fhir2.api.impl.BaseFhirService;
import org.openmrs.module.fhir2.api.search.SearchQuery;
import org.openmrs.module.fhir2.api.search.SearchQueryInclude;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.ipd.api.model.CareTeam;
import org.openmrs.module.ipd.fhir2.FhirCareTeamService;
import org.openmrs.module.ipd.fhir2.dao.FhirCareTeamDao;
import org.openmrs.module.ipd.fhir2.search.CareTeamSearchQueryInclude;
import org.openmrs.module.ipd.fhir2.search.param.CareTeamSearchParams;
import org.openmrs.module.ipd.fhir2.translators.CareTeamTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class FhirCareTeamServiceImpl
		extends BaseFhirService<org.hl7.fhir.r4.model.CareTeam, CareTeam>
		implements FhirCareTeamService {

	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE)
	@Autowired
	private FhirCareTeamDao dao;

	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE)
	@Autowired
	private CareTeamTranslator translator;

	@Override
	protected org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator<CareTeam, org.hl7.fhir.r4.model.CareTeam> getTranslator() {
		return translator;
	}

	@Override
	protected org.openmrs.module.fhir2.api.dao.FhirDao<CareTeam> getDao() {
		return dao;
	}

	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE)
	@Autowired
	@Qualifier("careTeamSearchQueryInclude")
	private SearchQueryInclude<org.hl7.fhir.r4.model.CareTeam> searchQueryInclude;

	@Getter(value = AccessLevel.PROTECTED)
	@Setter(value = AccessLevel.PACKAGE)
	@Autowired
	private SearchQuery<CareTeam, org.hl7.fhir.r4.model.CareTeam, FhirCareTeamDao, CareTeamTranslator, SearchQueryInclude<org.hl7.fhir.r4.model.CareTeam>> searchQuery;

	@Override
	public IBundleProvider searchForCareTeams(CareTeamSearchParams searchParams) {
		SearchParameterMap theParams = searchParams.toSearchParameterMap();
		return searchQuery.getQueryResults(theParams, dao, translator, searchQueryInclude);
	}
}
