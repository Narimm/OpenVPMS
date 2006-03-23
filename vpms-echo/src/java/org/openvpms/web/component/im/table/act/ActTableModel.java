package org.openvpms.web.component.im.table.act;

import echopointng.table.SortableTableColumn;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.table.DefaultIMObjectTableModel;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * Table model for {@link Act}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ActTableModel extends DefaultIMObjectTableModel {

    /**
     * Start column index.
     */
    private static final int START_INDEX = NEXT_INDEX;

    /**
     * Type column index.
     */
    private static final int TYPE_INDEX = START_INDEX + 1;

    /**
     * Status column index.
     */
    private static final int STATUS_INDEX = TYPE_INDEX + 1;

    /**
     * Act column identifiers.
     */
    private static final String[] ACT_COLUMNS = {"start", "type", "status",
                                                 "description"};

    /**
     * Column indexes.
     */
    private static final int INDEXES[] = {START_INDEX, TYPE_INDEX, STATUS_INDEX,
                                          DESCRIPTION_INDEX};


    /**
     * Construct a new <code>ActTableModel</code>.
     */
    public ActTableModel() {
        super(createColumnModel());
    }

    /**
     * Returns the node name associated with a column.
     *
     * @param column the column
     * @return the name of the node associated with the column, or
     *         <code>null</code>
     */
    @Override
    public String getNode(int column) {
        String node = null;
        switch (column) {
            case START_INDEX:
                node = "estimationDate";
                break;
            case STATUS_INDEX:
                node = "status";
                break;
            default:
                node = super.getNode(column);
                break;
        }
        return node;
    }

    /**
     * Helper to create a column model.
     *
     * @return a new column model
     */
    protected static TableColumnModel createColumnModel() {
        TableColumnModel model = new DefaultTableColumnModel();
        for (int i = 0; i < ACT_COLUMNS.length; ++i) {
            TableColumn column = new TableColumn(INDEXES[i]);
            String key = "table.act." + ACT_COLUMNS[i];
            String label = Messages.get(key);

            column.setHeaderValue(label);
            model.addColumn(column);
        }
        return model;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object the object
     * @param column
     * @param row    the table row
     */
    @Override
    protected Object getValue(IMObject object, int column, int row) {
        Act act = (Act) object;
        Object result;
        switch (column) {
            case START_INDEX:
                result = act.getActivityStartTime();
                break;
            case TYPE_INDEX:
                result = DescriptorHelper.getArchetypeDescriptor(act).getDisplayName();
                break;
            case STATUS_INDEX:
                result = act.getStatus();
                break;
            default:
                result = super.getValue(object, column, row);
                break;
        }
        return result;
    }


}
