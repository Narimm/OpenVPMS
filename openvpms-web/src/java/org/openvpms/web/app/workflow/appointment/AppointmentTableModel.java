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

package org.openvpms.web.app.workflow.appointment;

import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.app.workflow.scheduling.ScheduleTableModel;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;
import java.util.Map;


/**
 * Appointment table model.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AppointmentTableModel extends ScheduleTableModel {

    /**
     * Cached reason lookup names.
     */
    private Map<String, String> reasons;

    /**
     * The start time index.
     */
    protected static final int START_TIME_INDEX = 0;


    /**
     * Creates a new <tt>AppointmentTableModel</tt>.
     */
    public AppointmentTableModel(AppointmentGrid grid) {
        super(grid, "act.customerAppointment");
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
     * Returns a status name given its code.
     *
     * @param event the event
     * @return the status name
     */
    protected String getStatus(ObjectSet event) {
        String status = null;

        String code = event.getString(ScheduleEvent.ACT_STATUS);
        if (AppointmentStatus.CHECKED_IN.equals(code)) {
            Date arrival = event.getDate(ScheduleEvent.ARRIVAL_TIME);
            if (arrival != null) {
                String diff = DateHelper.formatTimeDiff(arrival, new Date());
                status = Messages.get("workflow.scheduling.table.waiting",
                                      diff);
            }
        }
        if (status == null) {
            status = getStatus(code);
        }
        return status;
    }

    /**
     * Returns a reason name for an event.
     *
     * @param event the event
     * @return the reason name
     */
    protected String getReason(ObjectSet event) {
        if (reasons == null) {
            reasons = LookupNameHelper.getLookupNames(
                    "act.customerAppointment", "reason");
        }
        String code = event.getString(ScheduleEvent.ACT_REASON);
        return (reasons != null) ? reasons.get(code) : null;
    }

}
