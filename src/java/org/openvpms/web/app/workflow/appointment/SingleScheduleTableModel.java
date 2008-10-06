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
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.appointment;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.app.workflow.scheduling.Schedule;
import org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid;
import static org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid.Availability.UNAVAILABLE;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.LabelFactory;

import java.util.Date;
import java.util.List;


/**
 * Appointment table model for a single schedule.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class SingleScheduleTableModel extends AppointmentTableModel {

    /**
     * The column names.
     */
    private String[] columnNames;

    /**
     * The status index.
     */
    private static final int STATUS_INDEX = 1;

    /**
     * The appointment name index.
     */
    private static final int APPOINTMENT_INDEX = 2;

    /**
     * The customer name index.
     */
    private static final int CUSTOMER_INDEX = 3;

    /**
     * The patient name index.
     */
    private static final int PATIENT_INDEX = 4;

    /**
     * The reason index.
     */
    private static final int REASON_INDEX = 5;

    /**
     * The description index.
     */
    private static final int DESCRIPTION_INDEX = 6;

    /**
     * The nodes to display.
     */
    private static final String[] NODE_NAMES = {
            "startTime", "status", "appointmentType", "customer", "patient",
            "reason", "description"};


    /**
     * Creates a new <tt>SingleScheduleTableModel</tt>.
     */
    public SingleScheduleTableModel(AppointmentGrid grid) {
        super(grid);
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
        AppointmentGrid grid = getGrid();
        if (column.getModelIndex() == START_TIME_INDEX) {
            result = grid.getStartTime(row);
        } else {
            ObjectSet set = getEvent(column, row);
            int rowSpan = 1;
            if (set != null) {
                result = getValue(set, column);
                rowSpan = grid.getSlots(set, row);
            } else {
                Schedule schedule = column.getSchedule();
                if (schedule != null) {
                    if (grid.getAvailability(schedule, row) == UNAVAILABLE) {
                        rowSpan = grid.getUnavailableSlots(schedule, row);
                    }
                }
            }
            if (rowSpan > 1) {
                if (!(result instanceof Component)) {
                    Label label = LabelFactory.create();
                    if (result != null) {
                        label.setText(result.toString());
                    }
                    result = label;
                }
                setSpan((Component) result, rowSpan);
            }
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
    protected Object getValue(ObjectSet set, TableColumn column) {
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
                result = getStatus(set);
                break;
            case REASON_INDEX:
                result = getReason(set);
                break;
            case DESCRIPTION_INDEX:
                result = set.getString(ScheduleEvent.ACT_DESCRIPTION);
                break;
            case APPOINTMENT_INDEX:
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
        String[] names = getColumnNames();
        for (int i = 0; i < names.length; ++i) {
            Column column = new Column(i, schedule, names[i]);
            result.addColumn(column);
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
            columnNames = new String[NODE_NAMES.length];
            ArchetypeDescriptor archetype
                    = DescriptorHelper.getArchetypeDescriptor(
                    "act.customerAppointment");
            if (archetype != null) {
                for (int i = 0; i < NODE_NAMES.length; ++i) {
                    columnNames[i] = getDisplayName(archetype, NODE_NAMES[i]);
                }
            }
        }
        return columnNames;
    }

}
