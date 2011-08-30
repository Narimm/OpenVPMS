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

package org.openvpms.web.component.im.layout;

import org.openvpms.web.component.im.view.ComponentState;

import java.util.ArrayList;
import java.util.List;


/**
 * Helper to organise components in a grid.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ComponentGrid {

    /**
     * The components.
     */
    private List<List<ComponentState>> components
            = new ArrayList<List<ComponentState>>();

    /**
     * The no. of rows.
     */
    private int rows;

    /**
     * The no. of columns
     */
    private int columns;


    /**
     * Creates a new <tt>ComponentGrid</tt>.
     */
    public ComponentGrid() {
    }

    /**
     * Creates a new <tt>ComponentGrid</tt>.
     *
     * @param set the set of components
     */
    public ComponentGrid(ComponentSet set) {
        add(set);
    }

    /**
     * Adds a set of components to the end of the grid.
     *
     * @param set the set of components
     */
    public void add(ComponentSet set) {
        ComponentState[] states = set.getComponents().toArray(new ComponentState[set.getComponents().size()]);
        int size = states.length;
        int columns = (size <= 2) ? 1 : 2;
        int rows;
        if (columns == 1) {
            rows = size;
        } else {
            rows = (size / 2) + (size % 2);
        }
        int index = 0;
        int start = components.size();
        int end = start + rows;
        for (int col = 0; col < columns; ++col) {
            for (int row = start; row < end && index < states.length; ++row) {
                set(row, col, states[index++]);
            }
        }
    }

    /**
     * Sets the component at the specified row and column.
     *
     * @param row    the row
     * @param column the column
     * @param state  the component
     */
    public void set(int row, int column, ComponentState state) {
        while (row >= components.size()) {
            components.add(new ArrayList<ComponentState>());
        }
        List<ComponentState> list = components.get(row);
        while (column >= list.size()) {
            list.add(null);
        }
        list.set(column, state);
        if (row >= rows) {
            rows = row + 1;
        }
        if (column >= columns) {
            columns = column + 1;
        }
    }

    /**
     * Returns the component at the specified row and column.
     *
     * @param row    the row
     * @param column the column
     * @return the corresponding component, or <tt>null</tt> if none is found
     */
    public ComponentState get(int row, int column) {
        List<ComponentState> list = components.get(row);
        return (column < list.size()) ? list.get(column) : null;
    }

    /**
     * Returns the number of rows.
     *
     * @return the number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the number of columns.
     *
     * @return the number of columns
     */
    public int getColumns() {
        return columns;
    }
}
