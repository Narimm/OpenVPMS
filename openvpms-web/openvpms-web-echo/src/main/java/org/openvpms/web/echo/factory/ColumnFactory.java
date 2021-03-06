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

package org.openvpms.web.echo.factory;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.layout.ColumnLayoutData;


/**
 * Factory for {@link Column}s.
 *
 * @author Tim Anderson
 */
public final class ColumnFactory extends ComponentFactory {

    /**
     * Create a new column.
     *
     * @return a new column
     */
    public static Column create() {
        return new Column();
    }

    /**
     * Create a new column.
     *
     * @param style the style name
     */
    public static Column create(String style) {
        Column column = create();
        column.setStyleName(style);
        return column;
    }

    /**
     * Create a column containing a set of components.
     *
     * @param components the components to add
     * @return a new column
     */
    public static Column create(Component... components) {
        Column column = create();
        add(column, components);
        return column;
    }

    /**
     * Create a new column with a specific style, and containing a set of
     * components.
     *
     * @param style      the style name
     * @param components the components to add
     * @return a new column
     */
    public static Column create(String style, Component... components) {
        Column column = create(style);
        add(column, components);
        return column;
    }

    /**
     * Creates layout data with the specified alignment.
     *
     * @param alignment the alignment
     * @return new layout data
     */
    public static ColumnLayoutData layout(Alignment alignment) {
        ColumnLayoutData result = new ColumnLayoutData();
        result.setAlignment(alignment);
        return result;
    }

}
