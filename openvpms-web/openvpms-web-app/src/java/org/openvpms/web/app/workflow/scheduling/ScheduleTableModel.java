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

package org.openvpms.web.app.workflow.scheduling;

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
import org.openvpms.web.component.util.BalloonHelpFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;

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
     * The column model.
     */
    private TableColumnModel model = new DefaultTableColumnModel();

    /**
     * The clinician to display svents for.
     * If {@code null} indicates to display events for all clinicians.
     */
    private IMObjectReference clinician;

    /**
     * The selected column.
     */
    private int selectedColumn = -1;

    /**
     * The selected row.
     */
    private int selectedRow = -1;

    /**
     * The marked cell column.
     */
    private int markedColumn = -1;

    /**
     * The marked cell row.
     */
    private int markedRow = -1;

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
     * @param grid    the schedule event grid
     * @param context the context
     */
    public ScheduleTableModel(ScheduleEventGrid grid, Context context) {
        this.grid = grid;
        this.context = context;
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
     * Returns the column index of a schedule.
     *
     * @param scheduleRef the schedule reference
     * @return the index of the schedule, or {@code -1} if the schedule isn't found
     */
    public int getColumn(IMObjectReference scheduleRef) {
        for (Column column : getColumns()) {
            if (column.getSchedule() != null) {
                Entity schedule = column.getSchedule().getSchedule();
                if (schedule.getObjectReference().equals(scheduleRef)) {
                    return column.getModelIndex();
                }
            }
        }
        return -1;
    }

    /**
     * Returns the row of the specified event.
     *
     * @param schedule the schedule
     * @param eventRef the event reference
     * @return the row, or {@code -1} if the event is not found
     */
    public abstract int getRow(Schedule schedule, IMObjectReference eventRef);

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
     * Returns the selected column.
     *
     * @return the selected column, or {@code -1} if none is selected
     */
    public int getSelectedColumn() {
        return selectedColumn;
    }

    /**
     * Returns the selected row.
     *
     * @return the selected row, or {@code -1} if none is selected
     */
    public int getSelectedRow() {
        return selectedRow;
    }

    /**
     * Determines if a cell is selected.
     *
     * @param column the column
     * @param row    the row
     * @return {@code true} if the cell is selected
     */
    public boolean isSelectedCell(int column, int row) {
        return selectedColumn == column && selectedRow == row;
    }

    /**
     * Sets the marked cell. This flags a cell as being marked for cutting/copying and pasting purposes.
     *
     * @param column the marked column, or {@code -1} to unmark the cell
     * @param row    the marked row, or {@code -1} to unmark the cell
     * @param isCut  if {@code true} indicates the cell is being cut; if {@code false} indicates its being copied.
     *               Ignored if the cell is being unmarked.
     */
    public void setMarkedCell(int column, int row, boolean isCut) {
        int oldColumn = markedColumn;
        int oldRow = markedRow;
        this.isCut = isCut;
        markedColumn = column;
        markedRow = row;
        if (oldColumn != -1 && oldRow != -1) {
            fireTableCellUpdated(oldColumn, oldRow);
        }
        if (markedColumn != -1 && markedRow != -1) {
            fireTableCellUpdated(markedColumn, markedRow);
        }
    }

    /**
     * Returns the cut column.
     *
     * @return the cut column, or {@code -1} if no cell is selected to be cut
     */
    public int getMarkedColumn() {
        return markedColumn;
    }

    /**
     * Returns the marked row.
     *
     * @return the marked row, or {@code -1} if no cell is selected to be cut/copied
     */
    public int getMarkedRow() {
        return markedRow;
    }

    /**
     * Determines if a cell is marked for cut/copy.
     *
     * @param column the column
     * @param row    the row
     * @return {@code true} if the cell is cut
     */
    public boolean isMarkedCell(int column, int row) {
        return markedColumn == column && markedColumn != -1 && markedRow == row && markedRow != -1;
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
        return grid.getSlots();
    }

    /**
     * Returns the event at the specified column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the event, or {@code null} if none is found
     */
    public PropertySet getEvent(int column, int row) {
        return getEvent(getColumn(column), row);
    }

    /**
     * Returns the schedule at the given column.
     *
     * @param column the column
     * @return the schedule, or {@code null} if there is no schedule associated
     *         with the column
     */
    public Schedule getSchedule(int column) {
        Column col = getColumn(column);
        return col.getSchedule();
    }

    /**
     * Returns the schedule entity at the given column.
     *
     * @param column the column
     * @return the schedule entity, or {@code null} if there is no schedule
     *         associated with the column
     */
    public Entity getScheduleEntity(int column) {
        Schedule schedule = getSchedule(column);
        return (schedule != null) ? schedule.getSchedule() : null;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     * Column and row values are 0-based.
     *
     * @param column the column index (0-based)
     * @param row    the row index (0-based)
     * @return the cell value
     */
    public Object getValueAt(int column, int row) {
        return getValueAt(getColumn(column), row);
    }

    /**
     * Returns the availability of the specified cell.
     *
     * @param column the column
     * @param row    the row
     * @return the availability of the cell
     */
    public ScheduleEventGrid.Availability getAvailability(int column, int row) {
        Column col = getColumn(column);
        Schedule schedule = col.getSchedule();
        if (schedule == null) {
            return ScheduleEventGrid.Availability.UNAVAILABLE;
        }
        return grid.getAvailability(schedule, row);
    }

    /**
     * Returns the event start time at the specified row.
     *
     * @param schedule the schedule
     * @param row      the row
     * @return the start time. May be {@code null}
     */
    public Date getStartTime(Schedule schedule, int row) {
        return grid.getStartTime(schedule, row);
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param column the column
     * @param row    the row
     * @return the cell value
     */
    protected abstract Object getValueAt(Column column, int row);

    /**
     * Returns the event at the specified column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the event, or {@code null} if none is found
     */
    protected PropertySet getEvent(Column column, int row) {
        Schedule schedule = column.getSchedule();
        return (schedule != null) ? grid.getEvent(schedule, row) : null;
    }

    /**
     * Creates a column model to display a list of schedules.
     *
     * @param grid the appointment grid
     * @return a new column model
     */
    protected abstract TableColumnModel createColumnModel(
        ScheduleEventGrid grid);

    /**
     * Returns a viewer for an object reference.
     *
     * @param set     the set
     * @param refKey  the object reference key
     * @param nameKey the entity name key
     * @param link    if {@code true} enable an hyperlink to the object
     * @return a new component to view the object reference
     */
    protected Component getViewer(PropertySet set, String refKey,
                                  String nameKey, boolean link) {
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
    protected List<Column> getColumns() {
        List<Column> result = new ArrayList<Column>();
        Iterator iterator = model.getColumns();
        while (iterator.hasNext()) {
            result.add((Column) iterator.next());
        }
        return result;
    }

    /**
     * Sets the row span of a component.
     *
     * @param component the component
     * @param rowSpan   the row span
     */
    protected void setSpan(Component component, int rowSpan) {
        TableLayoutDataEx layout = new TableLayoutDataEx();
        layout.setRowSpan(rowSpan);
        component.setLayoutData(layout);
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
            result = RowFactory.create("CellSpacing", label, help);
            help.setPopUpTopOffset(-45);       // workaround for OVPMS-1233
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
     * Schedule column.
     */
    protected static class Column extends TableColumnEx {

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
        public Column(int modelIndex, Schedule schedule) {
            this(modelIndex, schedule, schedule.getName());
        }

        /**
         * Creates a new {@code Column}.
         *
         * @param modelIndex the model index
         * @param schedule   the schedule
         * @param heading    the column heading
         */
        public Column(int modelIndex, Schedule schedule, String heading) {
            super(modelIndex);
            this.schedule = schedule;
            setHeaderValue(heading);
            setHeaderRenderer(null);
            setCellRenderer(null);
        }

        /**
         * Creates a new {@code Column}.
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
         * @return the schedule. May be {@code null}
         */
        public Schedule getSchedule() {
            return schedule;
        }

    }

}
