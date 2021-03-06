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

package org.openvpms.web.echo.factory;

import echopointng.layout.TableLayoutDataEx;
import nextapp.echo2.app.LayoutData;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.table.TableModel;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.KeyTable;


/**
 * Factory for {@link Table}s.
 *
 * @author Tim Anderson
 */
public final class TableFactory extends ComponentFactory {

    /**
     * Create a new table.
     *
     * @param model the table model
     * @return a new table
     */
    public static Table create(TableModel model) {
        return create(model, Styles.DEFAULT);
    }

    /**
     * Create a new table.
     *
     * @param model     the table model
     * @param styleName the table style name
     * @return a new table
     */
    public static Table create(TableModel model, String styleName) {
        Table table = new KeyTable();
        table.setModel(model);
        table.setStyleName(styleName);
        return table;
    }

    /**
     * Helper to create a layout with the row and column span set.
     *
     * @param column the column span
     * @param row    the row span
     * @return a new layout data
     */
    public static LayoutData span(int column, int row) {
        TableLayoutDataEx layout = new TableLayoutDataEx();
        layout.setColSpan(column);
        layout.setRowSpan(row);
        return layout;
    }

    /**
     * Helper to create a layout with the row span set.
     *
     * @param span the row span
     * @return a new layout data
     */
    public static TableLayoutDataEx rowSpan(int span) {
        TableLayoutDataEx layout = new TableLayoutDataEx();
        layout.setRowSpan(span);
        return layout;
    }

    /**
     * Helper to create a layout with the column span set.
     *
     * @param span the column span
     * @return a new layout data
     */
    public static LayoutData columnSpan(int span) {
        TableLayoutDataEx layout = new TableLayoutDataEx();
        layout.setColSpan(span);
        return layout;
    }
}
