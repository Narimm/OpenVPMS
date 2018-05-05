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

package org.openvpms.web.workspace.workflow.scheduling;

import echopointng.table.TableColumnEx;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.AbstractTableModel;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.function.factory.ArchetypeFunctionsFactory;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.CachingReadOnlyArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.echo.table.RenderTableModel;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Schedule event table model.
 *
 * @author Tim Anderson
 */
public abstract class ScheduleTableModel extends AbstractTableModel implements RenderTableModel {

    public enum Highlight {

        EVENT_TYPE, CLINICIAN, STATUS
    }

    /**
     * Used to restore selection state.
     */
    public static class State {

        /**
         * The table schedules.
         */
        private final Set<IMObjectReference> schedules;

        /**
         * The selected cell. May be {@code null}
         */
        private final Cell selected;

        /**
         * The selected event. May be {@code null}
         */
        private final IMObjectReference selectedEvent;

        /**
         * The marked event. May be {@code null}
         */
        private final IMObjectReference markedEvent;

        /**
         * Determines if the marked event is being cut.
         */
        private final boolean isCut;

        /**
         * The marked schedule. May be {@code null}
         */
        private IMObjectReference markedSchedule;

        public State(ScheduleTableModel model) {
            schedules = State.getSchedules(model.getSchedules());
            selected = model.getSelected();
            selectedEvent = getEvent(model, selected);
            Cell markedCell = model.getMarked();
            if (markedCell != null) {
                PropertySet event = model.getEvent(markedCell);
                markedEvent = getEvent(event);
                markedSchedule = getSchedule(event);
            } else {
                markedEvent = null;
                markedSchedule = null;
            }
            isCut = model.isCut();
        }

        public Set<IMObjectReference> getSchedules() {
            return schedules;
        }

        public Cell getSelected() {
            return selected;
        }

        public IMObjectReference getSelectedEvent() {
            return selectedEvent;
        }

        public IMObjectReference getMarkedEvent() {
            return markedEvent;
        }

        public IMObjectReference getMarkedSchedule() {
            return markedSchedule;
        }

        public boolean isCut() {
            return isCut;
        }

        public static Set<IMObjectReference> getSchedules(List<Schedule> schedules) {
            Set<IMObjectReference> result = new HashSet<>();
            for (Schedule schedule : schedules) {
                result.add(schedule.getSchedule().getObjectReference());
            }
            return result;
        }

        /**
         * Returns an event reference for an cell.
         *
         * @param cell the cell. May be {@code null}
         * @return the event reference. May be {@code null}
         */
        private IMObjectReference getEvent(ScheduleTableModel model, Cell cell) {
            IMObjectReference reference = null;
            if (cell != null) {
                PropertySet event = model.getEvent(cell);
                reference = getEvent(event);
            }
            return reference;
        }

        private IMObjectReference getEvent(PropertySet event) {
            return (event != null) ? event.getReference(ScheduleEvent.ACT_REFERENCE) : null;
        }

        /**
         * Returns a schedule reference for an event.
         *
         * @param event the event. May be {@code null}
         * @return the schedule reference. May be {@code null}
         */
        private IMObjectReference getSchedule(PropertySet event) {
            return (event != null) ? event.getReference(ScheduleEvent.SCHEDULE_REFERENCE) : null;
        }
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
     * The display expression, from the schedule view. May be {@code null}
     */
    private final String expression;

    /**
     * Determines if the notes popup should be displayed.
     */
    private final boolean displayNotes;

    /**
     * Determines if completed/cancelled appointments should be display with a strike-through.
     */
    private final boolean useStrikethrough;

    /**
     * The colour cache.
     */
    private final ScheduleColours colours;

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
     * A caching archetype service, used to improve performance when rendering with expressions.
     */
    private CachingReadOnlyArchetypeService service;

    /**
     * The functions used to evaluate expressions.
     * <p>
     * These use caching to improve performance.
     */
    private FunctionLibrary functions;

    /**
     * 'Use strike-through' node name.
     */
    private static final String USE_STRIKETHROUGH = "useStrikethrough";

