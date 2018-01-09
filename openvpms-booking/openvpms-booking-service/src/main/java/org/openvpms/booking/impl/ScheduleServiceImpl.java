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

package org.openvpms.booking.impl;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.ISODateTimeFormat;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.AppointmentService;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.booking.api.ScheduleService;
import org.openvpms.booking.domain.AppointmentType;
import org.openvpms.booking.domain.FreeBusy;
import org.openvpms.booking.domain.Range;
import org.openvpms.booking.domain.Schedule;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.system.common.util.PropertySet;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Service for querying appointment schedules.
 *
 * @author Tim Anderson
 */
@Component
public class ScheduleServiceImpl implements ScheduleService {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The appointment service.
     */
    private final AppointmentService appointments;

    /**
     * The appointment rules.
     */
    private final AppointmentRules rules;

    /**
     * Creates a {@link ScheduleServiceImpl}.
     *
     * @param service      the archetype service
     * @param appointments the appointment service
     * @param rules        the appointment rules
     */
    public ScheduleServiceImpl(IArchetypeService service, AppointmentService appointments, AppointmentRules rules) {
        this.service = service;
        this.appointments = appointments;
        this.rules = rules;
    }

    /**
     * Returns a schedule given its identifier.
     *
     * @param scheduleId the schedule identifier
     * @return the schedule
     */
    @Override
    public Schedule getSchedule(long scheduleId) {
        Entity schedule = getScheduleEntity(scheduleId);
        int slotSize = rules.getSlotSize(schedule);
        return new Schedule(schedule.getId(), schedule.getName(), slotSize);
    }

    /**
     * Returns free time ranges for a schedule between two dates.
     *
     * @param scheduleId the schedule identifier
     * @param from       the start of the date range, in ISO date/time format
     * @param to         the end of the date range, in ISO date/time format
     * @param slots      if {@code true}, split ranges into slots
     * @return the free time ranges
     */
    @Override
    public List<Range> getFree(long scheduleId, String from, String to, boolean slots) {
        List<Range> free = new ArrayList<>();
        query(scheduleId, from, to, free, null, slots);
        return free;
    }

    /**
     * Returns busy time ranges for a schedule between two dates.
     *
     * @param scheduleId the schedule identifier
     * @param from       the start of the date range, in ISO date/time format
     * @param to         the end of the date range, in ISO date/time format
     * @param slots      if {@code true}, split ranges into slots
     * @return the busy time ranges
     */
    @Override
    public List<Range> getBusy(long scheduleId, String from, String to, boolean slots) {
        List<Range> busy = new ArrayList<>();
        query(scheduleId, from, to, null, busy, slots);
        return busy;
    }

    /**
     * Returns free and busy time ranges for a schedule between two dates.
     *
     * @param scheduleId the schedule identifier
     * @param from       the start of the date range, in ISO date/time format
     * @param to         the end of the date range, in ISO date/time format
     * @param slots      if {@code true}, split ranges into slots
     * @return the free and busy time ranges
     */
    @Override
    public FreeBusy getFreeBusy(long scheduleId, String from, String to, boolean slots) {
        List<Range> free = new ArrayList<>();
        List<Range> busy = new ArrayList<>();
        query(scheduleId, from, to, free, busy, slots);
        return new FreeBusy(free, busy);
    }

    /**
     * Returns the appointment types associated with a schedule.
     *
     * @param scheduleId the schedule identifier
     * @return the appointment types
     */
    @Override
    public List<AppointmentType> getAppointmentTypes(long scheduleId) {
        List<AppointmentType> result = new ArrayList<>();
        Entity schedule = getScheduleEntity(scheduleId);
        IMObjectBean bean = new IMObjectBean(schedule, service);
        for (IMObject relationship : bean.getValues("appointmentTypes")) {
            IMObjectBean relationshipBean = new IMObjectBean(relationship, service);
            IMObject appointmentType = relationshipBean.getObject("target");
            if (appointmentType != null) {
                IMObjectBean appointmentTypeBean = new IMObjectBean(appointmentType, service);
                int slots = relationshipBean.getInt("noSlots");
                if (appointmentTypeBean.getBoolean("onlineBooking") && slots > 0) {
                    result.add(new AppointmentType(appointmentType.getId(), appointmentType.getName(), slots));
                }
            }
        }
        return result;
    }

