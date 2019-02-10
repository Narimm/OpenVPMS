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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.calendar;

import echopointng.table.TableCellRendererEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

/**
 * Header cell render for calendar columns.
 *
 * @author Tim Anderson
 */
public class CalendarHeaderCellRenderer implements TableCellRendererEx {

    /**
     * The singleton instance.
     */
    public static CalendarHeaderCellRenderer INSTANCE = new CalendarHeaderCellRenderer();

    /**
     * Default constructor.
     */
    protected CalendarHeaderCellRenderer() {

    }

    /**
     * Returns a {@code XhtmlFragment} that will be displayed as the content at the specified co-ordinate in the table.
     *
     * @param table  the {@code Table} for which the rendering is occurring
     * @param value  the value retrieved from the {@code TableModel} for the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a {@code XhtmlFragment} representation of the value
     */
    @Override
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
    @Override
    public Component getTableCellRendererComponent(Table table, Object value, int column, int row) {
        Component result;
        if (value instanceof Date) {
            Label day = LabelFactory.create(null, Styles.H3);
            Label name = LabelFactory.create();
            day.setText(Messages.format("calendar.day.date", value));
            name.setText(Messages.format("calendar.day.name", value));
            result = ColumnFactory.create("Calendar.Date", day, name);
        } else {
            result = ColumnFactory.create("Calendar.Date");
        }
        return result;
    }
}
