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
import nextapp.echo2.app.table.TableCellRenderer;


/**
 * TableCellRender that assigns even and odd rows a different style.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class EvenOddTableCellRenderer extends AbstractTableCellRenderer {

    /**
     * The style of the even rows.
     */
    public static final String EVEN_STYLE = "Table.EvenRow";

    /**
     * The style of the inset even rows.
     */
    public static final String EVEN_STYLE_INSET = "Table.EvenRow-InsetX";

    /**
     * The style of the odd rows.
     */
    public static final String ODD_STYLE = "Table.OddRow";

    /**
     * The style of the inset odd rows.
     */
    public static final String ODD_STYLE_INSET = "Table.OddRow-InsetX";

    /**
     * The singleton instance.
     */
    public static TableCellRenderer INSTANCE = new EvenOddTableCellRenderer();


    /**
     * Default constructor.
     */
    private EvenOddTableCellRenderer() {
    }

    /**
     * Returns the style name for a column and row.
     *
     * @param table  the <tt>Table</tt> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <tt>TableModel</tt> for
     *               the specified coordinate
     * @param column the column
     * @param row    the row
     * @return a style name for the given column and row.
     */
    protected String getStyle(Table table, Object value, int column, int row) {
        return (row % 2 == 0) ? EVEN_STYLE : ODD_STYLE;
    }

}
