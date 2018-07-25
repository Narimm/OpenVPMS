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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.functor.ActComparator;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.workspace.workflow.appointment.repeat.Repeats;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link CalendarBlockEditor}.
 *
 * @author Tim Anderson
 */
public class CalendarBlockEditorTestCase extends AbstractAppTest {

    /**
     * Tests the {@link CalendarBlockEditor#newInstance()} method.
     */
    @Test
    public void testNewInstance() {
        Date start = DateRules.getToday();
        Date end = DateRules.getTomorrow();
        Entity schedule = ScheduleTestHelper.createSchedule(TestHelper.createLocation());
        Entity blockType = ScheduleTestHelper.createCalendarBlockType();
        Act block = ScheduleTestHelper.createCalendarBlock(start, end, schedule, blockType, null);
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        CalendarBlockEditor editor = new CalendarBlockEditor(block, null, context);

        IMObjectEditor newInstance = editor.newInstance();
        assertTrue(newInstance instanceof CalendarBlockEditor);
    }

    /**
     * Tests week days repeat when the start time is changed after the repeat is selected.
     */
    @Test
    public void testWeekdaysRepeat() {
        Date initialStart = TestHelper.getDate("2018-07-23");
        Date initialEnd = DateRules.getDate(initialStart, 1, DateUnits.HOURS);
        Entity schedule = ScheduleTestHelper.createSchedule(TestHelper.createLocation());
        Entity blockType = ScheduleTestHelper.createCalendarBlockType();
        Act block = ScheduleTestHelper.createCalendarBlock(initialStart, initialEnd, schedule, blockType, null);
        LocalContext context = new LocalContext();
        context.setUser(TestHelper.createUser());
        LayoutContext layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
        CalendarBlockEditor editor = new CalendarBlockEditor(block, null, true, layout);
        editor.getComponent();
        editor.setExpression(Repeats.weekdays(initialStart));
        editor.setCondition(Repeats.times(10));
        Date newStart = DateRules.getDate(initialStart, 2, DateUnits.HOURS);
        Date newEnd = DateRules.getDate(newStart, 1, DateUnits.HOURS);
        editor.setStartTime(newStart);
        editor.setEndTime(newEnd);
        assertTrue(editor.isValid()); // forces generation on the series TODO
        editor.save();

        IMObjectBean bean = new IMObjectBean(block);
        Act series = bean.getSource("repeat", Act.class);
        assertNotNull(series);
        IMObjectBean seriesBean = new IMObjectBean(series);
        List<Act> items = seriesBean.getTargets("items", Act.class);
        Collections.sort(items, ActComparator.ascending());
        assertEquals(11, items.size());

        Date start = newStart;
        Date end = newEnd;
        for (Act item : items) {
            assertEquals(start, item.getActivityStartTime());
            assertEquals(end, item.getActivityEndTime());
            if (Instant.ofEpochMilli(start.getTime()).atZone(ZoneId.systemDefault()).toLocalDate().getDayOfWeek()
                == DayOfWeek.FRIDAY) {
                start = DateRules.getDate(start, 3, DateUnits.DAYS);
            } else {
                start = DateRules.getDate(start, 1, DateUnits.DAYS);
            }
            end = DateRules.getDate(start, 1, DateUnits.HOURS);
        }
    }

}
