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

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Abstract implementation of the {@link ScheduleEventGrid} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractScheduleEventGrid implements ScheduleEventGrid {

    /**
     * The schedule view.
     */
    private final Entity scheduleView;

    /**
     * The schedules.
     */
    private List<Schedule> schedules = Collections.emptyList();

    /**
     * The grid start date.
     */
    private Date startDate;

    /**
     * The grid end date.
     */
    private Date endDate;

    /**
     * The appointment rules.
     */
    private final AppointmentRules rules;

    /**
     * Constructs an {@link AbstractScheduleEventGrid}.
     *
     * @param scheduleView the schedule view
     * @param date         the grid start and end date
     * @param rules        the appointment rules
     */
    public AbstractScheduleEventGrid(Entity scheduleView, Date date, AppointmentRules rules) {
        this(scheduleView, date, date, rules);
    }

    /**
     * Constructs an {@link AbstractScheduleEventGrid}.
     *
     * @param scheduleView the schedule view
     * @param startDate    the grid start date
     * @param endDate      the grid end date
     * @param rules        the appointment rules
     */
    public AbstractScheduleEventGrid(Entity scheduleView, Date startDate, Date endDate, AppointmentRules rules) {
        this.scheduleView = scheduleView;
        this.rules = rules;
        setStartDate(startDate);
        setEndDate(endDate);
    }

    /**
     * Returns the schedule view associated with this grid.
     *
     * @return the schedule view
     */
    @Override
    public Entity getScheduleView() {
        return scheduleView;
    }

    /**
     * Returns the schedules.
     *
     * @return the schedules
     */
    @Override
    public List<Schedule> getSchedules() {
        return schedules;
    }

    /**
     * Returns the schedule start date.
     *
     * @return the start date, excluding any time
     */
    @Override
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the schedule start date.
     * <p/>
     * Any time is removed.
     *
     * @param startDate the schedule start date
     */
    public void setStartDate(Date startDate) {
        this.startDate = DateRules.getDate(startDate);
    }

    /**
     * Returns the schedule end date.
     *
     * @return the end date, excluding any time
     */
    @Override
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the schedule end date.
     * <p/>
     * Any time is removed.
     *
     * @param endDate the end date
     */
    public void setEndDate(Date endDate) {
        this.endDate = DateRules.getDate(endDate);
    }

    /**
     * Sets the schedules.
     *
     * @param schedules the schedules
     */
    protected void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
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
    protected void addEvent(Entity schedule, PropertySet event) {
        int index = -1;
        boolean found = false;
        Schedule column = null;
        Schedule match = null;

        boolean blockingEvent = Schedule.isBlockingEvent(event);
        // try and find a corresponding Schedule. If the event is non-blocking, try and find one that has no event
        // that intersects the supplied one.
        List<Schedule> columns = getSchedules();
        for (int i = 0; i < columns.size(); ++i) {
            column = columns.get(i);
            if (column.getSchedule().equals(schedule)) {
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
            column = new Schedule(match, rules);
            columns.add(index + 1, column);
        }
        column.addEvent(event);
    }

    /**
     * Returns the appointment rules.
     *
     * @return the rules
     */
    protected AppointmentRules getAppointmentRules() {
        return rules;
    }
}
