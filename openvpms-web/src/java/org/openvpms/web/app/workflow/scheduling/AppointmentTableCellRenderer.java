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
import org.openvpms.archetype.rules.workflow.AppointmentQuery;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.table.IMTable;
import org.openvpms.web.component.table.AbstractTableCellRenderer;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * TableCellRender that assigns blocks of appointments in different hours a
 * different style.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-09-06 07:52:23Z $
 */
public class AppointmentTableCellRenderer extends AbstractTableCellRenderer {

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
        String style = "Table.EvenRow";
        IMTable<ObjectSet> actTable = (IMTable<ObjectSet>) table;
        ObjectSet set = actTable.getObjects().get(row);
        Date startTime = (Date) set.get(AppointmentQuery.ACT_START_TIME);
        if (startTime != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startTime);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour % 2 == 1) {
                style = "Table.OddRow";
            }
        }
        return style;
    }

}
