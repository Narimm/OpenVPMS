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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.scheduling;

import echopointng.TableEx;
import echopointng.table.TableActionEventEx;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.AbstractBrowser;
import org.openvpms.web.component.im.query.BrowserListener;
import org.openvpms.web.component.im.query.QueryListener;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.table.DefaultTableHeaderRenderer;
import org.openvpms.web.echo.table.EvenOddTableCellRenderer;
import org.openvpms.web.echo.util.DoubleClickMonitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid.Availability;

/**
 * Schedule browser.
 *
 * @author Tim Anderson
 */
public abstract class ScheduleBrowser extends AbstractBrowser<PropertySet> {

    /**
     * The query.
     */
    private final ScheduleQuery query;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The schedule events, keyed on schedule.
     */
    private Map<Entity, List<PropertySet>> results;

    /**
     * The browser component.
     */
    private Component component;

    /**
     * The schedule event table.
     */
    private TableEx table;

    /**
     * The schedule event table model.
     */
    private ScheduleTableModel model;

    /**
     * The selected event.
     */
    private PropertySet selected;

    /**
     * The selected time. May be {@code null}.
     */
    private Date selectedTime;

    /**
     * The selected schedule. May be {@code null}
     */
    private Entity selectedSchedule;

    /**
     * The event selected to be cut. May be {@code null}
     */
    private PropertySet marked;

    /**
     * Used to determine if there has been a double click.
     */
    private final DoubleClickMonitor click = new DoubleClickMonitor();


