package org.openvpms.web.component;

import java.util.List;

import echopointng.table.PageableSortableTable;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.table.TableCellRenderer;
import nextapp.echo2.app.table.TableColumnModel;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.model.IMObjectTableModel;


/**
 * Paged, sortable table of {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class IMObjectTable extends PageableSortableTable {

    /**
     * The no. of rows per page.
     */
    int _rowsPerPage = 15;

    /**
     * Determines if object may be deleted.
     */
    private final boolean _deletable;


    /**
     * Construct a new <code>IMObjectTable</code>.
     */
    public IMObjectTable() {
        this(false);
    }

    /**
     * Construct a new <code>IMObjectTable</code>.
     *
     * @param deletable if <code>true</code>, add a column to mark objects for
     *                  deletion
     */
    public IMObjectTable(boolean deletable) {
        _deletable = deletable;
        setStyleName("default");
        TableColumnModel columns
                = IMObjectTableModel.createColumnModel(_deletable);

        IMObjectTableModel model = new IMObjectTableModel(columns);
        model.setRowsPerPage(_rowsPerPage);
        setModel(model);
        setColumnModel(columns);
        setDefaultRenderer(Object.class, new EvenOddTableCellRenderer());
    }

    /**
     * Sets the objects to display in the table.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<IMObject> objects) {
        TableColumnModel columns = IMObjectTableModel.createColumnModel(_deletable);
        IMObjectTableModel model = new IMObjectTableModel(objects, columns);
        model.setRowsPerPage(_rowsPerPage);
        setModel(model);
        setSelectionEnabled(true);
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if no object is
     *         selected
     */
    public IMObject getSelected() {
        IMObject result = null;
        int index = getSelectionModel().getMinSelectedIndex();
        if (index != -1) {
            IMObjectTableModel model = (IMObjectTableModel) getModel();
            int row = model.getAbsRow(index);
            result = model.getObject(row);
        }
        return result;
    }

    /**
     * Returns the objects marked for deletion.
     *
     * @return the objects marked for deletion
     */
    public List<IMObject> getMarked() {
        return ((IMObjectTableModel) getModel()).getMarked();
    }

    /**
     * Returns the no. of rows per page.
     *
     * @return the no. of rows per page
     */
    public int getRowsPerPage() {
        return ((IMObjectTableModel) getModel()).getRowsPerPage();
    }

    /**
     * TableCellRender that assigns even and odd rows a different style.
     */
    private static class EvenOddTableCellRenderer implements TableCellRenderer {

        /**
         * Returns a component that will be displayed at the specified
         * coordinate in the table.
         *
         * @param table  the <code>Table</code> for which the rendering is
         *               occurring
         * @param value  the value retrieved from the <code>TableModel</code>
         *               for the specified coordinate
         * @param column the column index to apply
         * @param row    the row index to apply
         * @return a component representation  of the value (This component must
         *         be unique.  Returning a single instance of a component across
         *         multiple calls to this method will result in undefined
         *         behavior.)
         */
        public Component getTableCellRendererComponent(Table table, Object value, int column, int row) {
            Component component;
            if (value instanceof Component) {
                component = (Component) value;
            } else {
                Label label;
                if (value != null) {
                    label = new Label(value.toString());
                } else {
                    label = new Label();
                }
                component = label;
            }
            if (row % 2 == 0) {
                component.setStyleName("Table.EvenRow");
            } else {
                component.setStyleName("Table.OddRow");
            }
            return component;
        }

    }

}
