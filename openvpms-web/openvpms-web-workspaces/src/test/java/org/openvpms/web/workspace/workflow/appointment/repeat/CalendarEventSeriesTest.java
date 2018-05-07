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

package org.openvpms.web.workspace.workflow.appointment.repeat;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.daily;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.monthly;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.once;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.times;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.weekdays;
import static org.openvpms.web.workspace.workflow.appointment.repeat.Repeats.yearly;

/**
 * Base class for {@link CalendarEventSeries} tests.
 *
 * @author Tim Anderson
 */
public abstract class CalendarEventSeriesTest extends ArchetypeServiceTest {

    /**
     * Calendar event start time.
     */
    private Date startTime;

    /**
     * Calendar event end time.
     */
    private Date endTime;

    /**
     * The schedule.
     */
    private Party schedule;

    /**
     * The appointment type.
     */
    private Entity appointmentType;

    /**
     * The author.
     */
    private User author;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        startTime = TestHelper.getDatetime("2015-01-01 09:30:00");
        endTime = TestHelper.getDatetime("2015-01-01 09:45:00");
        author = TestHelper.createUser();
        appointmentType = ScheduleTestHelper.createAppointmentType();
        schedule = ScheduleTestHelper.createSchedule(15, DateUnits.MINUTES.toString(), 1, appointmentType,
                                                     TestHelper.createLocation());
    }

    /**
     * Verifies that an event can be repeated daily.
     */
    @Test
    public void testRepeatDaily() {
        checkCreateSeries(Repeats.daily(), startTime, endTime, 1, DateUnits.DAYS);
    }

    /**
     * Verifies that an appointment can be repeated weekly.
     */
    @Test
    public void testRepeatWeekly() {
        checkCreateSeries(Repeats.weekly(), startTime, endTime, 1, DateUnits.WEEKS);
    }

    /**
     * Verifies that an appointment can be repeated monthly.
     */
    @Test
    public void testRepeatMonthly() {
        checkCreateSeries(monthly(), startTime, endTime, 1, DateUnits.MONTHS);
    }

    /**
     * Verifies that an appointment can be repeated yearly.
     */
    @Test
    public void testRepeatYearly() {
        checkCreateSeries(Repeats.yearly(), startTime, endTime, 1, DateUnits.YEARS);
    }

    /**
     * Verifies that changing the series expression updates the appointment times.
     */
    @Test
    public void testChangeSeriesExpression() {
        Act event = createEvent(startTime, endTime);

        CalendarEventSeries series = createSeries(event, monthly(), times(11));

        checkSeries(series, event, 1, DateUnits.MONTHS, 12);
        series.setExpression(Repeats.yearly());
        series.save();
        checkSeries(series, event, 1, DateUnits.YEARS, 12);
    }

    /**
     * Verifies that changing the series condition to fewer events deletes those no longer included.
     */
    @Test
    public void testChangeSeriesConditionToFewerEvents() {
        Act event = createEvent(startTime, endTime);

        CalendarEventSeries series = createSeries(event, monthly(), times(11));
        checkSeries(series, event, 1, DateUnits.MONTHS, 12);
        List<Act> oldEvents = series.getEvents();
        assertEquals(12, oldEvents.size());

        List<Act> toRemove = oldEvents.subList(10, 12);

        series.setCondition(times(9));
        series.save();
        checkSeries(series, event, 1, DateUnits.MONTHS, 10);
        List<Act> newEvents = series.getEvents();
        assertEquals(10, newEvents.size());

        for (Act act : oldEvents) {
            if (!toRemove.contains(act)) {
                assertTrue(newEvents.contains(act));
            } else {
                // verify it has been deleted
                assertNull(get(act));
            }
        }
    }

    /**
     * Verifies that changing the series condition to more events adds new events on save.
     */
    @Test
    public void testChangeSeriesConditionToMoreEvents() {
        Act event = createEvent(startTime, endTime);

        CalendarEventSeries series = createSeries(event, monthly(), times(9));
        checkSeries(series, event, 1, DateUnits.MONTHS, 10);
        List<Act> oldEvents = series.getEvents();
        assertEquals(10, oldEvents.size());

        series.setCondition(times(11));
        series.save();
        checkSeries(series, event, 1, DateUnits.MONTHS, 12);
        List<Act> newEvents = series.getEvents();
        assertEquals(12, newEvents.size());

        // verify the original events have been retained
        for (Act act : oldEvents) {
            assertTrue(newEvents.contains(act));
        }
    }

    /**
     * Verifies that a series with overlapping events is detected.
     * <p/>
     * Note that this only checks overlaps with events in the series, not with existing events.
     */
    @Test
    public void testSeriesWithOverlappingEvents() {
        Act event = createEvent(startTime, DateRules.getDate(startTime, 2, DateUnits.DAYS));
        CalendarEventSeries series = createSeries(event);
        series.setExpression(daily());  // the next event overlaps the previous
        series.setCondition(once());
        CalendarEventSeries.Overlap overlap = series.getFirstOverlap();
        assertNotNull(overlap);
        assertEquals(overlap.getEvent1(), Times.create(event));
    }

    /**
     * Verifies a series can be deleted.
     * <p/>
     * This deletes all events bar the current one.
     */
    @Test
    public void testDeleteSeriesWithNoExpiredEvents() {
        Act event = createEvent();
        CalendarEventSeries series = createSeries(event, Repeats.yearly(), times(9));
        Act act = series.getSeries();
        assertNotNull(act);

        List<Act> events = series.getEvents();
        assertEquals(10, events.size());

        series.setExpression(null);
        series.setCondition(null);
        series.save();
        events = series.getEvents();
        assertEquals(0, events.size());
        assertNull(series.getSeries());
        assertNotNull(get(event));
        assertNull(get(act));
    }

    /**
     * Verifies that the schedule can be updated.
     */
    @Test
    public void testChangeSchedule() {
        Act event = createEvent();
        CalendarEventSeries series = createSeries(event, Repeats.yearly(), times(2));
        checkSeries(series, event, 1, DateUnits.YEARS, 3);

        Entity schedule2 = ScheduleTestHelper.createSchedule(15, DateUnits.MINUTES.toString(), 1, appointmentType,
                                                             TestHelper.createLocation());

        ActBean bean = new ActBean(event);
        bean.setNodeParticipant("schedule", schedule2);

        checkSave(series);
        assertEquals(schedule2, bean.getNodeParticipant("schedule"));
        checkSeries(series, event, 1, DateUnits.YEARS, 3);
    }

    /**
     * Verifies that the author cannot be updated.
     */
    @Test
    public void testCannotChangeAuthor() {
        Act event = createEvent();

        CalendarEventSeries series = createSeries(event, monthly(), times(2));
        checkSeries(series, event, 1, DateUnits.MONTHS, 3);

        User author2 = TestHelper.createUser();
        ActBean bean = new ActBean(event);
        bean.setNodeParticipant("author", author2);

        checkSave(series);

        assertEquals(author2, bean.getNodeParticipant("author"));

        // series events should have the original author
        checkSeries(series, event, 1, DateUnits.MONTHS, 3, author);
    }

    /**
     * Verifies that changing the expression on a non-initial event in the series creates a new series.
     */
    @Test
    public void testNewSeriesCreatedForNonInitialEvent() {
        Act first = createEvent(startTime, endTime);
        CalendarEventSeries series1 = createSeries(first, monthly(), times(4));
        List<Act> events = checkSeries(series1, first, 1, DateUnits.MONTHS, 5);

        // get the third event, and create a new series
        Act third = events.get(2);
        CalendarEventSeries series2 = createSeries(third);
        RepeatCondition condition = series2.getCondition();
        assertEquals(times(2), condition);                   // times reflects the position in the series

        // change the expression
        series2.setExpression(yearly());
        checkSave(series2);
        checkSeries(series2, third, 1, DateUnits.YEARS, 3);

        // verify the original series is now shortened
        series1 = createSeries(first);
        checkSeries(series1, first, 1, DateUnits.MONTHS, 2);
    }

    /**
     * Verifies that changing the date on the first event moves the entire series.
     */
    @Test
    public void testChangeEventDate() {
        Act event = createEvent(startTime, endTime);

        CalendarEventSeries series = createSeries(event, monthly(), times(11));

        checkSeries(series, event, 1, DateUnits.MONTHS, 12);

        startTime = DateRules.getDate(startTime, 1, DateUnits.WEEKS);
        endTime = DateRules.getDate(this.endTime, 1, DateUnits.WEEKS);
        event.setActivityStartTime(startTime);
        event.setActivityEndTime(endTime);

        checkSave(series);
        checkSeries(series, event, 1, DateUnits.MONTHS, 12);
    }

    /**
     * Verifies that when changing the start and end times for a cron expression, it propagates to the rest of the
     * acts in the series.
     */
    @Test
    public void testChangeTimesWithCronRepeat() {
        startTime = TestHelper.getDatetime("2018-05-07 09:00:00");
        endTime = TestHelper.getDatetime("2018-05-07 10:00:00");
        Act event = createEvent(startTime, endTime);

        CalendarEventSeries series = createSeries(event, weekdays(startTime), times(4));
        List<Act> acts1 = series.getEvents();
        assertEquals(5, acts1.size());

        for (int i = 0; i < acts1.size(); ++i) {
            assertEquals(DateRules.getDate(startTime, i, DateUnits.DAYS), acts1.get(i).getActivityStartTime());
            assertEquals(DateRules.getDate(endTime, i, DateUnits.DAYS), acts1.get(i).getActivityEndTime());
        }

        // change the times and verify that each of the acts in the series have the new time
        startTime = TestHelper.getDatetime("2018-05-07 09:30:00");
        endTime = TestHelper.getDatetime("2018-05-07 10:30:00");
        event.setActivityStartTime(startTime);
        event.setActivityEndTime(endTime);
        series.refresh();
        series.setExpression(weekdays(series.getStartTime()));
        checkSave(series);

        List<Act> acts2 = series.getEvents();
        assertEquals(5, acts2.size());

        for (int i = 0; i < acts2.size(); ++i) {
            assertEquals(DateRules.getDate(startTime, i, DateUnits.DAYS), acts2.get(i).getActivityStartTime());
            assertEquals(DateRules.getDate(endTime, i, DateUnits.DAYS), acts2.get(i).getActivityEndTime());
        }
    }

    /**
     * Creates an {@link CalendarEventSeries}, and verifies the expected events have been created.
     *
     * @param expression the expression
     * @param startTime  the first event start time
     * @param endTime    the first event end time
     * @param interval   the interval between events
     * @param units      the interval units
     */
    protected void checkCreateSeries(RepeatExpression expression, Date startTime, Date endTime, int interval,
                                     DateUnits units) {
        Act event = createEvent(startTime, endTime);

        CalendarEventSeries series = createSeries(event, expression, times(9));
        checkSeries(series, event, interval, units, 10);
        assertFalse(series.isModified());
    }

    /**
     * Checks a series that was generated using calendar intervals.
     *
     * @param series   the series
     * @param event    the initial event
     * @param interval the interval
     * @param units    the interval units
     * @param count    the expected no. of events in the series
     * @return the events
     */
    protected List<Act> checkSeries(CalendarEventSeries series, Act event, int interval, DateUnits units, int count) {
        ActBean bean = new ActBean(event);
        return checkSeries(series, event, interval, units, count, (User) bean.getNodeParticipant("author"));
    }

    /**
     * Creates a new calendar event.
     *
     * @return a new event
     */
    protected Act createEvent() {
        return createEvent(startTime, endTime);
    }

    /**
     * Creates a new calendar event.
     *
     * @param startTime the event start time
     * @param endTime   the event end time
     * @return a new event
     */
    protected Act createEvent(Date startTime, Date endTime) {
        return createEvent(startTime, endTime, schedule, appointmentType, author);
    }

    /**
     * Creates a new calendar event.
     *
     * @param startTime       the event start time
     * @param endTime         the event end time
     * @param schedule        the schedule
     * @param appointmentType the appointment type
     * @param author          the author. May be {@code null}  @return a new event
     */
    protected abstract Act createEvent(Date startTime, Date endTime, Entity schedule, Entity appointmentType,
                                       User author);

    /**
     * Returns the schedule.
     *
     * @return the schedule
     */
    protected Entity getSchedule() {
        return schedule;
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
    protected abstract List<Act> checkSeries(CalendarEventSeries series, Act event, int interval, DateUnits units,
                                             int count, User author);

    /**
     * Saves a series if there are no overlaps detected.
     *
     * @param series the series
     */
    protected void checkSave(CalendarEventSeries series) {
        assertNull(series.getFirstOverlap());
        series.save();
    }

    /**
     * Creates a new series, generating events.
     *
     * @param event      the first event
     * @param expression the repeat expression
     * @param condition  the repeat condition
     * @return the series
     */
    protected CalendarEventSeries createSeries(Act event, RepeatExpression expression, RepeatCondition condition) {
        CalendarEventSeries series = createSeries(event);
        assertEquals(0, series.getEvents().size());
        assertTrue(series.isModified());
        assertNull(series.getSeries());

        series.setExpression(expression);
        series.setCondition(condition);
        assertTrue(series.isModified());
        checkSave(series);
        assertFalse(series.isModified());
        assertNotNull(series.getSeries());
        return series;
    }

    /**
     * Creates a new {@link CalendarEventSeries}.
     *
     * @param event the event
     * @return a new series
     */
    protected abstract CalendarEventSeries createSeries(final Act event);

}
