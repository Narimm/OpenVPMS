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
 */

package org.openvpms.web.component.im.layout;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.layout.GridLayoutData;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Helper to organise components in a grid.
 *
 * @author Tim Anderson
 */
public class ComponentGrid {

    /**
     * The components, in rows and columns.
     */
    private List<List<State>> components = new ArrayList<List<State>>();

    /**
     * The no. of rows.
     */
    private int rows;

    /**
     * The no. of columns
     */
    private int columns;


    /**
     * Constructs a {@code ComponentGrid}.
     */
    public ComponentGrid() {
    }

    /**
     * Adds a set of components to the end of the grid.
     *
     * @param set the set of components
     */
    public void add(ComponentSet set) {
        int columns = set.getComponents().size() <= 2 ? 1 : 2;
        add(set, columns);
    }

    /**
     * Adds a set of components to the end of the grid.
     *
     * @param set     the set of components
     * @param columns the number of columns to use
     */
    public void add(ComponentSet set, int columns) {
        add(set, columns, 1);
    }

    /**
     * Adds a set of components to the end of the grid.
     *
     * @param set        the set of components
     * @param columns    the number of columns to use
     * @param columnSpan the number of columns the components span
     */
    public void add(ComponentSet set, int columns, int columnSpan) {
        ComponentState[] states = set.getComponents().toArray(new ComponentState[set.getComponents().size()]);
        int size = states.length;
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
                set(row, col, columnSpan, states[index++]);
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
        set(row, column, 1, state);
    }

    /**
     * Sets the component at the specified row and column.
     *
     * @param row        the row
     * @param column     the column
     * @param columnSpan the number of columns the component spans
     * @param state      the component
     */
    public void set(int row, int column, int columnSpan, ComponentState state) {
        while (row >= components.size()) {
            components.add(new ArrayList<State>());
        }
        List<State> list = components.get(row);
        while (column >= list.size()) {
            list.add(new State());
        }
        list.set(column, new State(state, columnSpan));
        if (row >= rows) {
            rows = row + 1;
        }
        if (column >= columns) {
            columns = column + 1;
        }
    }

    /**
     * Returns the number of columns.
     *
     * @return the number of columns
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Creates a grid.
     *
     * @param set the set to manage focus traversal
     * @return a new grid
     */
    public Grid createGrid(ComponentSet set) {
        Grid grid = GridFactory.create(columns * 2);
        for (int row = 0; row < rows; ++row) {
            for (int col = 0; col < columns; ) {
                State state = getState(row, col);
                if (state != null && state.getComponentState() != null) {
                    if (state.getComponentState() != null) {
                        Component component = state.getComponent();
                        if (component instanceof SelectField) {
                            // workaround for render bug in firefox. See OVPMS-239
                            component = RowFactory.create(component);
                        }
                        grid.add(state.getLabel());
                        int span = state.getColumnSpan();
                        if (span > 1) {
                            GridLayoutData layout = new GridLayoutData();
                            layout.setColumnSpan(span + 1);
                            component.setLayoutData(layout);
                        }
                        grid.add(component);
                        col += span;
                    }
                } else {
                    grid.add(LabelFactory.create());
                    grid.add(LabelFactory.create());
                    col++;
                }
            }
        }
        // set the focus traversal in column order
        for (int col = 0; col < columns; ++col) {
            for (int row = 0; row < rows; ++row) {
                State state = getState(row, col);
                if (state != null && state.getComponentState() != null) {
                    set.setFocusTraversal(state.getComponentState());
                }
            }
        }
        return grid;
    }

    /**
     * Returns the component at the specified row and column.
     *
     * @param row    the row
     * @param column the column
     * @return the corresponding component, or <tt>null</tt> if none is found
     */
    private State getState(int row, int column) {
        List<State> list = components.get(row);
        return (column < list.size()) ? list.get(column) : null;
    }

    /**
     * The component state.
     */
    private static class State {

        /**
         * The component state.
         */
        private final ComponentState state;

        /**
         * The number of columns the component spans. Note that this refers to virtual columns
         * (i.e a label + component is one column)
         */
        private int columnSpan = 1;

        /**
         * Default constructor.
         */
        public State() {
            this(null, 1);
        }

        /**
         * Constructs a {@code State}.
         *
         * @param state      the component state
         * @param columnSpan the column span
         */
        public State(ComponentState state, int columnSpan) {
            this.state = state;
            this.columnSpan = columnSpan;
        }

        /**
         * Returns the column span.
         *
         * @return the column span
         */
        public int getColumnSpan() {
            return columnSpan;
        }

        /**
         * Returns the component state.
         *
         * @return the component state. May be {@code null}
         */
        public ComponentState getComponentState() {
            return state;
        }

        /**
         * Returns the component label.
         *
         * @return the component label
         */
        public Label getLabel() {
            return state.getLabel();
        }

        /**
         * Returns the component
         *
         * @return the component. May be {@code null}
         */
        public Component getComponent() {
            return (state != null) ? state.getComponent() : null;
        }

    }

}
