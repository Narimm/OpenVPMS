package org.openvpms.web.component.im.table;

import java.util.ArrayList;
import java.util.List;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.table.AbstractPageableSortableTableModel;


/**
 * Abstract {@link IMObject} table model.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractIMObjectTableModel
        extends AbstractPageableSortableTableModel {

    /**
     * The objects.
     */
    private List<IMObject> _objects = new ArrayList<IMObject>();

    /**
     * The object ids.
     */
    private List<Object> _ids = new ArrayList<Object>();


    /**
     * Returns the total number of rows. <em>NOTE: </em> the {@link *
     * #getRowCount} method return the number of visible rows.
     *
     * @return the total number of rows
     */
    public int getRows() {
        return _objects.size();
    }

    /**
     * Sets the objects to display.
     *
     * @param objects the objects to display
     */
    public void setObjects(List<IMObject> objects) {
        _objects.clear();
        _ids.clear();
        _objects = objects;
        _ids = new ArrayList<Object>(objects);
        if (getSortColumn() != -1) {
            sort(getSortColumn(), isSortedAscending());
        } else {
            fireTableDataChanged();
        }
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
     * Add an object.
     *
     * @param object the object to add
     */
    public void add(IMObject object) {
        _objects.add(object);
        _ids.add(object);
        fireTableDataChanged();
    }

    /**
     * Remove an object.
     *
     * @param object the object to remove
     */
    public void remove(IMObject object) {
        int index = _objects.indexOf(object);
        if (index != -1) {
            _objects.remove(index);
            _ids.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }

    /**
     * Return the object at the given sbsolute row.
     *
     * @param row the row
     * @return the object at <code>row</code>
     */
    public IMObject getObject(int row) {
        return (IMObject) _ids.get(row);
    }

    /**
     * Returns the row identifiers.
     *
     * @return the row identifiers.
     */
    protected List<Object> getRowIds() {
        return _ids;
    }

    /**
     * Sets the row identifiers.
     *
     * @param ids the row identifiers
     */
    protected void setRowIds(List<Object> ids) {
        _ids = ids;
    }


}
