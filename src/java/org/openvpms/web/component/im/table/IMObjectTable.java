package org.openvpms.web.component.im.table;

import java.util.List;

import echopointng.table.PageableSortableTable;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.table.TableCellRenderer;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Paged, sortable table of {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class IMObjectTable extends PageableSortableTable {

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
        this(IMObjectTableModel.create(deletable));
    }

    /**
     * Construct a new <code>IMObjectTable</code>.
     *
     * @param model the table model
     */
    public IMObjectTable(IMObjectTableModel model) {
        setStyleName("default");
        setModel(model);
        setColumnModel(model.getColumnModel());
        setSelectionEnabled(true);
        setDefaultRenderer(Object.class, new EvenOddTableCellRenderer());
    }

    /**
     * Sets the objects to display in the table.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<IMObject> objects) {
        ((IMObjectTableModel) getModel()).setObjects(objects);
    }

    /**
     * Returns the objects displayed in the table.
     *
     * @return the object being displayed.
     */
    public List<IMObject> getObjects() {
        return ((IMObjectTableModel) getModel()).getObjects();
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
     * Adds an object to the table.
     *
     * @param object the object to add
     */
    public void add(IMObject object) {
        ((IMObjectTableModel) getModel()).add(object);
    }

    /**
     * Removes an object from the table.
     *
     * @param object the object to remove
     */
    public void remove(IMObject object) {
        ((IMObjectTableModel) getModel()).remove(object);
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
