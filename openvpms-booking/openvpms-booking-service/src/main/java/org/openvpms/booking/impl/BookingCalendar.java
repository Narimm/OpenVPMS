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

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.AppointmentService;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.archetype.rules.workflow.roster.RosterService;
import org.openvpms.booking.domain.Range;
import org.openvpms.booking.domain.ScheduleRange;
import org.openvpms.booking.domain.UserFreeBusy;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Calendar to determine the free/busy times for users at a particular location, using their roster and appointments,
 * for the purposes of online booking.
 *
 * @author Tim Anderson
 */
public class BookingCalendar {

    /**
     * The roster service.
     */
    private final RosterService rosterService;

    /**
     * The appointment service.
     */
    private final AppointmentService appointmentService;

    /**
     * Constructs a {@link BookingCalendar}.
     *
     * @param rosterService      the roster service
     * @param appointmentService the appointment service
     */
    public BookingCalendar(RosterService rosterService, AppointmentService appointmentService) {
        this.rosterService = rosterService;
        this.appointmentService = appointmentService;
    }

    /**
     * Returns free time ranges for a user between two dates, for a particular location.
     *
     * @param user     the user
     * @param location the practice location
     * @param from     the start of the date range, in ISO date/time format
     * @param to       the end of the date range, in ISO date/time format
     * @return the free time ranges
     */
    public List<ScheduleRange> getFree(User user, Party location, String from, String to) {
        List<ScheduleRange> free = new ArrayList<>();
        query(location, user, from, to, free, null);
        return free;
    }

    /**
     * Returns busy time ranges for a user between two dates, for a particular location.
     *
     * @param user     the user
     * @param location the practice location
     * @param from     the start of the date range, in ISO date/time format
     * @param to       the end of the date range, in ISO date/time format
     * @return the busy time ranges
     */
    public List<ScheduleRange> getBusy(User user, Party location, String from, String to) {
        List<ScheduleRange> busy = new ArrayList<>();
        query(location, user, from, to, null, busy);
        return busy;
    }

    /**
     * Returns free and busy time ranges for a user between two dates, for a particular location.
     *
     * @param user     the user
     * @param location the practice location
     * @param from     the start of the date range, in ISO date/time format
     * @param to       the end of the date range, in ISO date/time format
     * @return the free and busy time ranges
     */
    public UserFreeBusy getFreeBusy(User user, Party location, String from, String to) {
        List<ScheduleRange> free = new ArrayList<>();
        List<ScheduleRange> busy = new ArrayList<>();
        query(location, user, from, to, free, busy);
        return new UserFreeBusy(user.getId(), free, busy);
    }

    /**
     * Queries free/busy ranges between the from and to dates for a user.
     *
     * @param location the practice location
     * @param user     the user
     * @param from     the from date
     * @param to       the to date
     * @param free     the free ranges to add to, or {@code null} if they aren't being queried
     * @param busy     the busy ranges to add to, or {@code null} if they aren't being queried
     */
    private void query(Party location, User user, String from, String to, List<ScheduleRange> free,
                       List<ScheduleRange> busy) {
        Date fromTime = DateHelper.getDate("from", from);
        Date toTime = DateHelper.getDate("to", to);

        Map<Long, List<ScheduleRange>> roster = getRosterEvents(user, location, fromTime, toTime);
        List<Times> allAppointments = appointmentService.getAppointmentsForClinician(user, fromTime, toTime);
        allAppointments = new ArrayList<>(allAppointments);
        Collections.sort(allAppointments);

        for (Date date = fromTime; date.compareTo(toTime) < 0; date = DateRules.getDate(date, 1, DateUnits.DAYS)) {
            Date startTime = DateRules.getDate(date);
            Date endTime = DateRules.getNextDate(startTime);
            Date min = DateRules.max(startTime, fromTime);
            Date max = DateRules.min(endTime, toTime);

            List<ScheduleRange> appointments = getAppointments(date, allAppointments);
            for (Map.Entry<Long, List<ScheduleRange>> entry : roster.entrySet()) {
                List<ScheduleRange> events = getRoster(date, entry.getValue());
                addFreeBusy(entry.getKey(), events, appointments, min, max, free, busy);
            }
        }
    }

    /**
     * Returns the roster events for a user at a practice location, between the specified date, keyed on
     * schedule identifier.
     *
     * @param user     the user
     * @param location the practice location
     * @param from     the from date
     * @param to       the to date
     * @return the roster events
     */
    private Map<Long, List<ScheduleRange>> getRosterEvents(User user, Party location, Date from, Date to) {
        List<RosterService.UserEvent> roster = rosterService.getUserEvents(user, location, from, to);
        Map<Long, List<ScheduleRange>> result = new LinkedHashMap<>();
        for (RosterService.UserEvent event : roster) {
            for (long scheduleId : getSchedules(event.getArea())) {
                List<ScheduleRange> list = result.computeIfAbsent(scheduleId, k -> new ArrayList<>());
                list.add(createRange(scheduleId, event.getStartTime(), event.getEndTime()));
            }
        }
        return result;
    }

