/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 */
package org.openmrs.module.ipd.fhir2.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.ipd.api.model.CareTeamParticipant;

@RunWith(MockitoJUnitRunner.class)
public class CareTeamParticipantTranslatorImplTest {

	@Mock
	private PractitionerReferenceTranslator<Provider> practitionerReferenceTranslator;

	@InjectMocks
	private CareTeamParticipantTranslatorImpl translator;

	@Test
	public void toFhirResource_shouldReturnNullWhenNull() {
		assertThat(translator.toFhirResource(null), nullValue());
	}

	@Test
	public void toFhirResource_shouldTranslateWithProvider() {
		CareTeamParticipant participant = new CareTeamParticipant();
		Provider provider = new Provider();
		provider.setUuid("provider-uuid");
		participant.setProvider(provider);

		CareTeam.CareTeamParticipantComponent comp = translator.toFhirResource(participant);

		assertThat(comp, org.hamcrest.Matchers.notNullValue());
		assertThat(comp.getMember(), org.hamcrest.Matchers.notNullValue());
		assertThat(comp.getMember().getReference(), equalTo("Practitioner/provider-uuid"));
	}

	@Test
	public void toFhirResource_shouldMapPeriod() {
		CareTeamParticipant participant = new CareTeamParticipant();
		Provider provider = new Provider();
		provider.setUuid("p1");
		participant.setProvider(provider);
		Date start = new Date();
		Date end = new Date(start.getTime() + 3600000);
		participant.setStartTime(start);
		participant.setEndTime(end);

		CareTeam.CareTeamParticipantComponent comp = translator.toFhirResource(participant);

		assertThat(comp.getPeriod(), org.hamcrest.Matchers.notNullValue());
		assertThat(comp.getPeriod().getStart(), equalTo(start));
		assertThat(comp.getPeriod().getEnd(), equalTo(end));
	}

	@Test
	public void toOpenmrsType_shouldReturnNullWhenNull() {
		assertThat(translator.toOpenmrsType(null), nullValue());
	}

	@Test
	public void toOpenmrsType_shouldReturnNullWhenNoMember() {
		CareTeam.CareTeamParticipantComponent comp = new CareTeam.CareTeamParticipantComponent();

		assertThat(translator.toOpenmrsType(comp), nullValue());
	}

	@Test
	public void toOpenmrsType_shouldTranslateFromFhir() {
		CareTeam.CareTeamParticipantComponent comp = new CareTeam.CareTeamParticipantComponent();
		comp.setMember(new Reference("Practitioner/provider-uuid"));
		Provider provider = new Provider();
		provider.setUuid("provider-uuid");
		when(practitionerReferenceTranslator.toOpenmrsType(any(Reference.class))).thenReturn(provider);

		CareTeamParticipant result = translator.toOpenmrsType(comp);

		assertThat(result, org.hamcrest.Matchers.notNullValue());
		assertThat(result.getProvider(), equalTo(provider));
	}

	@Test
	public void toOpenmrsType_shouldMapPeriod() {
		CareTeam.CareTeamParticipantComponent comp = new CareTeam.CareTeamParticipantComponent();
		comp.setMember(new Reference("Practitioner/p1"));
		org.hl7.fhir.r4.model.Period period = new org.hl7.fhir.r4.model.Period();
		Date start = new Date();
		Date end = new Date(start.getTime() + 3600000);
		period.setStart(start);
		period.setEnd(end);
		comp.setPeriod(period);

		Provider provider = new Provider();
		when(practitionerReferenceTranslator.toOpenmrsType(any(Reference.class))).thenReturn(provider);

		CareTeamParticipant result = translator.toOpenmrsType(comp);

		assertThat(result.getStartTime(), equalTo(start));
		assertThat(result.getEndTime(), equalTo(end));
	}
}
