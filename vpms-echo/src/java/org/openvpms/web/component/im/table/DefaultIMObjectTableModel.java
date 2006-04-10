package org.openvpms.web.component.im.table;

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
public class DefaultIMObjectTableModel extends AbstractIMObjectTableModel {

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
     * Table column identifiers.
     */
    protected static final String[] COLUMNS = {
            "table.imobject.archetype", "table.imobject.name",
            "table.imobject.description"};


    /**
     * Construct an unpopulated <code>DefaultIMObjectTableModel</code>.
     */
    public DefaultIMObjectTableModel() {
        this(createTableColumnModel());
    }

    /**
     * Construct a new <code>DefaultIMObjectTableModel</code>.
     *
     * @param model the column model
     */
    public DefaultIMObjectTableModel(TableColumnModel model) {
        super(model);
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
            String label = Messages.get(COLUMNS[i]);

            column.setHeaderValue(label);
            model.addColumn(column);
        }
        return model;
    }

    /**
     * @see TableModel#getColumnName
     */
    public String getColumnName(int column) {
        return Messages.get(COLUMNS[column]);
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
            case ARCHETYPE_INDEX:
                result = DescriptorHelper.getDisplayName(object);
                break;
            case NAME_INDEX:
                result = object.getName();
                break;
            case DESCRIPTION_INDEX:
                result = object.getDescription();
                break;
            default:
                throw new IllegalArgumentException("Illegal column=" + column);
        }
        return result;
    }

    /**
     * Returns the node name associated with a column.
     *
     * @param column the column
     * @return the name of the node associated with the column, or
     *         <code>null</code>
     */
    public String getNode(int column) {
        String node = null;
        switch (column) {
            case NAME_INDEX:
                node = "name";
                break;
            case DESCRIPTION_INDEX:
                node = "description";
                break;
        }
        return node;
    }

}