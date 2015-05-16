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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.repeat;

import org.apache.commons.collections4.Predicate;
import org.junit.Test;

import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.test.TestHelper.getDatetime;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfMonth;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfWeek;

/**
 * Tests the {@link CronRepeatExpression} class.
 *
 * @author Tim Anderson
 */
public class CronRepeatExpressionTestCase extends AbstractRepeatExpressionTest {

    /**
     * Tests the {@link CronRepeatExpression#parse(String)} method when a days of the week are specified.
     */
    @Test
    public void testParseDayOfWeek() {
        CronRepeatExpression expression1 = parse("0 0 12 ? * MON");
        assertTrue(expression1.getDayOfMonth().isAll());
        assertTrue(expression1.getMonth().isAll());
        checkSelected(expression1.getDayOfWeek(), Calendar.MONDAY);

        CronRepeatExpression expression2 = parse("0 0 12 ? * TUE-THU");
        assertTrue(expression2.getDayOfMonth().isAll());
        assertTrue(expression2.getMonth().isAll());
        checkSelected(expression2.getDayOfWeek(), Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY);

        CronRepeatExpression expression3 = parse("0 0 12 ? * FRI,SAT");
        assertTrue(expression3.getDayOfMonth().isAll());
        assertTrue(expression3.getMonth().isAll());
        checkSelected(expression3.getDayOfWeek(), Calendar.FRIDAY, Calendar.SATURDAY);
    }

    /**
     * Tests the {@link CronRepeatExpression#parse(String)} method when a day-of-week has an ordinal value.
     */
    @Test
    public void testParseDayOfWeekWithOrdinal() {
        CronRepeatExpression expression = parse("0 0 12 ? 1/2 MON#1");
        assertTrue(expression.getDayOfMonth().isAll());
        assertEquals(1, expression.getMonth().month());
        assertEquals(2, expression.getMonth().getInterval());
        assertEquals(1, expression.getDayOfWeek().getOrdinal());
        assertEquals("MON", expression.getDayOfWeek().getDay());
    }

    /**
     * Tests the {@link CronRepeatExpression#parse(String)} method when the expression specifies weekdays.
     */
    @Test
    public void testParseWeekdays() {
        CronRepeatExpression expression = parse("0 0 12 ? * MON-FRI");
        assertTrue(expression.getDayOfMonth().isAll());
        assertTrue(expression.getMonth().isAll());
        assertTrue(expression.getDayOfWeek().weekdays());
    }

    /**
     * Tests the {@link CronRepeatExpression#parse(String)} method when the expression specifies weekends.
     */
    @Test
    public void testParseWeekends() {
        CronRepeatExpression expression = parse("0 0 12 ? * SUN,SAT");
        assertTrue(expression.getDayOfMonth().isAll());
        assertTrue(expression.getMonth().isAll());
        assertTrue(expression.getDayOfWeek().weekends());
    }

    /**
     * Tests the {@link CronRepeatExpression#parse(String)} method when the expression specifies days of the month.
     */
    @Test
    public void testParseMonthDays() {
        CronRepeatExpression expression1 = parse("0 0 12 1,3,5,7 * ?");
        checkSelected(expression1.getDayOfMonth(), 1, 3, 5, 7);
        assertFalse(expression1.getDayOfMonth().hasLast());
        assertTrue(expression1.getMonth().isAll());
        assertTrue(expression1.getDayOfWeek().isAll());

        CronRepeatExpression expression2 = parse("0 0 12 20-24 * ?");
        checkSelected(expression2.getDayOfMonth(), 20, 21, 22, 23, 24);
        assertFalse(expression2.getDayOfMonth().hasLast());
        assertTrue(expression2.getMonth().isAll());
        assertTrue(expression2.getDayOfWeek().isAll());

        CronRepeatExpression expression3 = parse("0 0 12 L * ?");
        checkSelected(expression3.getDayOfMonth());
        assertTrue(expression3.getDayOfMonth().hasLast());
        assertTrue(expression3.getMonth().isAll());
        assertTrue(expression3.getDayOfWeek().isAll());
    }

    /**
     * Verifies the days of the month match those expected.
     *
     * @param dayOfMonth the days of the month
     * @param expected   the expected days
     */
    private void checkSelected(DayOfMonth dayOfMonth, int... expected) {
        BitSet set = new BitSet();
        for (int day : expected) {
            set.set(day, true);
        }
        for (int day = 1; day <= 31; ++day) {
            assertEquals(set.get(day), dayOfMonth.isSelected(day));
        }
    }

    /**
     * Verifies the days of the week match those expected.
     *
     * @param dayOfWeek the days of the week
     * @param expected  the expected days
     */
    private void checkSelected(DayOfWeek dayOfWeek, int... expected) {
        BitSet set = new BitSet();
        for (int day : expected) {
            set.set(day, true);
        }
        for (int day = 1; day <= 31; ++day) {
            assertEquals(set.get(day), dayOfWeek.isSelected(day));
        }
    }

    /**
     * Tests the {@link CronRepeatExpression#getRepeatAfter(Date, Predicate)} method for an expression
     * that repeats on the first monday every two months.
     */
    @Test
    public void testGetRepeatAfterForOrdinalDay() {
        Date startTime = getDatetime("2015-01-05 12:00:00");
        CronRepeatExpression expression = parse("0 0 12 ? 1/2 MON#1");
        Date date1 = checkNext(startTime, expression, "2015-03-02 12:00:00");
        Date date2 = checkNext(date1, expression, "2015-05-04 12:00:00");
        checkNext(date2, expression, "2015-07-06 12:00:00");
    }

    /**
     * Tests the {@link CronRepeatExpression#getRepeatAfter(Date, Predicate)} method for an expression
     * that repeats every two years.
     */
    @Test
    public void testGetRepeatAfterForDate() {
        Date startTime = getDatetime("2015-01-01 12:00:00");
        CronRepeatExpression expression = parse("0 0 12 1 1 ? 2015/2");

        Date date1 = checkNext(startTime, expression, "2017-01-01 12:00:00");
        Date date2 = checkNext(date1, expression, "2019-01-01 12:00:00");
        checkNext(date2, expression, "2021-01-01 12:00:00");
    }
}
