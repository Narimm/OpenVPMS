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

package org.openvpms.web.app.workflow.scheduling;

import echopointng.BalloonHelp;
import echopointng.layout.TableLayoutDataEx;
import echopointng.table.TableColumnEx;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.AbstractTableModel;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.workflow.Appointment;
import org.openvpms.archetype.rules.workflow.AppointmentStatus;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import static org.openvpms.web.app.workflow.scheduling.AppointmentGrid.Availability.UNAVAILABLE;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Appointment table model.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentTableModel extends AbstractTableModel {

    public enum Highlight {
        APPOINTMENT, CLINICIAN, STATUS
    }

    public enum TimeRange {
        ALL, MORNING, AFTERNOON, EVENING, AM, PM
    }

    /**
     * Appointment grid.
     */
    private AppointmentGrid grid;

    /**
     * The current view.
     */
    private AppointmentGrid view;

    /**
     * The column model.
     */
    private TableColumnModel model = new DefaultTableColumnModel();

    /**
     * Determines if a single schedule is being displayed.
     */
    private boolean singleScheduleView;

    /**
     * Determines cell colour.
     */
    private Highlight highlight = Highlight.APPOINTMENT;

    /**
     * The clinician to display appointments for.
     * If <tt>null</tt> indicates to display appointments for all clinicians.
     */
    private IMObjectReference clinician;

    /**
     * Determines the time range to display.
     */
    private TimeRange timeRange = TimeRange.ALL;

    /**
     * The selected column.
     */
    private int selectedColumn = -1;

    /**
     * The selected cell.
     */
    private int selectedRow = -1;

    /**
     * The column names, for single schedule view.
     */
    private String[] columnNames;

    /**
     * Cached status lookup names.
     */
    private Map<String, String> statuses;

    /**
     * Cached reason lookup names.
     */
    private Map<String, String> reasons;

    /**
     * Cell renderer.
     */
    private AppointmentTableCellRenderer cellRenderer;

    /**
     * The start time index.
     */
    protected static final int START_TIME_INDEX = 0;

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
    private static final String[][] NODE_NAMES = new String[][]{
            {"startTime", Appointment.ACT_START_TIME},
            {"status", Appointment.ACT_STATUS},
            {"appointmentType", Appointment.APPOINTMENT_TYPE_REFERENCE},
            {"customer", Appointment.CUSTOMER_REFERENCE},
            {"patient", Appointment.PATIENT_REFERENCE},
            {"reason", Appointment.ACT_REASON},
            {"description", Appointment.ACT_DESCRIPTION}};


    /**
     * Creates a new <tt>AppointmentTableModel</tt>.
     */
    public AppointmentTableModel() {
        cellRenderer = new AppointmentTableCellRenderer();

        columnNames = new String[NODE_NAMES.length];
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(
                "act.customerAppointment");
        if (archetype != null) {
            for (int i = 0; i < NODE_NAMES.length; ++i) {
                NodeDescriptor descriptor = archetype.getNodeDescriptor(
                        NODE_NAMES[i][0]);
                if (descriptor != null) {
                    columnNames[i] = descriptor.getDisplayName();
                }
            }
        }
    }

    /**
     * Returns the schedules.
     *
     * @return the schedules
     */
    public List<Party> getSchedules() {
        Set<Party> result = new HashSet<Party>();
        for (Column column : getColumns()) {
            if (column.getSchedule() != null) {
                result.add(column.getSchedule().getSchedule());
            }
        }
        return new ArrayList<Party>(result);
    }

    /**
     * Returns the availability of a slot, given its column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the availability
     */
    public AppointmentGrid.Availability getAvailability(int column, int row) {
        Column col = getColumn(column);
        if (col.getModelIndex() == START_TIME_INDEX && !singleScheduleView) {
            return UNAVAILABLE;
        }
        return col.getAvailability(row);
    }

    /**
     * Sets the appointments for each schedule.
     *
     * @param day          the day being displayed
     * @param appointments the appointments, keyed on schedule
     */
    public void setAppointments(Date day,
                                Map<Party, List<ObjectSet>> appointments) {
        cellRenderer.refresh();

        Set<Party> schedules = appointments.keySet();
        singleScheduleView = schedules.size() == 1;
        if (singleScheduleView) {
            Party schedule = schedules.iterator().next();
            grid = new SingleScheduleGrid(day, schedule,
                                          appointments.get(schedule));
        } else {
            grid = new MultiScheduleGrid(day, appointments);
        }
        view = createGridView(grid, timeRange);
        model = createColumnModel(grid);
        fireTableStructureChanged();
    }

    /**
     * Determines if a single schedule is being displayed.
     *
     * @return <tt>true</tt> if a single schedule is being displayed
     */
    public boolean isSingleScheduleView() {
        return singleScheduleView;
    }

    /**
     * Determines the scheme to colour cells.
     * <p/>
     * Defaults to {@link Highlight#APPOINTMENT}.
     *
     * @param highlight the highlight
     */
    public void setHighlight(Highlight highlight) {
        this.highlight = highlight;
        fireTableDataChanged();
    }

    /**
     * Determines the scheme to colour cells.
     *
     * @return the highlight
     */
    public Highlight getHighlight() {
        return highlight;
    }

    /**
     * Sets the clinician to display appointments for.
     *
     * @param clinician the clinician, or <tt>null</tt> to display appointments
     *                  for all clinicians
     */
    public void setClinician(IMObjectReference clinician) {
        this.clinician = clinician;
        fireTableDataChanged();
    }

    /**
     * Returns the clinician to display appointments for.
     *
     * @return the clinician, or <tt>null</tt> to display appointments
     *         for all clinicians
     */
    public IMObjectReference getClinician() {
        return clinician;
    }

    /**
     * Determines the time range to display.
     * <p/>
     * Defaults to {@link TimeRange#ALL}.
     *
     * @param timeRange the time range
     */
    public void setTimeRange(TimeRange timeRange) {
        this.timeRange = timeRange;
        view = createGridView(grid, timeRange);
        fireTableStructureChanged();
    }

    /**
     * Sets the selected cell.
     *
     * @param column the selected column
     * @param row    the selected row
     */
    public void setSelectedCell(int column, int row) {
        int oldColumn = selectedColumn;
        int oldRow = selectedRow;
        selectedColumn = column;
        selectedRow = row;
        if (oldColumn != -1 && oldRow != -1) {
            fireTableCellUpdated(oldColumn, oldRow);
        }
        if (selectedColumn != -1 && selectedRow != -1) {
            fireTableCellUpdated(selectedColumn, selectedRow);
        }
    }

    /**
     * Determines if a cell is selected.
     *
     * @param column the column
     * @param row    the row
     */
    public boolean isSelectedCell(int column, int row) {
        return selectedColumn == column && selectedRow == row;
    }

    /**
     * Returns the name of the specified column number.
     *
     * @param column the column index (0-based)
     * @return the column name
     */
    @Override
    public String getColumnName(int column) {
        return getColumn(column).getHeaderValue().toString();
    }

    /**
     * Returns the column model.
     *
     * @return the column model
     */
    public TableColumnModel getColumnModel() {
        return model;
    }

    /**
     * Returns the number of columns in the table.
     *
     * @return the column count
     */
    public int getColumnCount() {
        return model.getColumnCount();
    }

    /**
     * Returns the number of rows in the table.
     *
     * @return the row count
     */
    public int getRowCount() {
        return view.getSlots();
    }

    /**
     * Returns the value found at the given coordinate within the table.
     * Column and row values are 0-based.
     * <strong>WARNING: Take note that the column is the first parameter
     * passed to this method, and the row is the second parameter.</strong>
     *
     * @param column the column index (0-based)
     * @param row    the row index (0-based)
     */
    public Object getValueAt(int column, int row) {
        Object result = null;
        if (column == START_TIME_INDEX) {
            Date date = getStartTime(row);
            Label label = LabelFactory.create();
            if (date != null) {
                label.setText(DateHelper.formatTime(date, false));
            }
            result = label;
        } else {
            Column col = getColumn(column);
            ObjectSet set = col.getAppointment(row);
            int rowSpan = 1;
            if (set != null) {
                if (singleScheduleView) {
                    result = getValue(set, col);
                } else {
                    result = getAppointment(set);
                }
                rowSpan = view.getSlots(set, row);
            } else {
                Schedule schedule = col.getSchedule();
                if (schedule != null) {
                    if (view.getAvailability(schedule, row) == UNAVAILABLE) {
                        rowSpan = view.getUnavailableSlots(schedule, row);
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
     * Returns the start time at the specified row.
     *
     * @param row the row
     * @return the start time
     */
    public Date getStartTime(int row) {
        return view.getStartTime(row);
    }

    /**
     * Returns the hour at the specified row.
     *
     * @param row the row
     * @return the hour
     */
    public int getHour(int row) {
        return view.getHour(row);
    }

    /**
     * Returns the appointment at the specified column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the appointment, or <tt>null</tt> if none is found
     */
    public ObjectSet getAppointment(int column, int row) {
        return getColumn(column).getAppointment(row);
    }

    /**
     * Returns the schedule at the given column.
     *
     * @param column the column
     * @return the schedule, or <tt>null</tt> if there is no schedule associated
     *         with the column
     */
    public Party getSchedule(int column) {
        Column col = getColumn(column);
        Schedule schedule = col.getSchedule();
        return (schedule != null) ? schedule.getSchedule() : null;
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
        Object value = set.get(NODE_NAMES[index][1]);
        switch (index) {
            case START_TIME_INDEX:
                Date date = (Date) value;
                Label label = LabelFactory.create();
                if (date != null) {
                    label.setText(DateHelper.formatTime(date, false));
                }
                result = label;
                break;
            case STATUS_INDEX:
                if (value instanceof String) {
                    result = getStatus(set, (String) value);
                }
                break;
            case REASON_INDEX:
                if (value instanceof String) {
                    result = getReason((String) value);
                }
                break;
            case DESCRIPTION_INDEX:
                result = value;
                break;
            case APPOINTMENT_INDEX:
                result = getViewer(set, Appointment.APPOINTMENT_TYPE_REFERENCE,
                                   Appointment.APPOINTMENT_TYPE_NAME, false);
                break;
            case CUSTOMER_INDEX:
                result = getViewer(set, Appointment.CUSTOMER_REFERENCE,
                                   Appointment.CUSTOMER_NAME, true);
                break;
            case PATIENT_INDEX:
                result = getViewer(set, Appointment.PATIENT_REFERENCE,
                                   Appointment.PATIENT_NAME, true);
                break;
        }
        return result;
    }

    /**
     * Returns a component representing an appointment.
     *
     * @param set the appointment
     * @return a new component
     */
    private Component getAppointment(ObjectSet set) {
        Component result;
        String text;
        String customer = set.getString(Appointment.CUSTOMER_NAME);
        String patient = set.getString(Appointment.PATIENT_NAME);
        String notes = set.getString(Appointment.ACT_DESCRIPTION);
        if (patient == null) {
            text = Messages.get("workflow.scheduling.table.customer", customer);
        } else {
            text = Messages.get(
                    "workflow.scheduling.table.customerpatient",
                    customer, patient);
        }
        Label label = LabelFactory.create();
        label.setText(text);
        if (notes != null) {
            BalloonHelp help = new BalloonHelp("<p>" + notes + "</p>");
            result = RowFactory.create("CellSpacing", label, help);
        } else {
            result = label;
        }
        return result;
    }

    /**
     * Sets the row span of a component.
     *
     * @param component the component
     * @param rowSpan   the row span
     */
    private void setSpan(Component component, int rowSpan) {
        TableLayoutDataEx layout = new TableLayoutDataEx();
        layout.setRowSpan(rowSpan);
        component.setLayoutData(layout);
    }

    /**
     * Returns a column given its model index.
     *
     * @param column the column index
     * @return the column
     */
    private Column getColumn(int column) {
        Column result = null;
        Iterator iterator = model.getColumns();
        while (iterator.hasNext()) {
            Column col = (Column) iterator.next();
            if (col.getModelIndex() == column) {
                result = col;
                break;
            }
        }
        return result;
    }

    /**
     * Returns a status name given its code.
     *
     * @param set  the object set
     * @param code the status code
     * @return the status name
     */
    private String getStatus(ObjectSet set, String code) {
        String status = null;

        if (AppointmentStatus.CHECKED_IN.equals(code)) {
            Date arrival = set.getDate(Appointment.ARRIVAL_TIME);
            if (arrival != null) {
                String diff = DateHelper.formatTimeDiff(arrival, new Date());
                status = Messages.get("workflow.scheduling.table.waiting",
                                      diff);
            }
        }
        if (status == null) {
            if (statuses == null) {
                statuses = LookupNameHelper.getLookupNames(
                        "act.customerAppointment", "status");
            }
            if (statuses != null) {
                status = statuses.get(code);
            }
        }
        return status;
    }

    /**
     * Returns a reason name given its code.
     *
     * @param code the reason code
     * @return the reason name
     */
    private String getReason(String code) {
        if (reasons == null) {
            reasons = LookupNameHelper.getLookupNames("act.customerAppointment",
                                                      "reason");
        }
        return (reasons != null) ? reasons.get(code) : null;
    }

    /**
     * Returns a viewer for an object reference.
     *
     * @param set     the object set
     * @param refKey  the object reference key
     * @param nameKey the entity name key
     * @param link    if <code>true</code> enable an hyperlink to the object
     * @return a new component to view the object reference
     */
    private Component getViewer(ObjectSet set, String refKey, String nameKey,
                                boolean link) {
        IMObjectReference ref = set.getReference(refKey);
        String name = set.getString(nameKey);
        IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(
                ref, name, link);
        return viewer.getComponent();
    }

    /**
     * Creates a new view of the appointments.
     *
     * @param grid      the underlying appointment grid
     * @param timeRange the time range to view
     * @return view a new grid view, based on the time range
     */
    private AppointmentGrid createGridView(AppointmentGrid grid,
                                           TimeRange timeRange) {
        int startMins;
        int endMins;
        switch (timeRange) {
            case MORNING:
                startMins = 8 * 60;
                endMins = 12 * 60;
                break;
            case AFTERNOON:
                startMins = 12 * 60;
                endMins = 17 * 60;
                break;
            case EVENING:
                startMins = 17 * 60;
                endMins = 24 * 60;
                break;
            case AM:
                startMins = 0;
                endMins = 12 * 60;
                break;
            case PM:
                startMins = 12 * 60;
                endMins = 24 * 60;
                break;
            default:
                startMins = 0;
                endMins = 24 * 60;
        }
        if (startMins < grid.getStartMins()) {
            startMins = grid.getStartMins();
        }
        if (endMins > grid.getEndMins()) {
            endMins = grid.getEndMins();
        }
        if (startMins > endMins) {
            startMins = endMins;
        }
        return new AppointmentGridView(grid, startMins, endMins);
    }

    /**
     * Creates a column model to display a list of schedules. If there is only
     * a single schedule, a column will be created for each of the nodes
     * indicated by {@link #NODE_NAMES}, otherwise a column will be created for
     * each schedule.
     *
     * @param grid the appointment grid
     * @return a new column model
     */
    private TableColumnModel createColumnModel(AppointmentGrid grid) {
        DefaultTableColumnModel result = new DefaultTableColumnModel();
        List<Schedule> schedules = grid.getSchedules();
        int index = START_TIME_INDEX;
        if (schedules.size() == 1) {
            Schedule schedule = schedules.get(0);
            for (int i = 0; i < columnNames.length; ++i) {
                Column column = new Column(i, schedule, columnNames[i]);
                result.addColumn(column);
            }
        } else {
            result.addColumn(new Column(index, columnNames[index]));
            ++index;
            Column lastColumn = null;
            Party lastSchedule = null;
            for (Schedule schedule : schedules) {
                Column column = new Column(index++, schedule);
                result.addColumn(column);
                if (lastColumn != null
                        && ObjectUtils.equals(lastSchedule,
                                              schedule.getSchedule())) {
                    lastColumn.setNextColumn(column);
                }
                lastColumn = column;
                lastSchedule = schedule.getSchedule();
            }
        }
        return result;
    }

    /**
     * Returns the columns.
     *
     * @return the columns
     */
    private List<Column> getColumns() {
        List<Column> result = new ArrayList<Column>();
        Iterator iterator = model.getColumns();
        while (iterator.hasNext()) {
            result.add((Column) iterator.next());
        }
        return result;
    }

    /**
     * Schedule column.
     */
    private class Column extends TableColumnEx {

        /**
         * The schedule, or <tt>null</tt> if the column isn't associated with
         * a schedule.
         */
        private Schedule schedule;

        /**
         * The next column with the same schedule.
         */
        private Column nextColumn;


        /**
         * Creates a new <tt>Column</tt>.
         *
         * @param modelIndex the model index
         * @param schedule   the schedule
         */
        public Column(int modelIndex, Schedule schedule) {
            this(modelIndex, schedule, schedule.getName());
        }

        /**
         * Creates a new <tt>Column</tt>.
         *
         * @param modelIndex the model index
         * @param schedule   the schedule
         * @param heading    the column heading
         */
        public Column(int modelIndex, Schedule schedule, String heading) {
            super(modelIndex);
            this.schedule = schedule;
            setHeaderValue(heading);
            setHeaderRenderer(AppointmentTableHeaderRenderer.INSTANCE);
            setCellRenderer(cellRenderer);
            setWidth(new Extent(100));
        }

        /**
         * Creates a new <tt>Column</tt>.
         *
         * @param modelIndex the model index
         * @param heading    the column heading
         */
        public Column(int modelIndex, String heading) {
            this(modelIndex, null, heading);
        }

        /**
         * Returns the schedule.
         *
         * @return the schedule. May be <tt>null</tt>
         */
        public Schedule getSchedule() {
            return schedule;
        }

        /**
         * Returns the appointment at the specified slot.
         *
         * @param slot the slot
         * @return the appointment, or <tt>null</tt> if none is found
         */
        public ObjectSet getAppointment(int slot) {
            if (schedule != null) {
                return view.getAppointment(schedule, slot);
            }
            return null;
        }

        /**
         * Returns the schedule availability of the specified row.
         *
         * @param row the row
         * @return the availability
         */
        public AppointmentGrid.Availability getAvailability(int row) {
            if (schedule != null) {
                return view.getAvailability(schedule, row);
            }
            return UNAVAILABLE;
        }

        /**
         * Sets the next column with the same schedule.
         *
         * @param column the next column
         */
        public void setNextColumn(Column column) {
            nextColumn = column;
        }

        /**
         * Returns the next column with the same schedule
         *
         * @return the next column. May be <tt>null</tt>
         */
        public Column getNextColumn() {
            return nextColumn;
        }
    }

}
