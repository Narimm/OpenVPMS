package org.openvpms.web.component.im.table;

import echopointng.table.SortableTableColumn;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.util.DescriptorHelper;
import org.openvpms.web.util.Messages;


/**
 * Table model for {@link Act}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ActTableModel extends IMObjectTableModel {

    private static final int START_INDEX = DESCRIPTION_INDEX + 1;
    public static final int FROM_INDEX = START_INDEX + 1;
    public static final int TO_INDEX = FROM_INDEX + 1;
    public static final int TYPE_INDEX = TO_INDEX + 1;
    public static final int STATUS_INDEX = TYPE_INDEX + 1;

    private static final String[] ACT_COLUMNS = {"delete", "from", "to", "type", "status", "description"};

    private static final int INDEXES[] = {DELETE_INDEX, FROM_INDEX, TO_INDEX, TYPE_INDEX, STATUS_INDEX, DESCRIPTION_INDEX};


    /**
     * Construct a new <code>ActTableModel</code>.
     */
    public ActTableModel(TableColumnModel model) {
        super(model);
    }

    /**
     * Helper to create a model.
     *
     * @param deletable if <code>true</code>, add a column to enable deletions
     * @return a new column model
     */
    public static IMObjectTableModel create(boolean deletable) {
        TableColumnModel model = new DefaultTableColumnModel();
        int i = (deletable) ? 0 : 1;
        for (; i < ACT_COLUMNS.length; ++i) {
            TableColumn column = new SortableTableColumn(INDEXES[i]);
            String key = "table.act." + ACT_COLUMNS[i];
            String label = Messages.get(key);

            column.setHeaderValue(label);
            model.addColumn(column);
        }
        return new ActTableModel(model);
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object the object
     * @param index  the column model index
     * @param row    the table row
     */
    @Override
    protected Object getValue(IMObject object, int index, int row) {
        Act act = (Act) object;
        Object result;
        switch (index) {
            case FROM_INDEX:
                result = act.getActivityStartTime();
                break;
            case TO_INDEX:
                result = act.getActivityEndTime();
                break;
            case TYPE_INDEX:
                result = DescriptorHelper.getArchetypeDescriptor(act).getDisplayName();
                break;
            case STATUS_INDEX:
                result = act.getStatus();
                break;
            default:
                result = super.getValue(object, index, row);
                break;
        }
        return result;
    }

}