    /**
     * Constructs a {@link ScheduleTableModel}.
     *
     * @param grid            the schedule event grid
     * @param context         the context
     * @param scheduleColumns if {@code true}, display the schedules on the columns, otherwise display them on the rows
     * @param colours         the colour cache
     */
    public ScheduleTableModel(ScheduleEventGrid grid, Context context, boolean scheduleColumns,
                              ScheduleColours colours) {
        this.grid = grid;
        this.context = context;
        this.scheduleColumns = scheduleColumns;
        IMObjectBean bean = new IMObjectBean(grid.getScheduleView());
        expression = bean.getString("displayExpression");
        displayNotes = bean.getBoolean("displayNotes");
        useStrikethrough = bean.hasNode(USE_STRIKETHROUGH) && bean.getBoolean(USE_STRIKETHROUGH);
        this.colours = colours;
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
                    break;
                }
            }
        } else {
            for (ScheduleRow row : getRows(schedule)) {
                Schedule s = row.getSchedule();
                int slot = getSlot(s, event);
                if (slot != -1) {
                    int column = getCellColumn(slot);
                    result = new Cell(column, row.getRow());
                    break;
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
     * for all clinicians
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
     * <p>
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
     * Determines if completed/cancelled appointments should be display with strikethrough font.
     *
     * @return {@code true} if completed/cancelled appointments should be display with strikethrough font
     */
    public boolean useStrikeThrough() {
        return useStrikethrough;
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
        if (slot < 0) {
            return ScheduleEventGrid.Availability.UNAVAILABLE;
        }
        return getAvailability(schedule, slot);
    }

    /**
     * Returns the availability of the specified schedule slot.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the availability of the slot
     */
    public ScheduleEventGrid.Availability getAvailability(Schedule schedule, int slot) {
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
     * Returns the display expression, from the schedule view.
     * <p>
     * This may be used to customise the event display.
     *
     * @return the expression. May be {@code null}
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Determines if the notes popup should be displayed.
     *
     * @return {@code true} if the notes popup should be displayed
     */
    public boolean getDisplayNotes() {
        return displayNotes;
    }

    /**
     * Returns a status name given its code.
     *
     * @param event the event
     * @return the status name
     */
    public String getStatus(PropertySet event) {
        return event.getString(ScheduleEvent.ACT_STATUS_NAME);
    }

    /**
     * Returns the slot of a cell.
     *
     * @param column the column
     * @param row    the row
     * @return the slot
     */
    public int getSlot(int column, int row) {
        return (scheduleColumns) ? row : column;
    }

    /**
     * Returns the state of the model.
     *
     * @return the state of the model
     */
    public State getState() {
        return new State(this);
    }

    /**
     * Sets the state of the model.
     *
     * @param state the state
     */
    public void setState(State state) {
        Set<IMObjectReference> schedules = State.getSchedules(getSchedules());
        Set<IMObjectReference> newSchedules = state.getSchedules();
        Cell newSelected = state.getSelected();
        IMObjectReference newEvent = state.getSelectedEvent();
        if (newSelected != null && schedules.equals(newSchedules)) {
            if (newSelected.getColumn() < getColumnCount() && newSelected.getRow() < getRowCount()) {
                if (newEvent != null) {
                    PropertySet event = getEvent(newSelected);
                    if (event == null || !ObjectUtils.equals(newEvent,
                                                             event.getReference(ScheduleEvent.ACT_REFERENCE))) {
                        newSelected = null;
                    }
                }
            } else {
                newSelected = null;
            }
        } else {
            newSelected = null;
        }
        setSelected(newSelected);

        IMObjectReference newMarkedEvent = state.getMarkedEvent();
        IMObjectReference newMarkedSchedule = state.getMarkedSchedule();
        Cell newMarked = null;
        if (newMarkedEvent != null && newMarkedSchedule != null) {
            newMarked = getCell(newMarkedSchedule, newMarkedEvent);
        }
        setMarked(newMarked, state.isCut());
    }

    /**
     * Returns the colour cache.
     *
     * @return the colour cache
     */
    public ScheduleColours getColours() {
        return colours;
    }

    /**
     * Evaluates the view's displayExpression expression against the supplied
     * event. If no displayExpression is present, {@code null} is returned.
     * <p>
     * If the event has an {@link ScheduleEvent#ARRIVAL_TIME} property,
     * a formatted string named <em>waiting</em> will be added to the set prior
     * to evaluation of the expression. This indicates the waiting time, and
     * is the difference between the arrival time and the current time.
     *
     * @param event the event
     * @return the evaluate result. May be {@code null}
     */
    public String evaluate(PropertySet event) {
        String result = null;
        String expression = getExpression();
        if (!StringUtils.isEmpty(expression)) {
            if (functions == null) {
                service = new CachingReadOnlyArchetypeService(1000, ServiceHelper.getArchetypeService());
                functions = ServiceHelper.getBean(ArchetypeFunctionsFactory.class).create(service, true);
            }
            result = SchedulingHelper.evaluate(expression, event, functions);
        }
        return result;
    }

    /**
     * Invoked prior to the table being rendered.
     */
    @Override
    public void preRender() {

    }

    /**
     * Invoked after the table has been rendered.
     */
    @Override
    public void postRender() {
        if (service != null) {
            // Clear the cache, to both limit memory use and ensure stale data is not used in subsequent renders.
            service.clear();
        }
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
        List<ScheduleColumn> result = new ArrayList<>();
        Iterator iterator = model.getColumns();
        while (iterator.hasNext()) {
            result.add((ScheduleColumn) iterator.next());
        }
        return result;
    }

    /**
     * Returns the cell column corresponding to a slot.
     * <p>
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
     * <p>
     * This implementation returns the slot unchanged.
     *
     * @param slot the slot
     * @return the row
     */
    protected int getCellRow(int slot) {
        return slot;
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
     * Returns all rows that a schedule appears in.
     *
     * @param scheduleRef the schedule reference
     * @return the rows
     */
    protected List<ScheduleRow> getRows(IMObjectReference scheduleRef) {
        List<ScheduleRow> result = new ArrayList<>();
        int index = 0;
        for (Schedule schedule : grid.getSchedules()) {
            if (schedule.getSchedule().getId() == scheduleRef.getId()) {
                result.add(new ScheduleRow(schedule, index));
            }
            ++index;
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
        List<ScheduleColumn> result = new ArrayList<>();
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

    protected static class Column extends TableColumnEx {

        /**
         * Constructs a {@link Column}.
         *
         * @param modelIndex the model index
         * @param heading    the column heading. May be {@code null}
         */
        public Column(int modelIndex, String heading) {
            super(modelIndex);
            setHeaderValue(heading);
            setHeaderRenderer(null);
            setCellRenderer(null);
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