    /**
     * Queries free/busy ranges between the from and to dates for a single schedule.
     *
     * @param scheduleId the schedule identifier
     * @param from       the from date
     * @param to         the to date
     * @param free       the free ranges to add to, or {@code null} if they aren't being queried
     * @param busy       the busy ranges to add to, or {@code null} if they aren't being queried
     * @param slots      if {@code true}, split ranges into slots
     */
    protected void query(long scheduleId, String from, String to, List<Range> free, List<Range> busy, boolean slots) {
        Date fromTime = getDate("from", from);
        Date toTime = getDate("to", to);
        Entity schedule = getScheduleEntity(scheduleId);
        IMObjectBean bean = new IMObjectBean(schedule, service);
        IMObject times = bean.getNodeTargetObject("onlineBookingTimes");
        IMObjectBean timesBean = (times != null) ? new IMObjectBean(times, service) : null;
        int slotSize = rules.getSlotSize(schedule);
        for (Date date = fromTime; date.compareTo(toTime) <= 0; date = DateRules.getDate(date, 1, DateUnits.DAYS)) {
            Date startTime = getStartTime(timesBean, bean, date);
            Date endTime = getEndTime(timesBean, bean, date);
            if (startTime != null && endTime != null) {
                Date min = DateRules.max(startTime, fromTime);
                Date max = DateRules.min(endTime, toTime);
                addFreeBusy(date, schedule, min, max, free, busy);
            }
        }
        if (slots && slotSize > 0) {
            if (free != null) {
                split(free, slotSize);
            }
            if (busy != null) {
                split(busy, slotSize);
            }
        }
    }

    /**
     * Returns the schedule start time for a date.
     *
     * @param times    the online booking times associated with the schedule. May be {@code null}
     * @param schedule the schedule
     * @param date     the date
     * @return the start time, or {@code null} if the schedule is not available on the date
     */
    private Date getStartTime(IMObjectBean times, IMObjectBean schedule, Date date) {
        Date startTime;
        boolean open;
        if (times != null) {
            String key = getNodePrefix(date);
            open = times.getBoolean(key + "Open");
            startTime = (open) ? times.getDate(key + "StartTime") : null;
        } else {
            open = true;
            startTime = schedule.getDate("startTime");
        }
        if (open) {
            if (startTime == null) {
                startTime = date;
            } else {
                startTime = DateRules.addDateTime(date, startTime);
            }
        }
        return startTime;
    }

    /**
     * Returns the schedule end time for a date.
     *
     * @param times    the online booking times associated with the schedule. May be {@code null}
     * @param schedule the schedule
     * @param date     the date
     * @return the end time, or {@code null} if the schedule is not available on the date
     */
    private Date getEndTime(IMObjectBean times, IMObjectBean schedule, Date date) {
        Date endTime;
        boolean open;
        if (times != null) {
            String key = getNodePrefix(date);
            open = times.getBoolean(key + "Open");
            endTime = (open) ? times.getDate(key + "EndTime") : null;
        } else {
            open = true;
            endTime = schedule.getDate("endTime");
        }
        if (open) {
            if (endTime != null) {
                endTime = DateRules.addDateTime(date, endTime);
            } else {
                endTime = DateRules.getNextDate(date);
            }
        }
        return endTime;
    }

    /**
     * Returns the node name prefix for a given date.
     *
     * @param date the date
     * @return the corresponding node name prefix
     */
    private String getNodePrefix(Date date) {
        String key;
        switch (new DateTime(date).getDayOfWeek()) {
            case DateTimeConstants.MONDAY:
                key = "mon";
                break;
            case DateTimeConstants.TUESDAY:
                key = "tue";
                break;
            case DateTimeConstants.WEDNESDAY:
                key = "wed";
                break;
            case DateTimeConstants.THURSDAY:
                key = "thu";
                break;
            case DateTimeConstants.FRIDAY:
                key = "fri";
                break;
            case DateTimeConstants.SATURDAY:
                key = "sat";
                break;
            default:
                key = "sun";
        }
        return key;
    }

