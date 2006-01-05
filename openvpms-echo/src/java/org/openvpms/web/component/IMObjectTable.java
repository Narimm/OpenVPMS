package org.openvpms.web.component;

import java.util.List;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.table.TableCellRenderer;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.TableFactory;
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

    public IMObjectTable() {
        _table = TableFactory.create(new IMObjectTableModel());
        _table.setDefaultRenderer(Object.class, new EvenOddTableCellRenderer());
        add(_table);
    }

    public void setObjects(List<IMObject> objects) {
        _objects = objects;
        _table.setModel(new IMObjectTableModel(objects));
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
         * Returns a component that will be displayed at the specified coordinate in the table.
         *
         * @param table  the <code>Table</code> for which the rendering is occurring
         * @param value  the value retrieved from the <code>TableModel</code> for the specified coordinate
         * @param column the column index to render
         * @param row    the row index to render
         * @return a component representation  of the value (This component must be unique.  Returning a single instance
         *         of a component across multiple calls to this method will result in undefined behavior.)
         */
        public Component getTableCellRendererComponent(Table table, Object value, int column, int row) {
            Label label;
            if (value != null) {
                label = new Label(value.toString());
            } else {
                label = new Label();
            }
            if (row % 2 == 0) {
                label.setStyleName("table.evenrow");
            } else {
                label.setStyleName("table.oddrow");
            }
            return label;
        }

    }

}
