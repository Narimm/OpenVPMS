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

package org.openvpms.archetype.rules.workflow;

import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.sql.Time;
import java.util.Date;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.rules.workflow.ScheduleTestHelper.createAppointment;
import static org.openvpms.archetype.test.TestHelper.getDate;
import static org.openvpms.archetype.test.TestHelper.getDatetime;

/**
 * Tests the {@link FreeSlotQuery} class.
 *
 * @author Tim Anderson
 */
public class FreeSlotQueryTestCase extends ArchetypeServiceTest {

    /**
     * Tests finding free slots where the schedule doesn't define start and end times.
     */
    @Test
    public void testFindFreeSlotsForSingleSchedule() {
        Party schedule1 = createSchedule(null, null);
        Act act1 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:15:00"), schedule1);
        Act act2 = createAppointment(getDatetime("2014-01-01 10:00:00"), getDatetime("2014-01-01 10:15:00"), schedule1);
        save(act1, act2);

        Iterator<Slot> iterator = createIterator("2014-01-01", "2014-01-02", schedule1);
        checkSlot(iterator, schedule1, "2014-01-01 00:00:00", "2014-01-01 09:00:00");
        checkSlot(iterator, schedule1, "2014-01-01 09:15:00", "2014-01-01 10:00:00");
        checkSlot(iterator, schedule1, "2014-01-01 10:15:00", "2014-01-02 00:00:00");
        assertFalse(iterator.hasNext());
    }

    /**
     * Tests finding free slots for multiple schedules, where the schedules don't define start and end times.
     */
    @Test
    public void testFindFreeSlotsForMultipleSchedules() {
        Party schedule1 = createSchedule(null, null);
        Party schedule2 = createSchedule(null, null);
        Act act1 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:15:00"), schedule1);
        Act act2 = createAppointment(getDatetime("2014-01-01 10:00:00"), getDatetime("2014-01-01 10:15:00"), schedule1);
        Act act3 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:30:00"), schedule2);
        Act act4 = createAppointment(getDatetime("2014-01-01 09:45:00"), getDatetime("2014-01-01 10:30:00"), schedule2);
        save(act1, act2, act3, act4);

        Iterator<Slot> query = createIterator("2014-01-01", "2014-01-02", schedule1, schedule2);
        checkSlot(query, schedule1, "2014-01-01 00:00:00", "2014-01-01 09:00:00");
        checkSlot(query, schedule2, "2014-01-01 00:00:00", "2014-01-01 09:00:00");
        checkSlot(query, schedule1, "2014-01-01 09:15:00", "2014-01-01 10:00:00");
        checkSlot(query, schedule2, "2014-01-01 09:30:00", "2014-01-01 09:45:00");
        checkSlot(query, schedule1, "2014-01-01 10:15:00", "2014-01-02 00:00:00");
        checkSlot(query, schedule2, "2014-01-01 10:30:00", "2014-01-02 00:00:00");
        assertFalse(query.hasNext());
    }

    /**
     * Verifies that a free slot with the same length as the query range is returned if there are no appointments.
     */
    @Test
    public void testFindFreeSlotsForEmptySchedule() {
        Party schedule = createSchedule(null, null);

        Iterator<Slot> query = createIterator("2014-01-01", "2014-01-02", schedule);
        checkSlot(query, schedule, "2014-01-01 00:00:00", "2014-01-02 00:00:00");
        assertFalse(query.hasNext());
    }

    /**
     * Tests finding free slots when there is a single appointment during the date range.
     */
    @Test
    public void testFindFreeSlotsForAppointmentDuringDateRange() {
        Party schedule = createSchedule(null, null);
        Act act = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:15:00"), schedule);
        save(act);

        Iterator<Slot> query = createIterator("2014-01-01", "2014-01-02", schedule);
        checkSlot(query, schedule, "2014-01-01 00:00:00", "2014-01-01 09:00:00");
        checkSlot(query, schedule, "2014-01-01 09:15:00", "2014-01-02 00:00:00");
        assertFalse(query.hasNext());
    }

    /**
     * Tests finding free slots when there is a single appointment on the start of the date range.
     */
    @Test
    public void testFindFreeSlotForAppointmentAtStartOfDateRange() {
        Party schedule = createSchedule(null, null);
        Act act = createAppointment(getDatetime("2014-01-01 00:00:00"), getDatetime("2014-01-01 09:00:00"), schedule);
        save(act);
        Iterator<Slot> query = createIterator("2014-01-01", "2014-01-02", schedule);
        checkSlot(query, schedule, "2014-01-01 09:00:00", "2014-01-02 00:00:00");
        assertFalse(query.hasNext());
    }

