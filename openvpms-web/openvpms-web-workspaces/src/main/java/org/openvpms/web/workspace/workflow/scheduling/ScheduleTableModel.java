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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.scheduling;

import echopointng.BalloonHelp;
import echopointng.layout.TableLayoutDataEx;
import echopointng.table.TableColumnEx;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.AbstractTableModel;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.echo.factory.BalloonHelpFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Schedule event table model.
 *
 * @author Tim Anderson
 */
public abstract class ScheduleTableModel extends AbstractTableModel {

    public enum Highlight {

        EVENT_TYPE, CLINICIAN, STATUS
    }

    /**
     * Schedule event grid.
     */
    private final ScheduleEventGrid grid;

    /**
     * The context.
     */
    private final Context context;

    /**
     * Determines if the columns display schedules.
     */
    private final boolean scheduleColumns;

    /**
     * The column model.
     */
    private TableColumnModel model = new DefaultTableColumnModel();

    /**
     * The clinician to display events for.
     * If {@code null} indicates to display events for all clinicians.
     */
    private IMObjectReference clinician;

    /**
     * The selected cell.
     */
    private Cell selected;

    /**
     * The marked cell.
     */
    private Cell marked;

    /**
     * If {@code true} the marked cell is being cut, else it is being copied.
     */
    private boolean isCut;

    /**
     * Determines cell colour.
     */
    private Highlight highlight = Highlight.EVENT_TYPE;

    /**
     * The display expression, from the schedule view. May be {@code null}
     */
    private final String expression;

    /**
     * Determines if the notes popup should be displayed.
     */
    private final boolean displayNotes;


    /**
     * Constructs a {@code ScheduleTableModel}.
     *
     * @param grid            the schedule event grid
     * @param context         the context
     * @param scheduleColumns if {@code true}, display the schedules on the columns, otherwise display them on the rows
     */
    public ScheduleTableModel(ScheduleEventGrid grid, Context context, boolean scheduleColumns) {
        this.grid = grid;
        this.context = context;
        this.scheduleColumns = scheduleColumns;
        IMObjectBean bean = new IMObjectBean(grid.getScheduleView());
        expression = bean.getString("displayExpression");
        displayNotes = bean.getBoolean("displayNotes");
        model = createColumnModel(grid);
    }

    /**
     * Returns the schedules.
     *
     * @return the schedules
     */
    public List<Schedule> getSchedules() {
        return grid.getSchedules();
    }

    /**
     * Returns the cell for a schedule and event reference.
     *
     * @param schedule the schedule reference
     * @param event    the event reference
     * @return the cell, or {@code null} if none is found
     */
    public Cell getCell(IMObjectReference schedule, IMObjectReference event) {
        Cell result = null;
        if (scheduleColumns) {
            for (ScheduleColumn column : getColumns(schedule)) {
                Schedule s = column.getSchedule();
                int slot = getSlot(s, event);
                if (slot != -1) {
                    int row = getCellRow(slot);
                    result = new Cell(column.getModelIndex(), row);
                }
            }
        } else {
            for (ScheduleRow row : getRows(schedule)) {
                Schedule s = row.getSchedule();
                int slot = getSlot(s, event);
                if (slot != -1) {
                    int column = getCellColumn(slot);
                    result = new Cell(column, row.getRow());
                }
            }
        }
        return result;
    }

    /**
     * Returns the cell for the selected schedule and date.
     *
     * @param schedule the schedule
     * @param date     the date
     * @return the cell or {@code null} if there is none
     */
    public Cell getCell(IMObjectReference schedule, Date date) {
        Cell result = null;
        int slot = grid.getSlot(date);
        if (slot != -1) {
            if (scheduleColumns) {
                List<ScheduleColumn> columns = getColumns(schedule);
                if (!columns.isEmpty()) {
                    int index = columns.get(0).getModelIndex();
                    result = new Cell(index, getCellRow(slot));
                }
            } else {
                List<ScheduleRow> rows = getRows(schedule);
                if (!rows.isEmpty()) {
                    int row = rows.get(0).getRow();
                    result = new Cell(getCellColumn(slot), row);
                }
            }
        }
        return result;
    }

    /**
     * Returns the slot of the specified event.
     *
     * @param schedule the schedule
     * @param eventRef the event reference
     * @return the slot, or {@code -1} if the event is not found
     */
    public abstract int getSlot(Schedule schedule, IMObjectReference eventRef);

    /**
     * Sets the clinician to display appointments for.
     *
     * @param clinician the clinician, or {@code null} to display appointments
     *                  for all clinicians
     */
    public void setClinician(IMObjectReference clinician) {
        this.clinician = clinician;
        fireTableDataChanged();
    }

