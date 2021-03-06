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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.scheduling;

import echopointng.LabelEx;
import echopointng.TableEx;
import echopointng.table.TableActionEventEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.archetype.rules.workflow.ScheduleEvents;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.AbstractBrowser;
import org.openvpms.web.component.im.query.BrowserListener;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.Cell;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.echo.util.DoubleClickMonitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private final ScheduleServiceQuery query;

    /**
     * The context.
     */
    private final Context context;

    /**
     * Used to determine if there has been a double click.
     */
    private final DoubleClickMonitor click = new DoubleClickMonitor();

    /**
     * The schedule events, keyed on schedule.
     */
    private Map<Entity, ScheduleEvents> results;

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
     * Constructs a {@link ScheduleBrowser}.
     *
     * @param query   the schedule query
     * @param context the context
     */
    public ScheduleBrowser(ScheduleServiceQuery query, Context context) {
        this.query = query;
        this.context = context;
        query.setListener(this::onQuery);
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
     * @param object the object to select. May be {@code null} to deselect the current selection
     * @return {@code true} if the object was selected, {@code false} if it doesn't exist in the current view
     */
    public boolean setSelected(PropertySet object) {
        boolean found = false;
        selected = object;
        if (selected != null) {
            Cell cell = model.getCell(object.getReference(ScheduleEvent.SCHEDULE_REFERENCE),
                                      object.getReference(ScheduleEvent.ACT_REFERENCE));
            if (cell != null) {
                Schedule schedule = model.getSchedule(cell);
                model.setSelected(cell);
                selectedTime = object.getDate(ScheduleEvent.ACT_START_TIME);
                selectedSchedule = schedule.getSchedule();
                found = true;
            }
        }
        if (!found) {
            model.setSelected(null);
            selectedTime = null;
            selectedSchedule = null;
            getTable().getSelectionModel().clearSelection();
        }
        return found;
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
        List<PropertySet> result = new ArrayList<>();
        for (ScheduleEvents list : results.values()) {
            result.addAll(list.getEvents());
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
     * @return the associated act, or {@code null} if {@code event} is null or has been deleted
     */
    public Act getAct(PropertySet event) {
        if (event != null) {
            IMObjectReference actRef = event.getReference(ScheduleEvent.ACT_REFERENCE);
            return (Act) IMObjectHelper.getObject(actRef);
        }
        return null;
    }

    /**
     * Helper to return the event associated with an act.
     *
     * @param act the act. May be {@code null}
     * @return the associated event, or {@code null} if {@code act} is null or has been deleted
     */
    public PropertySet getEvent(Act act) {
        PropertySet result = null;
        if (act != null) {
            IMObjectBean bean = new IMObjectBean(act);
            Entity schedule = bean.getTarget("schedule", Entity.class);
            if (schedule != null) {
                ScheduleEvents events = results.get(schedule);
                if (events != null) {
                    result = events.getEvent(act.getObjectReference());
                }
            }
        }
        return result;
    }

    /**
     * Sets focus on the results.
     * <p>
     * This implementation is a no-op.
     */
    public void setFocusOnResults() {
    }

    /**
     * Refreshes the display, if the events have changed.
     */
    public void refresh() {
        if (results != null) {
            if (query.updated(results)) {
                query();
            }
        }
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
    protected ScheduleServiceQuery getQuery() {
        return query;
    }

    /**
     * Creates a new grid for a set of events.
     *
     * @param date   the query date
     * @param events the events
     * @return a new grid
     */
    protected abstract ScheduleEventGrid createEventGrid(Date date, Map<Entity, ScheduleEvents> events);

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
        TableEx table = new TableEx(model, model.getColumnModel()) {
            @Override
            protected void doRender() {
                ScheduleTableModel model = (ScheduleTableModel) getModel();
                model.preRender();
                try {
                    super.doRender();
                } finally {
                    model.postRender();
                }
            }
        };
        table.setStyleName("ScheduleTable");
        return table;
    }

    /**
     * Lays out the component.
     *
     * @return a new component
     */
    protected Component doLayout() {
        Component column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, layoutQuery());
        SplitPane component = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "ScheduleBrowser", column);
        if (getScheduleView() != null && table != null) {
            addTable(table, component);
        }
        return component;
    }

    /**
     * Lays out the query component.
     */
    protected Component layoutQuery() {
        return layoutQuery(query, new ActionListener() {
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });
    }

    /**
     * Lays out the query component.
     *
     * @param query    the query
     * @param listener the listener to notify when the query button is pressed
     * @return the query component
     */
    protected Component layoutQuery(ScheduleQuery query, ActionListener listener) {
        Row row = RowFactory.create(Styles.CELL_SPACING);
        FocusGroup group = getFocusGroup();
        row.add(query.getComponent());
        group.add(query.getFocusGroup());
        ButtonRow buttons = new ButtonRow(group);
        buttons.addButton("query", listener);
        row.add(buttons);
        return row;
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
     * @param reselect if {@code true} try and reselect the selected cell
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

    /**
     * Adds the title and table to the browser component.
     *
     * @param table     the table to add
     * @param component the component
     */
    protected void addTable(Table table, Component component) {
        LabelEx spacer = new LabelEx(new XhtmlFragment(TableHelper.SPACER));
        // add a spacer so that popup notes in the first line of the table won't be clipped
        component.add(ColumnFactory.create(Styles.INSET_CELL_SPACING, spacer, table));
    }

    /**
     * Selects a cell.
     *
     * @param cell the cell to select
     */
    protected void setSelectedCell(Cell cell) {
        model.setSelected(cell);
        selected = model.getEvent(cell);
        if (model.getAvailability(cell) != Availability.UNAVAILABLE) {
            Schedule schedule = model.getSchedule(cell);
            if (schedule != null) {
                selectedTime = model.getStartTime(schedule, cell);
                selectedSchedule = model.getScheduleEntity(cell);
            } else {
                selectedTime = null;
                selectedSchedule = null;
            }
        } else {
            selectedTime = null;
            selectedSchedule = null;
        }
    }

    /**
     * Invoked when a cell is selected.
     * <p>
     * Notifies listeners of the selection.
     *
     * @param event the event
     */
    protected void onSelected(TableActionEventEx event) {
        int column = event.getColumn();
        int row = event.getRow();
        boolean doubleClick = false;
        if (click.isDoubleClick()) {
            if (model.isSingleScheduleView()) {
                // click the same row to get double click in single schedule view
                Cell cell = model.getSelected();
                if (cell != null && cell.getRow() == row) {
                    doubleClick = true;
                }
            } else {
                // click the same cell to get double click in multi schedule view
                if (model.isSelected(column, row)) {
                    doubleClick = true;
                }
            }
        }
        setSelectedCell(new Cell(column, row));
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
     * Performs a query.
     *
     * @param reselect if {@code true} try and reselect the selected cell
     */
    private void doQueryWithView(boolean reselect) {
        results = query.query();

        ScheduleEventGrid grid = createEventGrid(query.getDate(), results);
        ScheduleTableModel.State state = null;
        if (model != null) {
            state = model.getState();
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

        if (reselect && state != null) {
            // attempt to restore the selection
            model.setState(state);
            if (model.getSelected() == null) {
                setSelected(null);
            }
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
                Cell cell = model.getCell(scheduleRef, eventRef);
                if (cell != null) {
                    model.setMarked(cell, isCut);
                    found = true;
                }
            }
            if (!found) {
                model.setMarked(null, isCut);
            }
        }
    }

}
