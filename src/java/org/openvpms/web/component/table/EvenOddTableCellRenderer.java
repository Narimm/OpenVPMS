package org.openvpms.web.component.table;


/**
 * TableCellRender that assigns even and odd rows a different style.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class EvenOddTableCellRenderer extends AbstractTableCellRenderer {

    /**
     * Returns the style name for a column and row.
     *
     * @param column the column
     * @param row    the row
     * @return a style name for the given column and row.
     */
    protected String getStyle(int column, int row) {
        return (row % 2 == 0) ? "Table.EvenRow" : "Table.OddRow";
    }

}
