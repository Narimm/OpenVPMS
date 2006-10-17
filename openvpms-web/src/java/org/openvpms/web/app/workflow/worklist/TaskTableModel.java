/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.worklist;

import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import nextapp.echo2.app.table.TableModel;
import org.apache.commons.lang.time.DateUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;


/**
 * Table model for display <em>act.customerTask<em>s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TaskTableModel extends AbstractActTableModel {

    /**
     * The index of the time column in the model.
     */
    private int timeIndex;


    /**
     * Creates a new <code>TaskTableModel</code>.
     */
    public TaskTableModel() {
        String[] shortNames = new String[]{"act.customerTask"};
        TableColumnModel model
                = createColumnModel(shortNames, getLayoutContext());
        timeIndex = getNextModelIndex(model);
        model.addColumn(new TableColumn(timeIndex));
        setTableColumnModel(model);
    }

    /**
     * @see TableModel#getColumnName
     */
    @Override
    public String getColumnName(int column) {
        String result;
        TableColumn col = getColumn(column);
        if (col.getModelIndex() == timeIndex) {
            result = Messages.get("tasktablemodel.time");
        } else {
            result = super.getColumnName(column);
        }
        return result;
    }

    /**
     * Returns the sort criteria.
     *
     * @param column    the primary sort column
     * @param ascending if <code>true</code> sort in ascending order; otherwise
     *                  sort in <code>descending</code> order
     * @return the sort criteria, or <code>null</code> if the column isn't
     *         sortable
     */
    @Override
    public SortConstraint[] getSortConstraints(int column, boolean ascending) {
        SortConstraint[] result;
        TableColumn col = getColumn(column);
        if (col.getModelIndex() == timeIndex) {
            result = null;
        } else {
            result = super.getSortConstraints(column, ascending);
        }
        return result;
    }

    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getDescriptorNames() {
        return new String[]{"status", "taskType", "customer", "patient",
                            "description"};
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(IMObject object, int column, int row) {
        TableColumn col = getColumn(column);
        Object result = null;
        if (col.getModelIndex() == timeIndex) {
            Act act = (Act) object;
            Date start = act.getActivityStartTime();
            Date end = act.getActivityEndTime();
            if (start != null) {
                if (end == null) {
                    end = new Date();
                }
                long diff = end.getTime() - start.getTime();
                long hours = 0;
                long mins = 0;
                if (diff > 0) {
                    hours = diff / DateUtils.MILLIS_IN_HOUR;
                    mins = (diff % DateUtils.MILLIS_IN_HOUR)
                            / DateUtils.MILLIS_IN_MINUTE;
                }
                result = Messages.get("tasktablemodel.time.format", hours,
                                      mins);
            }
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }
}
