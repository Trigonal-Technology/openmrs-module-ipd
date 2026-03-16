/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ipd.fhir2.providers;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PROTECTED;

import javax.annotation.Nonnull;

import java.util.HashSet;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.ipd.fhir2.FhirCareTeamService;
import org.openmrs.module.ipd.fhir2.search.param.CareTeamSearchParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("careTeamFhirR4ResourceProvider")
@R4Provider
public class CareTeamFhirResourceProvider implements IResourceProvider {

	@Getter(PROTECTED)
	@Setter(value = PACKAGE, onMethod_ = @Autowired)
	private FhirCareTeamService fhirCareTeamService;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return CareTeam.class;
	}

	@Read
	@SuppressWarnings("unused")
	public CareTeam getCareTeamById(@IdParam @Nonnull IdType id) {
		CareTeam resource = fhirCareTeamService.get(id.getIdPart());
		if (resource == null) {
			throw new ResourceNotFoundException("Could not find CareTeam with Id " + id.getIdPart());
		}
		return resource;
	}

	@Search
	@SuppressWarnings("unused")
	public IBundleProvider searchForCareTeams(
			@OptionalParam(name = CareTeam.SP_PATIENT, chainWhitelist = { "", Patient.SP_IDENTIFIER,
					Patient.SP_GIVEN, Patient.SP_FAMILY, Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam patientReference,
			@OptionalParam(name = CareTeam.SP_SUBJECT, chainWhitelist = { "", Patient.SP_IDENTIFIER,
					Patient.SP_GIVEN, Patient.SP_FAMILY, Patient.SP_NAME }, targetTypes = Patient.class) ReferenceAndListParam subjectReference,
			@OptionalParam(name = CareTeam.SP_ENCOUNTER, chainWhitelist = { "" }, targetTypes = Encounter.class) ReferenceAndListParam encounterReference,
			@OptionalParam(name = CareTeam.SP_RES_ID) TokenAndListParam id,
			@OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated,
			@IncludeParam(allow = { "CareTeam:" + CareTeam.SP_SUBJECT, "CareTeam:" + CareTeam.SP_ENCOUNTER }) HashSet<Include> includes) {

		if (patientReference == null) {
			patientReference = subjectReference;
		}
		if (CollectionUtils.isEmpty(includes)) {
			includes = null;
		}

		CareTeamSearchParams searchParams = new CareTeamSearchParams();
		searchParams.setPatientReference(patientReference);
		searchParams.setEncounterReference(encounterReference);
		searchParams.setId(id);
		searchParams.setLastUpdated(lastUpdated);
		searchParams.setIncludes(includes);
		searchParams.setRevIncludes(null);

		return fhirCareTeamService.searchForCareTeams(searchParams);
	}
}
