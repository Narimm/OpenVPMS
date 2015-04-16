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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.table.TableCellRenderer;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

/**
 * Renders the day column headers for {@link MultiDayTableModel}.
 *
 * @author Tim Anderson
 */
class MultiDayTableHeaderRenderer implements TableCellRenderer {

    /**
     * The singleton instance.
     */
    public static final MultiDayTableHeaderRenderer INSTANCE = new MultiDayTableHeaderRenderer();

    /**
     * Returns a component that will be displayed at the specified coordinate in the table.
     *
     * @param table  the {@code Table} for which the rendering is occurring
     * @param value  the value retrieved from the {@code TableModel} for the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a component representation  of the value
     */
    @Override
    public Component getTableCellRendererComponent(Table table, Object value, int column, int row) {
        Component result;
        if (value instanceof Date) {
            Label label = LabelFactory.create();
            Date date = (Date) value;
            if (DateRules.compareDateToToday(date) == 0) {
                label.setStyleName("MultiDayTable.Today");
            } else {
                label.setStyleName("Table.Header");
            }
            label.setText(Messages.format("workflow.scheduling.appointment.column.day", date));
            result = label;
        } else {
            result = new Label();
        }
        return result;
    }
}
