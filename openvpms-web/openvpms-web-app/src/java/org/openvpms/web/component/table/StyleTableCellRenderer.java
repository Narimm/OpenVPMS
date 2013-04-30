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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.table;

import nextapp.echo2.app.Table;


/**
 * <code>TableCellRender<code> that assigns a style to a cell.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class StyleTableCellRenderer extends AbstractTableCellRenderer {

    /**
     * The style name.
     */
    private final String _style;


    /**
     * Construct a new <code>StyleTableCellRenderer</code>.
     */
    public StyleTableCellRenderer(String style) {
        _style = style;
    }

    /**
     * Returns the style name for a column and row.
     *
     * @param table  the <code>Table</code> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <code>TableModel</code> for
     *               the specified coordinate
     * @param column the column
     * @param row    the row
     * @return a style name for the given column and row.
     */
    protected String getStyle(Table table, Object value, int column, int row) {
        return _style;
    }

}