    /**
     * Returns the schedule identifiers associated with a roster area.
     *
     * @param area the roster area reference
     * @return the schedule identifiers
     */
    private List<Long> getSchedules(Reference area) {
        List<Long> result = new ArrayList<>();
        List<Reference> schedules = rosterService.getSchedules(area);
        for (Reference schedule : schedules) {
            result.add(schedule.getId());
        }
        if (result.size() > 1) {
            result.sort(null);
        }
        return result;
    }

    /**
     * Returns date ranges for all appointments falling on a date.
     *
     * @param date            the date
     * @param allAppointments all appointments for the clinician
     * @return the date ranges
     */
    private List<ScheduleRange> getAppointments(Date date, List<Times> allAppointments) {
        List<ScheduleRange> result = new ArrayList<>();
        Date min = DateRules.getDate(date);
        Date max = DateRules.getNextDate(min);
        for (Times appointment : allAppointments) {
            addRange(result, appointment.getStartTime(), appointment.getEndTime(), min, max, -1);
        }
        return result;
    }

    /**
     * Collects free and busy ranges.
     *
     * @param scheduleId   the schedule identifier
     * @param events       the roster events for the user on the date for the schedule
     * @param appointments the appointments for the user on the date
     * @param min          the minimum date/time. Any range prior to this should be discarded or truncated if it
     *                     overlaps
     * @param max          the maximum date/time. Any range after to this should be discarded or truncated if it
     *                     overlaps
     * @param free         the free ranges to add to, or {@code null} if they aren't being determined
     * @param busy         the busy ranges to add to, or {@code null} if they aren't being determined
     */
    private void addFreeBusy(long scheduleId, List<ScheduleRange> events, List<ScheduleRange> appointments, Date min,
                             Date max, List<ScheduleRange> free, List<ScheduleRange> busy) {
        Date freeStart = null;
        Date freeEnd = null;
        Date busyStart = min;
        Date busyEnd = max;
        for (ScheduleRange event : events) {
            Date eventStart = event.getStart();
            Date eventEnd = event.getEnd();
            if (busy != null) {
                if (DateRules.compareTo(eventStart, busyStart) > 0) {
                    addRange(busy, busyStart, eventStart, min, max, scheduleId);
                }
            }
            if (free != null) {
                if (freeStart == null || DateRules.compareTo(eventStart, freeEnd) > 0) {
                    if (freeStart != null) {
                        addRange(free, freeStart, freeEnd, min, max, scheduleId);
                    }
                    freeStart = eventStart;
                    freeEnd = eventEnd;
                } else if (DateRules.compareTo(eventEnd, freeEnd) > 0) {
                    freeEnd = eventEnd;
                }
            }
            if (busy != null) {
                if (DateRules.compareTo(eventEnd, busyStart) > 0) {
                    busyStart = eventEnd;
                }
                if (busyEnd != null && DateRules.compareTo(eventEnd, busyEnd) > 0) {
                    busyEnd = null;
                }
            }
        }
        if (free != null) {
            if (freeStart != null) {
                addRange(free, freeStart, freeEnd, min, max, scheduleId);
            }
            subtractAppointments(scheduleId, free, appointments);
        }
        if (busy != null) {
            if (busyEnd != null) {
                addRange(busy, busyStart, busyEnd, min, max, scheduleId);
            }
            addAppointments(scheduleId, busy, appointments);
        }
    }

    /**
     * Subtracts appointments from the free ranges.
     *
     * @param scheduleId   the schedule identifier
     * @param free         the free ranges
     * @param appointments the appointments
     */
    private void subtractAppointments(long scheduleId, List<ScheduleRange> free, List<ScheduleRange> appointments) {
        for (Range appointment : appointments) {
            ListIterator<ScheduleRange> iterator = free.listIterator();
            Date appointmentStart = appointment.getStart();
            Date appointmentEnd = appointment.getEnd();
            while (iterator.hasNext()) {
                Range range = iterator.next();
                Date freeStart = range.getStart();
                Date freeEnd = range.getEnd();
                if (appointmentEnd.compareTo(freeStart) <= 0) {
                    // appointment before free ranges, so skip to next appointment
                    break;
                } else if (appointmentStart.compareTo(freeEnd) < 0) {
                    int startToStart = appointmentStart.compareTo(freeStart);
                    int endToEnd = appointmentEnd.compareTo(freeEnd);
                    if (startToStart <= 0 && endToEnd < 0) {
                        // appointment overlaps start of free range
                        iterator.set(createRange(scheduleId, appointmentEnd, freeEnd));
                    } else if (startToStart <= 0) {  // endToEnd >= 0
                        // appointment covers free range
                        iterator.remove();
                    } else if (endToEnd < 0) {       // startToStart > 0
                        // appointment is within free range
                        iterator.set(createRange(scheduleId, freeStart, appointmentStart));
                        iterator.add(createRange(scheduleId, appointmentEnd, freeEnd));
                    } else {
                        // appointment overlaps end of free range
                        iterator.set(createRange(scheduleId, freeStart, appointmentStart));
                    }
                }
            }
        }
    }