    /**
     * Returns the clinician to display appointments for.
     *
     * @return the clinician, or {@code null} to display appointments
     *         for all clinicians
     */
    public IMObjectReference getClinician() {
        return clinician;
    }

    /**
     * Sets the selected cell.
     *
     * @param cell the selected cell. May be {@code null}
     */
    public void setSelected(Cell cell) {
        Cell old = selected;
        selected = cell;
        if (old != null) {
            fireTableCellUpdated(old.getColumn(), old.getRow());
        }
        if (selected != null) {
            fireTableCellUpdated(selected.getColumn(), selected.getRow());
        }
    }

    /**
     * Returns the selected cell.
     *
     * @return the selected cell, or {@code null} if none is selected
     */
    public Cell getSelected() {
        return selected;
    }

    /**
     * Determines if a cell is selected.
     *
     * @param column the column
     * @param row    the row
     * @return {@code true} if the cell is selected
     */
    public boolean isSelected(int column, int row) {
        return selected != null && selected.equals(column, row);
    }

    /**
     * Sets the marked cell. This flags a cell as being marked for cutting/copying and pasting purposes.
     *
     * @param cell  the cell, or {@code null} to unmark the cell
     * @param isCut if {@code true} indicates the cell is being cut; if {@code false} indicates its being copied.
     *              Ignored if the cell is being unmarked.
     */
    public void setMarked(Cell cell, boolean isCut) {
        Cell old = marked;
        this.isCut = isCut;
        marked = cell;
        if (old != null) {
            fireTableCellUpdated(old.getColumn(), old.getRow());
        }
        if (marked != null) {
            fireTableCellUpdated(marked.getColumn(), marked.getRow());
        }
    }

    /**
     * Returns the marked cell.
     *
     * @return the marked cell, or {@code null} if no cell is marked
     */
    public Cell getMarked() {
        return marked;
    }

    /**
     * Determines if a cell is marked for cut/copy.
     *
     * @param column the column
     * @param row    the row
     * @return {@code true} if the cell is cut
     */
    public boolean isMarked(int column, int row) {
        return marked != null && marked.equals(column, row);
    }

    /**
     * Determines if the marked cell is being cut or copied.
     *
     * @return {@code true} if the cell is being cut; {@code false} if it is being copied
     */
    public boolean isCut() {
        return isCut;
    }

