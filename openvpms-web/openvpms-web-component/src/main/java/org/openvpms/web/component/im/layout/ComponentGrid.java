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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.layout;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.layout.GridLayoutData;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Helper to organise components in a grid.
 *
 * @author Tim Anderson
 */
public class ComponentGrid {

    /**
     * The grid cells, in rows and columns.
     */
    private List<List<Cell>> cells = new ArrayList<List<Cell>>();

    /**
     * The no. of grid columns
     */
    private int columns;

    /**
     * Grid column widths.
     */
    private ArrayList<Extent> columnWidths = new ArrayList<Extent>();

    /**
     * Indicates a cell that is spanned by another.
     */
    private static final Cell SPAN = new Cell();

    /**
     * Constructs a {@code ComponentGrid}.
     */
    public ComponentGrid() {
    }

    /**
     * Adds components to the end of the grid, one component per grid column.
     *
     * @param components the component to add
     */
    public void add(Component... components) {
        List<Cell> row = new ArrayList<Cell>();
        for (Component component : components) {
            row.add(new Cell(component));
        }
        addRow(row);
    }

    /**
     * Adds a component to the end of the grid.
     *
     * @param state           the component state to add
     * @param columnGroupSpan the number of column groups the component spans
     */
    public void add(ComponentState state, int columnGroupSpan) {
        ComponentState[] states = new ComponentState[]{state};
        add(states, 1, columnGroupSpan);
    }

    /**
     * Adds components to the end of the grid.
     * <p/>
     * Each state spans two grid columns. If a state has a label, then this is displayed in the first column, and
     * the component in the second. If the state has no label, then the component is displayed in the first column
     * and spans the second.
     *
     * @param states the component states to add
     */
    public void add(ComponentState... states) {
        List<Cell> row = new ArrayList<Cell>();
        for (ComponentState state : states) {
            if (state.hasLabel()) {
                row.add(new Cell(state.getLabel()));
                row.add(new Cell(state.getComponent(), state));
            } else {
                row.add(new Cell(state.getComponent(), state));
                row.add(null);
            }
        }
        addRow(row);
    }

    /**
     * Adds a set of components to the end of the grid.
     * <p/>
     * For sets of of {@code <= 2} components, one column group will be used, otherwise 2 column groups will be used.
     * <br/>
     * This corresponds to 2 and 4 grid columns respectively.
     *
     * @param set the set of components
     */
    public void add(ComponentSet set) {
        int columnGroups = set.getComponents().size() <= 2 ? 1 : 2;
        add(set, columnGroups);
    }

    /**
     * Adds a set of components to the end of the grid.
     *
     * @param set          the set of components
     * @param columnGroups the number of column groups to use. Each column group spans two cells
     */
    public void add(ComponentSet set, int columnGroups) {
        add(set, columnGroups, 0);
    }

    /**
     * Adds a set of components to the end of the grid.
     *
     * @param set             the set of components
     * @param columnGroups    the number of column groups to use. Each column group spans two cells
     * @param columnGroupSpan the number of column groups the components span
     */
    public void add(ComponentSet set, int columnGroups, int columnGroupSpan) {
        ComponentState[] states = set.getComponents().toArray(new ComponentState[set.getComponents().size()]);
        add(states, columnGroups, columnGroupSpan);
    }

    /**
     * Sets the component at the specified row and column.
     *
     * @param row       the row
     * @param column    the column
     * @param component the component
     */
    public void set(int row, int column, Component component) {
        set(row, column, 1, component);
    }

    /**
     * Sets the component at the specified row and column.
     *
     * @param row        the row
     * @param column     the column
     * @param columnSpan the no. of cell columns to span
     * @param component  the component
     */
    public void set(int row, int column, int columnSpan, Component component) {
        set(row, column, 1, columnSpan, component);
    }

    /**
     * Sets the component at the specified row and column.
     *
     * @param row        the row
     * @param column     the column
     * @param rowSpan    the no. of rows to span
     * @param columnSpan the no. of columns to span
     * @param component  the component
     */
    public void set(int row, int column, int rowSpan, int columnSpan, Component component) {
        set(row, column, layout(rowSpan, columnSpan), component);
    }