    /**
     * Adds any appointments to the busy range.
     * <p/>
     * This assumes that the user cannot work on multiple appointments at once so if a user is rostered to multiple
     * schedules, an appointment's times will be added to each.
     *
     * @param scheduleId   the schedule identifier
     * @param busy         the busy range
     * @param appointments the appointments to add
     */
    private void addAppointments(long scheduleId, List<ScheduleRange> busy, List<ScheduleRange> appointments) {
        boolean modified = false;
        for (ScheduleRange appointment : appointments) {
            ListIterator<ScheduleRange> iterator = busy.listIterator();
            Date appointmentStart = appointment.getStart();
            Date appointmentEnd = appointment.getEnd();
            boolean found = false;
            while (iterator.hasNext()) {
                ScheduleRange range = iterator.next();
                Date busyStart = range.getStart();
                Date busyEnd = range.getEnd();
                if (DateRules.intersects(appointmentStart, appointmentEnd, busyStart, busyEnd)) {
                    // expand the range to include the appointment
                    iterator.set(createRange(range.getSchedule(), DateRules.min(appointmentStart, busyStart), DateRules.max(appointmentEnd, busyEnd)));
                    found = true;
                    modified = true;
                }
            }
            if (!found) {
                // insert the appointment into the busy range
                ScheduleRange range = createRange(scheduleId, appointmentStart, appointmentEnd);
                int index = Collections.binarySearch(busy, range,
                                                     (o1, o2) -> DateRules.compareTo(o1.getStart(), o2.getStart()));
                if (index < 0) {
                    // new value to insert
                    index = -index - 1;
                }
                busy.add(index, range);
                modified = true;
            }
        }
        if (modified) {
            // merge overlapping ranges
            List<ScheduleRange> result = new ArrayList<>();
            Date start = null;
            Date end = null;

            for (ScheduleRange range : busy) {
                if (start == null) {
                    start = range.getStart();
                    end = range.getEnd();
                } else if (end.compareTo(range.getStart()) >= 0) {
                    end = DateRules.max(range.getEnd(), end);
                } else {
                    result.add(createRange(scheduleId, start, end));
                    start = range.getStart();
                    end = range.getEnd();
                }
            }
            result.add(createRange(scheduleId, start, end));
            busy.clear();  // TODO
            busy.addAll(result);
        }
    }

    /**
     * Returns the roster events for a user for date.
     *
     * @param date   the date
     * @param events all events for the user for a schedule
     * @return the events
     */
    private List<ScheduleRange> getRoster(Date date, List<ScheduleRange> events) {
        List<ScheduleRange> result = new ArrayList<>();
        Date min = DateRules.getDate(date);
        Date max = DateRules.getNextDate(min);
        for (ScheduleRange event : events) {
            addRange(result, event.getStart(), event.getEnd(), min, max, event.getSchedule());
        }
        return result;
    }

    /**
     * Adds a range, if it intersects the min..max range. If it overlaps, it will be truncated.
     *
     * @param ranges   the ranges to add to
     * @param from     the start of the range
     * @param to       the end of the range
     * @param min      the minimum
     * @param max      the maximum
     * @param schedule the schedule that the range is for
     */
    private void addRange(List<ScheduleRange> ranges, Date from, Date to, Date min, Date max, long schedule) {
        if (DateRules.compareTo(to, min) >= 0) {
            if (DateRules.compareTo(from, min) <= 0) {
                from = min;
            }
            if (DateRules.compareTo(from, max) < 0) {
                if (DateRules.compareTo(to, max) > 0) {
                    to = max;
                }
                if (from.compareTo(to) < 0) {
                    ranges.add(createRange(schedule, from, to));
                }
            }
        }
    }

    /**
     * Creates a schedule range.
     *
     * @param schedule the schedule
     * @param from     the start time
     * @param to       the end time
     * @return a new range
     */
    private ScheduleRange createRange(long schedule, Date from, Date to) {
        from = DateHelper.convert(from); // make sure Timestamps are converted to Dates in the local timezone
        to = DateHelper.convert(to);
        return new ScheduleRange(schedule, from, to);
    }

}
