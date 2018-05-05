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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.TableFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.workflow.scheduling.Cell;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleColours;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;

import java.util.Date;
import java.util.List;

import static org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid.Availability.UNAVAILABLE;

/**
 * Appointment table model for a single schedule.
 *
 * @author Tim Anderson
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
            "startTime", "status", "appointmentType", "customer", "patient", "reason", "description"};


    /**
     * Constructs a {@link SingleScheduleTableModel}.
     *
     * @param grid    the appointment grid
     * @param context the context
     * @param colours the colour cache
     */
    public SingleScheduleTableModel(AppointmentGrid grid, Context context, ScheduleColours colours) {
        super(grid, context, colours);
    }

    /**
     * Determines if a cell is cut.
     * <p>
     * This implementation returns true if the row matches the cut row, and the column is any
     * other than the start time column.
     *
     * @param column the column
     * @param row    the row
     * @return {@code true} if the cell is cut
     */
    @Override
    public boolean isMarked(int column, int row) {
        Cell cell = getMarked();
        if (row != -1 && column != -1 && cell != null && row == cell.getRow()) {
            ScheduleColumn col = getColumns().get(column);
            if (col.getModelIndex() != START_TIME_INDEX) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param column the column
     * @param row    the row
     * @return the cell value
     */
    @Override
    public Object getValueAt(int column, int row) {
        Object result = null;
        AppointmentGrid grid = getGrid();
        Column c = getColumn(column);
        if (c.getModelIndex() == START_TIME_INDEX) {
            result = grid.getStartTime(row);
        } else {
            PropertySet set = getEvent(column, row);
            int rowSpan = 1;
            Schedule schedule = getSchedule(column, row);
            if (set != null) {
                if (Schedule.isBlockingEvent(set)) {
                    result = getBlock(set, c);
                } else {
                    result = getAppointment(set, c);
                }
                rowSpan = grid.getSlots(set, schedule, row);
            } else {
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
                ((Component) result).setLayoutData(TableFactory.rowSpan(rowSpan));
            }
        }
        return result;
    }

    /**
     * Returns the value found at the given coordinate within the table, for an appointment.
     *
     * @param set    the object
     * @param column the column
     * @return the value at the given coordinate.
     */
    protected Object getAppointment(PropertySet set, TableColumn column) {
        Object result = null;
        int index = column.getModelIndex();
        switch (index) {
            case STATUS_INDEX:
                result = getStatus(set);
                break;
            case REASON_INDEX:
                result = set.getString(ScheduleEvent.ACT_REASON_NAME);
                // fall back to the code
                if (result == null) {
                    result = set.getString(ScheduleEvent.ACT_REASON);
                }
                break;
            case DESCRIPTION_INDEX:
                result = set.getString(ScheduleEvent.ACT_DESCRIPTION);
                break;
            case APPOINTMENT_INDEX:
                result = getViewer(set, ScheduleEvent.SCHEDULE_TYPE_REFERENCE, ScheduleEvent.SCHEDULE_TYPE_NAME, false);
                break;
            case CUSTOMER_INDEX:
                result = getCustomer(set);
                break;
            case PATIENT_INDEX:
                result = getViewer(set, ScheduleEvent.PATIENT_REFERENCE, ScheduleEvent.PATIENT_NAME, true);
                break;
        }
        return result;
    }

    /**
     * Returns the value found at the given coordinate within the table, for a calendar block.
     *
     * @param set    the object
     * @param column the column
     * @return the value at the given coordinate.
     */
    protected Object getBlock(PropertySet set, TableColumn column) {
        Object result = null;
        int index = column.getModelIndex();
        switch (index) {
            case DESCRIPTION_INDEX:
                result = set.getString(ScheduleEvent.ACT_DESCRIPTION);
                break;
            case APPOINTMENT_INDEX:
                result = getViewer(set, ScheduleEvent.SCHEDULE_TYPE_REFERENCE, ScheduleEvent.SCHEDULE_TYPE_NAME, false);
                break;
        }
        return result;
    }

    /**
     * Creates a column model to display a single schedule.
     *
     * @param grid the appointment grid
     * @return a new column model
     */
    protected TableColumnModel createColumnModel(ScheduleEventGrid grid) {
        DefaultTableColumnModel result = new DefaultTableColumnModel();
        List<Schedule> schedules = grid.getSchedules();
        Schedule schedule = schedules.get(0);
        String[] names = getColumnNames();

        // the first column is the start time
        ScheduleColumn startTime = new ScheduleColumn(0, schedule, names[0]);
        startTime.setHeaderRenderer(AppointmentTableHeaderRenderer.INSTANCE);
        startTime.setCellRenderer(new TimeColumnCellRenderer());
        result.addColumn(startTime);

        // add the node columns
        SingleScheduleTableCellRenderer renderer = new SingleScheduleTableCellRenderer(this);
        for (int i = 1; i < names.length; ++i) {
            ScheduleColumn column = new ScheduleColumn(i, schedule, names[i]);
            column.setHeaderRenderer(AppointmentTableHeaderRenderer.INSTANCE);
            column.setCellRenderer(renderer);
            result.addColumn(column);
        }
        return result;
    }

    /**
     * Returns the display name of the specified node.
     *
     * @param archetype the archetype descriptor
     * @param name      the node name
     * @return the display name, or {@code null} if the node doesn't exist
     */
    protected String getDisplayName(ArchetypeDescriptor archetype, String name) {
        NodeDescriptor descriptor = archetype.getNodeDescriptor(name);
        return (descriptor != null) ? descriptor.getDisplayName() : null;
    }

    /**
     * Returns a component representing the customer.
     *
     * @param event the appointment event
     * @return a new component
     */
    private Component getCustomer(PropertySet event) {
        Component result = getViewer(event, ScheduleEvent.CUSTOMER_REFERENCE, ScheduleEvent.CUSTOMER_NAME, true);
        boolean sendReminder = event.getBoolean(ScheduleEvent.SEND_REMINDER);
        Date reminderSent = event.getDate(ScheduleEvent.REMINDER_SENT);
        String reminderError = event.getString(ScheduleEvent.REMINDER_ERROR);
        if (sendReminder || reminderSent != null || reminderError != null) {
            Label reminder = AbstractAppointmentTableCellRender.createReminderIcon(reminderSent, reminderError);
            reminder.setLayoutData(RowFactory.layout(new Alignment(Alignment.RIGHT, Alignment.TOP), Styles.FULL_WIDTH));
            result = RowFactory.create(Styles.CELL_SPACING, result, reminder);
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
            columnNames[0] = Messages.get("workflow.scheduling.table.time");
            ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(ScheduleArchetypes.APPOINTMENT);
            if (archetype != null) {
                for (int i = 1; i < NODE_NAMES.length; ++i) {
                    columnNames[i] = getDisplayName(archetype, NODE_NAMES[i]);
                }
            }
        }
        return columnNames;
    }

}
