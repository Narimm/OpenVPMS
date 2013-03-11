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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.event.ActionListener;
import nextapp.echo2.app.table.TableCellRenderer;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.LabelFactory;


/**
 * Header cell renderer for {@link SortableTableModel} backed tables.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class SortableTableHeaderRenderer implements TableCellRenderer {

    /**
     * The button style.
     */
    private final String style;

    /**
     * Up arrow resource path.
     */
    private static final String UP_ARROW_PATH
            = "/echopointng/resource/images/ArrowUp.gif";

    /**
     * Down arrow resource path.
     */
    private static final String DOWN_ARROW_PATH
            = "/echopointng/resource/images/ArrowDown.gif";

    /**
     * Up arrow image.
     */
    private static final ImageReference UP_ARROW
            = new ResourceImageReference(UP_ARROW_PATH);

    /**
     * Default style.
     */
    private static final String STYLE = "Table.Header";

    /**
     * Down arrow image.
     */
    private static final ImageReference DOWN_ARROW
            = new ResourceImageReference(DOWN_ARROW_PATH);

    /**
     * Construct a new <code>SortableTableHeaderRenderer</code>, with the
     * default style.
     */
    public SortableTableHeaderRenderer() {
        this(STYLE);
    }

    /**
     * Construct a new <code>SortableTableHeaderRenderer</code>.
     *
     * @param style the style name.
     */
    public SortableTableHeaderRenderer(String style) {
        this.style = style;
    }

    /**
     * Returns a component that will be displayed at the specified coordinate in
     * the table.
     *
     * @param table  the <code>Table</code> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <code>TableModel</code> for
     *               the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a component representation  of the value
     */
    public Component getTableCellRendererComponent(Table table, Object value,
                                                   int column, int row) {
        SortableTableModel model = (SortableTableModel) table.getModel();
        String text = (String) value;
        Component result;
        if (model.isSortable(column)) {
            result = getSortButton(text, column, model);
        } else {
            Label label = LabelFactory.create(null, style);
            label.setText(text);
            result = label;
        }
        return result;
    }

    /**
     * Returns a button to sort a column.
     *
     * @param label  the button label
     * @param column the column to sort
     * @param model  the table model
     * @return a button to sort <code>column</code>
     */
    protected Button getSortButton(String label, final int column,
                                   final SortableTableModel model) {
        Button button = new Button();
        button.setStyleName(style);
        button.setText(label);
        button.setFocusTraversalParticipant(false);
        button.addActionListener(new ActionListener() {
            public void onAction(ActionEvent e) {
                boolean ascending = false;
                if (column == model.getSortColumn()) {
                    ascending = model.isSortedAscending();
                }
                model.sort(column, !ascending);
            }
        });

        ImageReference icon;
        if (model.getSortColumn() == column) {
            icon = (model.isSortedAscending()) ? DOWN_ARROW : UP_ARROW;
            button.setIcon(icon);
        }

        return button;
    }

}
