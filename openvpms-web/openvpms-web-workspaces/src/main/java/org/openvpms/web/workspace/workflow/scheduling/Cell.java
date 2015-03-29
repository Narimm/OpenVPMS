package org.openvpms.web.workspace.workflow.scheduling;

/**
 * Table cell.
 *
 * @author Tim Anderson
 */
public class Cell {

    /**
     * The row.
     */
    private final int row;

    /**
     * The column.
     */
    private final int column;

    /**
     * Constructs a {@link Cell}.
     *
     * @param row    the row
     * @param column the column
     */
    public Cell(int row, int column) {
        this.row = row;
        this.column = column;
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

}
