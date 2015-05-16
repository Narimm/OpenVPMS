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

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link RepeatOnDateEditor} class.
 *
 * @author Tim Anderson
 */
public class RepeatOnDateEditorTestCase extends AbstractRepeatExpressionTest {

    /**
     * Verifies that creating an editor with a start time creates a valid expression that repeats each year on
     * the same date.
     */
    @Test
    public void testCreateWithStartTime() {
        Date startTime = TestHelper.getDatetime("2015-01-02 09:30:00");
        RepeatOnDateEditor editor = new RepeatOnDateEditor(startTime);
        RepeatExpression expression = editor.getExpression();
        assertTrue(expression instanceof CronRepeatExpression);
        CronRepeatExpression cron = (CronRepeatExpression) expression;
        assertEquals("9", cron.getHours());
        assertEquals("30", cron.getMinutes());
        assertTrue(cron.getDayOfMonth().singleDay());
        assertEquals(2, cron.getDayOfMonth().day());
        assertEquals(1, cron.getMonth().month());
        assertTrue(cron.getDayOfWeek().isAll());
        assertEquals(2015, cron.getYear().year());

        assertEquals("0 30 9 2 1 ? 2015/1", cron.getExpression());

        Date date1 = checkNext(startTime, cron, "2016-01-02 09:30:00");
        Date date2 = checkNext(date1, cron, "2017-01-02 09:30:00");
        checkNext(date2, cron, "2018-01-02 09:30:00");
    }

    /**
     * Verifies that creating an editor with an expression returns that same expression.
     */
    @Test
    public void testCreateWithExpression() {
        CronRepeatExpression cron = parse("0 30 9 2 1 ? 2015/2");
        assertTrue(RepeatOnDateEditor.supports(cron));
        RepeatOnDateEditor editor = new RepeatOnDateEditor(cron);

        Date startTime = TestHelper.getDatetime("2015-01-02 09:30:00");
        editor.setStartTime(startTime);  // still need a start time to create the expression

        RepeatExpression expression = editor.getExpression();
        assertEquals(cron, expression);

        Date date1 = checkNext(startTime, expression, "2017-01-02 09:30:00");
        Date date2 = checkNext(date1, expression, "2019-01-02 09:30:00");
        checkNext(date2, expression, "2021-01-02 09:30:00");
    }
}
