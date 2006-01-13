package org.openvpms.web.component;

import java.util.List;

import echopointng.table.PageableSortableTable;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.table.TableCellRenderer;
import nextapp.echo2.app.table.TableColumnModel;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.model.IMObjectTableModel;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class IMObjectTable extends Column {

    private Table _table;
    private List<IMObject> _objects;

    /**
     * The no. of rows per page.
     */
    int _rowsPerPage = 15;


    public IMObjectTable() {
        TableColumnModel columns = IMObjectTableModel.createColumnModel();

        IMObjectTableModel model = new IMObjectTableModel(columns);
        model.setRowsPerPage(_rowsPerPage);
        _table = new PageableSortableTable(model, columns);
        _table.setDefaultRenderer(Object.class, new EvenOddTableCellRenderer());
        add(_table);
        add(new TableNavigator(_table));
    }

    public void setObjects(List<IMObject> objects) {
        TableColumnModel columns = IMObjectTableModel.createColumnModel();
        _objects = objects;
        IMObjectTableModel model = new IMObjectTableModel(objects, columns);
        model.setRowsPerPage(_rowsPerPage);
        _table.setModel(model);
        _table.setSelectionEnabled(true);
    }

    public void addActionListener(ActionListener listener) {
        _table.addActionListener(listener);
    }

    public IMObject getSelected() {
        int index = _table.getSelectionModel().getMinSelectedIndex();
        return (index != -1) ? _objects.get(index) : null;
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
         * @param column the column index to render
         * @param row    the row index to render
         * @return a component representation  of the value (This component must
         *         be unique.  Returning a single instance of a component across
         *         multiple calls to this method will result in undefined
         *         behavior.)
         */
        public Component getTableCellRendererComponent(Table table, Object value, int column, int row) {
            Label label;
            if (value != null) {
                label = new Label(value.toString());
            } else {
                label = new Label();
            }
            if (row % 2 == 0) {
                label.setStyleName("Table.EvenRow");
            } else {
                label.setStyleName("Table.OddRow");
            }
            return label;
        }

    }

}
