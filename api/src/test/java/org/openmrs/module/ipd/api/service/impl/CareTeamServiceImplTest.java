/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 */
package org.openmrs.module.ipd.api.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Visit;
import org.openmrs.module.ipd.api.dao.CareTeamDAO;
import org.openmrs.module.ipd.api.model.CareTeam;

@RunWith(MockitoJUnitRunner.class)
public class CareTeamServiceImplTest {

	@Mock
	private CareTeamDAO careTeamDAO;

	private CareTeamServiceImpl service;

	@Before
	public void setup() {
		service = new CareTeamServiceImpl();
		service.setCareTeamDAO(careTeamDAO);
	}

	@Test
	public void saveCareTeam_shouldDelegateToDao() {
		CareTeam careTeam = new CareTeam();
		CareTeam saved = new CareTeam();
		saved.setUuid("saved-uuid");
		when(careTeamDAO.saveCareTeam(careTeam)).thenReturn(saved);

		CareTeam result = service.saveCareTeam(careTeam);

		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo("saved-uuid"));
		verify(careTeamDAO).saveCareTeam(careTeam);
	}

	@Test
	public void getCareTeamByVisit_shouldDelegateToDao() {
		Visit visit = new Visit();
		CareTeam careTeam = new CareTeam();
		careTeam.setUuid("ct-uuid");
		when(careTeamDAO.getCareTeamByVisit(visit)).thenReturn(careTeam);

		CareTeam result = service.getCareTeamByVisit(visit);

		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo("ct-uuid"));
		verify(careTeamDAO).getCareTeamByVisit(visit);
	}
}
