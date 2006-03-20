package org.openvpms.web.component.table;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.table.TableCellRenderer;


/**
 * Header cell renderer for {@link SortableTableModel} backed tables.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class SortableTableHeaderRenderer implements TableCellRenderer {

    /**
     * The button style.
     */
    private final String _style;

    /**
     * Up arrow resource path.
     */
    private static final String UP_ARROW_PATH
            = "/echopointng/resource/images/ArrowUp.gif";

    /**
     * Down arrow resource path.
     */
    private static final String DOWN_ARROW_PATH
            = "/echopointng/resource/images/ArrowDown.gif";

    /**
     * Up arrow image.
     */
    private static final ImageReference UP_ARROW
            = new ResourceImageReference(UP_ARROW_PATH);

    /**
     * Default button style.
     */
    private static final String STYLE = "Table.Header.Button";

    /**
     * Down arrow image.
     */
    private static final ImageReference DOWN_ARROW
            = new ResourceImageReference(DOWN_ARROW_PATH);

    /**
     * Construct a new <code>SortableTableHeaderRenderer</code>, with the
     * default button style.
     */
    public SortableTableHeaderRenderer() {
        this(STYLE);
    }

    /**
     * Construct a new <code>SortableTableHeaderRenderer</code>.
     *
     * @param style the button style.
     */
    public SortableTableHeaderRenderer(String style) {
        _style = style;
    }

    /**
     * Returns a component that will be displayed at the specified coordinate in
     * the table.
     *
     * @param table  the <code>Table</code> for which the rendering is
     *               occurring
     * @param value  the value retrieved from the <code>TableModel</code> for
     *               the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a component representation  of the value
     */
    public Component getTableCellRendererComponent(Table table, Object value,
                                                   int column, int row) {
        SortableTableModel model = (SortableTableModel) table.getModel();
        String label = (String) value;
        return getSortButton(label, column, model);
    }

    /**
     * Returns a button to sort a column.
     *
     * @param label  the button label
     * @param column the column to sort
     * @param model  the table model
     * @return a button to sort <code>column</code>
     */
    protected Button getSortButton(String label, final int column,
                                   final SortableTableModel model) {
        Button button = new Button(label);
        button.setStyleName(_style);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                boolean ascending = model.isSortedAscending();
                model.sort(column, !ascending);
            }
        });

        ImageReference icon = null;
        if (model.getSortColumn() == column) {
            icon = (model.isSortedAscending()) ? DOWN_ARROW : UP_ARROW;
            button.setIcon(icon);
        }

        return button;
    }

}
