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

import echopointng.table.TableCellRendererEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleColours;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel;

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
     * Constructs an {@link AppointmentTableModel}.
     *
     * @param grid    the appointment grid
     * @param context the context
     * @param colours the colour cache
     */
    public AppointmentTableModel(AppointmentGrid grid, Context context, ScheduleColours colours) {
        super(grid, context, true, colours);
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
    public int getSlot(Schedule schedule, IMObjectReference eventRef) {
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
    public String getStatus(PropertySet event) {
        String status = null;

        String code = event.getString(ScheduleEvent.ACT_STATUS);
        if (AppointmentStatus.CHECKED_IN.equals(code)) {
            Date arrival = event.getDate(ScheduleEvent.ARRIVAL_TIME);
            if (arrival != null) {
                String diff = DateFormatter.formatTimeDiff(arrival, new Date());
                status = Messages.format("workflow.scheduling.table.waiting", diff);
            }
        } else {
            status = event.getString(ScheduleEvent.ACT_STATUS_NAME);
        }
        return status;
    }

    protected class TimeColumnCellRenderer implements TableCellRendererEx {

        /**
         * Returns a {@code XhtmlFragment} that will be displayed as the
         * content at the specified coordinate in the table.
         *
         * @param table  the {@code Table} for which the rendering is occurring
         * @param value  the value retrieved from the {@code TableModel} for the specified coordinate
         * @param column the column index to render
         * @param row    the row index to render
         * @return a {@code XhtmlFragment} representation of the value
         */
        public XhtmlFragment getTableCellRendererContent(Table table, Object value, int column, int row) {
            return null;
        }

        /**
         * This method allows you to "restrict" the cells (within a row) that will
         * cause selection of the row to occur. By default any cell will cause
         * selection of a row. If this methods returns false then only certain cells
         * within the row will cause selection when clicked on.
         *
         * @param table  the table
         * @param column the column
         * @param row    the row
         * @return {@code true} if the cell causes selection
         */
        public boolean isSelectionCausingCell(Table table, int column, int row) {
            return false;
        }

        /**
         * This method is called to determine which cells within a row can cause an
         * action to be raised on the server when clicked.
         * <p>
         * By default if a Table has attached actionListeners then any click on any
         * cell within a row will cause the action to fire.
         * <p>
         * This method allows this to be overrriden and only certain cells within a
         * row can cause an action event to be raise.
         *
         * @param table  the Table in question
         * @param column the column in question
         * @param row    the row in quesiton
         * @return true means that the cell can cause actions while false means the cells can not cause action events.
         */
        public boolean isActionCausingCell(Table table, int column, int row) {
            return false;
        }

        /**
         * Returns a component that will be displayed at the specified coordinate in
         * the table.
         *
         * @param table  the {@code Table} for which the rendering is occurring
         * @param value  the value retrieved from the {@code TableModel} for the specified coordinate
         * @param column the column index to render
         * @param row    the row index to render
         * @return a component representation  of the value.
         */
        public Component getTableCellRendererComponent(Table table, Object value, int column, int row) {
            Date date = (Date) value;
            String text = DateFormatter.formatTime(date, false);
            Label label = LabelFactory.create();
            label.setText(text);
            int hour = getHour(row);
            String styleName = (hour % 2 == 0) ? "ScheduleTable.Even" : "ScheduleTable.Odd";
            label.setStyleName(styleName);
            return label;
        }
    }

}
