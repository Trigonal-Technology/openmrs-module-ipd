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

import javax.annotation.Nonnull;

import org.hl7.fhir.r4.model.Period;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.impl.ReferenceHandlingTranslator;
import org.openmrs.module.ipd.api.model.CareTeamParticipant;
import org.openmrs.module.ipd.fhir2.translators.CareTeamParticipantTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Setter;

@Component
@Setter(AccessLevel.PACKAGE)
public class CareTeamParticipantTranslatorImpl implements CareTeamParticipantTranslator {

	@Autowired
	@Qualifier("practitionerReferenceTranslatorProviderImpl")
	private PractitionerReferenceTranslator<Provider> practitionerReferenceTranslator;

	@Override
	public org.hl7.fhir.r4.model.CareTeam.CareTeamParticipantComponent toFhirResource(
			@Nonnull CareTeamParticipant openmrsParticipant) {
		if (openmrsParticipant == null) {
			return null;
		}

		org.hl7.fhir.r4.model.CareTeam.CareTeamParticipantComponent comp =
				new org.hl7.fhir.r4.model.CareTeam.CareTeamParticipantComponent();
		if (openmrsParticipant.getProvider() != null) {
			comp.setMember(ReferenceHandlingTranslator.createPractitionerReference(openmrsParticipant.getProvider()));
		}
		if (openmrsParticipant.getStartTime() != null || openmrsParticipant.getEndTime() != null) {
			Period period = new Period();
			if (openmrsParticipant.getStartTime() != null) {
				period.setStart(openmrsParticipant.getStartTime());
			}
			if (openmrsParticipant.getEndTime() != null) {
				period.setEnd(openmrsParticipant.getEndTime());
			}
			comp.setPeriod(period);
		}
		return comp;
	}

	@Override
	public CareTeamParticipant toOpenmrsType(
			@Nonnull org.hl7.fhir.r4.model.CareTeam.CareTeamParticipantComponent fhirComponent) {
		if (fhirComponent == null || !fhirComponent.hasMember()) {
			return null;
		}

		CareTeamParticipant participant = new CareTeamParticipant();
		participant.setProvider(practitionerReferenceTranslator.toOpenmrsType(fhirComponent.getMember()));
		if (fhirComponent.hasPeriod()) {
			Period period = fhirComponent.getPeriod();
			if (period.hasStart()) {
				participant.setStartTime(period.getStart());
			}
			if (period.hasEnd()) {
				participant.setEndTime(period.getEnd());
			}
		}
		return participant;
	}
}
