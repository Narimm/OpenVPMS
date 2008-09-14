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

package org.openvpms.web.app.workflow.scheduling;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.table.TableCellRenderer;
import org.openvpms.web.component.util.LabelFactory;


/**
 * Cell renderer for the {@link AppointmentTableModel} when displaying multiple
 * schedules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-09-06 07:52:23Z $
 */
public class AppointmentGridCellRenderer implements TableCellRenderer {

    /**
     * The singleton instance.
     */
    public static final AppointmentGridCellRenderer INSTANCE
            = new AppointmentGridCellRenderer();


    /**
     * Default constructor.
     */
    protected AppointmentGridCellRenderer() {
    }

    /**
     * Returns a component that will be displayed at the specified coordinate
     * in the table.
     *
     * @param table  the <code>Table</code> for which the rendering is occurring
     * @param value  the value retrieved from the <code>TableModel</code> for the
     *               specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a component representation  of the value (This component must
     *         be unique.  Returning a single instance of a component across
     *         multiple calls to this method will result in undefined
     *         behavior.)
     */
    public Component getTableCellRendererComponent(Table table, Object value,
                                                   int column, int row) {
        Component component;
        if (value instanceof Component) {
            component = (Component) value;
        } else {
            Label label = LabelFactory.create();
            if (value != null) {
                label.setText(value.toString());
            }
            component = label;
        }
        return component;
    }
}
