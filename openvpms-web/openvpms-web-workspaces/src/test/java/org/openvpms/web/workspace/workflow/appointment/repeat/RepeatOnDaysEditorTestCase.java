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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.repeat;

import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link RepeatOnDaysEditor} class.
 *
 * @author Tim Anderson
 */
public class RepeatOnDaysEditorTestCase extends AbstractRepeatExpressionTest {

    /**
     * Verifies that creating an editor with a start time creates a valid expression that repeats on the expected
     * dates.
     */
    @Test
    public void testCreateWithStartTime() {
        Date startTime = TestHelper.getDatetime("2015-01-08 09:30:00");
        RepeatOnDaysEditor editor = new RepeatOnDaysEditor(startTime);
        RepeatExpression expression = editor.getExpression();
        assertTrue(expression instanceof CronRepeatExpression);
        CronRepeatExpression cron = (CronRepeatExpression) expression;
        assertEquals("9", cron.getHours());
        assertEquals("30", cron.getMinutes());
        assertTrue(cron.getDayOfMonth().isAll());
        assertTrue(cron.getMonth().isAll());
        assertEquals(Calendar.THURSDAY, cron.getDayOfWeek().day());
        assertTrue(cron.getYear().isAll());

        assertEquals("0 30 9 ? * THU *", cron.getExpression());

        Date date1 = checkNext(startTime, cron, "2015-01-15 09:30:00");
        Date date2 = checkNext(date1, cron, "2015-01-22 09:30:00");
        checkNext(date2, cron, "2015-01-29 09:30:00");
    }

    /**
     * Verifies that creating an editor with an expression returns that same expression.
     */
    @Test
    public void testCreateWithExpression() {
        CronRepeatExpression cron = parse("0 30 9 ? * THU *");
        assertTrue(RepeatOnDaysEditor.supports(cron));
        RepeatOnDaysEditor editor = new RepeatOnDaysEditor(cron);

        Date startTime = TestHelper.getDatetime("2015-01-08 09:30:00");
        editor.setStartTime(startTime);  // still need a start time to create the expression

        RepeatExpression expression = editor.getExpression();
        assertEquals(cron, expression);

        Date date1 = checkNext(startTime, cron, "2015-01-15 09:30:00");
        Date date2 = checkNext(date1, cron, "2015-01-22 09:30:00");
        checkNext(date2, cron, "2015-01-29 09:30:00");
    }

    /**
     * Tests the {@link RepeatOnDaysEditor#supports(CronRepeatExpression)} method.
     */
    @Test
    public void testSupports() {
        assertTrue(RepeatOnDaysEditor.supports(parse("0 0 10 ? * * *")));
        assertTrue(RepeatOnDaysEditor.supports(parse("0 0 10 ? * SUN,MON,TUE,WED,THU,FRI,SAT *")));
        assertTrue(RepeatOnDaysEditor.supports(parse("0 0 10 ? * SUN-SAT *")));

        assertFalse(RepeatOnDaysEditor.supports(parse("0 30 9 2 1 ? 2015/1")));
        assertFalse(RepeatOnDaysEditor.supports(parse("0 30 9 8,10 1/2 ? *")));
        assertFalse(RepeatOnDaysEditor.supports(parse("0 30 9 ? 1 THU#2 2015/1")));
    }
}
