package org.openvpms.web.component.im.table.act;

import echopointng.table.SortableTableColumn;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * Table model for {@link Act}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ActTableModel extends IMObjectTableModel {

    private static final int ACT_INDEX = NEXT_INDEX;
    private static final int START_INDEX = ACT_INDEX + 1;
    private static final int END_INDEX = START_INDEX + 1;
    private static final int TYPE_INDEX = END_INDEX + 1;
    private static final int STATUS_INDEX = TYPE_INDEX + 1;

    private static final String[] ACT_COLUMNS = {"start", "end", "type", "status", "description"};

    private static final int INDEXES[] = {START_INDEX, END_INDEX, TYPE_INDEX, STATUS_INDEX, DESCRIPTION_INDEX};


    /**
     * Construct a new <code>ActTableModel</code>.
     */
    public ActTableModel() {
        super(createColumnModel());
    }

    /**
     * Helper to create a column model.
     *
     * @return a new column model
     */
    protected static TableColumnModel createColumnModel() {
        TableColumnModel model = new DefaultTableColumnModel();
        for (int i = 0; i < ACT_COLUMNS.length; ++i) {
            TableColumn column = new SortableTableColumn(INDEXES[i]);
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
     * @param index  the column model index
     * @param row    the table row
     */
    @Override
    protected Object getValue(IMObject object, int index, int row) {
        Act act = (Act) object;
        Object result;
        switch (index) {
            case START_INDEX:
                result = act.getActivityStartTime();
                break;
            case END_INDEX:
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
