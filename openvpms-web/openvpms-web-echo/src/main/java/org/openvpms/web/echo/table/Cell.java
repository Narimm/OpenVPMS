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

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.HashMap;

/**
 * Table cell.
 *
 * @author Tim Anderson
 */
public class Cell {

    /**
     * The column.
     */
    private final int column;

    /**
     * The row.
     */
    private final int row;

    /**
     * Constructs a {@link Cell}.
     *
     * @param column the column
     * @param row    the row
     */
    public Cell(int column, int row) {
        this.column = column;
        this.row = row;
    }

    /**
     * Returns the cell column.
     *
     * @return the cell column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the cell row.
     *
     * @return the cell row
     */
    public int getRow() {
        return row;
    }

    /**
     * Determines if the cell is equal to the specified column and row.
     *
     * @param column the column
     * @param row    the row
     * @return {@code true} if they are equal
     */
    public boolean equals(int column, int row) {
        return this.column == column && this.row == row;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Cell) {
            Cell other = (Cell) obj;
            return equals(other.column, other.row);
        }
        return false;
    }

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hash tables such as those provided by
     * {@link HashMap}.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(column).append(row).toHashCode();
    }

}