    /**
     * Tests finding free slots when there is a single appointment overlapping the start of the date range.
     */
    @Test
    public void testFindFreeSlotForAppointmentOverlappingStart() {
        // test an appointment overlapping the start of the date range
        Party schedule = createSchedule(null, null);
        Act act = createAppointment(getDatetime("2013-12-31 09:00:00"), getDatetime("2014-01-01 08:00:00"), schedule);
        save(act);
        Iterator<Slot> query = createIterator("2014-01-01", "2014-01-02", schedule);
        checkSlot(query, schedule, "2014-01-01 08:00:00", "2014-01-02 00:00:00");
        assertFalse(query.hasNext());
    }

    /**
     * Verifies that free slots are handled correctly if a schedule has a single appointment at the start, during
     * or at the end of the date range.
     */
    @Test
    public void testFindFreeSlotsForSingleAppointment() {

        // test an appointment at the end of the date range
        Party schedule4 = createSchedule(null, null);
        Act act4 = createAppointment(getDatetime("2014-01-01 17:00:00"), getDatetime("2014-01-02 00:00:00"), schedule4);
        save(act4);
        Iterator<Slot> query4 = createIterator("2014-01-01", "2014-01-02", schedule4);
        checkSlot(query4, schedule4, getDatetime("2014-01-01 00:00:00"), getDatetime("2014-01-01 17:00:00"));
        assertFalse(query4.hasNext());
    }

    /**
     * Verifies that duplicate appointments don't cause duplicate free slots to be reported.
     */
    @Test
    public void testDuplicateAppointments() {
        Party schedule1 = createSchedule(null, null);
        Act act1 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:15:00"), schedule1);
        Act act2 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:15:00"), schedule1);
        Act act3 = createAppointment(getDatetime("2014-01-01 10:00:00"), getDatetime("2014-01-01 10:15:00"), schedule1);
        Act act4 = createAppointment(getDatetime("2014-01-01 10:00:00"), getDatetime("2014-01-01 10:15:00"), schedule1);
        save(act1, act2, act3, act4);

        Iterator<Slot> query = createIterator("2014-01-01", "2014-01-02", schedule1);
        checkSlot(query, schedule1, "2014-01-01 00:00:00", "2014-01-01 09:00:00");
        checkSlot(query, schedule1, "2014-01-01 09:15:00", "2014-01-01 10:00:00");
        checkSlot(query, schedule1, "2014-01-01 10:15:00", "2014-01-02 00:00:00");
        assertFalse(query.hasNext());
    }

    /**
     * Verifies that overlapping appointments are handled.
     */
    @Test
    public void testOverlappingAppointments() {
        Party schedule = createSchedule(null, null);
        Act act1 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:15:00"), schedule);
        Act act2 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:30:00"), schedule);
        Act act3 = createAppointment(getDatetime("2014-01-01 09:45:00"), getDatetime("2014-01-01 10:15:00"), schedule);
        Act act4 = createAppointment(getDatetime("2014-01-01 10:00:00"), getDatetime("2014-01-01 10:30:00"), schedule);
        save(act1, act2, act3, act4);

        Iterator<Slot> query = createIterator("2014-01-01", "2014-01-02", schedule);
        checkSlot(query, schedule, "2014-01-01 00:00:00", "2014-01-01 09:00:00");
        checkSlot(query, schedule, "2014-01-01 09:30:00", "2014-01-01 09:45:00");
        checkSlot(query, schedule, "2014-01-01 10:30:00", "2014-01-02 00:00:00");
        assertFalse(query.hasNext());
    }

    /**
     * Verifies that specifying a minimum slot size filters out slots too small.
     */
    @Test
    public void testMinSlotSize() {
        Party schedule = createSchedule(null, null);
        Act act1 = createAppointment(getDatetime("2013-12-31 09:00:00"), getDatetime("2014-01-01 08:00:00"), schedule);
        Act act2 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:45:00"), schedule);
        Act act3 = createAppointment(getDatetime("2014-01-01 10:00:00"), getDatetime("2014-01-01 10:15:00"), schedule);
        Act act4 = createAppointment(getDatetime("2014-01-01 11:00:00"), getDatetime("2014-01-01 11:15:00"), schedule);
        save(act1, act2, act3, act4);

        FreeSlotQuery query = createQuery("2014-01-01", "2014-01-02", schedule);
        query.setMinSlotSize(30, DateUnits.MINUTES);

        Iterator<Slot> iterator = query.query();
        checkSlot(iterator, schedule, "2014-01-01 08:00:00", "2014-01-01 09:00:00");
        checkSlot(iterator, schedule, "2014-01-01 10:15:00", "2014-01-01 11:00:00");
        checkSlot(iterator, schedule, "2014-01-01 11:15:00", "2014-01-02 00:00:00");
        assertFalse(iterator.hasNext());
    }