    /**
     * Collects free and busy ranges.
     * <p/>
     * Cancelled acts are ignored.
     *
     * @param date     the date to get events for
     * @param schedule te schedule
     * @param min      the minimum date/time. Any range prior to this should be discarded or truncated if it overlaps
     * @param max      the maximum date/time. Any range after to this should be discarded or truncated if it overlaps
     * @param free     the free ranges to add to, or {@code null} if they aren't being determined
     * @param busy     the busy ranges to add to, or {@code null} if they aren't being determined
     */
    private void addFreeBusy(Date date, Entity schedule, Date min, Date max,
                             List<Range> free, List<Range> busy) {
        List<PropertySet> events = appointments.getEvents(schedule, date);
        Date freeStart = min;
        Date freeEnd = max;
        Date busyStart = null;
        Date busyEnd = null;
        for (PropertySet event : events) {
            if (!ActStatus.CANCELLED.equals(event.getString(ScheduleEvent.ACT_STATUS))) {
                Date actStart = event.getDate(ScheduleEvent.ACT_START_TIME);
                Date actEnd = event.getDate(ScheduleEvent.ACT_END_TIME);
                if (free != null) {
                    if (DateRules.compareTo(actStart, freeStart) > 0) {
                        addRange(free, freeStart, actStart, min, max);
                    }
                }
                if (busy != null) {
                    if (busyStart == null || DateRules.compareTo(actStart, busyEnd) > 0) {
                        if (busyStart != null) {
                            addRange(busy, busyStart, busyEnd, min, max);
                        }
                        busyStart = actStart;
                        busyEnd = actEnd;
                    } else if (DateRules.compareTo(actEnd, busyEnd) > 0) {
                        busyEnd = actEnd;
                    }
                }
                if (free != null) {
                    if (DateRules.compareTo(actEnd, freeStart) > 0) {
                        freeStart = actEnd;
                    }
                    if (freeEnd != null && DateRules.compareTo(actEnd, freeEnd) > 0) {
                        freeEnd = null;
                    }
                }
            }
        }
        if (free != null && freeEnd != null) {
            addRange(free, freeStart, freeEnd, min, max);
        }
        if (busy != null && busyStart != null) {
            addRange(busy, busyStart, busyEnd, min, max);
        }
    }

    /**
     * Adds a range, if it intersects the min..max range. If it overlaps, it will be truncated.
     *
     * @param ranges the ranges to add to
     * @param from   the start of the range
     * @param to     the end of the range
     * @param min    the minimum
     * @param max    the maximum
     */
    private void addRange(List<Range> ranges, Date from, Date to, Date min, Date max) {
        from = convert(from); // make sure Timestamps are converted to Dates in the local timezone
        to = convert(to);
        if (DateRules.compareTo(to, min) >= 0) {
            if (DateRules.compareTo(from, min) <= 0) {
                from = min;
            }
            if (DateRules.compareTo(from, max) < 0) {
                if (DateRules.compareTo(to, max) > 0) {
                    to = max;
                }
                ranges.add(new Range(from, to));
            }
        }
    }

    /**
     * Splits ranges based on the slot size.
     *
     * @param ranges   the ranges to split
     * @param slotSize the slot size
     */
    private void split(List<Range> ranges, int slotSize) {
        if (!ranges.isEmpty()) {
            List<Range> split = new ArrayList<>();
            for (Range range : ranges) {
                Date start = rules.getSlotTime(range.getStart(), slotSize, true);
                Date end = rules.getSlotTime(range.getEnd(), slotSize, false);
                Date from = start;
                while (from.compareTo(end) < 0) {
                    Date to = DateRules.getDate(from, slotSize, DateUnits.MINUTES);
                    if (to.compareTo(end) <= 0) {
                        split.add(new Range(from, to));
                    } else {
                        break;
                    }
                    from = to;
                }
            }
            ranges.clear();
            ranges.addAll(split);
        }
    }

    /**
     * Ensures a {code Timestamp} date instances are converted to {@code Date}.
     * <p/>
     * This is so all dates appear in the server's timezone.
     *
     * @param date the date
     * @return the date, converted if necessary
     */
    private Date convert(Date date) {
        return (date instanceof Timestamp) ? new Date(date.getTime()) : date;
    }

    /**
     * Returns a schedule given its identifier.
     *
     * @param id the schedule identifier
     * @return the corresponding schedule
     * @throws NotFoundException if the schedule cannot be found
     */
    private Entity getScheduleEntity(long id) {
        IMObjectReference scheduleRef = new IMObjectReference(ScheduleArchetypes.ORGANISATION_SCHEDULE, id);
        Entity schedule = (Entity) service.get(scheduleRef);
        if (schedule == null) {
            throw new NotFoundException("Schedule not found");
        }
        IMObjectBean bean = new IMObjectBean(schedule, service);
        if (!bean.getBoolean("onlineBooking")) {
            throw new BadRequestException("Schedule is not available for online booking");
        }
        return schedule;
    }

    /**
     * Helper to convert a string query parameter to an ISO 8601 date.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return the corresponding date
     * @throws BadRequestException if the value is invalid
     */
    private Date getDate(String name, String value) {
        if (value == null) {
            throw new BadRequestException("Missing '" + name + "' parameter");
        }
        DateTime result;
        try {
            result = ISODateTimeFormat.dateTimeNoMillis().parseDateTime(value);
        } catch (IllegalArgumentException e) {
            try {
                result = ISODateTimeFormat.dateTime().parseDateTime(value);
            } catch (IllegalArgumentException nested) {
                throw new BadRequestException("Parameter '" + name + "' is not a valid ISO date/time: " + value);
            }
        }
        return result.toDate();
    }

}
