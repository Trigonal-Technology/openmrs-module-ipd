/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 */
package org.openmrs.module.ipd.api.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import org.junit.Test;

public class DateTimeUtilTest {

	@Test
	public void convertEpocUTCToLocalTimeZone_shouldConvert() {
		long utcEpoch = 1704067200L; // 2024-01-01 00:00:00 UTC

		LocalDateTime result = DateTimeUtil.convertEpocUTCToLocalTimeZone(utcEpoch);

		assertThat(result, notNullValue());
	}

	@Test
	public void convertLocalDateTimeToUTCEpoc_shouldConvert() {
		LocalDateTime local = LocalDateTime.of(2024, 1, 1, 12, 0);

		long result = DateTimeUtil.convertLocalDateTimeToUTCEpoc(local);

		assertThat(result, notNullValue());
	}

	@Test
	public void convertDateToLocalDateTime_shouldConvert() {
		Date date = new Date(1704067200000L);

		LocalDateTime result = DateTimeUtil.convertDateToLocalDateTime(date);

		assertThat(result, notNullValue());
	}

	@Test
	public void convertLocalDateTimeDate_shouldConvert() {
		LocalDateTime local = LocalDateTime.of(2024, 1, 1, 12, 0);

		Date result = DateTimeUtil.convertLocalDateTimeDate(local);

		assertThat(result, notNullValue());
	}

	@Test
	public void convertEpochTimeToDate_shouldConvert() {
		long utcEpoch = 1704067200L;

		Date result = DateTimeUtil.convertEpochTimeToDate(utcEpoch);

		assertThat(result, notNullValue());
		assertThat(result.getTime(), equalTo(1704067200000L));
	}
}