    /**
     * Creates a new <tt>ScheduleBrowser</tt>.
     *
     * @param query   the schedule query
     * @param context the context
     */
    public ScheduleBrowser(ScheduleQuery query, Context context) {
        this.query = query;
        this.context = context;
        query.setListener(new QueryListener() {
            public void query() {
                onQuery();
            }
        });
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        doQuery(true);
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or {@code null} if none has been selected.
     */
    public PropertySet getSelected() {
        return selected;
    }

    /**
     * Select an object.
     *
     * @param object the object to select. May be {@code null}
     */
    public void setSelected(PropertySet object) {
        boolean found = false;
        selected = object;
        if (selected != null) {
            int column = model.getColumn(object.getReference(ScheduleEvent.SCHEDULE_REFERENCE));
            if (column != -1) {
                Schedule schedule = model.getSchedule(column);
                int row = model.getRow(schedule, object.getReference(ScheduleEvent.ACT_REFERENCE));
                if (row != -1) {
                    model.setSelectedCell(column, row);
                    selectedTime = object.getDate(ScheduleEvent.ACT_START_TIME);
                    selectedSchedule = schedule.getSchedule();
                    found = true;
                }
            }
        }
        if (!found) {
            model.setSelectedCell(-1, -1);
            selectedTime = null;
            selectedSchedule = null;
            getTable().getSelectionModel().clearSelection();
        }
    }

    /**
     * Returns the event marked to be cut or copied.
     *
     * @return the event, or {@code null} if none has been marked
     */
    public PropertySet getMarked() {
        return marked;
    }

    /**
     * Marks an event to be cut/copied.
     *
     * @param event the event to mark, or {@code null} to deselect the event
     * @param isCut if {@code true} indicates the cell is being cut; if {@code false} indicates its being copied.
     *              Ignored if the cell is being unmarked.
     */
    public void setMarked(PropertySet event, boolean isCut) {
        marked = event;
        updateMarked(isCut);
    }

    /**
     * Clears the cell marked to be cut/copied.
     */
    public void clearMarked() {
        setMarked(null, isCut());
    }

    /**
     * Determines if the marked cell is being cut or copied.
     *
     * @return {@code true} if the cell is being cut; {@code false} if it is being copied
     */
    public boolean isCut() {
        return model != null && model.isCut();
    }

    /**
     * Sets the query date.
     *
     * @param date the date
     */
    public void setDate(Date date) {
        query.setDate(date);
    }

    /**
     * Returns the query date.
     *
     * @return the query date
     */
    public Date getDate() {
        return query.getDate();
    }

    /**
     * Sets the schedule view.
     *
     * @param view the schedule view. May be {@code null}
     */
    public void setScheduleView(Entity view) {
        query.setScheduleView(view);
    }

    /**
     * Returns the schedule view.
     *
     * @return the schedule view. May be {@code null}
     */
    public Entity getScheduleView() {
        return query.getScheduleView();
    }

    /**
     * Returns the selected schedule.
     *
     * @return the selected schedule. May be {@code null}
     */
    public Entity getSelectedSchedule() {
        return selectedSchedule;
    }

    /**
     * Returns the selected time.
     *
     * @return the selected time. May be {@code null}
     */
    public Date getSelectedTime() {
        return selectedTime;
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matching the query
     */
    public List<PropertySet> getObjects() {
        if (results == null) {
            query();
        }
        List<PropertySet> result = new ArrayList<PropertySet>();
        for (List<PropertySet> list : results.values()) {
            for (PropertySet set : list) {
                result.add(set);
            }
        }
        return result;
    }

    /**
     * Returns the browser component.
     *
     * @return the browser component
     */
    public Component getComponent() {
        if (component == null) {
            component = doLayout();
        }
        return component;
    }

    /**
     * Adds an schedule browser listener.
     *
     * @param listener the listener to add
     */
    public void addScheduleBrowserListener(ScheduleBrowserListener listener) {
        addBrowserListener(listener);
    }

    /**
     * Helper to return the act associated with an event.
     *
     * @param event the event. May be {@code null}
     * @return the associated act, or {@code null} if <tt>event</tt> is null or has been deleted
     */
    public Act getAct(PropertySet event) {
        if (event != null) {
            IMObjectReference actRef = event.getReference(ScheduleEvent.ACT_REFERENCE);
            return (Act) IMObjectHelper.getObject(actRef, context);
        }
        return null;
    }

    /**
     * Helper to return the event associated with an act.
     *
     * @param act the act. May be {@code null}
     * @return the associated event, or <tt>nukl</tt> if <tt>act</tt> is null or has been deleted
     */
    public PropertySet getEvent(Act act) {
        if (act != null) {
            ActBean bean = new ActBean(act);
            Entity schedule = bean.getNodeParticipant("schedule");
            if (schedule != null) {
                List<PropertySet> events = results.get(schedule);
                IMObjectReference ref = act.getObjectReference();
                if (events != null) {
                    for (PropertySet set : events) {
                        if (ref.equals(set.getReference(ScheduleEvent.ACT_REFERENCE))) {
                            return set;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Sets focus on the results.
     * <p/>
     * This implementation is a no-op.
     */
    public void setFocusOnResults() {
    }

    /**
     * Returns the table model.
     *
     * @return the table model. May be {@code null}
     */
    protected ScheduleTableModel getModel() {
        return model;
    }

    /**
     * Returns the table.
     *
     * @return the table. May be {@code null}
     */
    protected TableEx getTable() {
        return table;
    }

    /**
     * Returns the query.
     *
     * @return the query
     */
    protected ScheduleQuery getQuery() {
        return query;
    }

    /**
     * Creates a new grid for a set of events.
     *
     * @param date   the query date
     * @param events the events
     * @return a new grid
     */
    protected abstract ScheduleEventGrid createEventGrid(Date date, Map<Entity, List<PropertySet>> events);

    /**
     * Creates a new table model.
     *
     * @param grid the schedule event grid
     * @return the table model
     */
    protected abstract ScheduleTableModel createTableModel(ScheduleEventGrid grid);

    /**
     * Creates a new table.
     *
     * @param model the model
     * @return a new table
     */
    protected TableEx createTable(ScheduleTableModel model) {
        TableEx table = new TableEx(model, model.getColumnModel());
        table.setStyleName("ScheduleTable");
        table.setDefaultHeaderRenderer(DefaultTableHeaderRenderer.DEFAULT);
        table.setDefaultRenderer(EvenOddTableCellRenderer.INSTANCE);
/*
        table.setScrollable(true);
        table.setResizeable(true);
        table.setResizeGrowsTable(true);
        table.setResizeDragBarUsed(true);
*/
        return table;
    }

    /**
     * Lays out the component.
     *
     * @return a new component
     */
    protected Component doLayout() {
        Row row = RowFactory.create("CellSpacing");
        layoutQueryRow(row);

        Component column = ColumnFactory.create("WideCellSpacing", row);
        SplitPane component = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "ScheduleBrowser", column);
        if (getScheduleView() != null && table != null) {
            addTable(table, component);
        }
        return component;
    }

    /**
     * Lays out the query row.
     *
     * @param row the row
     */
    protected void layoutQueryRow(Row row) {
        FocusGroup group = getFocusGroup();
        row.add(query.getComponent());
        group.add(query.getFocusGroup());
        ButtonRow buttons = new ButtonRow(group);
        buttons.addButton("query", new ActionListener() {
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });
        row.add(buttons);
    }

    /**
     * Performs a query and notifies registered listeners.
     */
    protected void onQuery() {
        query();
        notifyBrowserListeners();
    }

    /**
     * Performs a query.
     *
     * @param reselect if <tt>true</tt> try and reselect the selected cell
     */
    protected void doQuery(boolean reselect) {
        getComponent();
        if (query.getScheduleView() != null) {
            doQueryWithView(reselect);
        } else {
            // no schedule view selected
            if (table != null) {
                component.remove(table);
            }
            results = null;
            model = null;
            table = null;
        }
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    protected Context getContext() {
        return context;
    }

    private void doQueryWithView(boolean reselect) {
        Set<Entity> lastSchedules = (results != null) ? results.keySet() : null;
        results = query.query();

        ScheduleEventGrid grid = createEventGrid(query.getDate(), results);
        int lastRow = -1;
        int lastColumn = -1;
        boolean isCut = true;
        IMObjectReference lastEventId = null;
        if (model != null) {
            lastRow = model.getSelectedRow();
            lastColumn = model.getSelectedColumn();
            lastEventId = getEventReference(lastColumn, lastRow);
            isCut = model.isCut();
        }
        model = createTableModel(grid);
        if (table == null) {
            table = createTable(model);
            table.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    onSelected((TableActionEventEx) event);
                }
            });
            addTable(table, component);
        } else {
            table.setModel(model);
            table.setColumnModel(model.getColumnModel());
        }
        User clinician = query.getClinician();
        if (clinician != null) {
            model.setClinician(clinician.getObjectReference());
        } else {
            model.setClinician(null);
        }
        model.setHighlight(query.getHighlight());

        if (reselect) {
            boolean sameSchedules = ObjectUtils.equals(lastSchedules, results.keySet());
            Date selectedDate = (selectedTime != null) ? DateRules.getDate(selectedTime) : null;

            // if the schedules and date haven't changed and there was no previously selected object or the object
            // hasn't changed, reselect the selected cell
            boolean reselected = false;
            if (lastRow != -1 && lastColumn != -1 && sameSchedules && lastColumn < model.getColumnCount()) {
                IMObjectReference eventId = getEventReference(lastColumn, lastRow);
                if (ObjectUtils.equals(selectedDate, query.getDate())
                    && (lastEventId == null || ObjectUtils.equals(lastEventId, eventId))) {
                    model.setSelectedCell(lastColumn, lastRow);
                    reselected = true;
                }
            }
            if (!reselected) {
                setSelected(null);
            }

            updateMarked(isCut);
        }
    }

    /**
     * Updates the event marked to be cut or copied.
     *
     * @param isCut if {@code true} indicates the cell is being cut; if {@code false} indicates its being copied.
     *              Ignored if the cell is being unmarked.
     */
    private void updateMarked(boolean isCut) {
        boolean found = false;
        ScheduleTableModel model = getModel();
        if (model != null) {
            if (marked != null) {
                IMObjectReference scheduleRef = marked.getReference(ScheduleEvent.SCHEDULE_REFERENCE);
                IMObjectReference eventRef = marked.getReference(ScheduleEvent.ACT_REFERENCE);
                int column = model.getColumn(scheduleRef);
                if (column != -1) {
                    Schedule schedule = model.getSchedule(column);
                    int row = model.getRow(schedule, eventRef);
                    if (row != -1) {
                        model.setMarkedCell(column, row, isCut);
                        found = true;
                    }
                }
            }
            if (!found) {
                model.setMarkedCell(-1, -1, isCut);
            }
        }
    }

    /**
     * Adds a table to the browser component.
     * <p/>
     * This implementation adds it with a small inset.
     *
     * @param table     the table to add
     * @param component the component
     */
    protected void addTable(TableEx table, Component component) {
        component.add(ColumnFactory.create("Inset.SmallXLargeY", table));
    }

    /**
     * Invoked when a cell is selected.
     * <p/>
     * Notifies listeners of the selection.
     *
     * @param event the event
     */
    private void onSelected(TableActionEventEx event) {
        int column = event.getColumn();
        int row = event.getRow();
        boolean doubleClick = false;
        if (click.isDoubleClick()) {
            if (model.isSingleScheduleView()) {
                // click the same row to get double click in single schedule view
                if (model.getSelectedRow() == row) {
                    doubleClick = true;
                }
            } else {
                // click the same cell to get double click in multi schedule view
                if (model.isSelectedCell(column, row)) {
                    doubleClick = true;
                }
            }
        }
        model.setSelectedCell(column, row);
        selected = model.getEvent(column, row);
        if (model.getAvailability(column, row) != Availability.UNAVAILABLE) {
            Schedule schedule = model.getSchedule(column);
            if (schedule != null) {
                selectedTime = model.getStartTime(schedule, row);
                selectedSchedule = model.getScheduleEntity(column);
            } else {
                selectedTime = null;
                selectedSchedule = null;
            }
        } else {
            selectedTime = null;
            selectedSchedule = null;
        }
        if (doubleClick) {
            if (selected == null) {
                for (BrowserListener<PropertySet> listener : getBrowserListeners()) {
                    if (listener instanceof ScheduleBrowserListener) {
                        ((ScheduleBrowserListener) listener).create();
                    }
                }
            } else {
                for (BrowserListener<PropertySet> listener : getBrowserListeners()) {
                    if (listener instanceof ScheduleBrowserListener) {
                        ((ScheduleBrowserListener) listener).edit(selected);
                    }
                }
            }
        } else {
            notifySelected(selected);
        }

        // deselect the row if displaying multiple schedules
        if (!model.isSingleScheduleView()) {
            table.getSelectionModel().clearSelection();
        }
    }

    /**
     * Returns the reference of the event at the specified column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the corresponding event reference, or {@code null} if none exists
     */
    private IMObjectReference getEventReference(int column, int row) {
        IMObjectReference result = null;
        if (column != -1 && row != -1) {
            PropertySet event = model.getEvent(column, row);
            if (event != null) {
                result = event.getReference(ScheduleEvent.ACT_REFERENCE);
            }
        }
        return result;
    }

}
