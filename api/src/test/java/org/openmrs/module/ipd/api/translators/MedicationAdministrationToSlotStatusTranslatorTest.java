/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 */
package org.openmrs.module.ipd.api.translators;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;
import org.openmrs.module.ipd.api.model.MedicationAdministration;
import org.openmrs.module.ipd.api.model.Slot;

public class MedicationAdministrationToSlotStatusTranslatorTest {

	private final MedicationAdministrationToSlotStatusTranslator translator =
			new MedicationAdministrationToSlotStatusTranslator();

	@Test
	public void toSlotStatus_shouldReturnNullWhenNull() {
		assertThat(translator.toSlotStatus(null), nullValue());
	}

	@Test
	public void toSlotStatus_shouldMapCompleted() {
		Slot.SlotStatus result = translator.toSlotStatus(
				MedicationAdministration.MedicationAdministrationStatus.COMPLETED);

		assertThat(result, equalTo(Slot.SlotStatus.COMPLETED));
	}

	@Test
	public void toSlotStatus_shouldMapNotDone() {
		Slot.SlotStatus result = translator.toSlotStatus(
				MedicationAdministration.MedicationAdministrationStatus.NOTDONE);

		assertThat(result, equalTo(Slot.SlotStatus.NOT_DONE));
	}

	@Test
	public void toSlotStatus_shouldMapStopped() {
		Slot.SlotStatus result = translator.toSlotStatus(
				MedicationAdministration.MedicationAdministrationStatus.STOPPED);

		assertThat(result, equalTo(Slot.SlotStatus.STOPPED));
	}

	@Test
	public void toSlotStatus_shouldReturnNullForUnknownStatus() {
		Slot.SlotStatus result = translator.toSlotStatus(
				MedicationAdministration.MedicationAdministrationStatus.INPROGRESS);

		assertThat(result, nullValue());
	}
}