    /**
     * Determines the scheme to colour cells.
     * <p/>
     * Defaults to {@link Highlight#EVENT_TYPE}.
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
     * Determines if this is a single schedule view.
     *
     * @return {@code true} if this is a single schedule view
     */
    public boolean isSingleScheduleView() {
        return getSchedules().size() == 1;
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
     * Returns the grid.
     *
     * @return the grid
     */
    public ScheduleEventGrid getGrid() {
        return grid;
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
        return (scheduleColumns) ? grid.getSlots() : grid.getSchedules().size();
    }

    /**
     * Returns the event at the specified cell.
     *
     * @param cell the cell
     * @return the event, or {@code null} if none is found
     */
    public PropertySet getEvent(Cell cell) {
        return getEvent(cell.getColumn(), cell.getRow());
    }

    /**
     * Returns the event at the specified column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the event, or {@code null} if none is found
     */
    public PropertySet getEvent(int column, int row) {
        int slot = getSlot(column, row);
        Schedule schedule = getSchedule(column, row);
        return (schedule != null) ? grid.getEvent(schedule, slot) : null;
    }

    /**
     * Returns the schedule at the given cell.
     *
     * @param cell the cell
     * @return the schedule, or {@code null} if there is no schedule associated with the cell
     */
    public Schedule getSchedule(Cell cell) {
        return getSchedule(cell.getColumn(), cell.getRow());
    }

    /**
     * Returns the schedule at the given column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the schedule, or {@code null} if there is no schedule associated with the column and row
     */
    public Schedule getSchedule(int column, int row) {
        if (scheduleColumns) {
            ScheduleColumn col = (ScheduleColumn) getColumn(column);
            return col.getSchedule();
        } else {
            return grid.getSchedules().get(row);
        }
    }

    /**
     * Returns the schedule entity at the given cell.
     *
     * @return the schedule entity, or {@code null} if there is no schedule associated with the cell
     */
    public Entity getScheduleEntity(Cell cell) {
        return getScheduleEntity(cell.getColumn(), cell.getRow());
    }

    /**
     * Returns the schedule entity at the given column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the schedule entity, or {@code null} if there is no schedule associated with the column and row
     */
    public Entity getScheduleEntity(int column, int row) {
        Schedule schedule = getSchedule(column, row);
        return (schedule != null) ? schedule.getSchedule() : null;
    }


    /**
     * Returns the availability of the specified cell.
     *
     * @param cell the cell
     * @return the availability of the cell
     */
    public ScheduleEventGrid.Availability getAvailability(Cell cell) {
        return getAvailability(cell.getColumn(), cell.getRow());
    }

    /**
     * Returns the availability of the specified cell.
     *
     * @param column the column
     * @param row    the row
     * @return the availability of the cell
     */
    public ScheduleEventGrid.Availability getAvailability(int column, int row) {
        Schedule schedule = getSchedule(column, row);
        if (schedule == null) {
            return ScheduleEventGrid.Availability.UNAVAILABLE;
        }
        int slot = getSlot(column, row);
        return grid.getAvailability(schedule, slot);
    }

    /**
     * Returns the event start time at the specified cell.
     *
     * @param schedule the schedule
     * @param cell     the cell
     * @return the start time. May be {@code null}
     */
    public Date getStartTime(Schedule schedule, Cell cell) {
        int slot = getSlot(cell.getColumn(), cell.getRow());
        return grid.getStartTime(schedule, slot);
    }

    /**
     * Returns the event at the specified schedule and slot.
     *
     * @param schedule the schedule. May be {@code null}
     * @param slot     the slot
     * @return the event, or {@code null} if none is found
     */
    protected PropertySet getEvent(Schedule schedule, int slot) {
        return (schedule != null) ? grid.getEvent(schedule, slot) : null;
    }

    /**
     * Creates a column model to display a list of schedules.
     *
     * @param grid the appointment grid
     * @return a new column model
     */
    protected abstract TableColumnModel createColumnModel(ScheduleEventGrid grid);

    /**
     * Returns a viewer for an object reference.
     *
     * @param set     the set
     * @param refKey  the object reference key
     * @param nameKey the entity name key
     * @param link    if {@code true} enable an hyperlink to the object
     * @return a new component to view the object reference
     */
    protected Component getViewer(PropertySet set, String refKey, String nameKey, boolean link) {
        IMObjectReference ref = set.getReference(refKey);
        String name = set.getString(nameKey);
        IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(ref, name, link, context);
        return viewer.getComponent();
    }

    /**
     * Helper to returns the columns.
     *
     * @return the columns
     */
    protected List<ScheduleColumn> getColumns() {
        List<ScheduleColumn> result = new ArrayList<ScheduleColumn>();
        Iterator iterator = model.getColumns();
        while (iterator.hasNext()) {
            result.add((ScheduleColumn) iterator.next());
        }
        return result;
    }

    /**
     * Returns the slot of a cell.
     *
     * @param column the column
     * @param row    the row
     * @return the slot
     */
    protected int getSlot(int column, int row) {
        return (scheduleColumns) ? row : column;
    }

    /**
     * Sets the row span of a component.
     *
     * @param component the component
     * @param span      the row span
     */
    protected void setRowSpan(Component component, int span) {
        TableLayoutDataEx layout = new TableLayoutDataEx();
        layout.setRowSpan(span);
        component.setLayoutData(layout);
    }

    /**
     * Sets the column span of a component.
     *
     * @param component the component
     * @param span      the row span
     */
    protected void setColumnSpan(Component component, int span) {
        TableLayoutDataEx layout = new TableLayoutDataEx();
        layout.setColSpan(span);
        component.setLayoutData(layout);
    }

    /**
     * Returns the cell column corresponding to a slot.
     * <p/>
     * This implementation returns the slot unchanged.
     *
     * @param slot the slot
     * @return the column
     */
    protected int getCellColumn(int slot) {
        return slot;
    }

    /**
     * Returns the cell row corresponding to a slot.
     * <p/>
     * This implementation returns the slot unchanged.
     *
     * @param slot the slot
     * @return the row
     */
    protected int getCellRow(int slot) {
        return slot;
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
     * Evaluates the view's displayExpression expression against the supplied
     * event. If no displayExpression is present, {@code null} is returned.
     * <p/>
     * If the event has an {@link ScheduleEvent#ARRIVAL_TIME} property,
     * a formatted string named <em>waiting</em> will be added to the set prior
     * to evaluation of the expression. This indicates the waiting time, and
     * is the difference between the arrival time and the current time.
     *
     * @param event the event
     * @return the evaluate result. May be {@code null}
     */
    protected String evaluate(PropertySet event) {
        if (!StringUtils.isEmpty(expression)) {
            return SchedulingHelper.evaluate(expression, event);
        }
        return null;
    }

    /**
     * Helper to create a multiline label with optional notes popup,
     * if the supplied notes are non-null and {@code displayNotes} is
     * {@code true}.
     *
     * @param text  the label text
     * @param notes the notes. May be {@code null}
     * @return a component representing the label with optional popup
     */
    protected Component createLabelWithNotes(String text, String notes) {
        Label label = LabelFactory.create(true);
        Component result;
        if (text != null) {
            label.setText(text);
        }
        if (displayNotes && notes != null) {
            BalloonHelp help = BalloonHelpFactory.create(notes);
            result = RowFactory.create(Styles.CELL_SPACING, label, help);
        } else {
            result = label;
        }
        return result;
    }

    /**
     * Returns a column given its model index.
     *
     * @param column the column index
     * @return the column
     */
    protected Column getColumn(int column) {
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
     * Returns all columns that a schedule appears in.
     *
     * @param scheduleRef the schedule reference
     * @return the columns
     */
    private List<ScheduleColumn> getColumns(IMObjectReference scheduleRef) {
        List<ScheduleColumn> result = new ArrayList<ScheduleColumn>();
        for (ScheduleColumn column : getColumns()) {
            if (column.getSchedule() != null) {
                Entity schedule = column.getSchedule().getSchedule();
                if (schedule.getObjectReference().equals(scheduleRef)) {
                    result.add(column);
                }
            }
        }
        return result;
    }

    /**
     * Returns all rows that a schedule appears in.
     *
     * @param scheduleRef the schedule reference
     * @return the rows
     */
    private List<ScheduleRow> getRows(IMObjectReference scheduleRef) {
        List<ScheduleRow> result = new ArrayList<ScheduleRow>();
        int index = 0;
        for (Schedule schedule : grid.getSchedules()) {
            if (schedule.getSchedule().getId() == scheduleRef.getId()) {
                result.add(new ScheduleRow(schedule, index));
            }
            ++index;
        }
        return result;
    }

    protected static class Column extends TableColumnEx {

        /**
         * Constructs a {@link Column}.
         *
         * @param modelIndex the model index
         * @param heading    the column heading
         */
        public Column(int modelIndex, String heading) {
            super(modelIndex);
            setHeaderValue(heading);
            setHeaderRenderer(null);
            setCellRenderer(null);
        }
    }

    /**
     * Date column.
     */
    protected static class DateColumn extends Column {

        public DateColumn(int modelIndex, Date startTime) {
            super(modelIndex, new SimpleDateFormat("d E").format(startTime));
        }

    }

    /**
     * Schedule column.
     */
    protected static class ScheduleColumn extends Column {

        /**
         * The schedule, or {@code null} if the column isn't associated with
         * a schedule.
         */
        private Schedule schedule;

        /**
         * Creates a new {@code Column}.
         *
         * @param modelIndex the model index
         * @param schedule   the schedule
         */
        public ScheduleColumn(int modelIndex, Schedule schedule) {
            this(modelIndex, schedule, schedule.getName());
        }

        /**
         * Creates a new {@code Column}.
         *
         * @param modelIndex the model index
         * @param schedule   the schedule
         * @param heading    the column heading
         */
        public ScheduleColumn(int modelIndex, Schedule schedule, String heading) {
            super(modelIndex, heading);
            this.schedule = schedule;
        }

        /**
         * Creates a new {@code Column}.
         *
         * @param modelIndex the model index
         * @param heading    the column heading
         */
        public ScheduleColumn(int modelIndex, String heading) {
            this(modelIndex, null, heading);
        }

        /**
         * Returns the schedule.
         *
         * @return the schedule. May be {@code null}
         */
        public Schedule getSchedule() {
            return schedule;
        }
    }

    /**
     * Associates a schedule with a row.
     */
    protected static class ScheduleRow {

        /**
         * The schedule.
         */
        private final Schedule schedule;

        /**
         * The row.
         */
        private final int row;

        /**
         * Constructs a {@link ScheduleRow}.
         *
         * @param schedule the schedule
         * @param row      the row
         */
        public ScheduleRow(Schedule schedule, int row) {
            this.schedule = schedule;
            this.row = row;
        }

        /**
         * Returns the schedule.
         *
         * @return the schedule. May be {@code null}
         */
        public Schedule getSchedule() {
            return schedule;
        }

        /**
         * Returns the row.
         *
         * @return the row
         */
        public int getRow() {
            return row;
        }
    }
}
