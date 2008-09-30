/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.worklist;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.app.workflow.scheduling.Schedule;
import org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Task event grid.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskGrid implements ScheduleEventGrid {

    /**
     * The grid date.
     */
    private Date date;

    /**
     * The schedules.
     */
    private List<Schedule> schedules;

    /**
     * The no. of slots in the grid. This is calculated to be number of
     * events in the schedule containing the most events. If slots is less
     * than that schedules maxSlots, then 1 is added to indicate that there
     * are slots available.
     */
    private int slots;


    /**
     * Creates a new <tt>TaskGrid</tt>.
     *
     * @param date  the date
     * @param tasks the tasks
     */
    public TaskGrid(Date date, Map<Entity, List<ObjectSet>> tasks) {
        setDate(date);
        this.date = DateRules.getDate(date);

        schedules = new ArrayList<Schedule>();
        for (Map.Entry<Entity, List<ObjectSet>> entry : tasks.entrySet()) {
            Entity workList = entry.getKey();
            List<ObjectSet> sets = entry.getValue();
            TaskSchedule schedule = new TaskSchedule(workList);
            for (ObjectSet set : sets) {
                schedule.addEvent(set);
            }
            schedules.add(schedule);

            // display up to events + 1 slots
            int events = schedule.getSlots() + 1;
            if (slots < events && events <= schedule.getMaxSlots()) {
                slots = events;
            }
        }
    }

    /**
     * Sets the schedule date.
     * <p/>
     * All events in the grid start or end on this date.
     * <p/>
     * Any time is removed.
     *
     * @param date the schedule date
     */
    public void setDate(Date date) {
        this.date = DateRules.getDate(date);
    }

    /**
     * Returns the schedule date.
     * <p/>
     * All events in the grid start or end on this date.
     *
     * @return the date, excluding any time
     */
    public Date getDate() {
        return date;
    }

    /**
     * Returns the schedules.
     *
     * @return the schedules
     */
    public List<Schedule> getSchedules() {
        return schedules;
    }

    /**
     * Returns the no. of slots in the grid.
     *
     * @return the no. of slots
     */
    public int getSlots() {
        return slots;
    }

    /**
     * Returns the event for the specified schedule and slot.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the corresponding event, or <tt>null</tt> if none is found
     */
    public ObjectSet getEvent(Schedule schedule, int slot) {
        List<ObjectSet> events = schedule.getEvents();
        return (events.size() > slot) ? events.get(slot) : null;
    }

    /**
     * Returns the time that the specified slot starts at.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the start time of the specified slot. May be <tt>null</tt>
     */
    public Date getStartTime(Schedule schedule, int slot) {
        ObjectSet event = getEvent(schedule, slot);
        return (event != null)
                ? event.getDate(ScheduleEvent.ACT_START_TIME) : null;
    }

    /**
     * Determines the availability of a slot for the specified schedule.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the availability of the schedule
     */
    public Availability getAvailability(Schedule schedule,
                                        int slot) {
        TaskSchedule s = (TaskSchedule) schedule;
        if (slot < s.getSlots()) {
            return Availability.BUSY;
        } else if (slot < s.getMaxSlots()) {
            return Availability.FREE;
        }
        return Availability.UNAVAILABLE;
    }

    /**
     * Determines how many slots are unavailable from the specified slot, for
     * a schedule.
     *
     * @param schedule the schedule
     * @param slot     the starting slot
     * @return the no. of concurrent slots that are unavailable
     */
    public int getUnavailableSlots(Schedule schedule, int slot) {
        TaskSchedule s = (TaskSchedule) schedule;
        if (slot >= s.getMaxSlots()) {
            return 0;
        }
        return getSlots() - slot;
    }

}
