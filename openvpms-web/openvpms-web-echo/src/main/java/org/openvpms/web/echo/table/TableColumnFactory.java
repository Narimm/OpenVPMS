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

package org.openvpms.web.echo.table;

import nextapp.echo2.app.table.TableCellRenderer;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.web.resource.i18n.Messages;

/**
 * Factory for {@code TableColumn}s.
 *
 * @author Tim Anderson
 */
public class TableColumnFactory {

    /**
     * Creates a table column.
     *
     * @param index the column model index
     * @param value the header value
     * @return a new table column
     */
    public static TableColumn create(int index, Object value) {
        TableColumn column = new TableColumn(index);
        column.setHeaderValue(value);
        return column;
    }

    /**
     * Creates a table column.
     *
     * @param index          the column model index
     * @param headerRenderer the header renderer. May be {@code null}
     * @param cellRenderer   the cell renderer. May be {@code null}
     * @return a new table column
     */
    public static TableColumn create(int index, TableCellRenderer headerRenderer, TableCellRenderer cellRenderer) {
        return create(index, null, headerRenderer, cellRenderer);
    }

    /**
     * Creates a table column.
     *
     * @param index          the column model index
     * @param value          the header value. May be {@code null}
     * @param headerRenderer the header renderer. May be {@code null}
     * @param cellRenderer   the cell renderer. May be {@code null}
     * @return a new table column
     */
    public static TableColumn create(int index, Object value, TableCellRenderer headerRenderer,
                                     TableCellRenderer cellRenderer) {
        TableColumn column = new TableColumn(index);
        column.setHeaderValue(value);
        column.setHeaderRenderer(headerRenderer);
        column.setCellRenderer(cellRenderer);
        return column;
    }

    /**
     * Creates a table column with the header coming from a resource key.
     *
     * @param index     the column model index
     * @param headerKey the header label resource key
     * @return a new table column
     */
    public static TableColumn createKey(int index, String headerKey) {
        TableColumn column = new TableColumn(index);
        column.setHeaderValue(Messages.get(headerKey));
        return column;
    }

    /**
     * Creates a table column with the header coming from a resource key.
     *
     * @param index          the column model index
     * @param headerKey      the header label resource key
     * @param headerRenderer the header cell renderer. May be {@code null}
     * @param cellRenderer   the cell renderer. May be {@code null}
     * @return a new table column
     */
    public static TableColumn createKey(int index, String headerKey, TableCellRenderer headerRenderer,
                                        TableCellRenderer cellRenderer) {
        TableColumn column = createKey(index, headerKey);
        column.setHeaderRenderer(headerRenderer);
        column.setCellRenderer(cellRenderer);
        return column;
    }
}
