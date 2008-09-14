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
 * Header cell renderer for the {@link AppointmentTableModel}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-09-06 07:52:23Z $
 */
public class AppointmentTableHeaderRenderer implements TableCellRenderer {

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
     * Construct a new <code>AppointmentGridHeaderRenderer</code>, with the
     * default style.
     */
    protected AppointmentTableHeaderRenderer() {
    }

    /**
     * Returns a component that will be displayed at the specified coordinate in
     * the table.
     *
     * @param table  the <code>Table</code> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <code>TableModel</code> for
     *               the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a component representation  of the value
     */
    public Component getTableCellRendererComponent(Table table, Object value,
                                                   int column, int row) {
        String text = (String) value;
        Label label = LabelFactory.create(null, STYLE);
        label.setText(text);
        return label;
    }

}
