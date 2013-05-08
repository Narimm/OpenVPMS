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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.app.workflow.scheduling.Schedule;
import org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.echo.i18n.Messages;

import java.util.List;


/**
 * Appointment table model for multiple schedules.
 *
 * @author Tim Anderson
 */
class MultiScheduleTableModel extends AppointmentTableModel {

    /**
     * The column index of the right start time column.
     */
    private int rightStartTimeIndex;

    /**
     * Constructs a {@code MultiScheduleTableModel}.
     *
     * @param grid the appointment grid
     */
    public MultiScheduleTableModel(AppointmentGrid grid, Context context) {
        super(grid, context);
    }

    /**
     * Determines if the specified column is a 'start time' column.
     *
     * @param column the column
     * @return {@code true} if the column is a 'start time' column
     */
    @Override
    public boolean isStartTimeColumn(int column) {
        return super.isStartTimeColumn(column) || column == rightStartTimeIndex;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param column the column
     * @param row    the row
     * @return the cell value
     */
    protected Object getValueAt(Column column, int row) {
        Object result = null;
        int index = column.getModelIndex();
        if (index == START_TIME_INDEX || index == rightStartTimeIndex) {
            result = getGrid().getStartTime(row);
        } else {
            PropertySet set = getEvent(column, row);
            AppointmentGrid grid = getGrid();
            int rowSpan = 1;
            if (set != null) {
                result = getEvent(set);
                rowSpan = grid.getSlots(set, row);
            }
            if (rowSpan > 1) {
                if (!(result instanceof Component)) {
                    Label label = LabelFactory.create();
                    if (result != null) {
                        label.setText(result.toString());
                    }
                    result = label;
                }
                setSpan((Component) result, rowSpan);
            }
        }
        return result;
    }

    /**
     * Returns a component representing an event.
     *
     * @param event the event
     * @return a new component
     */
    private Component getEvent(PropertySet event) {
        String text = evaluate(event);
        if (text == null) {
            String customer = event.getString(ScheduleEvent.CUSTOMER_NAME);
            String patient = event.getString(ScheduleEvent.PATIENT_NAME);
            String status = getStatus(event);
            String reason = event.getString(ScheduleEvent.ACT_REASON_NAME);
            if (reason == null) {
                // fall back to the code
                reason = event.getString(ScheduleEvent.ACT_REASON);
            }

            if (patient == null) {
                text = Messages.get(
                    "workflow.scheduling.appointment.table.customer",
                    customer, reason, status);
            } else {
                text = Messages.get(
                    "workflow.scheduling.appointment.table.customerpatient",
                    customer, patient, reason, status);
            }
        }

        String notes = event.getString(ScheduleEvent.ACT_DESCRIPTION);
        return createLabelWithNotes(text, notes);
    }

    /**
     * Creates a column model to display a list of schedules.
     *
     * @param grid the appointment grid
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(ScheduleEventGrid grid) {
        DefaultTableColumnModel result = new DefaultTableColumnModel();
        List<Schedule> schedules = grid.getSchedules();
        int index = START_TIME_INDEX;
        String startTime = DescriptorHelper.getDisplayName(ScheduleArchetypes.APPOINTMENT, "startTime");
        Column leftStartCol = new Column(index, startTime);
        leftStartCol.setWidth(new Extent(100));
        result.addColumn(leftStartCol);
        ++index;
        int percent = (!schedules.isEmpty()) ? 100 / schedules.size() : 0;
        for (Schedule schedule : schedules) {
            Column column = new Column(index++, schedule);
            if (percent != 0) {
                column.setWidth(new Extent(percent, Extent.PERCENT));
            }
            result.addColumn(column);
        }
        rightStartTimeIndex = index;
        Column rightStartCol = new Column(rightStartTimeIndex, startTime);
        rightStartCol.setWidth(new Extent(100));
        result.addColumn(rightStartCol);
        return result;
    }
}
