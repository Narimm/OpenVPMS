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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.booking.impl;

import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.booking.domain.Range;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.user.User;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Base class for booking service tests dealing with date ranges.
 *
 * @author Tim Anderson
 */
public abstract class AbstractBookingServiceTest extends ArchetypeServiceTest {

    /**
     * Verifies ranges match those expected.
     *
     * @param ranges   the ranges
     * @param expected the expected ranges
     */
    void checkRanges(List<? extends Range> ranges, Range... expected) {
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
    void checkRange(Range range, String start, String end) {
        assertEquals(range.getStart(), TestHelper.getDatetime(start));
        assertEquals(range.getEnd(), TestHelper.getDatetime(end));
    }

    /**
     * Creates a new range.
     *
     * @param start the start of the range
     * @param end   the end of the range
     * @return a new range
     */
    Range createRange(String start, String end) {
        return new Range(TestHelper.getDatetime(start), TestHelper.getDatetime(end));
    }

    /**
     * Returns an ISO date/time, with the current timezone offset.
     *
     * @param date a date string, yyyy-mm-dd format
     * @return the ISO date/time
     */
    String getISODate(String date) {
        return getISODate(date, "00:00");
    }

    /**
     * Returns an ISO date/time, with the current timezone offset.
     *
     * @param date a date string, yyyy-mm-dd format
     * @param time a time string, hh:mm format
     * @return the ISO date/time
     */
    String getISODate(String date, String time) {
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

    /**
     * Creates a new practice location.
     *
     * @param onlineBooking if {@code true}, the location supports online booking
     * @return the location
     */
    Party createLocation(boolean onlineBooking) {
        Party location = TestHelper.createLocation();
        IMObjectBean bean = getBean(location);
        bean.setValue("onlineBooking", onlineBooking);
        bean.save();
        return location;
    }

    /**
     * Creates an appointment.
     *
     * @param startTime the appointment start time
     * @param endTime   the appointment end time
     * @param schedule  the schedule
     * @return a new appointment
     */
    Act createAppointment(String startTime, String endTime, Entity schedule) {
        return createAppointment(startTime, endTime, schedule, null);
    }

    /**
     * Creates an appointment.
     *
     * @param startTime the appointment start time
     * @param endTime   the appointment end time
     * @param schedule  the schedule
     * @param clinician the clinician. May be {@code null}
     * @return a new appointment
     */
    Act createAppointment(String startTime, String endTime, Entity schedule, User clinician) {
        Act appointment = ScheduleTestHelper.createAppointment(TestHelper.getDatetime(startTime),
                                                               TestHelper.getDatetime(endTime),
                                                               schedule);
        if (clinician != null) {
            getBean(appointment).setTarget("clinician", clinician);
        }
        save(appointment);
        return appointment;
    }

    /**
     * Creates a new schedule with 15 minute slots.
     *
     * @param startTime the schedule start time. May be {@code null}
     * @param endTime   the schedule end time. May be {@code null}
     * @return a new schedule
     */
    Entity createSchedule(String startTime, String endTime) {
        return createSchedule(startTime, endTime, 15, DateUnits.MINUTES);
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
    Entity createSchedule(String startTime, String endTime, int slotSize, DateUnits units) {
        Party location = TestHelper.createLocation();
        Entity schedule = ScheduleTestHelper.createSchedule(slotSize, units.toString(), 1, null, location);
        IMObjectBean bean = getBean(schedule);
        bean.setValue("startTime", startTime != null ? Time.valueOf(startTime) : null);
        bean.setValue("endTime", endTime != null ? Time.valueOf(endTime) : null);
        bean.setValue("onlineBooking", true);
        bean.save();
        return schedule;
    }

    /**
     * Creates a clinician.
     *
     * @param onlineBooking if {@code true}, the clinician is available for online booking
     * @return the clinician
     */
    User createClinician(boolean onlineBooking) {
        User clinician = TestHelper.createClinician(false);
        IMObjectBean bean = getBean(clinician);
        bean.setValue("onlineBooking", onlineBooking);
        bean.save();
        return clinician;
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
}