    /**
     * Sets the component at the specified row and column.
     *
     * @param row        the row
     * @param column     the column
     * @param layoutData the layout data. May be {@code null}
     * @param component  the component
     */
    public void set(int row, int column, GridLayoutData layoutData, Component component) {
        int rowSpan = 1;
        int columnSpan = 1;
        if (layoutData != null) {
            rowSpan = layoutData.getRowSpan();
            columnSpan = layoutData.getColumnSpan();
        }
        for (int i = row; i < row + rowSpan; ++i) {
            for (int j = column; j < column + columnSpan; ++j) {
                set(i, j, SPAN);
            }
        }
        set(row, column, new Cell(component, null, layoutData));
    }

    /**
     * Sets the component at the specified row and column.
     *
     * @param row    the row
     * @param column the column.
     * @param state  the component
     */
    public void set(int row, int column, ComponentState state) {
        set(row, column, 0, state);
    }

    /**
     * Sets the component at the specified row and column.
     *
     * @param row             the row
     * @param column          the column
     * @param columnGroupSpan the number of column groups the components span
     * @param state           the component
     */
    public void set(int row, int column, int columnGroupSpan, ComponentState state) {
        int span = 2 * columnGroupSpan;
        if (state.hasLabel()) {
            set(row, column++, new Cell(state.getLabel()));
            if (span > 0) {
                span--;
            }
        }
        set(row, column++, new Cell(state.getComponent(), state, layout(1, span)));
        for (int i = 0; i < span - 1; i++) {
            set(row, column + i, SPAN);  // mark the remaining columns as being spanned
        }
    }

    /**
     * Sets the width of a column.
     *
     * @param column the column
     * @param width  the column width
     */
    public void setColumnWidth(int column, Extent width) {
        while (column >= columnWidths.size()) {
            columnWidths.add(null);
        }
        columnWidths.set(column, width);
    }

    /**
     * Helper to create a layout for a grid cell.
     *
     * @param rowSpan    the row span
     * @param columnSpan the column span
     * @return a new layout
     */
    public static GridLayoutData layout(int rowSpan, int columnSpan) {
        return layout(rowSpan, columnSpan, null);
    }

    /**
     * Helper to create a layout for a grid cell.
     *
     * @param rowSpan    the row span
     * @param columnSpan the column span
     * @param alignment  the cell alignment. May be {@code null}
     * @return a new layout
     */
    public static GridLayoutData layout(int rowSpan, int columnSpan, Alignment alignment) {
        GridLayoutData layoutData = new GridLayoutData();
        layoutData.setRowSpan(rowSpan);
        layoutData.setColumnSpan(columnSpan);
        layoutData.setAlignment(alignment);
        return layoutData;
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
        Grid grid = createGrid();
        int rows = cells.size();
        // set the focus traversal in column order
        for (int col = 0; col < columns; ++col) {
            for (int row = 0; row < rows; ++row) {
                Cell cell = getCell(row, col);
                if (cell != null && cell.getComponentState() != null) {
                    set.setFocusTraversal(cell.getComponentState());
                }
            }
        }
        return grid;
    }

    /**
     * Creates a grid.
     *
     * @return a new grid
     */
    public Grid createGrid() {
        Grid grid = GridFactory.create(columns);
        for (int i = 0; i < columnWidths.size(); ++i) {
            grid.setColumnWidth(i, columnWidths.get(i));
        }
        int rows = cells.size();
        for (int row = 0; row < rows; ++row) {
            for (int col = 0; col < columns; ) {
                Cell cell = getCell(row, col);
                if (cell != null && cell.getComponent() != null) {
                    if (cell.getComponent() != null) {
                        Component component = cell.getComponent();
                        if (component instanceof SelectField) {
                            // workaround for render bug in firefox. See OVPMS-239
                            component = RowFactory.create(component);
                        }
                        int columnSpan = cell.getColumnSpan();
                        if (columnSpan <= 1) {
                            columnSpan = 1;
                        }
                        component.setLayoutData(cell.getLayoutData());
                        grid.add(component);
                        col += columnSpan;
                    }
                } else if (cell != SPAN) {
                    grid.add(LabelFactory.create());
                    col++;
                } else {
                    col++;
                }
            }
        }
        return grid;
    }

