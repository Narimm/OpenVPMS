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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.app.workflow.worklist;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.app.workflow.scheduling.Schedule;
import org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid;
import org.openvpms.web.app.workflow.scheduling.ScheduleTableModel;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;
import java.util.List;


/**
 * Table model for display <em>act.customerTask<em>s for a single schedule.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SingleScheduleTaskTableModel extends ScheduleTableModel {

    /**
     * The column names, for single schedule view.
     */
    private String[] columnNames;

    /**
     * The start time index.
     */
    private static final int START_TIME_INDEX = 0;

    /**
     * The status index.
     */
    private static final int STATUS_INDEX = 1;

    /**
     * The task type name index.
     */
    private static final int TASK_TYPE_INDEX = 2;

    /**
     * The customer name index.
     */
    private static final int CUSTOMER_INDEX = 3;

    /**
     * The patient name index.
     */
    private static final int PATIENT_INDEX = 4;

    /**
     * The description index.
     */
    private static final int DESCRIPTION_INDEX = 5;

    /**
     * The elapsed time index.
     */
    private static final int ELAPSED_TIME_INDEX = 6;

    /**
     * The nodes to display.
     */
    private static final String[] NODE_NAMES = {
        "startTime", "status", "taskType", "customer", "patient",
        "description"};


    /**
     * Creates a new {@code SingleScheduleTaskTableModel}.
     *
     * @param grid the task grid
     */
    public SingleScheduleTaskTableModel(TaskGrid grid, Context context) {
        super(grid, context);
    }

    /**
     * Returns the row of the specified event.
     *
     * @param schedule the schedule
     * @param eventRef the event reference
     * @return the row, or {@code -1} if the event is not found
     */
    public int getRow(Schedule schedule, IMObjectReference eventRef) {
        return schedule.indexOf(eventRef);
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param column the column
     * @param row    the row
     * @return the cell value
     */
    protected Object getValueAt(Column column, int row) {
        Object result = null;
        PropertySet set = getEvent(column, row);
        if (set != null) {
            result = getValue(set, column);
        }
        return result;
    }

    /**
     * Creates a column model to display a list of schedules.
     *
     * @param grid the appointment grid
     * @return a new column model
     */
    protected TableColumnModel createColumnModel(ScheduleEventGrid grid) {
        DefaultTableColumnModel result = new DefaultTableColumnModel();
        List<Schedule> schedules = grid.getSchedules();
        Schedule schedule = schedules.get(0);
        int i = 0;
        String[] names = getColumnNames();
        for (; i < names.length; ++i) {
            Column column = new Column(i, schedule, names[i]);
            result.addColumn(column);
        }
        return result;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param set    the object
     * @param column the column
     * @return the value at the given coordinate.
     */
    private Object getValue(PropertySet set, Column column) {
        Object result = null;
        int index = column.getModelIndex();
        switch (index) {
            case START_TIME_INDEX:
                Date date = set.getDate(ScheduleEvent.ACT_START_TIME);
                Label label = LabelFactory.create();
                if (date != null) {
                    label.setText(DateHelper.formatTime(date, false));
                }
                result = label;
                break;
            case STATUS_INDEX:
                result = set.getString(ScheduleEvent.ACT_STATUS_NAME);
                break;
            case DESCRIPTION_INDEX:
                result = set.getString(ScheduleEvent.ACT_DESCRIPTION);
                break;
            case TASK_TYPE_INDEX:
                result = getViewer(set, ScheduleEvent.SCHEDULE_TYPE_REFERENCE,
                                   ScheduleEvent.SCHEDULE_TYPE_NAME, false);
                break;
            case CUSTOMER_INDEX:
                result = getViewer(set, ScheduleEvent.CUSTOMER_REFERENCE,
                                   ScheduleEvent.CUSTOMER_NAME, true);
                break;
            case PATIENT_INDEX:
                result = getViewer(set, ScheduleEvent.PATIENT_REFERENCE,
                                   ScheduleEvent.PATIENT_NAME, true);
                break;
            case ELAPSED_TIME_INDEX:
                Date start = set.getDate(ScheduleEvent.ACT_START_TIME);
                Date end = set.getDate(ScheduleEvent.ACT_END_TIME);
                if (start != null) {
                    if (end == null) {
                        end = new Date();
                    }
                    result = DateHelper.formatTimeDiff(start, end);
                }
                break;
        }
        return result;
    }

    /**
     * Returns the column names.
     *
     * @return the column names
     */
    private String[] getColumnNames() {
        if (columnNames == null) {
            columnNames = new String[NODE_NAMES.length + 1];
            ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(
                "act.customerTask");
            if (archetype != null) {
                for (int i = 0; i < NODE_NAMES.length; ++i) {
                    NodeDescriptor descriptor = archetype.getNodeDescriptor(
                        NODE_NAMES[i]);
                    if (descriptor != null) {
                        columnNames[i] = descriptor.getDisplayName();
                    }
                }
            }
            columnNames[ELAPSED_TIME_INDEX] = Messages.get("table.act.time");
        }
        return columnNames;
    }

}
