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

package org.openvpms.web.app.workflow.scheduling;

import nextapp.echo2.app.Table;
import org.openvpms.archetype.rules.workflow.Appointment;
import org.openvpms.archetype.rules.workflow.WorkflowStatus;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.table.AbstractTableCellRenderer;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * TableCellRender that assigns blocks of appointments in different hours a
 * different style.
 * Note that for this renderer will not work for partial table renders as
 * it maintains state for the style of the previous row.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-09-06 07:52:23Z $
 */
public class AppointmentTableCellRenderer extends AbstractTableCellRenderer {

    /**
     * The previous rendered row.
     */
    private int previousRow = -1;

    /**
     * The previous rendered row hour.
     */
    private int previousHour;

    /**
     * The previous rendered row style.
     */
    private String previousStyle;

    /**
     * The style of the first block of hours.
     */
    private static final String BLOCK_STYLE1 = "TaskTable.EvenRow";

    /**
     * The style of the second block of hours.
     */
    private static final String BLOCK_STYLE2 = "TaskTable.OddRow";


    /**
     * Returns the style name for a column and row.
     *
     * @param table  the <code>Table</code> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <code>TableModel</code> for
     *               the specified coordinate
     * @param column the column
     * @param row    the row
     * @return a style name for the given column and row.
     */
    @SuppressWarnings("unchecked")
    protected String getStyle(Table table, Object value, int column, int row) {
        String style = BLOCK_STYLE1;
        AppointmentTableModel model = (AppointmentTableModel) table.getModel();
        ObjectSet set = model.getAppointment(column, row);
        String status = null;
        if (set != null) {
            status = set.getString(Appointment.ACT_STATUS);
        }
        if (status != null && !status.equals(WorkflowStatus.PENDING)) {
            style = "TaskTable." + status;
        } else {
            if (row == previousRow) {
                style = previousStyle;
            } else {
                Date startTime = model.getStartTime(row);
                if (startTime != null) {
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(startTime);
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    if (row == (previousRow + 1)) {
                        if (hour == previousHour) {
                            style = previousStyle;
                        } else if (BLOCK_STYLE1.equals(previousStyle)) {
                            style = BLOCK_STYLE2;
                        }
                    } else {
                        if (hour % 2 == 1) {
                            style = BLOCK_STYLE2;
                        }
                    }
                    previousHour = hour;
                }
                previousRow = row;
                previousStyle = style;
            }
        }
        return style;
    }
}
