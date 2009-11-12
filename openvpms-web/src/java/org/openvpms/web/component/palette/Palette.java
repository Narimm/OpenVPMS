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

package org.openvpms.web.component.palette;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.event.ActionListener;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListCellRenderer;
import nextapp.echo2.app.list.ListSelectionModel;
import org.apache.commons.collections.ListUtils;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.ListBoxFactory;

import java.util.List;


/**
 * A component used to make a number of selections from a list. Items available
 * for selection are displayed in an 'available' list box. Selected Items are
 * displayed in a 'selected' list box. Items are moved from one to the other
 * using a pair of buttons.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Palette extends Row {

    /**
     * The set of unselected items.
     */
    private final List _unselected;

    /**
     * The set of selected items.
     */
    private final List _selected;

    /**
     * The unselected item list box.
     */
    private ListBox _unselectedList;

    /**
     * The selected item list box.
     */
    private ListBox _selectedList;

    /**
     * The add button.
     */
    private Button _add;

    /**
     * The remove button.
     */
    private Button _remove;


    /**
     * Construct a new <code>Palette</code>,
     *
     * @param items    all items that may be selected
     * @param selected the selected items
     */
    public Palette(List items, List selected) {
        setStyleName("Palette");
        _selected = selected;
        _unselected = ListUtils.subtract(items, selected);

        doLayout();
    }

    /**
     * Set the cell renderer.
     *
     * @param renderer the cell renderer
     */
    public void setCellRenderer(ListCellRenderer renderer) {
        _unselectedList.setCellRenderer(renderer);
        _selectedList.setCellRenderer(renderer);
    }

    /**
     * Sets the focus traversal (tab) index of the component.
     *
     * @param newValue the new focus traversal index
     * @see #getFocusTraversalIndex()
     */
    @Override
    public void setFocusTraversalIndex(int newValue) {
        _unselectedList.setFocusTraversalIndex(newValue);
        _selectedList.setFocusTraversalIndex(newValue);
        _add.setFocusTraversalIndex(newValue);
        _remove.setFocusTraversalIndex(newValue);
    }

    /**
     * Lay out the component.
     */
    protected void doLayout() {
        final int mode = ListSelectionModel.MULTIPLE_SELECTION;
        _unselectedList = ListBoxFactory.create(_unselected);
        _unselectedList.setStyleName("Palette.List");
        _unselectedList.setSelectionMode(mode);

        _selectedList = ListBoxFactory.create(_selected);
        _selectedList.setStyleName("Palette.List");
        _selectedList.setSelectionMode(mode);

        _add = ButtonFactory.create("right_add", new ActionListener() {
            public void onAction(ActionEvent e) {
                onAdd();
            }
        });
        _remove = ButtonFactory.create("left_remove", new ActionListener() {
            public void onAction(ActionEvent e) {
                onRemove();
            }
        });
        Label available = LabelFactory.create("available", "Palette.ListLabel");
        Label selected = LabelFactory.create("selected", "Palette.ListLabel");
        Column left = ColumnFactory.create("Palette.ListColumn", available,
                                           _unselectedList);
        Column middle = ColumnFactory.create("ControlColumn", _add, _remove);
        Column right = ColumnFactory.create("Palette.ListColumn", selected,
                                            _selectedList);
        add(left);
        add(middle);
        add(right);
    }

    /**
     * Invoked when the add button is pressed.
     */
    protected void onAdd() {
        Object[] values = _unselectedList.getSelectedValues();
        move(_unselectedList, _selectedList);
        add(values);
    }

    /**
     * Invoked when the remove button is pressed.
     */
    protected void onRemove() {
        Object[] values = _selectedList.getSelectedValues();
        move(_selectedList, _unselectedList);
        remove(values);
    }

    /**
     * Add items to the 'selected' list.
     *
     * @param values the values to add.
     */
    protected void add(Object[] values) {
    }

    /**
     * Remove items from the 'selected' list.
     *
     * @param values the values to remove
     */
    protected void remove(Object[] values) {
    }

    /**
     * Move items from one list to another.
     *
     * @param from the source list box
     * @param to   the target list box
     */
    protected void move(ListBox from, ListBox to) {
        Object[] values = from.getSelectedValues();
        DefaultListModel toModel = (DefaultListModel) to.getModel();
        DefaultListModel fromModel = (DefaultListModel) from.getModel();

        for (int index : from.getSelectedIndices()) {
            // @todo workaround for OVPMS-303
            from.setSelectedIndex(index, false);
        }
        for (Object value : values) {
            toModel.add(value);
            fromModel.remove(value);
        }
    }

}
