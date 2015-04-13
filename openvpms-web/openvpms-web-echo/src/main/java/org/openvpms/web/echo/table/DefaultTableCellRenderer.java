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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.table;

import nextapp.echo2.app.Table;
import nextapp.echo2.app.table.TableCellRenderer;

/**
 * Default implementation of the <tt>TableCellRenderer</tt> interface.
 *
 * @author Tim Anderson
 */
public class DefaultTableCellRenderer extends AbstractTableCellRenderer {

    /**
     * The singleton instance.
     */
    public static TableCellRenderer INSTANCE = new DefaultTableCellRenderer();

    /**
     * Default constructor.
     */
    private DefaultTableCellRenderer() {

    }

    /**
     * Returns the style name for a column and row.
     * <p/>This implementation returns <tt>null</tt>
     *
     * @param table  the <tt>Table</tt> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <tt>TableModel</tt> for the
     *               specified coordinate
     * @param column the column
     * @param row    the row
     * @return a style name for the given column and row. May be <tt>null</tt>
     */
    @Override
    protected String getStyle(Table table, Object value, int column, int row) {
        return "Table.Row";
    }
}
