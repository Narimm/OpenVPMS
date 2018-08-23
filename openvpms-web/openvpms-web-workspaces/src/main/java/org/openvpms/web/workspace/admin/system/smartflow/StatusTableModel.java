/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.system.smartflow;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.table.AbstractIMTableModel;
import org.openvpms.web.resource.i18n.format.DateFormatter;

/**
 * Table model to display the Smart Flow Sheet status at each practice location.
 *
 * @author Tim Anderson
 */
class StatusTableModel extends AbstractIMTableModel<Status> {

    /**
     * Id column index.
     */
    private static final int ID_INDEX = 0;

    /**
     * Location name column index.
     */
    private static final int NAME_INDEX = 1;

    /**
     * API key name column index.
     */
    private static final int KEY_INDEX = NAME_INDEX + 1;

    /**
     * The status column index.
     */
    private static final int STATUS_INDEX = KEY_INDEX + 1;

    /**
     * The last event received date column index.
     */
    private static final int RECEIVED_INDEX = STATUS_INDEX + 1;


    /**
     * Constructs a {@link StatusTableModel}.
     */
    public StatusTableModel() {
        super(null);
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(ID_INDEX, ID));
        model.addColumn(createTableColumn(NAME_INDEX, NAME));
        model.addColumn(createTableColumn(KEY_INDEX, "admin.system.smartflow.key"));
        model.addColumn(createTableColumn(STATUS_INDEX, "admin.system.smartflow.status"));
        model.addColumn(createTableColumn(RECEIVED_INDEX, "admin.system.smartflow.received"));
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
                result = object.getDisplayKey();
                break;
            case STATUS_INDEX:
                result = object.getStatus();
                break;
            case RECEIVED_INDEX:
                result = object.getEventReceived() != null ? DateFormatter.formatDateTimeAbbrev(object.getEventReceived()) : null;
                break;
        }
        return result;
    }


}
