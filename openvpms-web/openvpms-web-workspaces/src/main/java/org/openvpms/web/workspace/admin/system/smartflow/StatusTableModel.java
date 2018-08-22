package org.openvpms.web.workspace.admin.system.smartflow;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.resource.i18n.Messages;

/**
 * .
 *
 * @author Tim Anderson
 */
class StatusTableModel extends AbstractIMTableModel<Status> {

    private static final int ID_INDEX = 0;

    private static final int NAME_INDEX = 1;

    private static final int KEY_INDEX = NAME_INDEX + 1;

    private static final int STATUS_INDEX = KEY_INDEX + 1;

    public StatusTableModel() {
        super(null);
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(ID_INDEX, ID));
        model.addColumn(createTableColumn(NAME_INDEX, NAME));
        model.addColumn(createTableColumn(KEY_INDEX, Messages.get("admin.system.smartflow.key")));
        model.addColumn(createTableColumn(STATUS_INDEX, Messages.get("admin.system.smartflow.status")));
        setTableColumnModel(model);
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if {@code true} sort in ascending order; otherwise sort in {@code descending} order
     * @return the sort criteria, or {@code null} if the column isn't sortable
     */
    @Override
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        return null;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the column
     * @param row    the row
     * @return the value at the given coordinate.
     */
    @Override
    protected Object getValue(Status object, TableColumn column, int row) {
        Object result = null;
        switch (column.getModelIndex()) {
            case ID_INDEX:
                result = object.getId();
                break;
            case NAME_INDEX:
                result = object.getName();
                break;
            case KEY_INDEX:
                result = object.getKey();
                break;
            case STATUS_INDEX:
                result = object.getStatus();
                break;
        }
        return result;
    }
}
