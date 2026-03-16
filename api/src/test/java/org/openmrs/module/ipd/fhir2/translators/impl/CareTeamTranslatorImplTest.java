/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 */
package org.openmrs.module.ipd.fhir2.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.ipd.api.model.CareTeam;
import org.openmrs.module.ipd.api.model.CareTeamParticipant;
import org.openmrs.module.ipd.fhir2.translators.CareTeamParticipantTranslator;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CareTeamTranslatorImplTest {

	private static final String UUID = "care-team-uuid-123";
	private static final String PATIENT_UUID = "patient-uuid";
	private static final String VISIT_UUID = "visit-uuid";

	@Mock
	private PatientReferenceTranslator patientReferenceTranslator;

	@Mock
	private EncounterReferenceTranslator<org.openmrs.Visit> encounterReferenceTranslator;

	@Mock
	private CareTeamParticipantTranslator participantTranslator;

	@InjectMocks
	private CareTeamTranslatorImpl translator;

	private CareTeam openmrsObject;

	@Before
	public void setup() {
		openmrsObject = new CareTeam();
		openmrsObject.setUuid(UUID);
	}

	@Test
	public void toFhirResource_shouldTranslateToFhirR4() {
		org.hl7.fhir.r4.model.CareTeam.CareTeamParticipantComponent comp =
				new org.hl7.fhir.r4.model.CareTeam.CareTeamParticipantComponent();
		comp.setMember(new Reference("Practitioner/p1"));
		when(participantTranslator.toFhirResource(any(CareTeamParticipant.class))).thenReturn(comp);

		org.hl7.fhir.r4.model.CareTeam fhirRes = translator.toFhirResource(openmrsObject);

		assertThat(fhirRes, notNullValue());
		assertThat(fhirRes.getId(), equalTo(UUID));
		assertThat(fhirRes.getStatus(), equalTo(org.hl7.fhir.r4.model.CareTeam.CareTeamStatus.ACTIVE));
	}

	@Test
	public void toFhirResource_shouldMapPatient() {
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		openmrsObject.setPatient(patient);

		Reference patientRef = new Reference("Patient/" + PATIENT_UUID);
		when(patientReferenceTranslator.toFhirResource(any(Patient.class))).thenReturn(patientRef);

		org.hl7.fhir.r4.model.CareTeam fhir = translator.toFhirResource(openmrsObject);

		assertThat(fhir.getSubject(), notNullValue());
		assertThat(fhir.getSubject().getReference(), equalTo("Patient/" + PATIENT_UUID));
	}

	@Test
	public void toFhirResource_shouldMapVisit() {
		Visit visit = new Visit();
		visit.setUuid(VISIT_UUID);
		openmrsObject.setVisit(visit);

		Reference encounterRef = new Reference("Encounter/" + VISIT_UUID);
		when(encounterReferenceTranslator.toFhirResource(any(Visit.class))).thenReturn(encounterRef);

		org.hl7.fhir.r4.model.CareTeam fhir = translator.toFhirResource(openmrsObject);

		assertThat(fhir.getEncounter(), notNullValue());
		assertThat(fhir.getEncounter().getReference(), equalTo("Encounter/" + VISIT_UUID));
	}

	@Test
	public void toFhirResource_shouldMapPeriod() {
		Date start = new Date();
		Date end = new Date(start.getTime() + 3600000);
		openmrsObject.setStartTime(start);
		openmrsObject.setEndTime(end);

		org.hl7.fhir.r4.model.CareTeam fhir = translator.toFhirResource(openmrsObject);

		assertThat(fhir.getPeriod(), notNullValue());
		assertThat(fhir.getPeriod().getStart(), equalTo(start));
		assertThat(fhir.getPeriod().getEnd(), equalTo(end));
	}

	@Test
	public void toFhirResource_shouldMapParticipants() {
		CareTeamParticipant participant = new CareTeamParticipant();
		participant.setVoided(false);
		Set<CareTeamParticipant> participants = new HashSet<>();
		participants.add(participant);
		openmrsObject.setParticipants(participants);

		org.hl7.fhir.r4.model.CareTeam.CareTeamParticipantComponent comp =
				new org.hl7.fhir.r4.model.CareTeam.CareTeamParticipantComponent();
		comp.setMember(new Reference("Practitioner/p1"));
		when(participantTranslator.toFhirResource(any(CareTeamParticipant.class))).thenReturn(comp);

		org.hl7.fhir.r4.model.CareTeam fhir = translator.toFhirResource(openmrsObject);

		assertThat(fhir.getParticipant(), notNullValue());
		assertThat(fhir.getParticipant().size(), equalTo(1));
	}

	@Test
	public void toFhirResource_shouldSkipVoidedParticipants() {
		CareTeamParticipant participant = new CareTeamParticipant();
		participant.setVoided(true);
		Set<CareTeamParticipant> participants = new HashSet<>();
		participants.add(participant);
		openmrsObject.setParticipants(participants);

		org.hl7.fhir.r4.model.CareTeam fhir = translator.toFhirResource(openmrsObject);

		assertThat(fhir.getParticipant(), notNullValue());
		assertThat(fhir.getParticipant().size(), equalTo(0));
	}

	@Test
	public void toOpenmrsType_shouldTranslateFromFhir() {
		org.hl7.fhir.r4.model.CareTeam fhir = new org.hl7.fhir.r4.model.CareTeam();
		fhir.setId(UUID);
		Patient patient = new Patient();
		patient.setUuid(PATIENT_UUID);
		when(patientReferenceTranslator.toOpenmrsType(any(Reference.class))).thenReturn(patient);

		fhir.setSubject(new Reference("Patient/" + PATIENT_UUID));

		CareTeam result = translator.toOpenmrsType(fhir);

		assertThat(result, notNullValue());
		assertThat(result.getUuid(), equalTo(UUID));
		assertThat(result.getPatient(), equalTo(patient));
	}

	@Test
	public void toOpenmrsType_shouldMapPeriod() {
		org.hl7.fhir.r4.model.CareTeam fhir = new org.hl7.fhir.r4.model.CareTeam();
		fhir.setId(UUID);
		org.hl7.fhir.r4.model.Period period = new org.hl7.fhir.r4.model.Period();
		Date start = new Date();
		Date end = new Date(start.getTime() + 3600000);
		period.setStart(start);
		period.setEnd(end);
		fhir.setPeriod(period);

		CareTeam result = translator.toOpenmrsType(fhir);

		assertThat(result.getStartTime(), equalTo(start));
		assertThat(result.getEndTime(), equalTo(end));
	}

	@Test(expected = NullPointerException.class)
	public void toFhirResource_shouldThrowWhenNull() {
		translator.toFhirResource(null);
	}

	@Test(expected = NullPointerException.class)
	public void toOpenmrsType_shouldThrowWhenNull() {
		translator.toOpenmrsType(null);
	}
}
