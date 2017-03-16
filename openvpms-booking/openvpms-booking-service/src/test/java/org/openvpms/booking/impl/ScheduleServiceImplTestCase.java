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

package org.openvpms.booking.impl;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.AppointmentService;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.booking.api.ScheduleService;
import org.openvpms.booking.domain.FreeBusy;
import org.openvpms.booking.domain.Range;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link ScheduleServiceImpl}.
 *
 * @author Tim Anderson
 */
public class ScheduleServiceImplTestCase extends ArchetypeServiceTest {

    /**
     * The appointment rules.
     */
    private AppointmentRules rules;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        rules = new AppointmentRules(getArchetypeService());
    }

    /**
     * Verifies that there is a single free range for a 24 hour schedule with no appointments.
     */
    @Test
    public void testFreeDayFor24HourSchedule() {
        Entity schedule = createSchedule(null, null);
        ScheduleService service = new ScheduleServiceImpl(getArchetypeService(), createAppointmentService(), rules);
        List<Range> free = service.getFree(schedule.getId(), getISODate("2016-05-14"), getISODate("2016-05-15"),
                                           false);
        List<Range> busy = service.getBusy(schedule.getId(), getISODate("2016-05-14"), getISODate("2016-05-15"), false);
        FreeBusy freeBusy = service.getFreeBusy(schedule.getId(), getISODate("2016-05-14"), getISODate("2016-05-15"),
                                                false);
        assertEquals(1, free.size());
        assertEquals(0, busy.size());
        checkRange(free.get(0), "2016-05-14 00:00:00", "2016-05-15 00:00:00");
        assertEquals(1, freeBusy.getFree().size());
        assertEquals(0, freeBusy.getBusy().size());
        checkRange(freeBusy.getFree().get(0), "2016-05-14 00:00:00", "2016-05-15 00:00:00");
    }

    /**
     * Tests free/busy ranges for a 24 hour schedule.
     */
    @Test
    public void testScheduleAppointmentsFor24HourSchedule() {
        Entity schedule = createSchedule(null, null);
        ScheduleService service = new ScheduleServiceImpl(getArchetypeService(), createAppointmentService(), rules);
        createAppointment("2016-05-14 09:00:00", "2016-05-14 10:00:00", schedule);
        createAppointment("2016-05-14 10:00:00", "2016-05-14 11:00:00", schedule);
        createAppointment("2016-05-14 12:00:00", "2016-05-14 13:00:00", schedule);

        String from = getISODate("2016-05-14");
        String to = getISODate("2016-05-15");
        List<Range> free = service.getFree(schedule.getId(), from, to, false);
        Range free1 = createRange("2016-05-14 00:00:00", "2016-05-14 09:00:00");
        Range free2 = createRange("2016-05-14 11:00:00", "2016-05-14 12:00:00");
        Range free3 = createRange("2016-05-14 13:00:00", "2016-05-15 00:00:00");
        checkRanges(free, free1, free2, free3);

        List<Range> busy = service.getBusy(schedule.getId(), from, to, false);
        Range busy1 = createRange("2016-05-14 09:00:00", "2016-05-14 11:00:00");
        Range busy2 = createRange("2016-05-14 12:00:00", "2016-05-14 13:00:00");
        checkRanges(busy, busy1, busy2);

        FreeBusy freeBusy = service.getFreeBusy(schedule.getId(), from, to, false);
        checkRanges(freeBusy.getFree(), free1, free2, free3);
        checkRanges(freeBusy.getBusy(), busy1, busy2);
    }

    /**
     * Verifies that ranges are correctly split into slots.
     */
    @Test
    public void testSlots() {
        // create a schedule with 4 hours slots
        Entity schedule = createSchedule(null, null, 4, DateUnits.HOURS);
        ScheduleService service = new ScheduleServiceImpl(getArchetypeService(), createAppointmentService(), rules);
        createAppointment("2016-05-14 08:00:00", "2016-05-14 12:00:00", schedule);
        createAppointment("2016-05-14 16:00:00", "2016-05-14 20:00:00", schedule);
        String from = getISODate("2016-05-14");
        String to = getISODate("2016-05-15");

        List<Range> free = service.getFree(schedule.getId(), from, to, true);
        Range free1 = createRange("2016-05-14 00:00:00", "2016-05-14 04:00:00");
        Range free2 = createRange("2016-05-14 04:00:00", "2016-05-14 08:00:00");
        Range free3 = createRange("2016-05-14 12:00:00", "2016-05-14 16:00:00");
        Range free4 = createRange("2016-05-14 20:00:00", "2016-05-15 00:00:00");
        checkRanges(free, free1, free2, free3, free4);

        List<Range> busy = service.getBusy(schedule.getId(), from, to, true);
        Range busy1 = createRange("2016-05-14 08:00:00", "2016-05-14 12:00:00");
        Range busy2 = createRange("2016-05-14 16:00:00", "2016-05-14 20:00:00");
        checkRanges(busy, busy1, busy2);

        FreeBusy freeBusy = service.getFreeBusy(schedule.getId(), from, to, true);
        checkRanges(freeBusy.getFree(), free1, free2, free3, free4);
        checkRanges(freeBusy.getBusy(), busy1, busy2);
    }

    /**
     * Tests querying ranges where the range is the same as the schedule opening hours. Ranges should be truncated to
     * the from and to times.
     */
    @Test
    public void testQueryOpeningHoursRange() {
        Entity schedule = createSchedule("9:00:00", "17:00:00");
        ScheduleService service = new ScheduleServiceImpl(getArchetypeService(), createAppointmentService(), rules);
        createAppointment("2016-05-14 09:00:00", "2016-05-14 10:00:00", schedule);
        createAppointment("2016-05-14 10:00:00", "2016-05-14 11:00:00", schedule);
        createAppointment("2016-05-14 12:00:00", "2016-05-14 13:00:00", schedule);

        String from = getISODate("2016-05-14", "09:00");
        String to = getISODate("2016-05-14", "17:00");
        List<Range> free = service.getFree(schedule.getId(), from, to, false);
        Range free1 = createRange("2016-05-14 11:00:00", "2016-05-14 12:00:00");
        Range free2 = createRange("2016-05-14 13:00:00", "2016-05-14 17:00:00");
        checkRanges(free, free1, free2);

        List<Range> busy = service.getBusy(schedule.getId(), from, to, false);
        Range busy1 = createRange("2016-05-14 09:00:00", "2016-05-14 11:00:00");
        Range busy2 = createRange("2016-05-14 12:00:00", "2016-05-14 13:00:00");
        checkRanges(busy, busy1, busy2);

        FreeBusy freeBusy = service.getFreeBusy(schedule.getId(), from, to, false);
        checkRanges(freeBusy.getFree(), free1, free2);
        checkRanges(freeBusy.getBusy(), busy1, busy2);
    }

    /**
     * Tests the behaviour when querying outside of the schedule opening hours.
     * <p/>
     * Note: times outside this range are not considered busy.
     */
    @Test
    public void testQueryOutsideOpeningHoursRange() {
        Entity schedule = createSchedule("9:00:00", "17:00:00");
        ScheduleService service = new ScheduleServiceImpl(getArchetypeService(), createAppointmentService(), rules);
        createAppointment("2016-05-14 09:00:00", "2016-05-14 10:00:00", schedule);
        createAppointment("2016-05-14 10:00:00", "2016-05-14 11:00:00", schedule);
        createAppointment("2016-05-14 12:00:00", "2016-05-14 13:00:00", schedule);

        String from = getISODate("2016-05-14", "07:00");
        String to = getISODate("2016-05-14", "18:00");
        List<Range> free = service.getFree(schedule.getId(), from, to, false);
        Range free1 = createRange("2016-05-14 11:00:00", "2016-05-14 12:00:00");
        Range free2 = createRange("2016-05-14 13:00:00", "2016-05-14 17:00:00");
        checkRanges(free, free1, free2);

        List<Range> busy = service.getBusy(schedule.getId(), from, to, false);
        Range busy1 = createRange("2016-05-14 09:00:00", "2016-05-14 11:00:00");
        Range busy2 = createRange("2016-05-14 12:00:00", "2016-05-14 13:00:00");
        checkRanges(busy, busy1, busy2);

        FreeBusy freeBusy = service.getFreeBusy(schedule.getId(), from, to, false);
        checkRanges(free, free1, free2);
        checkRanges(freeBusy.getBusy(), busy1, busy2);
    }

    /**
     * Tests the behaviour when the query range intersects appointments. The times should be truncated.
     */
    @Test
    public void testQueryIntersectsAppointments() {
        Entity schedule = createSchedule("9:00:00", "17:00:00");
        ScheduleService service = new ScheduleServiceImpl(getArchetypeService(), createAppointmentService(), rules);
        createAppointment("2016-05-14 09:00:00", "2016-05-14 10:00:00", schedule);
        createAppointment("2016-05-14 10:00:00", "2016-05-14 11:00:00", schedule);
        createAppointment("2016-05-14 12:00:00", "2016-05-14 13:00:00", schedule);
        createAppointment("2016-05-14 13:00:00", "2016-05-14 14:00:00", schedule);

        String from = getISODate("2016-05-14", "09:30");
        String to = getISODate("2016-05-14", "13:30");
        List<Range> free = service.getFree(schedule.getId(), from, to, false);
        Range free1 = createRange("2016-05-14 11:00:00", "2016-05-14 12:00:00");
        checkRanges(free, free1);

        List<Range> busy = service.getBusy(schedule.getId(), from, to, false);
        Range busy1 = createRange("2016-05-14 09:30:00", "2016-05-14 11:00:00");
        Range busy2 = createRange("2016-05-14 12:00:00", "2016-05-14 13:30:00");
        checkRanges(busy, busy1, busy2);

        FreeBusy freeBusy = service.getFreeBusy(schedule.getId(), from, to, false);
        checkRanges(free, free1);
        checkRanges(freeBusy.getBusy(), busy1, busy2);
    }

    /**
     * Verifies that a time slot appears as free when an appointment is cancelled.
     */
    @Test
    public void testCancelAppointment() {
        Entity schedule = createSchedule("9:00:00", "17:00:00");
        ScheduleService service = new ScheduleServiceImpl(getArchetypeService(), createAppointmentService(), rules);
        Act appointment = createAppointment("2016-05-14 09:00:00", "2016-05-14 10:00:00", schedule);

        String from = getISODate("2016-05-14", "09:00");
        String to = getISODate("2016-05-14", "10:00");
        Range range = createRange("2016-05-14 09:00:00", "2016-05-14 10:00:00");

        List<Range> free = service.getFree(schedule.getId(), from, to, false);
        checkRanges(free);
        List<Range> busy = service.getBusy(schedule.getId(), from, to, false);
        checkRanges(busy, range);

        appointment.setStatus(ActStatus.CANCELLED);
        save(appointment);

        free = service.getFree(schedule.getId(), from, to, false);
        checkRanges(free, range);
        busy = service.getBusy(schedule.getId(), from, to, false);
        checkRanges(busy);
    }

    /**
     * Tests online booking times.
     */
    @Test
    public void testOnlineBookingTimes() {
        Entity schedule = createSchedule(null, null);
        Entity times = (Entity) create("entity.onlineBookingTimesType");
        IMObjectBean timesBean = new IMObjectBean(times);
        IMObjectBean bean = new IMObjectBean(schedule);
        bean.addNodeTarget("onlineBookingTimes", times);
        timesBean.setValue("name", "Z Online Booking Times");
        timesBean.setValue("monOpen", true);
        timesBean.setValue("monStartTime", Time.valueOf("09:00:00"));
        timesBean.setValue("monEndTime", Time.valueOf("18:00:00"));
        timesBean.setValue("tueOpen", false);
        timesBean.setValue("wedOpen", true);
        timesBean.setValue("wedStartTime", Time.valueOf("10:00:00"));
        timesBean.setValue("wedEndTime", Time.valueOf("17:00:00"));
        save(schedule, times);

        ScheduleService service = new ScheduleServiceImpl(getArchetypeService(), createAppointmentService(), rules);
        String from = getISODate("2016-08-22", "00:00");
        String to = getISODate("2016-08-25", "00:00");
        List<Range> free = service.getFree(schedule.getId(), from, to, false);
        checkRanges(free, createRange("2016-08-22 09:00:00", "2016-08-22 18:00:00"),
                    createRange("2016-08-24 10:00:00", "2016-08-24 17:00:00"));
    }

    /**
     * Tests online booking times where appointments overlap the start of the online booking range.
     */
    @Test
    public void testOnlineBookingTimesAppointmentsOverlapStartOfRange() {
        Entity schedule = createSchedule("07:30:00", "23:59:00");
        Entity times = createOnlineBookingTimes("09:00:00", "19:00:00");
        IMObjectBean bean = new IMObjectBean(schedule);
        bean.addNodeTarget("onlineBookingTimes", times);
        save(schedule, times);

        createAppointment("2016-06-14 07:30:00", "2016-06-14 18:15:00", schedule);
        createAppointment("2016-06-14 18:45:00", "2016-06-14 19:45:00", schedule);
        createAppointment("2016-06-14 20:00:00", "2016-06-14 20:15:00", schedule);
        createAppointment("2016-06-14 20:30:00", "2016-06-14 21:00:00", schedule);

        ScheduleService service = new ScheduleServiceImpl(getArchetypeService(), createAppointmentService(), rules);
        String from = getISODate("2016-06-14", "00:00");
        String to = getISODate("2016-06-15", "00:00");
        List<Range> free = service.getFree(schedule.getId(), from, to, false);
        checkRanges(free, createRange("2016-06-14 18:15:00", "2016-06-14 18:45:00"));
    }

    /**
     * Tests online booking times where appointments overlap the end of the online booking range.
     */
    @Test
    public void testOnlineBookingTimesAppointmentsOverlapEndOfRange() {
        Entity schedule = createSchedule("07:30:00", "21:00:00");
        Entity times = createOnlineBookingTimes("09:00:00", "19:00:00");
        IMObjectBean bean = new IMObjectBean(schedule);
        bean.addNodeTarget("onlineBookingTimes", times);
        save(schedule, times);

        createAppointment("2016-06-14 16:00:00", "2016-06-14 20:00:00", schedule);

        ScheduleService service = new ScheduleServiceImpl(getArchetypeService(), createAppointmentService(), rules);
        String from = getISODate("2016-06-14", "00:00");
        String to = getISODate("2016-06-15", "00:00");
        List<Range> free = service.getFree(schedule.getId(), from, to, false);
        checkRanges(free, createRange("2016-06-14 09:00:00", "2016-06-14 16:00:00"));
    }

    /**
     * Tests online booking times with multi-day appointments.
     */
    @Test
    public void testMultiDayAppointmentWithOnlineBookingTime() {
        Entity schedule = createSchedule(null, null);
        Entity times = createOnlineBookingTimes("09:00:00", "19:00:00");
        IMObjectBean bean = new IMObjectBean(schedule);
        bean.addNodeTarget("onlineBookingTimes", times);
        save(schedule, times);

        createAppointment("2016-06-13 09:00:00", "2016-06-14 10:00:00", schedule);
        createAppointment("2016-06-14 15:00:00", "2016-06-15 11:00:00", schedule);

        ScheduleService service = new ScheduleServiceImpl(getArchetypeService(), createAppointmentService(), rules);
        String from = getISODate("2016-06-14", "00:00"); // query 2 days
        String to = getISODate("2016-06-16", "00:00");
        List<Range> free = service.getFree(schedule.getId(), from, to, false);
        checkRanges(free, createRange("2016-06-14 10:00:00", "2016-06-14 15:00:00"),
                    createRange("2016-06-15 11:00:00", "2016-06-15 19:00:00"));
    }

    /**
     * Verifies that if a query spans multiple days, the correct results are returned.
     * <p/>
     * This simulates a call being made from a time zone offset +1:30 to the locale time zone.
     */
    @Test
    public void testQueryPartialMultiDateRange() {
        Entity schedule = createSchedule("9:00:00", "17:00:00");
        ScheduleService service = new ScheduleServiceImpl(getArchetypeService(), createAppointmentService(), rules);
        createAppointment("2016-05-14 09:00:00", "2016-05-14 10:00:00", schedule);
        createAppointment("2016-05-14 10:00:00", "2016-05-14 11:00:00", schedule);
        createAppointment("2016-05-14 12:00:00", "2016-05-14 13:00:00", schedule);
        createAppointment("2016-05-14 13:00:00", "2016-05-14 14:00:00", schedule);

        String from = getISODate("2016-05-13", "22:30");
        String to = getISODate("2016-05-14", "22:30");
        List<Range> free = service.getFree(schedule.getId(), from, to, false);
        Range free1 = createRange("2016-05-14 11:00:00", "2016-05-14 12:00:00");
        Range free2 = createRange("2016-05-14 14:00:00", "2016-05-14 17:00:00");
        checkRanges(free, free1, free2);

        List<Range> busy = service.getBusy(schedule.getId(), from, to, false);
        Range busy1 = createRange("2016-05-14 09:00:00", "2016-05-14 11:00:00");
        Range busy2 = createRange("2016-05-14 12:00:00", "2016-05-14 14:00:00");
        checkRanges(busy, busy1, busy2);

        FreeBusy freeBusy = service.getFreeBusy(schedule.getId(), from, to, false);
        checkRanges(freeBusy.getFree(), free1, free2);
        checkRanges(freeBusy.getBusy(), busy1, busy2);
    }

    /**
     * Creates an appointment.
     *
     * @param startTime the appointment start time
     * @param endTime   the appointment end time
     * @param schedule  the schedule
     * @return a new appointment
     */
    private Act createAppointment(String startTime, String endTime, Entity schedule) {
        Act appointment = ScheduleTestHelper.createAppointment(TestHelper.getDatetime(startTime),
                                                               TestHelper.getDatetime(endTime),
                                                               schedule);
        save(appointment);
        return appointment;
    }

    /**
     * Verifies ranges match those expected.
     *
     * @param ranges   the ranges
     * @param expected the expected ranges
     */
    private void checkRanges(List<Range> ranges, Range... expected) {
        assertEquals(expected.length, ranges.size());
        for (int i = 0; i < expected.length; ++i) {
            checkRange(expected[i], ranges.get(i));
        }
    }

    /**
     * Verifies a range matches that expected.
     *
     * @param range the range
     * @param start the expected start time
     * @param end   the expected end time
     */
    private void checkRange(Range range, String start, String end) {
        assertEquals(range.getStart(), TestHelper.getDatetime(start));
        assertEquals(range.getEnd(), TestHelper.getDatetime(end));
    }

    /**
     * Verifies a range matches that expected.
     *
     * @param expected the expected range
     * @param range    the range
     */
    private void checkRange(Range expected, Range range) {
        assertEquals(expected.getStart(), range.getStart());
        assertEquals(expected.getEnd(), range.getEnd());
    }

    /**
     * Creates a new range.
     *
     * @param start the start of the range
     * @param end   the end of the range
     * @return a new range
     */
    private Range createRange(String start, String end) {
        return new Range(TestHelper.getDatetime(start), TestHelper.getDatetime(end));
    }

    /**
     * Creates a new {@link org.openvpms.archetype.rules.workflow.ScheduleService}.
     *
     * @return the new service
     */
    protected AppointmentService createAppointmentService() {
        return new AppointmentService(getArchetypeService(), applicationContext.getBean(ILookupService.class),
                                      ScheduleTestHelper.createCache(30));
    }

    /**
     * Creates a new schedule with 15 minute slots.
     *
     * @param startTime the schedule start time. May be {@code null}
     * @param endTime   the schedule end time. May be {@code null}
     * @return a new schedule
     */
    private Entity createSchedule(String startTime, String endTime) {
        return createSchedule(startTime, endTime, 15, DateUnits.MINUTES);
    }

    /**
     * Creates monday-saturday online booking times.
     *
     * @param startTime the start time for each day
     * @param endTime   the end time for each day
     * @return the times
     */
    private Entity createOnlineBookingTimes(String startTime, String endTime) {
        Entity times = (Entity) create("entity.onlineBookingTimesType");
        Date start = TestHelper.getDatetime("1970-01-01 " + startTime);
        Date end = TestHelper.getDatetime("1970-01-01 " + endTime);
        IMObjectBean timesBean = new IMObjectBean(times);
        timesBean.setValue("name", "Z Online Booking Times");
        timesBean.setValue("monOpen", true);
        timesBean.setValue("monStartTime", start);
        timesBean.setValue("monEndTime", end);
        timesBean.setValue("tueOpen", true);
        timesBean.setValue("tueStartTime", start);
        timesBean.setValue("tueEndTime", end);
        timesBean.setValue("wedOpen", true);
        timesBean.setValue("wedStartTime", start);
        timesBean.setValue("wedEndTime", end);
        timesBean.setValue("thuOpen", true);
        timesBean.setValue("thuStartTime", start);
        timesBean.setValue("thuEndTime", end);
        timesBean.setValue("thuOpen", true);
        timesBean.setValue("friStartTime", start);
        timesBean.setValue("friEndTime", end);
        timesBean.setValue("satStartTime", start);
        timesBean.setValue("satEndTime", end);
        return times;
    }

    /**
     * Creates a new schedule.
     *
     * @param startTime the schedule start time. May be {@code null}
     * @param endTime   the schedule end time. May be {@code null}
     * @param slotSize  the slot size
     * @param units     the slot size units
     * @return a new schedule
     */
    private Entity createSchedule(String startTime, String endTime, int slotSize, DateUnits units) {
        Party location = TestHelper.createLocation();
        Entity schedule = ScheduleTestHelper.createSchedule(slotSize, units.toString(), 1, null, location);
        IMObjectBean bean = new IMObjectBean(schedule);
        bean.setValue("startTime", startTime != null ? Time.valueOf(startTime) : null);
        bean.setValue("endTime", endTime != null ? Time.valueOf(endTime) : null);
        bean.setValue("onlineBooking", true);
        bean.save();
        return schedule;
    }

    /**
     * Returns an ISO date/time, with the current timezone offset.
     *
     * @param date a date string, yyyy-mm-dd format
     * @return the ISO date/time
     */
    private String getISODate(String date) {
        return getISODate(date, "00:00");
    }

    /**
     * Returns an ISO date/time, with the current timezone offset.
     *
     * @param date a date string, yyyy-mm-dd format
     * @param time a time string, hh:mm format
     * @return the ISO date/time
     */
    private String getISODate(String date, String time) {
        Date value = TestHelper.getDate(date);
        TimeZone tz = TimeZone.getDefault();
        int offset = tz.getOffset(value.getTime()); // to allow for daylight savings
        long hours = TimeUnit.MILLISECONDS.toHours(offset);
        long minutes = Math.abs(TimeUnit.MILLISECONDS.toMinutes(offset) - TimeUnit.HOURS.toMinutes(hours));
        StringBuilder result = new StringBuilder();
        result.append(date).append('T').append(time).append(":00");
        if (hours > 0) {
            result.append('+');
        }
        result.append(String.format("%d:%02d", hours, minutes));
        return result.toString();
    }

}
