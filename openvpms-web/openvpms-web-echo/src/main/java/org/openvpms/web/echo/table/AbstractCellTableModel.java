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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.echo.table;

import nextapp.echo2.app.table.TableColumnModel;

/**
 * A {@code TableModel} that tracks cell selection to support cut and paste.
 *
 * @author Tim Anderson
 */
public abstract class AbstractCellTableModel extends AbstractTableModel {

    /**
     * The selected cell.
     */
    private Cell selected;

    /**
     * The marked cell.
     */
    private Cell marked;

    /**
     * If {@code true} the marked cell is being cut, else it is being copied.
     */
    private boolean isCut;


    /**
     * Constructs an {@link AbstractCellTableModel}.
     */
    public AbstractCellTableModel() {
        super();
    }

    /**
     * Constructs an {@link AbstractCellTableModel}.
     *
     * @param model the column model
     */
    public AbstractCellTableModel(TableColumnModel model) {
        super(model);
    }

    /**
     * Sets the selected cell.
     *
     * @param cell the selected cell. May be {@code null}
     */
    public void setSelected(Cell cell) {
        Cell old = selected;
        selected = cell;
        if (old != null) {
            fireTableCellUpdated(old.getColumn(), old.getRow());
        }
        if (selected != null) {
            fireTableCellUpdated(selected.getColumn(), selected.getRow());
        }
    }

    /**
     * Returns the selected cell.
     *
     * @return the selected cell, or {@code null} if none is selected
     */
    public Cell getSelected() {
        return selected;
    }

    /**
     * Determines if a cell is selected.
     *
     * @param column the column
     * @param row    the row
     * @return {@code true} if the cell is selected
     */
    public boolean isSelected(int column, int row) {
        return selected != null && selected.equals(column, row);
    }

    /**
     * Sets the marked cell. This flags a cell as being marked for cutting/copying and pasting purposes.
     *
     * @param cell  the cell, or {@code null} to unmark the cell
     * @param isCut if {@code true} indicates the cell is being cut; if {@code false} indicates its being copied.
     *              Ignored if the cell is being unmarked.
     */
    public void setMarked(Cell cell, boolean isCut) {
        Cell old = marked;
        this.isCut = isCut;
        marked = cell;
        if (old != null) {
            fireTableCellUpdated(old.getColumn(), old.getRow());
        }
        if (marked != null) {
            fireTableCellUpdated(marked.getColumn(), marked.getRow());
        }
    }

    /**
     * Returns the marked cell.
     *
     * @return the marked cell, or {@code null} if no cell is marked
     */
    public Cell getMarked() {
        return marked;
    }

    /**
     * Determines if a cell is marked for cut/copy.
     *
     * @param column the column
     * @param row    the row
     * @return {@code true} if the cell is cut
     */
    public boolean isMarked(int column, int row) {
        return marked != null && marked.equals(column, row);
    }

    /**
     * Determines if the marked cell is being cut or copied.
     *
     * @return {@code true} if the cell is being cut; {@code false} if it is being copied
     */
    public boolean isCut() {
        return isCut;
    }

}
