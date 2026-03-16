/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 */
package org.openmrs.module.ipd.fhir2.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.CareTeam;
import org.junit.Test;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;

public class CareTeamSearchQueryIncludeTest {

	@Test
	public void getIncludedResources_shouldReturnEmptySet() {
		CareTeamSearchQueryInclude include = new CareTeamSearchQueryInclude();
		List<CareTeam> resourceList = Collections.singletonList(new CareTeam());
		SearchParameterMap params = new SearchParameterMap();

		assertThat(include.getIncludedResources(resourceList, params), notNullValue());
		assertThat(include.getIncludedResources(resourceList, params), empty());
	}

	@Test
	public void getIncludedResources_shouldHandleEmptyList() {
		CareTeamSearchQueryInclude include = new CareTeamSearchQueryInclude();

		assertThat(include.getIncludedResources(Collections.emptyList(), new SearchParameterMap()), empty());
	}
}
