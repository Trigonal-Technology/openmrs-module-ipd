/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ipd.fhir2.translators.impl;

import static org.apache.commons.lang3.Validate.notNull;

import javax.annotation.Nonnull;

import java.util.HashSet;
import java.util.Set;

import org.hl7.fhir.r4.model.Period;
import org.openmrs.module.fhir2.api.translators.EncounterReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.impl.FhirTranslatorUtils;
import org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator;
import org.openmrs.module.ipd.api.model.CareTeam;
import org.openmrs.module.ipd.api.model.CareTeamParticipant;
import org.openmrs.module.ipd.fhir2.translators.CareTeamParticipantTranslator;
import org.openmrs.module.ipd.fhir2.translators.CareTeamTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Setter;

@Component
@Setter(AccessLevel.PACKAGE)
public class CareTeamTranslatorImpl implements CareTeamTranslator {

	@Autowired
	private PatientReferenceTranslator patientReferenceTranslator;

	@Autowired
	private EncounterReferenceTranslator<org.openmrs.Visit> encounterReferenceTranslator;

	@Autowired
	private CareTeamParticipantTranslator participantTranslator;

	@Override
	public org.hl7.fhir.r4.model.CareTeam toFhirResource(@Nonnull CareTeam openmrsObject) {
		notNull(openmrsObject, "The CareTeam object should not be null");

		org.hl7.fhir.r4.model.CareTeam fhirObject = new org.hl7.fhir.r4.model.CareTeam();
		fhirObject.setId(openmrsObject.getUuid());
		fhirObject.setStatus(org.hl7.fhir.r4.model.CareTeam.CareTeamStatus.ACTIVE);

		if (openmrsObject.getPatient() != null) {
			fhirObject.setSubject(patientReferenceTranslator.toFhirResource(openmrsObject.getPatient()));
		}
		if (openmrsObject.getVisit() != null) {
			fhirObject.setEncounter(encounterReferenceTranslator.toFhirResource(openmrsObject.getVisit()));
		}
		if (openmrsObject.getStartTime() != null || openmrsObject.getEndTime() != null) {
			Period period = new Period();
			if (openmrsObject.getStartTime() != null) {
				period.setStart(openmrsObject.getStartTime());
			}
			if (openmrsObject.getEndTime() != null) {
				period.setEnd(openmrsObject.getEndTime());
			}
			fhirObject.setPeriod(period);
		}
		if (openmrsObject.getParticipants() != null) {
			for (CareTeamParticipant participant : openmrsObject.getParticipants()) {
				if (participant != null && !Boolean.TRUE.equals(participant.getVoided())) {
					fhirObject.addParticipant(participantTranslator.toFhirResource(participant));
				}
			}
		}
		fhirObject.getMeta().setLastUpdated(FhirTranslatorUtils.getLastUpdated(openmrsObject));
		return fhirObject;
	}

	@Override
	public CareTeam toOpenmrsType(@Nonnull org.hl7.fhir.r4.model.CareTeam fhirObject) {
		notNull(fhirObject, "The CareTeam object should not be null");
		return toOpenmrsType(new CareTeam(), fhirObject);
	}

	public CareTeam toOpenmrsType(@Nonnull CareTeam openmrsObject,
			@Nonnull org.hl7.fhir.r4.model.CareTeam fhirObject) {
		notNull(openmrsObject, "The existing Openmrs CareTeam object should not be null");
		notNull(fhirObject, "The FHIR CareTeam object should not be null");

		if (fhirObject.hasId()) {
			openmrsObject.setUuid(fhirObject.getIdElement().getIdPart());
		}
		if (fhirObject.hasSubject()) {
			openmrsObject.setPatient(patientReferenceTranslator.toOpenmrsType(fhirObject.getSubject()));
		}
		if (fhirObject.hasEncounter()) {
			openmrsObject.setVisit(encounterReferenceTranslator.toOpenmrsType(fhirObject.getEncounter()));
		}
		if (fhirObject.hasPeriod()) {
			Period period = fhirObject.getPeriod();
			if (period.hasStart()) {
				openmrsObject.setStartTime(period.getStart());
			}
			if (period.hasEnd()) {
				openmrsObject.setEndTime(period.getEnd());
			}
		}
		if (fhirObject.hasParticipant()) {
			Set<CareTeamParticipant> participants = new HashSet<>();
			for (org.hl7.fhir.r4.model.CareTeam.CareTeamParticipantComponent comp : fhirObject.getParticipant()) {
				CareTeamParticipant p = participantTranslator.toOpenmrsType(comp);
				if (p != null) {
					participants.add(p);
				}
			}
			openmrsObject.setParticipants(participants);
		}
		return openmrsObject;
	}
}
