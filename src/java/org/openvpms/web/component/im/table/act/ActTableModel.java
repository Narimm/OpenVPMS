package org.openvpms.web.component.im.table.act;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
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
     * Credit column index.
     */
    private static final int CREDIT_INDEX = STATUS_INDEX + 1;


    /**
     * Construct a new <code>ActTableModel</code>.
     */
    public ActTableModel() {
        this(true, false);
    }

    /**
     * Construct a new <code>ActTableModel</code>.
     *
     * @param showStatus determines if the status colunn should be displayed
     * @param showCredit determines if the credit/debit column should be
     *                   displayed
     */
    public ActTableModel(boolean showStatus, boolean showCredit) {
        super(createColumnModel(showStatus, showCredit));
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
            case CREDIT_INDEX:
                node = "credit";
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
     * @param showStatus determines if the status colunn should be displayed
     * @param showCredit determines if the credit/debit column should be
     *                   displayed
     * @return a new column model
     */
    protected static TableColumnModel createColumnModel(boolean showStatus,
                                                        boolean showCredit) {
        TableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(START_INDEX, "start"));
        model.addColumn(createTableColumn(TYPE_INDEX, "type"));
        if (showStatus) {
            model.addColumn(createTableColumn(STATUS_INDEX, "status"));
        }
        if (showCredit) {
            model.addColumn(createTableColumn(CREDIT_INDEX, "debit_credit"));
        }
        model.addColumn(createTableColumn(DESCRIPTION_INDEX, "description"));
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
            case CREDIT_INDEX:
                ArchetypeDescriptor archetype
                        = DescriptorHelper.getArchetypeDescriptor(act);
                NodeDescriptor credit = archetype.getNodeDescriptor("credit");
                if (credit != null) {
                    Boolean value = (Boolean) credit.getValue(object);
                    if (Boolean.TRUE.equals(value)) {
                        result = Messages.get("table.act.credit");
                    } else {
                        result = Messages.get("table.act.debit");
                    }
                } else {
                    result = "";
                }
                break;
            default:
                result = super.getValue(object, column, row);
                break;
        }
        return result;
    }

    /**
     * Helper to create a table column.
     *
     * @param index the column model index
     * @param name  the column name
     * @return a new column
     */
    private static TableColumn createTableColumn(int index, String name) {
        TableColumn column = new TableColumn(index);
        String key = "table.act." + name;
        String label = Messages.get(key);
        column.setHeaderValue(label);
        return column;
    }


}
