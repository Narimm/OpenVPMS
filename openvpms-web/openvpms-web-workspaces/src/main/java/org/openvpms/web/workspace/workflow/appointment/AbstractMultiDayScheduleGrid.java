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

import org.apache.commons.lang.ObjectUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.component.system.common.cache.SoftRefIMObjectCache;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Multiple-day schedule grid.
 *
 * @author Tim Anderson
 */
public abstract class AbstractMultiDayScheduleGrid extends AbstractScheduleEventGrid {

    /**
     * The number of days to display.
     */
    private final int days;

    /**
     * Constructs an {@link AbstractMultiDayScheduleGrid}.
     *
     * @param scheduleView the schedule view
     * @param date         the date
     * @param days         the number of days to display
     * @param events       the events
     * @param rules        the appointment rules
     */
    public AbstractMultiDayScheduleGrid(Entity scheduleView, Date date, int days,
                                        Map<Entity, List<PropertySet>> events, AppointmentRules rules) {
        super(scheduleView, date, DateRules.getDate(date, days - 1, DateUnits.DAYS), rules);
        this.days = days;
        setEvents(events);
    }

    /**
     * Returns the no. of slots in the grid.
     *
     * @return the no. of slots
     */
    @Override
    public int getSlots() {
        return days;
    }

    /**
     * Returns the event for the specified schedule and slot.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the corresponding event, or {@code null} if none is found
     */
    @Override
    public PropertySet getEvent(Schedule schedule, int slot) {
        return getEvent(schedule, slot, true);
    }

    /**
     * Returns the event for the specified schedule and slot.
     *
     * @param schedule              the schedule
     * @param slot                  the slot
     * @param includeBlockingEvents if {@code true}, look for blocking events if there are no non-blocking events
     * @return the corresponding event, or {@code null} if none is found
     */
    public PropertySet getEvent(Schedule schedule, int slot, boolean includeBlockingEvents) {
        Date time = getStartTime(schedule, slot);
        PropertySet result = schedule.getEvent(time, 24 * 60, includeBlockingEvents);
        if (result == null) {
            result = schedule.getIntersectingEvent(time, includeBlockingEvents);
        }
        return result;
    }

    /**
     * Returns the time that the specified slot starts at.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the start time of the specified slot. May be {@code null}
     */
    @Override
    public Date getStartTime(Schedule schedule, int slot) {
        Date date = DateRules.getDate(getStartDate(), slot, DateUnits.DAYS);
        return DateRules.getDate(date, schedule.getStartMins(), DateUnits.MINUTES);
    }

    /**
     * Determines the availability of a slot for the specified schedule.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the availability of the schedule
     */
    @Override
    public Availability getAvailability(Schedule schedule, int slot) {
        PropertySet event = getEvent(schedule, slot, false);
        return (event != null) ? Availability.BUSY : Availability.FREE;
    }

    /**
     * Determines how many slots are unavailable from the specified slot, for
     * a schedule.
     *
     * @param schedule the schedule
     * @param slot     the starting slot
     * @return the no. of concurrent slots that are unavailable
     */
    @Override
    public int getUnavailableSlots(Schedule schedule, int slot) {
        return 0;
    }

    /**
     * Returns the slot that a time falls in.
     *
     * @param time the time
     * @return the slot, or {@code -1} if the time doesn't intersect any slot
     */
    @Override
    public int getSlot(Date time) {
        return Days.daysBetween(new DateTime(getStartDate()), new DateTime(time)).getDays();
    }

    /**
     * Returns the no. of slots an event occupies, from the specified slot.
     * <p/>
     * If the event begins prior to the slot, the remaining slots will be returned.
     *
     * @param event the event
     * @param slot  the starting slot
     * @return the no. of slots that the event occupies
     */
    public int getSlots(PropertySet event, int slot) {
        DateTime endTime = new DateTime(event.getDate(ScheduleEvent.ACT_END_TIME));
        int endSlot = Days.daysBetween(new DateTime(getStartDate()), endTime).getDays();
        if (endTime.getHourOfDay() > 0 || endTime.getMinuteOfHour() > 0) {
            ++endSlot;
        }
        return endSlot - slot;
    }

    /**
     * Returns the date of a slot.
     *
     * @param slot the slot
     * @return the start time of the specified slot
     */
    public Date getDate(int slot) {
        return DateRules.getDate(getStartDate(), slot, DateUnits.DAYS);
    }

    /**
     * Sets the events.
     *
     * @param events the events, keyed on schedule
     */
    protected void setEvents(Map<Entity, List<PropertySet>> events) {
        List<Schedule> schedules = new ArrayList<>();
        IMObjectCache cageTypes = new SoftRefIMObjectCache(ServiceHelper.getArchetypeService());

        int index = -1;
        Entity last = null;
        for (Entity entity : events.keySet()) {
            IMObjectBean bean = new IMObjectBean(entity);
            Entity cageType = (Entity) cageTypes.get(bean.getNodeTargetObjectRef("cageType"));
            Schedule schedule = new Schedule(entity, cageType, 0, 24 * 60, 24 * 60, getAppointmentRules());
            if (!ObjectUtils.equals(last, entity)) {
                index++;
            }
            last = entity;
            schedule.setRenderEven(index % 2 == 0);
            schedules.add(schedule);
        }
        setSchedules(schedules);

        // add the events
        for (Map.Entry<Entity, List<PropertySet>> entry : events.entrySet()) {
            Party schedule = (Party) entry.getKey();
            List<PropertySet> sets = entry.getValue();

            for (PropertySet set : sets) {
                addEvent(schedule, set);
            }
        }
    }

    /**
     * Adds an event or blocking event.
     * <p/>
     * If the event is not a blocking event, and the corresponding Schedule already has an event that intersects
     * it, a new Schedule will be created with the same start and end times, and the event added to that.
     *
     * @param schedule the schedule to add the appointment to
     * @param event    the event
     */
    @Override
    protected void addEvent(Entity schedule, PropertySet event) {
        int index = -1;
        boolean found = false;
        Schedule column = null;
        Schedule match = null;

        boolean blockingEvent = Schedule.isBlockingEvent(event);
        // try and find a corresponding Schedule. If the event is non-blocking, try and find one that has no event
        // that intersects the supplied one.
        List<Schedule> columns = getSchedules();
        IMObjectReference eventRef = event.getReference(ScheduleEvent.ACT_REFERENCE);
        for (int i = 0; i < columns.size(); ++i) {
            column = columns.get(i);
            if (column.getSchedule().equals(schedule)) {
                if (column.indexOf(eventRef) != -1) {
                    // multi-day appointments are duplicated on subsequent days, so skip it if it has already been added
                    return;
                }
                if (blockingEvent) {
                    found = true;
                    break;
                } else if (column.hasIntersectingEvent(event)) {
                    match = column;
                    index = i;
                } else {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            // event intersects an existing one, so create a new Schedule. Any blocking event will be shared.
            column = new Schedule(match, getAppointmentRules());
            columns.add(index + 1, column);
        }
        column.addEvent(event);
    }
}
