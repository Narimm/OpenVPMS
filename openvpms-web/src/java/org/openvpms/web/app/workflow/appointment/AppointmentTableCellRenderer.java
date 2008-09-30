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

package org.openvpms.web.app.workflow.appointment;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Table;
import org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid;
import org.openvpms.web.app.workflow.scheduling.ScheduleTableCellRenderer;
import org.openvpms.web.app.workflow.scheduling.ScheduleTableModel;


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
     * Determines if the cell can be highlighted.
     *
     * @param table  the table
     * @param column the column
     * @param row    the row
     * @return <tt>true</tt> if the cell can be highlighted
     */
    @Override
    protected boolean canHighlightCell(Table table, int column, int row) {
        if (column != AppointmentTableModel.START_TIME_INDEX) {
            return super.canHighlightCell(table, column, row);
        }
        return false;
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
    protected void colourCell(Component component, int column, int row,
                              ScheduleTableModel model) {
        if (column == AppointmentTableModel.START_TIME_INDEX) {
            super.colourCell(component, ScheduleEventGrid.Availability.FREE,
                             model, row);
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
