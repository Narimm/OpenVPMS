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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.customer.info;

import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;
import org.openvpms.web.resource.i18n.format.DateFormatter;

import java.util.Date;
import java.util.List;


/**
 * Table for <em>act.customerAppointment</em> acts.
 *
 * @author Tim Anderson
 */
public class CustomerAppointmentTableModel extends AbstractActTableModel {

    /**
     * The nodes to display.
     */
    private static final String[] NODES = {"patient", "reason", "status", "description"};

    /**
     * The date column index.
     */
    private int dateIndex;

    /**
     * The time column index.
     */
    private int timeIndex;


    /**
     * Constructs a {@code CustomerAppointmentTableModel}.
     *
     * @param context the layout context
     */
    public CustomerAppointmentTableModel(LayoutContext context) {
        super(ScheduleArchetypes.APPOINTMENT, context);
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
        TableColumn col = getColumn(column);
        int index = col.getModelIndex();
        if (index == dateIndex) {
            return new SortConstraint[]{Constraints.sort("startTime", ascending)};
        }
        return super.getSortConstraints(column, ascending);
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column the table column
     * @param row    the table row
     */
    @Override
    protected Object getValue(Act object, TableColumn column, int row) {
        int index = column.getModelIndex();
        if (index == dateIndex || index == timeIndex) {
            Date startTime = object.getActivityStartTime();
            if (startTime != null) {
                if (index == dateIndex) {
                    return DateFormatter.formatDate(startTime, false);
                } else {
                    return DateFormatter.formatTime(startTime, false);
                }
            }
        }
        return super.getValue(object, column, row);
    }

    /**
     * Creates a column model.
     * <p/>
     * This splits the <em>startTime</em> node into date and time columns.
     *
     * @param archetypes the archetypes
     * @param context    the layout context
     * @return a new column model
     */
    @Override
    protected TableColumnModel createColumnModel(List<ArchetypeDescriptor> archetypes, LayoutContext context) {
        DefaultTableColumnModel model = (DefaultTableColumnModel) super.createColumnModel(archetypes, context);

        dateIndex = getNextModelIndex(model);
        timeIndex = dateIndex + 1;
        TableColumn date = createTableColumn(dateIndex, "table.act.date");
        TableColumn time = createTableColumn(timeIndex, "table.act.time");

        model.addColumn(date);
        model.moveColumn(model.getColumnCount() - 1, 0);
        model.addColumn(time);
        model.moveColumn(model.getColumnCount() - 1, 1);
        return model;
    }

    /**
     * Returns a list of node descriptor names to include in the table.
     *
     * @return the list of node descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return NODES;
    }
}
