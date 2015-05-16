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

import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link RepeatOnNthDayEditor} class.
 *
 * @author Tim Anderson
 */
public class RepeatOnNthDayEditorTestCase extends AbstractRepeatExpressionTest {

    /**
     * Verifies that creating an editor with a start time creates a valid expression that repeats on the expected
     * dates.
     */
    @Test
    public void testCreateWithStartTime() {
        Date startTime = TestHelper.getDatetime("2015-01-08 09:30:00");
        RepeatOnNthDayEditor editor = new RepeatOnNthDayEditor(startTime);
        RepeatExpression expression = editor.getExpression();
        assertTrue(expression instanceof CronRepeatExpression);
        CronRepeatExpression cron = (CronRepeatExpression) expression;
        assertEquals("9", cron.getHours());
        assertEquals("30", cron.getMinutes());
        assertTrue(cron.getDayOfMonth().isAll());
        assertEquals(1, cron.getMonth().month());
        assertEquals(1, cron.getMonth().getInterval());
        assertEquals(Calendar.THURSDAY, cron.getDayOfWeek().day());
        assertEquals(2, cron.getDayOfWeek().getOrdinal());
        assertTrue(cron.getYear().isAll());

        assertEquals("0 30 9 ? 1/1 THU#2 *", cron.getExpression());

        Date date1 = checkNext(startTime, cron, "2015-02-12 09:30:00");
        Date date2 = checkNext(date1, cron, "2015-03-12 09:30:00");
        checkNext(date2, cron, "2015-04-09 09:30:00");
    }

    /**
     * Verifies that creating an editor with an expression returns that same expression.
     */
    @Test
    public void testCreateWithExpression() {
        CronRepeatExpression cron = parse("0 30 9 ? 1/2 THU#2 *");
        assertTrue(RepeatOnNthDayEditor.supports(cron));
        RepeatOnNthDayEditor editor = new RepeatOnNthDayEditor(cron);

        Date startTime = TestHelper.getDatetime("2015-01-08 09:30:00");
        editor.setStartTime(startTime);  // still need a start time to create the expression

        RepeatExpression expression = editor.getExpression();
        assertEquals(cron, expression);

        Date date1 = checkNext(startTime, expression, "2015-03-12 09:30:00");
        Date date2 = checkNext(date1, expression, "2015-05-14 09:30:00");
        checkNext(date2, expression, "2015-07-09 09:30:00");
    }
}
