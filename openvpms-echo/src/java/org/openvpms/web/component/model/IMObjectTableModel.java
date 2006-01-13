package org.openvpms.web.component.model;

import java.util.ArrayList;
import java.util.List;

import echopointng.table.DefaultPageableSortableTableModel;
import echopointng.table.SortableTableColumn;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumnModel;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.util.Messages;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class IMObjectTableModel extends DefaultPageableSortableTableModel {

    /**
     * Id column index.
     */
    public static final int ID_COLUMN = 0;

    /**
     * Type column index.
     */
    public static final int TYPE_COLUMN = 1;

    /**
     * Name column index.
     */
    public static final int NAME_COLUMN = 2;

    /**
     * Description column index.
     */
    public static final int DESCRIPTION_COLUMN = 3;

    /**
     * The objects.
     */
    private List<IMObject> _objects;

    /**
     * Table column identifiers.
     */
    private static final String[] COLUMNS = {
            "id", "type", "name", "description"};

    /**
     * Construct an unpopulated  <code>IMObjectTableModel</code>.
     */
    public IMObjectTableModel(TableColumnModel model) {
        this(new ArrayList<IMObject>(), model);
    }

    /**
     * Construct a populated <code>IMObjectTableModel</code>.
     *
     * @param objects the objects to populate the model with
     */
    public IMObjectTableModel(List<IMObject> objects, TableColumnModel model) {
        super(model);
        _objects = objects;
        for (int row = 0; row < _objects.size(); ++row) {
            for (int col = 0; col < getColumnCount(); ++col) {
                setValueAt(getValue(col, row), col, row);
            }
        }
    }

    /**
     * Returns the number of COLUMNS in the table.
     *
     * @return the column count
     */
    public int getColumnCount() {
        return COLUMNS.length;
    }

    /**
     * Returns the name of the specified column number.
     *
     * @param column the column index (0-based)
     * @return the column name
     */
    public String getColumnName(int column) {
        String key = "table.imobject." + COLUMNS[column];
        return Messages.get(key);
    }

    /**
     * Returns the value found at the given coordinate within the table. Column
     * and row values are 0-based.
     *
     * @param column the column index (0-based)
     * @param row    the row index (0-based)
     */
    protected Object getValue(int column, int row) {
        Object result = null;
        IMObject object = _objects.get(row);
        switch (column) {
            case ID_COLUMN:
                result = new Long(object.getUid());
                break;
            case TYPE_COLUMN:
                result = object.getArchetypeId().getConcept();
                break;
            case NAME_COLUMN:
                result = object.getName();
                break;
            case DESCRIPTION_COLUMN:
                result = object.getDescription();
                break;
            default:
                throw new IllegalArgumentException("Illegal column=" + column);
        }
        return result;
    }

    /**
     * Helper to create a column model.
     *
     * @return a new column model
     */
    public static TableColumnModel createColumnModel() {
        TableColumnModel model = new DefaultTableColumnModel();
        for (int i = 0; i < COLUMNS.length; ++i) {
            model.addColumn(new SortableTableColumn(i));
        }
        return model;
    }
}
