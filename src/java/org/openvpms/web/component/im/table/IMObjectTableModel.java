package org.openvpms.web.component.im.table;

import java.util.Iterator;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * Table model for {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 * @see IMObjectTable
 */
public class IMObjectTableModel extends AbstractIMObjectTableModel {

    /**
     * Archetype column index.
     */
    public static final int ARCHETYPE_INDEX = 0;

    /**
     * Name column index.
     */
    public static final int NAME_INDEX = 1;

    /**
     * Description column index.
     */
    public static final int DESCRIPTION_INDEX = 2;

    /**
     * Next unused model index.
     */
    public static final int NEXT_INDEX = 3;

    /**
     * Table column model.
     */
    private final TableColumnModel _model;


    /**
     * Table column identifiers.
     */
    protected static final String[] COLUMNS = {
            "archetype", "name", "description"};


    /**
     * Construct an unpopulated <code>IMObjectTableModel</code>.
     */
    public IMObjectTableModel() {
        this(createTableColumnModel());
    }

    /**
     * Construct a new <code>IMObjectTableModel</code>.
     */
    public IMObjectTableModel(TableColumnModel model) {
        setRowsPerPage(15);
        _model = model;
        sort(NAME_INDEX, true);
    }

    /**
     * @see TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return _model.getColumnCount();
    }

    /**
     * @see TableModel#getColumnName
     */
    public String getColumnName(int column) {
        String key = "table.imobject." + COLUMNS[column];
        return Messages.get(key);
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
     * Helper to create a new column model.
     *
     * @return a new columns model.
     */
    public static TableColumnModel createTableColumnModel() {
        TableColumnModel model = new DefaultTableColumnModel();
        for (int i = 1; i < COLUMNS.length; ++i) {
            TableColumn column = new TableColumn(i);
            String key = "table.imobject." + COLUMNS[i];
            String label = Messages.get(key);

            column.setHeaderValue(label);
            model.addColumn(column);
        }
        return model;
    }

    /**
     * Returns the value at a given column and absolute row.
     *
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate.
     */
    @Override
    protected Object getValue(int column, int row) {
        IMObject object = getObject(row);
        return getValue(object, column, row);
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate.
     */
    protected Object getValue(IMObject object, int column, int row) {
        Object result;
        switch (column) {
            case IMObjectTableModel.ARCHETYPE_INDEX:
                result = DescriptorHelper.getDisplayName(object);
                break;
            case IMObjectTableModel.NAME_INDEX:
                result = object.getName();
                break;
            case IMObjectTableModel.DESCRIPTION_INDEX:
                result = object.getDescription();
                break;
            default:
                throw new IllegalArgumentException("Illegal column=" + column);
        }
        return result;
    }

    /**
     * Returns a column given its model index.
     *
     * @param index the model index
     * @return the column
     */
    protected TableColumn getColumn(int index) {
        TableColumn result = null;
        for (Iterator iter = _model.getColumns(); iter.hasNext();) {
            TableColumn column = (TableColumn) iter.next();
            if (column.getModelIndex() == index) {
                result = column;
                break;
            }
        }
        return result;
    }

}