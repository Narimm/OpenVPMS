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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link RepeatOnWeekdaysEditor} class.
 *
 * @author Tim Anderson
 */
public class RepeatOnWeekdaysEditorTestCase extends AbstractRepeatExpressionTest {

    /**
     * Tests the {@link RepeatOnWeekdaysEditor#supports(CronRepeatExpression)} method.
     */
    @Test
    public void testSupports() {
        CronRepeatExpression cron1 = parse("0 30 9 ? * MON-FRI *");
        CronRepeatExpression cron2 = parse("0 30 9 ? * MON,TUE,WED,THU,FRI *");
        CronRepeatExpression cron3 = parse("0 30 9 ? * MON,TUE,WED,THU *");
        CronRepeatExpression cron4 = parse("0 30 9 ? * SAT,SUN *");
        CronRepeatExpression cron5 = parse("0 30 9 ? * * *");
        assertTrue(RepeatOnWeekdaysEditor.supports(cron1));
        assertTrue(RepeatOnWeekdaysEditor.supports(cron2));
        assertFalse(RepeatOnWeekdaysEditor.supports(cron3));
        assertFalse(RepeatOnWeekdaysEditor.supports(cron4));
        assertFalse(RepeatOnWeekdaysEditor.supports(cron5));
    }

    /**
     * Tests the {@link RepeatOnWeekdaysEditor#getExpression()}  method.
     */
    @Test
    public void testExpression() {
        RepeatOnWeekdaysEditor editor = new RepeatOnWeekdaysEditor();
        assertNull(editor.getExpression());

        Date startTime = TestHelper.getDatetime("2015-01-08 09:30:00");
        editor.setStartTime(startTime);

        RepeatExpression expression = editor.getExpression();
        assertTrue(expression instanceof CronRepeatExpression);
        CronRepeatExpression cron = (CronRepeatExpression) expression;
        assertEquals("0 30 9 ? * MON-FRI *", cron.getExpression());

        Date date1 = checkNext(startTime, cron, "2015-01-09 09:30:00");
        Date date2 = checkNext(date1, cron, "2015-01-12 09:30:00");
        Date date3 = checkNext(date2, cron, "2015-01-13 09:30:00");
        checkNext(date3, cron, "2015-01-14 09:30:00");
    }
}
