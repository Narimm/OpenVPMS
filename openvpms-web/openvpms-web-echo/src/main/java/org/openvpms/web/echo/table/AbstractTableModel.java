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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.table;

import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;

import java.util.Iterator;

/**
 * A {@code TableModel} with a {@code TableColumnModel}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractTableModel extends nextapp.echo2.app.table.AbstractTableModel {

    /**
     * The column model.
     */
    private TableColumnModel model;

    /**
     * Default constructor.
     */
    public AbstractTableModel() {
        super();
    }

    /**
     * Constructs an {@link AbstractTableModel}.
     *
     * @param model the column model
     */
    public AbstractTableModel(TableColumnModel model) {
        this.model = model;
    }

    /**
     * Returns the number of columns in the table.
     *
     * @return the column count
     */
    public int getColumnCount() {
        return model.getColumnCount();
    }

    /**
     * Returns the name of the specified column number.
     *
     * @param column the column index (0-based)
     * @return the column name
     */
    public String getColumnName(int column) {
        TableColumn col = getColumn(column);
        Object value = col.getHeaderValue();
        return (value != null) ? value.toString() : null;
    }

    /**
     * Returns the column model.
     *
     * @return the column model
     */
    public TableColumnModel getColumnModel() {
        return model;
    }

    /**
     * Returns a column given its model index.
     *
     * @param column the column index
     * @return the column
     */
    protected TableColumn getColumn(int column) {
        TableColumn result = null;
        Iterator iterator = model.getColumns();
        while (iterator.hasNext()) {
            TableColumn col = (TableColumn) iterator.next();
            if (col.getModelIndex() == column) {
                result = col;
                break;
            }
        }
        return result;
    }

    /**
     * Sets the column model.
     *
     * @param model the column model
     */
    protected void setTableColumnModel(TableColumnModel model) {
        this.model = model;
        fireTableStructureChanged();
    }

}
