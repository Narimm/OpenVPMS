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
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListCellRenderer;
import nextapp.echo2.app.list.ListModel;
import nextapp.echo2.app.list.ListSelectionModel;
import org.apache.commons.collections.ListUtils;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.ListBoxFactory;

import java.util.ArrayList;
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
public class Palette<T> extends Row {

    /**
     * The set of unselected items.
     */
    private final List<T> unselected;

    /**
     * The set of selected items.
     */
    private final List<T> selected;

    /**
     * The unselected item list box.
     */
    private ListBox unselectedList;

    /**
     * The selected item list box.
     */
    private ListBox selectedList;

    /**
     * The add button.
     */
    private Button add;

    /**
     * The remove button.
     */
    private Button remove;


    /**
     * Construct a new <code>Palette</code>,
     *
     * @param items    all items that may be selected
     * @param selected the selected items
     */
    @SuppressWarnings("unchecked")
    public Palette(List<T> items, List<T> selected) {
        setStyleName("Palette");
        this.selected = selected;
        sort(selected);
        unselected = (List<T>) ListUtils.subtract(items, selected);
        sort(unselected);

        doLayout();
    }

    /**
     * Set the cell renderer.
     *
     * @param renderer the cell renderer
     */
    public void setCellRenderer(ListCellRenderer renderer) {
        unselectedList.setCellRenderer(renderer);
        selectedList.setCellRenderer(renderer);
    }

    /**
     * Sets the focus traversal (tab) index of the component.
     *
     * @param newValue the new focus traversal index
     * @see #getFocusTraversalIndex()
     */
    @Override
    public void setFocusTraversalIndex(int newValue) {
        unselectedList.setFocusTraversalIndex(newValue);
        selectedList.setFocusTraversalIndex(newValue);
        add.setFocusTraversalIndex(newValue);
        remove.setFocusTraversalIndex(newValue);
    }

    /**
     * Lay out the component.
     */
    protected void doLayout() {
        final int mode = ListSelectionModel.MULTIPLE_SELECTION;
        unselectedList = ListBoxFactory.create(unselected);
        unselectedList.setSelectionMode(mode);

        selectedList = ListBoxFactory.create(selected);
        selectedList.setSelectionMode(mode);

        if (!unselected.isEmpty()) {
            // make sure there is only one selected list to start off with
            selectedList.getSelectionModel().clearSelection();
        }

        add = ButtonFactory.create("right_add", new ActionListener() {
            public void onAction(ActionEvent e) {
                onAdd();
            }
        });
        remove = ButtonFactory.create("left_remove", new ActionListener() {
            public void onAction(ActionEvent e) {
                onRemove();
            }
        });
        Label available = LabelFactory.create("available", "Palette.ListLabel");
        Label selected = LabelFactory.create("selected", "Palette.ListLabel");
        Column left = ColumnFactory.create("Palette.ListColumn", available,
                                           unselectedList);
        Column middle = ColumnFactory.create("ControlColumn", add, remove);
        Column right = ColumnFactory.create("Palette.ListColumn", selected,
                                            selectedList);
        add(left);
        add(middle);
        add(right);
    }

    /**
     * Invoked when the add button is pressed.
     */
    protected void onAdd() {
        Object[] values = unselectedList.getSelectedValues();
        move(unselectedList, selectedList);
        add(values);
    }

    /**
     * Invoked when the remove button is pressed.
     */
    protected void onRemove() {
        Object[] values = selectedList.getSelectedValues();
        move(selectedList, unselectedList);
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
    @SuppressWarnings("unchecked")
    protected void move(ListBox from, ListBox to) {
        Object[] values = from.getSelectedValues();
        DefaultListModel fromModel = (DefaultListModel) from.getModel();
        List<T> toValues = getValues(to.getModel());

        for (int index : from.getSelectedIndices()) {
            // @todo workaround for OVPMS-303
            from.setSelectedIndex(index, false);
        }
        for (Object value : values) {
            T object = (T) value;
            fromModel.remove(value);
            toValues.add(object);
        }
        sort(toValues);
        DefaultListModel toModel = new DefaultListModel(toValues.toArray());
        to.setModel(toModel);

        int[] selected = new int[values.length];
        for (int i = 0; i < values.length; ++i) {
            selected[i] = toModel.indexOf(values[i]);
        }
        to.setSelectedIndices(selected);
    }

    /**
     * Sorts a list.
     * <p/>
     * This implementation is a no-op
     *
     * @param values the list to sort
     */
    @SuppressWarnings("unused")
    protected void sort(List<T> values) {

    }

    @SuppressWarnings("unchecked")
    private List<T> getValues(ListModel model) {
        List<T> result = new ArrayList<T>();
        for (int i = 0; i < model.size(); ++i) {
            result.add((T) model.get(i));
        }
        return result;
    }

}
