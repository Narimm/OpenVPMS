/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.archetype.util;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.TestHelper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link DateRules} class.
 *
 * @author Tim Anderson
 */
public class DateRulesTestCase {

    /**
     * Tests the {@link DateRules#getToday()} method.
     */
    @Test
    public void testGetToday() {
        Date expected = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
        assertEquals(expected, DateRules.getToday());
    }

    /**
     * Tests the {@link DateRules#getTomorrow()} method.
     */
    @Test
    public void testGetTomorrow() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        Date expected = DateUtils.truncate(calendar.getTime(), Calendar.DAY_OF_MONTH);
        assertEquals(expected, DateRules.getTomorrow());
    }

    /**
     * Tests the {@link DateRules#getNextDate(Date)}.
     */
    @Test
    public void testGetNextDate() {
        Date date = TestHelper.getDatetime("2012-01-05 12:34:32");
        Date expected = TestHelper.getDate("2012-01-06");
        assertEquals(expected, DateRules.getNextDate(date));
    }

    /**
     * Tests the {@link DateRules#getPreviousDate(Date)}.
     */
    @Test
    public void testGetPreviousDate() {
        Date date = TestHelper.getDatetime("2012-01-05 12:34:32");
        Date expected = TestHelper.getDate("2012-01-04");
        assertEquals(expected, DateRules.getPreviousDate(date));
    }

    /**
     * Tests the {@link DateRules#getDaysInMonth(Date)} method.
     */
    @Test
    public void testGetDaysInMonth() {
        assertEquals(31, DateRules.getDaysInMonth(TestHelper.getDate("2015-12-01")));
        assertEquals(30, DateRules.getDaysInMonth(TestHelper.getDate("2015-06-06")));
        assertEquals(28, DateRules.getDaysInMonth(TestHelper.getDate("2015-02-05")));
        assertEquals(29, DateRules.getDaysInMonth(TestHelper.getDate("2012-02-05")));
    }

    /**
     * Tests the {@link DateRules#isToday(OffsetDateTime)} method.
     */
    @Test
    public void testIsToday() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime yesterday = now.minusDays(1);
        OffsetDateTime tomorrow = now.plusDays(1);
        assertTrue(DateRules.isToday(now));
        assertFalse(DateRules.isToday(yesterday));
        assertFalse(DateRules.isToday(tomorrow));

        int offsetSeconds = now.getOffset().getTotalSeconds();
        ZoneOffset plus1 = ZoneOffset.ofTotalSeconds(offsetSeconds + 60 * 60);
        ZoneOffset minus1 = ZoneOffset.ofTotalSeconds(offsetSeconds - 60 * 60);

        OffsetDateTime nowPlus1Zone = now.withOffsetSameInstant(plus1);
        assertFalse(DateRules.isToday(nowPlus1Zone.withHour(0).truncatedTo(HOURS)));        // previous day 11pm local
        assertTrue(DateRules.isToday(nowPlus1Zone.withHour(23).withMinute(59)));            // 10:59pm local

        OffsetDateTime nowMinus1Zone = now.withOffsetSameInstant(minus1);
        assertTrue(DateRules.isToday(nowMinus1Zone.withHour(0).truncatedTo(HOURS)));        // 1am local time
        assertFalse(DateRules.isToday(nowMinus1Zone.withHour(23).withMinute(59)));          // 00:59am local tomorrow
    }

    /**
     * Tests the {@link DateRules#isTomorrow(OffsetDateTime)} method.
     */
    @Test
    public void testIsTomorrow() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime yesterday = now.minusDays(1);
        OffsetDateTime tomorrow = now.plusDays(1);
        assertTrue(DateRules.isTomorrow(tomorrow));
        assertFalse(DateRules.isTomorrow(now));
        assertFalse(DateRules.isTomorrow(yesterday));

        int offsetSeconds = tomorrow.getOffset().getTotalSeconds();
        ZoneOffset plus1 = ZoneOffset.ofTotalSeconds(offsetSeconds + 60 * 60);
        ZoneOffset minus1 = ZoneOffset.ofTotalSeconds(offsetSeconds - 60 * 60);

        OffsetDateTime tomPlus1Zone = tomorrow.withOffsetSameInstant(plus1);
        assertFalse(DateRules.isTomorrow(tomPlus1Zone.withHour(0).truncatedTo(HOURS)));      // today 11pm local
        assertTrue(DateRules.isTomorrow(tomPlus1Zone.withHour(23).withMinute(59)));          // tomorrow 10:59pm local

        OffsetDateTime tomMinus1Zone = tomorrow.withOffsetSameInstant(minus1);
        assertTrue(DateRules.isTomorrow(tomMinus1Zone.withHour(0).truncatedTo(HOURS)));      // tomorrow 1am local time
        assertFalse(DateRules.isTomorrow(tomMinus1Zone.withHour(23).withMinute(59)));        // 00:59am local following
    }

}
