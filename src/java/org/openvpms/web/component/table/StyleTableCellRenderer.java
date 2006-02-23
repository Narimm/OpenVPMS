package org.openvpms.web.component.table;

/**
 * <code>TableCellRender<code> that assigns a style to a cell. *
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class StyleTableCellRenderer extends AbstractTableCellRenderer {

    /**
     * The style name.
     */
    private final String _style;

    /**
     * Construct a new <coe>StyleTableCellRenderer</code>.
     */
    public StyleTableCellRenderer(String style) {
        _style = style;
    }

    /**
     * Returns the style name for a column and row.
     *
     * @param column the column
     * @param row    the row
     * @return a style name for the given column and row.
     */
    protected String getStyle(int column, int row) {
        return _style;
    }

}