    /**
     * Adds a set of components to the end of the grid.
     *
     * @param states          the component states to add
     * @param columnGroups    the number of column groups to use. Each column spans 2 cells
     * @param columnGroupSpan the number of column groups the components span
     */
    protected void add(ComponentState[] states, int columnGroups, int columnGroupSpan) {
        int size = states.length;
        int rows;
        if (columnGroups == 1) {
            rows = size;
        } else {
            rows = (size / 2) + (size % 2);
        }
        add(states, columnGroups, rows, columnGroupSpan);
    }

    /**
     * Adds a set of components to the end of the grid.
     *
     * @param states          the component states to add
     * @param columns         the number of columns to use
     * @param rows            the number of rows to use
     * @param columnGroupSpan the number of column groups the components span
     */
    protected void add(ComponentState[] states, int columns, int rows, int columnGroupSpan) {
        int index = 0;
        int start = cells.size();
        int end = start + rows;
        for (int col = 0; col < columns; ++col) {
            for (int row = start; row < end && index < states.length; ++row) {
                set(row, col * 2, columnGroupSpan, states[index++]);
            }
        }
    }

    /**
     * Sets a cell.
     *
     * @param row    the row
     * @param column the column
     * @param cell   the cell. May be {@code null}
     */
    private void set(int row, int column, Cell cell) {
        List<Cell> cells = grow(row, column);
        cells.set(column, cell);
    }

    /**
     * Grows the grid to fit the specified row and column.
     *
     * @param row    the row
     * @param column the column
     * @return the cells corresponding to {@code row}
     */
    private List<Cell> grow(int row, int column) {
        while (row >= cells.size()) {
            cells.add(new ArrayList<Cell>());
        }
        List<Cell> list = cells.get(row);
        while (column >= list.size()) {
            list.add(null);
        }
        if (column >= columns) {
            columns = column + 1;
        }
        return list;
    }

    /**
     * Adds a row of cells to the end of the grid.
     *
     * @param row the row to add
     */
    private void addRow(List<Cell> row) {
        cells.add(row);
        if (columns < row.size()) {
            columns = row.size();
        }
    }

    /**
     * Returns the cell at the specified row and column.
     *
     * @param row    the row
     * @param column the column
     * @return the corresponding cell, or {@code null} if none is found
     */
    private Cell getCell(int row, int column) {
        List<Cell> list = cells.get(row);
        return (column < list.size()) ? list.get(column) : null;
    }

    /**
     * Represents a grid cell.
     */
    private static class Cell {

        /**
         * The component. May be {@code null}.
         */
        private final Component component;

        /**
         * The component state that the component is from. May be {@code null}
         */
        private final ComponentState state;

        /**
         * The cell layout data. May be {@code null}.
         */
        private final GridLayoutData layoutData;

        /**
         * Default constructor.
         */
        public Cell() {
            this(null);
        }

        /**
         * Constructs a {@link Cell}.
         *
         * @param component the component. May be {@code null}
         */
        public Cell(Component component) {
            this(component, null);
        }

        /**
         * Constructs a {@link Cell}.
         *
         * @param component the component. May be {@code null}
         * @param state     the component state that the component is from. May be {@code null}
         */
        public Cell(Component component, ComponentState state) {
            this(component, state, null);
        }

        /**
         * Constructs a {@link Cell}.
         *
         * @param component  the component
         * @param state      the component state that the component is from. May be {@code null}
         * @param layoutData the layoutData. May be {@code null}
         */
        public Cell(Component component, ComponentState state, GridLayoutData layoutData) {
            this.component = component;
            this.state = state;
            this.layoutData = layoutData;
        }

        /**
         * Returns the column span.
         *
         * @return the column span
         */
        public int getColumnSpan() {
            return layoutData != null ? layoutData.getColumnSpan() : 1;
        }

        /**
         * Returns the component
         *
         * @return the component. May be {@code null}
         */
        public Component getComponent() {
            return component;
        }

        /**
         * Returns the component state that the component comes from.
         *
         * @return the component state, or {@code null} if the component isn't associated with any state
         */
        public ComponentState getComponentState() {
            return state;
        }

        /**
         * Returns the cell layout data.
         *
         * @return the layout data. May be {@code null}
         */
        public GridLayoutData getLayoutData() {
            return layoutData;
        }
    }

}
