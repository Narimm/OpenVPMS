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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.worklist;

import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.archetype.rules.workflow.WorkflowStatus;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel;

import java.util.Date;


/**
 * Table model to display <em>act.customerTask<em>s.
 *
 * @author Tim Anderson
 */
public abstract class TaskTableModel extends ScheduleTableModel {

    /**
     * Constructs a {@link TaskTableModel}.
     *
     * @param grid the task grid
     */
    public TaskTableModel(TaskGrid grid, Context context) {
        super(grid, context);
    }

    /**
     * Returns the row of the specified event.
     *
     * @param schedule the schedule
     * @param eventRef the event reference
     * @return the row, or {@code -1} if the event is not found
     */
    public int getRow(Schedule schedule, IMObjectReference eventRef) {
        return schedule.indexOf(eventRef);
    }

    /**
     * Returns a status name given its code.
     *
     * @param event the event
     * @return the status name
     */
    protected String getStatus(PropertySet event) {
        String status;
        String code = event.getString(ScheduleEvent.ACT_STATUS);
        if (WorkflowStatus.PENDING.equals(code)) {
            Date startTime = event.getDate(ScheduleEvent.ACT_START_TIME);
            String diff = DateFormatter.formatTimeDiff(startTime, new Date());
            status = Messages.format("workflow.scheduling.table.waiting", diff);
        } else {
            status = event.getString(ScheduleEvent.ACT_STATUS_NAME);
        }
        return status;
    }

}