    /**
     * Verifies that when a schedule has start and end times, free slots will adjusted.
     */
    @Test
    public void testFindFreeSlotsForLimitedScheduleTimes() {
        Party schedule = createSchedule("09:00:00", "17:00:00");
        Act act1 = createAppointment(getDatetime("2013-12-31 09:00:00"), getDatetime("2014-01-01 08:00:00"), schedule);
        Act act2 = createAppointment(getDatetime("2014-01-01 09:30:00"), getDatetime("2014-01-01 09:45:00"), schedule);
        Act act3 = createAppointment(getDatetime("2014-01-02 09:00:00"), getDatetime("2014-01-02 10:00:00"), schedule);
        save(act1, act2, act3);

        Iterator<Slot> query = createIterator("2014-01-01", "2014-01-03", schedule);
        checkSlot(query, schedule, "2014-01-01 09:00:00", "2014-01-01 09:30:00");
        checkSlot(query, schedule, "2014-01-01 09:45:00", "2014-01-01 17:00:00");
        checkSlot(query, schedule, "2014-01-02 10:00:00", "2014-01-02 17:00:00");
        assertFalse(query.hasNext());
    }

    /**
     * Verifies that when a {@link FreeSlotQuery#setFromTime(Duration)} is specified, only free slots after that
     * time are returned.
     */
    @Test
    public void testFindFreeSlotsWithFromTimeRange() {
        Party schedule = createSchedule(null, null);
        Act act1 = createAppointment(getDatetime("2013-12-31 09:00:00"), getDatetime("2014-01-01 08:00:00"), schedule);
        Act act2 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:15:00"), schedule);
        Act act3 = createAppointment(getDatetime("2014-01-01 10:00:00"), getDatetime("2014-01-01 10:15:00"), schedule);
        Act act4 = createAppointment(getDatetime("2014-01-01 10:30:00"), getDatetime("2014-01-01 11:00:00"), schedule);
        save(act1, act2, act3, act4);

        FreeSlotQuery query = createQuery("2014-01-01", "2014-01-02", schedule);
        query.setFromTime(getTime("09:30"));
        Iterator<Slot> iterator = createIterator(query);
        checkSlot(iterator, schedule, "2014-01-01 09:15:00", "2014-01-01 10:00:00");
        checkSlot(iterator, schedule, "2014-01-01 10:15:00", "2014-01-01 10:30:00");
        checkSlot(iterator, schedule, "2014-01-01 11:00:00", "2014-01-02 00:00:00");
        assertFalse(iterator.hasNext());
    }

    /**
     * Verifies that when a {@link FreeSlotQuery#setToTime(Duration)} is specified, only free slots before that
     * time are returned.
     */
    @Test
    public void testFindFreeSlotsWithToTimeRange() {
        Party schedule = createSchedule(null, null);
        Act act1 = createAppointment(getDatetime("2013-12-31 09:00:00"), getDatetime("2014-01-01 08:00:00"), schedule);
        Act act2 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:15:00"), schedule);
        Act act3 = createAppointment(getDatetime("2014-01-01 10:00:00"), getDatetime("2014-01-01 10:15:00"), schedule);
        Act act4 = createAppointment(getDatetime("2014-01-01 10:30:00"), getDatetime("2014-01-01 11:00:00"), schedule);
        save(act1, act2, act3, act4);

        FreeSlotQuery query = createQuery("2014-01-01", "2014-01-02", schedule);
        query.setToTime(getTime("09:30"));
        Iterator<Slot> iterator = createIterator(query);
        checkSlot(iterator, schedule, "2014-01-01 08:00:00", "2014-01-01 09:00:00");
        checkSlot(iterator, schedule, "2014-01-01 09:15:00", "2014-01-01 10:00:00");
        assertFalse(iterator.hasNext());
    }

    /**
     * Verifies that when both a {@link FreeSlotQuery#setFromTime(Duration)} and
     * {@link FreeSlotQuery#setToTime(Duration)} is specified, only free slots between those times are returned.
     */
    @Test
    public void testFindFreeSlotsWithFromToTimeRange() {
        Party schedule = createSchedule(null, null);
        Act act1 = createAppointment(getDatetime("2013-12-31 09:00:00"), getDatetime("2014-01-01 08:00:00"), schedule);
        Act act2 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:15:00"), schedule);
        Act act3 = createAppointment(getDatetime("2014-01-01 10:00:00"), getDatetime("2014-01-01 10:15:00"), schedule);
        Act act4 = createAppointment(getDatetime("2014-01-01 10:30:00"), getDatetime("2014-01-01 11:00:00"), schedule);
        save(act1, act2, act3, act4);

        FreeSlotQuery query = createQuery("2014-01-01", "2014-01-02", schedule);
        query.setFromTime(getTime("9:00"));
        query.setToTime(getTime("10:00"));
        query.setSchedules(schedule);
        Iterator<Slot> iterator = createIterator(query);
        checkSlot(iterator, schedule, "2014-01-01 09:15:00", "2014-01-01 10:00:00");
        assertFalse(iterator.hasNext());
    }

