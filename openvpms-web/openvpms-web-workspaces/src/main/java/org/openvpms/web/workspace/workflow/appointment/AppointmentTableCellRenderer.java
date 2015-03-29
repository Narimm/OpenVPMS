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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.workspace.workflow.appointment;

import echopointng.layout.TableLayoutDataEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Table;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid.Availability;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableCellRenderer;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel;

import java.util.Date;


/**
 * TableCellRender that assigns blocks of appointments in different hours a
 * different style.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-09-06 07:52:23Z $
 */
public class AppointmentTableCellRenderer extends ScheduleTableCellRenderer {

    /**
     * Default constructor.
     */
    public AppointmentTableCellRenderer() {
        super("entity.appointmentType");
    }

    /**
     * Returns a <tt>XhtmlFragment</tt> that will be displayed as the
     * content at the specified coordinate in the table.
     *
     * @param table  the <tt>Table</tt> for which the rendering is occurring
     * @param value  the value retrieved from the <tt>TableModel</tt> for the
     *               specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a <tt>XhtmlFragment</tt> representation of the value
     */
    public XhtmlFragment getTableCellRendererContent(Table table, Object value,
                                                     int column, int row) {
        XhtmlFragment result;
        TableLayoutDataEx layout;
        AppointmentTableModel model = (AppointmentTableModel) table.getModel();

        if (model.isStartTimeColumn(column)) {
            Date date = (Date) value;
            String text = DateFormatter.formatTime(date, false);

            result = TableHelper.createFragment(text);

            PropertySet event = model.getEvent(column, row);
            if (event != null) {
                layout = getEventLayoutData(event, model);
            } else {
                String style = getFreeStyle(model, row);
                layout = TableHelper.getTableLayoutDataEx(style);
            }
        } else {
            result = new XhtmlFragment();

            Availability avail = model.getAvailability(column, row);
            String style = getStyle(avail, model, row);
            layout = TableHelper.getTableLayoutDataEx(style);

            if (layout != null && avail == Availability.UNAVAILABLE) {
                Schedule schedule = model.getSchedule(column);
                int span = model.getGrid().getUnavailableSlots(schedule, row);
                layout.setRowSpan(span);
            }
        }

        result.setLayoutData(layout);
        return result;
    }

    /**
     * Returns a component for a value.
     *
     * @param table  the <tt>Table</tt> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <tt>TableModel</tt> for the
     *               specified coordinate
     * @param column the column
     * @param row    the row
     * @return a component representation of the value. May be <tt>null</tt>
     */
    @Override
    protected Component getComponent(Table table, Object value, int column, int row) {
        AppointmentTableModel model = (AppointmentTableModel) table.getModel();
        if (model.isStartTimeColumn(column)) {
            // use getTableCellRendererContent to render the cell. Bit tedious,
            // but its the only way to get the isSelectionCausingCell() and
            // isActionCausingCell() methods to be invoked
            return null;
        }
        return super.getComponent(table, value, column, row);
    }

    /**
     * Determines if the cell can be highlighted.
     *
     * @param table  the table
     * @param column the column
     * @param row    the row
     * @return <tt>true</tt> if the cell can be highlighted
     */
    @Override
    protected boolean canHighlightCell(Table table, int column, int row) {
        AppointmentTableModel model = (AppointmentTableModel) table.getModel();
        return !model.isStartTimeColumn(column) && super.canHighlightCell(table, column, row);
    }

    /**
     * Colours a cell based on its availability.
     *
     * @param component a component representing the cell
     * @param column    the cell column
     * @param row       the cell row
     * @param model     the event model
     */
    @Override
    protected void colourCell(Component component, int column, int row, ScheduleTableModel model) {
        if (((AppointmentTableModel) model).isStartTimeColumn(column)) {
            super.colourCell(component, Availability.FREE, model, row);
        } else {
            super.colourCell(component, column, row, model);
        }
    }

    /**
     * Returns the style for a free row.
     *
     * @param model the appointment table model
     * @param row   the row
     * @return a style for the row
     */
    @Override
    protected String getFreeStyle(ScheduleTableModel model, int row) {
        AppointmentTableModel m = (AppointmentTableModel) model;
        int hour = m.getHour(row);
        return (hour % 2 == 0) ? "ScheduleTable.Even" : "ScheduleTable.Odd";
    }

}
