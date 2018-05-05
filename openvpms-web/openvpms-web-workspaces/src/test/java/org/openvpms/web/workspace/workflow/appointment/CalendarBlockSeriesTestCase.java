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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.workspace.workflow.appointment.repeat.CalendarBlockSeries;
import org.openvpms.web.workspace.workflow.appointment.repeat.CalendarEventSeries;
import org.openvpms.web.workspace.workflow.appointment.repeat.CalendarEventSeriesTest;
import org.openvpms.web.workspace.workflow.appointment.repeat.Repeats;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.times;

/**
 * Tests the {@link CalendarBlockSeries} class.
 *
 * @author Tim Anderson
 */
public class CalendarBlockSeriesTestCase extends CalendarEventSeriesTest {

    /**
     * The block type.
     */
    private Entity blockType;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        super.setUp();
        blockType = ScheduleTestHelper.createCalendarBlockType();
    }

    /**
     * Verifies that the calendar block type can be updated.
     */
    @Test
    public void testChangeBlockType() {
        Entity blockType2 = ScheduleTestHelper.createCalendarBlockType();
        Act block = createEvent();

        CalendarEventSeries series = createSeries(block, Repeats.weekly(), times(2));
        checkSeries(series, block, 1, DateUnits.WEEKS, 3);

        ActBean bean = new ActBean(block);
        bean.setNodeParticipant("type", blockType2);

        checkSave(series);
        assertEquals(blockType2, bean.getNodeParticipant("type"));
        checkSeries(series, block, 1, DateUnits.WEEKS, 3);
    }

    /**
     * Creates a new calendar event.
     *
     * @param startTime       the event start time
     * @param endTime         the event end time
     * @param schedule        the schedule
     * @param appointmentType the appointment type
     * @param author          the author. May be {@code null}
     * @return a new event
     */
    @Override
    protected Act createEvent(Date startTime, Date endTime, Entity schedule, Entity appointmentType, User author) {
        Act block = ScheduleTestHelper.createCalendarBlock(startTime, endTime, schedule, blockType, author);
        block.setName("block name");
        block.setDescription("block description");
        return block;
    }

    /**
     * Checks a series that was generated using calendar intervals.
     *
     * @param series   the series
     * @param event    the initial event
     * @param interval the interval
     * @param units    the interval units
     * @param count    the expected no. of events in the series
     * @param author   the expected author
     * @return the events
     */
    @Override
    protected List<Act> checkSeries(CalendarEventSeries series, Act event, int interval, DateUnits units, int count,
                                    User author) {
        List<Act> acts = series.getEvents();
        assertEquals(count, acts.size());
        Date from = event.getActivityStartTime();
        Date to = event.getActivityEndTime();
        assertEquals(event, acts.get(0));
        ActBean bean = new ActBean(event);
        Entity schedule = bean.getNodeParticipant("schedule");
        Entity blockType = bean.getNodeParticipant("type");
        String name = event.getName();
        String description = event.getDescription();
        for (Act act : acts) {
            if (act.equals(event)) {
                User eventAuthor = (User) bean.getNodeParticipant("author");
                checkEvent(act, from, to, schedule, blockType, eventAuthor, name, description);
            } else {
                checkEvent(act, from, to, schedule, blockType, author, name, description);
            }
            from = DateRules.getDate(from, interval, units);
            to = DateRules.getDate(to, interval, units);
        }
        return acts;
    }

    /**
     * Creates a new {@link CalendarEventSeries}.
     *
     * @param event the event
     * @return a new series
     */
    @Override
    protected CalendarEventSeries createSeries(Act event) {
        return new CalendarBlockSeries(event, getArchetypeService());
    }

    /**
     * Verifies an event matches that expected.
     *
     * @param act         the appointment
     * @param startTime   the expected start time
     * @param endTime     the expected end time
     * @param schedule    the expected schedule
     * @param blockType   the expected appointment type
     * @param author      the expected author
     * @param name        the expected name
     * @param description the expected description
     */
    private void checkEvent(Act act, Date startTime, Date endTime, Entity schedule, Entity blockType,
                            User author, String name, String description) {
        assertEquals(0, DateRules.compareTo(startTime, act.getActivityStartTime()));
        assertEquals(0, DateRules.compareTo(endTime, act.getActivityEndTime()));
        ActBean bean = new ActBean(act);
        assertEquals(schedule, bean.getNodeParticipant("schedule"));
        assertEquals(blockType, bean.getNodeParticipant("type"));
        assertNull(act.getStatus());
        assertEquals(author, bean.getNodeParticipant("author"));
        assertEquals(name, bean.getString("name"));
        assertEquals(description, bean.getString("description"));
    }

}
