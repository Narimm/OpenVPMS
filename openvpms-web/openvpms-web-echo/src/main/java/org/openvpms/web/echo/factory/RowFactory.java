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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.factory;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.LayoutData;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.web.echo.style.Styles;


/**
 * Factory for {@link Row}s.
 *
 * @author Tim Anderson
 */
public final class RowFactory extends ComponentFactory {

    /**
     * Create a new row.
     *
     * @return a new row
     */
    public static Row create() {
        return new Row();
    }

    /**
     * Create a new row, and containing a set of components.
     *
     * @param components the components to add
     * @return a new row
     */
    public static Row create(Component... components) {
        Row row = create();
        add(row, components);
        return row;
    }

    /**
     * Create a new row with a specific style, and containing a set of components.
     *
     * @param style      the row style
     * @param components the components to add
     * @return a new row
     */
    public static Row create(String style, Component... components) {
        Row row = create(components);
        setStyle(row, style);
        return row;
    }

    /**
     * Create a new row with a specific style and layout, and containing a set of components.
     *
     * @param style      the row style
     * @param layout     the layout data
     * @param components the components to add
     * @return a new row
     */
    public static Row create(String style, LayoutData layout, Component... components) {
        Row row = create(style, components);
        row.setLayoutData(layout);
        return row;
    }

    /**
     * Creates layout data with the specified alignment.
     *
     * @param alignment the alignment
     * @return new layout data
     */
    public static RowLayoutData layout(Alignment alignment) {
        RowLayoutData result = new RowLayoutData();
        result.setAlignment(alignment);
        return result;
    }

    /**
     * Creates a layout data with the specified width.
     *
     * @param width the width
     * @return new layout data
     */
    public static LayoutData layout(Extent width) {
        RowLayoutData result = new RowLayoutData();
        result.setWidth(width);
        return result;
    }

    /**
     * Creates layout data with the specified alignment and width.
     *
     * @param alignment the alignment
     * @param width     the width
     * @return new layout data
     */
    public static RowLayoutData layout(Alignment alignment, Extent width) {
        RowLayoutData result = layout(alignment);
        result.setWidth(width);
        return result;
    }

    /**
     * Creates a right-aligned layout with full width. This can be used to right align a {@code Row} within
     * another {@code Row}.
     *
     * @return new layout data
     */
    public static RowLayoutData rightAlign() {
        return layout(Alignment.ALIGN_RIGHT, Styles.FULL_WIDTH);
    }

}
