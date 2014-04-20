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
    public void testFindFreeSlots() {
        Party schedule1 = createSchedule(null, null);
        Party schedule2 = createSchedule(null, null);
        Act act1 = createAppointment(getDatetime("2013-12-31 09:00:00"), getDatetime("2014-01-01 08:00:00"), schedule1);
        Act act2 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:15:00"), schedule1);
        Act act3 = createAppointment(getDatetime("2014-01-01 10:00:00"), getDatetime("2014-01-01 10:15:00"), schedule1);
        Act act4 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:30:00"), schedule2);
        Act act5 = createAppointment(getDatetime("2014-01-01 09:45:00"), getDatetime("2014-01-01 10:30:00"), schedule2);
        save(act1, act2, act3, act4, act5);

        FreeSlotQuery query = new FreeSlotQuery(getArchetypeService());
        query.setFromDate(getDate("2014-01-01"));
        query.setToDate(getDate("2014-01-02"));
        query.setSchedules(schedule1, schedule2);
        long start = System.currentTimeMillis();
        Iterator<Slot> iterator = query.query();
        long end = System.currentTimeMillis();
        System.out.println("Executed query in " + (end - start) + "ms");
        checkNext(iterator, getDatetime("2014-01-01 08:00:00"), getDatetime("2014-01-01 09:00:00"));
        checkNext(iterator, getDatetime("2014-01-01 09:15:00"), getDatetime("2014-01-01 10:00:00"));
        checkNext(iterator, getDatetime("2014-01-01 09:30:00"), getDatetime("2014-01-01 09:45:00"));
        assertFalse(iterator.hasNext());
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

        FreeSlotQuery query = new FreeSlotQuery(getArchetypeService());
        query.setFromDate(getDate("2014-01-01"));
        query.setToDate(getDate("2014-01-02"));
        query.setSchedules(schedule1);
        long start = System.currentTimeMillis();
        Iterator<Slot> iterator = query.query();
        long end = System.currentTimeMillis();
        System.out.println("Executed query in " + (end - start) + "ms");
        checkNext(iterator, getDatetime("2014-01-01 09:15:00"), getDatetime("2014-01-01 10:00:00"));
        assertFalse(iterator.hasNext());
    }

    /**
     * Verifies that overlapping appointments are handled.
     */
    @Test
    public void testOverlappingAppointments() {
        Party schedule1 = createSchedule(null, null);
        Act act1 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:15:00"), schedule1);
        Act act2 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:30:00"), schedule1);
        Act act3 = createAppointment(getDatetime("2014-01-01 09:45:00"), getDatetime("2014-01-01 10:15:00"), schedule1);
        Act act4 = createAppointment(getDatetime("2014-01-01 10:00:00"), getDatetime("2014-01-01 10:30:00"), schedule1);
        save(act1, act2, act3, act4);

        FreeSlotQuery query = new FreeSlotQuery(getArchetypeService());
        query.setFromDate(getDate("2014-01-01"));
        query.setToDate(getDate("2014-01-02"));
        query.setSchedules(schedule1);
        long start = System.currentTimeMillis();
        Iterator<Slot> iterator = query.query();
        long end = System.currentTimeMillis();
        System.out.println("Executed query in " + (end - start) + "ms");
        checkNext(iterator, getDatetime("2014-01-01 09:30:00"), getDatetime("2014-01-01 09:45:00"));
        assertFalse(iterator.hasNext());
    }

    /**
     * Verifies that specifying a minimum slot size filters out slots too small.
     */
    @Test
    public void testMinSlotSize() {
        Party schedule1 = createSchedule(null, null);
        Act act1 = createAppointment(getDatetime("2013-12-31 09:00:00"), getDatetime("2014-01-01 08:00:00"), schedule1);
        Act act2 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:45:00"), schedule1);
        Act act3 = createAppointment(getDatetime("2014-01-01 10:00:00"), getDatetime("2014-01-01 10:15:00"), schedule1);
        Act act4 = createAppointment(getDatetime("2014-01-01 11:00:00"), getDatetime("2014-01-01 11:15:00"), schedule1);
        save(act1, act2, act3, act4);

        FreeSlotQuery query = new FreeSlotQuery(getArchetypeService());
        query.setFromDate(getDate("2014-01-01"));
        query.setToDate(getDate("2014-01-02"));
        query.setSchedules(schedule1);
        query.setMinSlotSize(30, DateUnits.MINUTES);

        Iterator<Slot> iterator = query.query();
        checkNext(iterator, getDatetime("2014-01-01 08:00:00"), getDatetime("2014-01-01 09:00:00"));
        checkNext(iterator, getDatetime("2014-01-01 10:15:00"), getDatetime("2014-01-01 11:00:00"));
        assertFalse(iterator.hasNext());
    }

    /**
     * Verifies that when a schedule has start and end times, free slots will adjusted.
     */
    @Test
    public void testFindFreeSlotsForLimitedScheduleTimes() {
        Party schedule1 = createSchedule("09:00:00", "17:00:00");
        Act act1 = createAppointment(getDatetime("2013-12-31 09:00:00"), getDatetime("2014-01-01 08:00:00"), schedule1);
        Act act2 = createAppointment(getDatetime("2014-01-01 09:30:00"), getDatetime("2014-01-01 09:45:00"), schedule1);
        Act act3 = createAppointment(getDatetime("2014-01-02 09:00:00"), getDatetime("2014-01-02 10:00:00"), schedule1);
        save(act1, act2, act3);

        FreeSlotQuery query = new FreeSlotQuery(getArchetypeService());
        query.setFromDate(getDate("2014-01-01"));
        query.setToDate(getDate("2014-01-03"));
        query.setSchedules(schedule1);

        Iterator<Slot> iterator = query.query();
        checkNext(iterator, getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:30:00"));
        checkNext(iterator, getDatetime("2014-01-01 09:45:00"), getDatetime("2014-01-01 17:00:00"));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testFindFreeSlotsWithFromTimeRange() {
        Party schedule1 = createSchedule(null, null);
        Act act1 = createAppointment(getDatetime("2013-12-31 09:00:00"), getDatetime("2014-01-01 08:00:00"), schedule1);
        Act act2 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:15:00"), schedule1);
        Act act3 = createAppointment(getDatetime("2014-01-01 10:00:00"), getDatetime("2014-01-01 10:15:00"), schedule1);
        Act act4 = createAppointment(getDatetime("2014-01-01 10:30:00"), getDatetime("2014-01-01 11:00:00"), schedule1);
        save(act1, act2, act3, act4);

        FreeSlotQuery query = new FreeSlotQuery(getArchetypeService());
        query.setFromDate(getDate("2014-01-01"));
        query.setToDate(getDate("2014-01-02"));
        query.setFromTime(getTime("09:30"));
        query.setSchedules(schedule1);
        long start = System.currentTimeMillis();
        Iterator<Slot> iterator = query.query();
        long end = System.currentTimeMillis();
        System.out.println("Executed query in " + (end - start) + "ms");
        checkNext(iterator, getDatetime("2014-01-01 09:15:00"), getDatetime("2014-01-01 10:00:00"));
        checkNext(iterator, getDatetime("2014-01-01 10:15:00"), getDatetime("2014-01-01 10:30:00"));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testFindFreeSlotsWithToTimeRange() {
        Party schedule1 = createSchedule(null, null);
        Act act1 = createAppointment(getDatetime("2013-12-31 09:00:00"), getDatetime("2014-01-01 08:00:00"), schedule1);
        Act act2 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:15:00"), schedule1);
        Act act3 = createAppointment(getDatetime("2014-01-01 10:00:00"), getDatetime("2014-01-01 10:15:00"), schedule1);
        Act act4 = createAppointment(getDatetime("2014-01-01 10:30:00"), getDatetime("2014-01-01 11:00:00"), schedule1);
        save(act1, act2, act3, act4);

        FreeSlotQuery query = new FreeSlotQuery(getArchetypeService());
        query.setFromDate(getDate("2014-01-01"));
        query.setToDate(getDate("2014-01-02"));
        query.setToTime(getTime("09:30"));
        query.setSchedules(schedule1);
        long start = System.currentTimeMillis();
        Iterator<Slot> iterator = query.query();
        long end = System.currentTimeMillis();
        System.out.println("Executed query in " + (end - start) + "ms");
        checkNext(iterator, getDatetime("2014-01-01 08:00:00"), getDatetime("2014-01-01 09:00:00"));
        checkNext(iterator, getDatetime("2014-01-01 09:15:00"), getDatetime("2014-01-01 10:00:00"));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testFindFreeSlotsWithFromToTimeRange() {
        Party schedule1 = createSchedule(null, null);
        Act act1 = createAppointment(getDatetime("2013-12-31 09:00:00"), getDatetime("2014-01-01 08:00:00"), schedule1);
        Act act2 = createAppointment(getDatetime("2014-01-01 09:00:00"), getDatetime("2014-01-01 09:15:00"), schedule1);
        Act act3 = createAppointment(getDatetime("2014-01-01 10:00:00"), getDatetime("2014-01-01 10:15:00"), schedule1);
        Act act4 = createAppointment(getDatetime("2014-01-01 10:30:00"), getDatetime("2014-01-01 11:00:00"), schedule1);
        save(act1, act2, act3, act4);

        FreeSlotQuery query = new FreeSlotQuery(getArchetypeService());
        query.setFromDate(getDate("2014-01-01"));
        query.setToDate(getDate("2014-01-02"));
        query.setFromTime(getTime("9:00"));
        query.setToTime(getTime("10:00"));
        query.setSchedules(schedule1);
        long start = System.currentTimeMillis();
        Iterator<Slot> iterator = query.query();
        long end = System.currentTimeMillis();
        System.out.println("Executed query in " + (end - start) + "ms");
        checkNext(iterator, getDatetime("2014-01-01 09:15:00"), getDatetime("2014-01-01 10:00:00"));
        assertFalse(iterator.hasNext());
    }

    private void checkNext(Iterator<Slot> iterator, Date startTime, Date endTime) {
        assertTrue(iterator.hasNext());
        Slot slot = iterator.next();
        checkDate(startTime, slot.getStartTime());
        checkDate(endTime, slot.getEndTime());
    }

    private void checkDate(Date expected, Date actual) {
        assertEquals("expected=" + expected + ", actual=" + actual, 0, DateRules.compareTo(expected, actual));
    }

    private Duration getTime(String time) {
        PeriodFormatter periodFormatter = new PeriodFormatterBuilder().appendHours().appendLiteral(":").appendMinutes().toFormatter();
        Period period = periodFormatter.parsePeriod(time);

        // return new Duration(new DateTime(Time.valueOf(time).getTime()).getMillisOfDay());
        return period.toStandardDuration();
    }

    private Party createSchedule(String startTime, String endTime) {
        Party schedule = ScheduleTestHelper.createSchedule();
        IMObjectBean bean = new IMObjectBean(schedule);
        bean.setValue("startTime", (startTime != null) ? Time.valueOf(startTime) : null);
        bean.setValue("endTime", (endTime != null) ? Time.valueOf(endTime) : null);
        return schedule;
    }
}
