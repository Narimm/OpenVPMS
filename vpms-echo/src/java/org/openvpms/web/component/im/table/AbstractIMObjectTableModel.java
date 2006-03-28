package org.openvpms.web.component.im.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nextapp.echo2.app.table.AbstractTableModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Abstract {@link IMObject} table model.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractIMObjectTableModel
        extends AbstractTableModel implements IMObjectTableModel {

    /**
     * The column model.
     */
    private final TableColumnModel _model;

    /**
     * The objects.
     */
    private List<IMObject> _objects = new ArrayList<IMObject>();


    /**
     * Construct a new <code>AbstractIMObjectTableModel</code>.
     *
     * @param model the table column model
     */
    public AbstractIMObjectTableModel(TableColumnModel model) {
        _model = model;
    }

    /**
     * Returns the number of columns in the table.
     *
     * @return the column count
     */
    public int getColumnCount() {
        return _model.getColumnCount();
    }

    /**
     * Returns the number of rows in the table.
     *
     * @return the row count
     */
    public int getRowCount() {
        return _objects.size();
    }

    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<IMObject> objects) {
        _objects.clear();
        _objects = objects;
        fireTableDataChanged();
    }

    /**
     * Returns the objects being displayed.
     *
     * @return the objects being displayed
     */
    public List<IMObject> getObjects() {
        return _objects;
    }

    /**
     * Return the object at the given row.
     *
     * @param row the row
     * @return the object at <code>row</code>
     */
    public IMObject getObject(int row) {
        return _objects.get(row);
    }

    /**
     * Returns the column model.
     *
     * @return the column model
     */
    public TableColumnModel getColumnModel() {
        return _model;
    }

    /**
     * Returns the value found at the given coordinate within the table. Column
     * and row values are 0-based. <strong>WARNING: Take note that the column is
     * the first parameter passed to this method, and the row is the second
     * parameter.</strong>
     *
     * @param column the column index (0-based)
     * @param row    the row index (0-based)
     */
    public Object getValueAt(int column, int row) {
        IMObject object = getObject(row);
        return getValue(object, column, row);
    }

    /**
     * Determines if selection should be enabled.
     *
     * @return <code>true</code> if selection should be enabled; otherwise
     * <code>false</code>
     */
    public boolean getEnableSelection() {
        return true;
    }
    
    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate.
     */
    protected abstract Object getValue(IMObject object, int column, int row);

    /**
     * Returns a column given its model index.
     *
     * @param column the column index
     * @return the column
     */
    protected TableColumn getColumn(int column) {
        TableColumn result = null;
        Iterator iterator = _model.getColumns();
        while (iterator.hasNext()) {
            TableColumn col = (TableColumn) iterator.next();
            if (col.getModelIndex() == column) {
                result = col;
                break;
            }
        }
        return result;
    }

}
