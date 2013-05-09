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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.echo.table;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.table.TableCellRenderer;
import org.openvpms.web.echo.factory.LabelFactory;


/**
 * Default table header cell renderer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-12-29 00:53:51 +1100 (Sat, 29 Dec 2007) $
 */
public class DefaultTableHeaderRenderer implements TableCellRenderer {

    /**
     * The default instance.
     */
    public static final TableCellRenderer DEFAULT
        = new DefaultTableHeaderRenderer();

    /**
     * The button style.
     */
    private final String style;

    /**
     * Default style.
     */
    private static final String STYLE = "Table.Header";


    /**
     * Construct a new <tt>DefaultTableHeaderRenderer</tt>, with the
     * default style.
     */
    public DefaultTableHeaderRenderer() {
        this(DefaultTableHeaderRenderer.STYLE);
    }

    /**
     * Construct a new <tt>DefaultTableHeaderRenderer</tt>.
     *
     * @param style the style name.
     */
    public DefaultTableHeaderRenderer(String style) {
        this.style = style;
    }

    /**
     * Returns a component that will be displayed at the specified coordinate in
     * the table.
     *
     * @param table  the <tt>Table</tt> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <tt>TableModel</tt> for
     *               the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a component representation  of the value
     */
    public Component getTableCellRendererComponent(Table table, Object value,
                                                   int column, int row) {
        String text = (String) value;
        Label label = LabelFactory.create(null, style);
        label.setText(text);
        return label;
    }

}
