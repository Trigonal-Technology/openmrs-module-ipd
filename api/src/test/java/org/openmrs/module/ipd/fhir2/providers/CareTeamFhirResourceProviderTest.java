/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 */
package org.openmrs.module.ipd.fhir2.providers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceAndListParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.IdType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.module.ipd.fhir2.FhirCareTeamService;
import org.openmrs.module.ipd.fhir2.search.param.CareTeamSearchParams;

@RunWith(MockitoJUnitRunner.class)
public class CareTeamFhirResourceProviderTest {

	private static final String UUID = "care-team-uuid-123";

	@Mock
	private FhirCareTeamService fhirCareTeamService;

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	private CareTeamFhirResourceProvider provider;

	@Before
	public void setup() {
		provider = new CareTeamFhirResourceProvider();
		provider.setFhirCareTeamService(fhirCareTeamService);
	}

	@Test
	public void getResourceType_shouldReturnCareTeam() {
		Class<? extends IBaseResource> type = provider.getResourceType();

		assertThat(type, equalTo(CareTeam.class));
	}

	@Test
	public void getCareTeamById_shouldReturnResource() {
		CareTeam resource = new CareTeam();
		resource.setId(UUID);
		when(fhirCareTeamService.get(UUID)).thenReturn(resource);

		CareTeam result = provider.getCareTeamById(new IdType(UUID));

		assertThat(result, notNullValue());
		assertThat(result.getId(), equalTo(UUID));
		verify(fhirCareTeamService).get(UUID);
	}

	@Test
	public void getCareTeamById_shouldThrowWhenNotFound() {
		when(fhirCareTeamService.get("bad-uuid")).thenReturn(null);

		exceptionRule.expect(ResourceNotFoundException.class);
		exceptionRule.expectMessage("Could not find CareTeam with Id bad-uuid");

		provider.getCareTeamById(new IdType("bad-uuid"));
	}

	@Test
	public void searchForCareTeams_shouldDelegateToService() {
		IBundleProvider bundleProvider = org.mockito.Mockito.mock(IBundleProvider.class);
		when(fhirCareTeamService.searchForCareTeams(any(CareTeamSearchParams.class)))
				.thenReturn(bundleProvider);

		IBundleProvider result = provider.searchForCareTeams(
				null, null, new ReferenceAndListParam(), null, null, null);

		assertThat(result, equalTo(bundleProvider));
		verify(fhirCareTeamService).searchForCareTeams(any(CareTeamSearchParams.class));
	}

	@Test
	public void searchForCareTeams_shouldUseSubjectWhenPatientIsNull() {
		ReferenceAndListParam subjectRef = new ReferenceAndListParam();
		IBundleProvider bundleProvider = org.mockito.Mockito.mock(IBundleProvider.class);
		when(fhirCareTeamService.searchForCareTeams(any(CareTeamSearchParams.class)))
				.thenReturn(bundleProvider);

		provider.searchForCareTeams(null, subjectRef, null, null, null, null);

		verify(fhirCareTeamService).searchForCareTeams(any(CareTeamSearchParams.class));
	}
}