    /**
     * Verifies that when a schedule has start and end times, free slots are split over those times.
     */
    @Test
    public void testFreeSlotSplitsOverScheduleTimes() {
        Party schedule = createSchedule("09:00:00", "17:00:00");
        Iterator<Slot> query = createIterator("2014-01-01", "2014-01-03", schedule);
        checkSlot(query, schedule, "2014-01-01 09:00:00", "2014-01-01 17:00:00");
        checkSlot(query, schedule, "2014-01-02 09:00:00", "2014-01-02 17:00:00");
    }

    /**
     * Creates a new query.
     *
     * @param fromDate  the query from date
     * @param toDate    the query to date
     * @param schedules the schedules to query
     * @return a new query
     */
    private FreeSlotQuery createQuery(String fromDate, String toDate, Entity... schedules) {
        FreeSlotQuery query = new FreeSlotQuery(getArchetypeService());
        query.setFromDate(getDate(fromDate));
        query.setToDate(getDate(toDate));
        query.setSchedules(schedules);
        return query;
    }

    /**
     * Creates a new query iterator.
     *
     * @param fromDate  the query from date
     * @param toDate    the query to date
     * @param schedules the schedules to query
     * @return a new query
     */
    private Iterator<Slot> createIterator(String fromDate, String toDate, Entity... schedules) {
        FreeSlotQuery query = createQuery(fromDate, toDate, schedules);
        return createIterator(query);
    }

    /**
     * Creates a new query iterator.
     *
     * @param query the query
     * @return the query iterator
     */
    private Iterator<Slot> createIterator(FreeSlotQuery query) {
        long start = System.currentTimeMillis();
        Iterator<Slot> iterator = query.query();
        long end = System.currentTimeMillis();
        System.out.println("Executed query in " + (end - start) + "ms");
        return iterator;
    }

    /**
     * Verifies that the next slot returned by the iterator matches that expected.
     *
     * @param iterator  the slot iterator
     * @param schedule  the expected schedule
     * @param startTime the expected slot start time
     * @param endTime   the expected slot end time
     */
    private void checkSlot(Iterator<Slot> iterator, Entity schedule, String startTime, String endTime) {
        checkSlot(iterator, schedule, getDatetime(startTime), getDatetime(endTime));
    }

    /**
     * Verifies that the next slot returned by the iterator matches that expected.
     *
     * @param iterator  the slot iterator
     * @param schedule  the expected schedule
     * @param startTime the expected slot start time
     * @param endTime   the expected slot end time
     */
    private void checkSlot(Iterator<Slot> iterator, Entity schedule, Date startTime, Date endTime) {
        assertTrue(iterator.hasNext());
        Slot slot = iterator.next();
        assertEquals(schedule.getId(), slot.getSchedule());
        checkDate(startTime, slot.getStartTime());
        checkDate(endTime, slot.getEndTime());
    }

    /**
     * Verifies that a date matches that expected,
     *
     * @param expected the expected date
     * @param actual   the actual date
     */
    private void checkDate(Date expected, Date actual) {
        assertEquals("expected=" + expected + ", actual=" + actual, 0, DateRules.compareTo(expected, actual));
    }

    private Duration getTime(String time) {
        PeriodFormatterBuilder builder = new PeriodFormatterBuilder().appendHours().appendLiteral(":").appendMinutes();
        PeriodFormatter formatter = builder.toFormatter();
        Period period = formatter.parsePeriod(time);
        return period.toStandardDuration();
    }

    /**
     * Creates a schedule.
     *
     * @param startTime the schedule start time. May be {@code null}
     * @param endTime   the schedule end time. May be {@code null}
     * @return the schedule
     */
    private Party createSchedule(String startTime, String endTime) {
        Party schedule = ScheduleTestHelper.createSchedule();
        IMObjectBean bean = new IMObjectBean(schedule);
        bean.setValue("startTime", (startTime != null) ? Time.valueOf(startTime) : null);
        bean.setValue("endTime", (endTime != null) ? Time.valueOf(endTime) : null);
        bean.save();
        return schedule;
    }
}
