package org.openvpms.web.workspace.workflow.scheduling;

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

}
