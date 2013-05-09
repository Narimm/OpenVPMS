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
 */

package org.openvpms.web.app.workflow.appointment;

import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.app.workflow.scheduling.Schedule;
import org.openvpms.web.app.workflow.scheduling.ScheduleTableModel;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateHelper;

import java.util.Date;


/**
 * Appointment table model.
 *
 * @author Tim Anderson
 */
public abstract class AppointmentTableModel extends ScheduleTableModel {

    /**
     * The start time index.
     */
    protected static final int START_TIME_INDEX = 0;

    /**
     * Constructs an {@code AppointmentTableModel}.
     *
     * @param grid    the appointment grid
     * @param context the context
     */
    public AppointmentTableModel(AppointmentGrid grid, Context context) {
        super(grid, context);
    }

    /**
     * Determines if the specified column is a 'start time' column.
     *
     * @param column the column
     * @return {@code true} if the column is a 'start time' column
     */
    public boolean isStartTimeColumn(int column) {
        return column == START_TIME_INDEX;
    }

    /**
     * Returns the hour at the specified row.
     *
     * @param row the row
     * @return the hour
     */
    public int getHour(int row) {
        return getGrid().getHour(row);
    }

    /**
     * Returns the grid.
     *
     * @return the grid
     */
    @Override
    public AppointmentGrid getGrid() {
        return (AppointmentGrid) super.getGrid();
    }

    /**
     * Returns the row of the specified event.
     *
     * @param schedule the schedule
     * @param eventRef the event reference
     * @return the row, or {@code -1} if the event is not found
     */
    public int getRow(Schedule schedule, IMObjectReference eventRef) {
        PropertySet event = schedule.getEvent(eventRef);
        if (event != null) {
            return getGrid().getSlot(event.getDate(ScheduleEvent.ACT_START_TIME));
        }
        return -1;
    }

    /**
     * Returns a status name given its code.
     *
     * @param event the event
     * @return the status name
     */
    protected String getStatus(PropertySet event) {
        String status = null;

        String code = event.getString(ScheduleEvent.ACT_STATUS);
        if (AppointmentStatus.CHECKED_IN.equals(code)) {
            Date arrival = event.getDate(ScheduleEvent.ARRIVAL_TIME);
            if (arrival != null) {
                String diff = DateHelper.formatTimeDiff(arrival, new Date());
                status = Messages.get("workflow.scheduling.table.waiting",
                                      diff);
            }
        } else {
            status = event.getString(ScheduleEvent.ACT_STATUS_NAME);
        }
        return status;
    }

}
