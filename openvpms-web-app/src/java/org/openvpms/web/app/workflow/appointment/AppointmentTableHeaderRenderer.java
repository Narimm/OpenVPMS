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

import echopointng.layout.TableLayoutDataEx;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Table;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.table.AbstractTableCellRenderer;


/**
 * Header cell renderer for the {@link AppointmentTableModel}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-09-06 07:52:23Z $
 */
class AppointmentTableHeaderRenderer extends AbstractTableCellRenderer {

    /**
     * The singleton instance.
     */
    public static AppointmentTableHeaderRenderer INSTANCE
        = new AppointmentTableHeaderRenderer();

    /**
     * Default style.
     */
    private static final String STYLE = "Table.Header";


    /**
     * Default constructor.
     */
    private AppointmentTableHeaderRenderer() {
    }

    /**
     * Returns the style name for a column and row.
     *
     * @param table  the <tt>Table</tt> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <tt>TableModel</tt> for the
     *               specified coordinate
     * @param column the column
     * @param row    the row
     * @return a style name for the given column and row.
     */
    protected String getStyle(Table table, Object value, int column, int row) {
        return STYLE;
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
     * @return a component representation of the value
     */
    @Override
    protected Component getComponent(Table table, Object value, int column,
                                     int row) {
        Component component = super.getComponent(table, value, column, row);
        AppointmentTableModel model = (AppointmentTableModel) table.getModel();
        if (!model.isSingleScheduleView()) {
            Entity schedule = model.getScheduleEntity(column);
            if (schedule != null) {
                ++column;
                int span = 1;
                while (column < model.getColumnCount()) {
                    if (!ObjectUtils.equals(schedule,
                                            model.getScheduleEntity(column))) {
                        break;
                    }
                    ++column;
                    ++span;
                }
                if (span > 1) {
                    TableLayoutDataEx layout = new TableLayoutDataEx();
                    layout.setColSpan(span);
                    layout.setAlignment(Alignment.ALIGN_CENTER);
                    component.setLayoutData(layout);
                }
            }
        }
        return component;
    }

}
