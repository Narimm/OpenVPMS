package org.openvpms.web.component.model;

import java.util.List;
import java.util.ArrayList;

import nextapp.echo2.app.table.AbstractTableModel;
import org.openvpms.web.util.Messages;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class IMObjectTableModel extends AbstractTableModel {

    /**
     * Id column index.
     */
    public static final int ID_COLUMN  = 0;

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
    public IMObjectTableModel() {
        this(new ArrayList<IMObject>());
    }

    /**
     * Construct a populated <code>IMObjectTableModel</code>.
     *
     * @param objects the objects to populate the model with
     */
    public IMObjectTableModel(List<IMObject> objects) {
        _objects = objects;
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
        return Messages.getString(key);
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
     * Returns the value found at the given coordinate within the table.
     * Column and row values are 0-based.
     *
     * @param column the column index (0-based)
     * @param row    the row index (0-based)
     */
    public Object getValueAt(int column, int row) {
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
}
