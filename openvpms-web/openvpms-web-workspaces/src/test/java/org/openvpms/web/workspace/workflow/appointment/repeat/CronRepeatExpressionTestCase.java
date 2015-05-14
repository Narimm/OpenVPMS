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
import org.apache.commons.collections4.PredicateUtils;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.test.TestHelper.getDatetime;

/**
 * Tests the {@link CronRepeatExpression} class.
 *
 * @author Tim Anderson
 */
public class CronRepeatExpressionTestCase {

    /**
     * Tests the {@link CronRepeatExpression#parse(String)} method when a day-of-week has an ordinal value.
     */
    @Test
    public void testParseOfExpressionWithOrdinal() {
        CronRepeatExpression expression = CronRepeatExpression.parse("0 0 12 ? */2 MON#1");
        assertTrue(expression.getDayOfMonth().isAll());
        assertEquals(2, expression.getMonth().getInterval());
        assertEquals(1, expression.getDayOfWeek().getOrdindal());
        assertEquals("MON", expression.getDayOfWeek().getDay());
    }

    /**
     * Tests the {@link CronRepeatExpression#parse(String)} method when the expression specifies weekdays.
     */
    @Test
    public void testParseWeekdays() {
        CronRepeatExpression expression = CronRepeatExpression.parse("0 0 12 ? * MON-FRI");
        assertTrue(expression.getDayOfMonth().isAll());
        assertTrue(expression.getMonth().isAll());
        assertTrue(expression.getDayOfWeek().weekDays());
    }

    /**
     * Tests the {@link CronRepeatExpression#parse(String)} method when the expression specifies weekends.
     */
    @Test
    public void testParseWeekends() {
        CronRepeatExpression expression = CronRepeatExpression.parse("0 0 12 ? * SUN,SAT");
        assertTrue(expression.getDayOfMonth().isAll());
        assertTrue(expression.getMonth().isAll());
        assertTrue(expression.getDayOfWeek().weekends());
    }

    /**
     * Tests the {@link CronRepeatExpression#getRepeatAfter(Date, Predicate)} method.
     */
    @Test
    public void testGetRepeatAfter() {
        Date startTime = getDatetime("2015-01-01 12:00:00");
        CronRepeatExpression expression = CronRepeatExpression.parse("0 0 12 ? */2 MON#1");
        Date date1 = expression.getRepeatAfter(startTime, PredicateUtils.<Date>truePredicate());
        Date date2 = expression.getRepeatAfter(date1, PredicateUtils.<Date>truePredicate());
        Date date3 = expression.getRepeatAfter(date2, PredicateUtils.<Date>truePredicate());
        Date date4 = expression.getRepeatAfter(date3, PredicateUtils.<Date>falsePredicate());

        assertEquals(getDatetime("2015-01-05 12:00:00"), date1);
        assertEquals(getDatetime("2015-03-02 12:00:00"), date2);
        assertEquals(getDatetime("2015-05-04 12:00:00"), date3);
        assertNull(date4);
    }
}
