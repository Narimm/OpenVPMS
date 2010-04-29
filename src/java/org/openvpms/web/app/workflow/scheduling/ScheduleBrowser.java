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
import static org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid.Availability;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.query.AbstractBrowser;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.query.QueryListener;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.table.DefaultTableHeaderRenderer;
import org.openvpms.web.component.table.EvenOddTableCellRenderer;
import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SplitPaneFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Schedule browser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class ScheduleBrowser extends AbstractBrowser<PropertySet> {

    /**
     * The query.
     */
    private ScheduleQuery query;

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
     * The selected time. May be <tt>null</tt>.
     */
    private Date selectedTime;

    /**
     * The selected schedule. May be <tt>null</tt>
     */
    private Entity selectedSchedule;

    /**
     * The event selected to be cut. May be <tt>null</tt>
     */
    private PropertySet cut;

    /**
     * The last click time, to detect double click.
     */
    private Date lastClick;


    /**
     * Creates a new <tt>ScheduleBrowser</tt>.
     *
     * @param query the schedule query
     */
    public ScheduleBrowser(ScheduleQuery query) {
        this.query = query;
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
     * @return the selected object, or <tt>null</tt> if none has been
     *         selected.
     */
    public PropertySet getSelected() {
        return selected;
    }

    /**
     * Select an object.
     *
     * @param object the object to select. May be <tt>null</tt>
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
        }
    }

    /**
     * Returns the event selected to be cut.
     *
     * @return the cut event, or <tt>null</tt> if none has been selected to be cut.
     */
    public PropertySet getCut() {
        return cut;
    }

    /**
     * Sets the event selected to be cut.
     *
     * @param event the event to cut, or <tt>null</tt> to deselect the event
     */
    public void setCut(PropertySet event) {
        cut = event;
        updateCutSelection();

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
     * @param view the schedule view. May be <tt>null</tt>
     */
    public void setScheduleView(Entity view) {
        query.setScheduleView(view);
    }

    /**
     * Returns the schedule view.
     *
     * @return the schedule view. May be <tt>null</tt>
     */
    public Entity getScheduleView() {
        return query.getScheduleView();
    }

    /**
     * Returns the selected schedule.
     *
     * @return the selected schedule. May be <tt>null</tt>
     */
    public Entity getSelectedSchedule() {
        return selectedSchedule;
    }

    /**
     * Returns the selected time.
     *
     * @return the selected time. May be <tt>null</tt>
     */
    public Date getSelectedTime() {
        return selectedTime;
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matcing the query
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
        addQueryListener(listener);
    }

    /**
     * Helper to return the act associated with an event.
     *
     * @param event the event. May be <tt>null</tt>
     * @return the associated act, or <tt>null</tt> if <tt>event</tt> is null or has been deleted
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
     * @param act the act. May be <tt>null</tt>
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
     * Returns the table model.
     *
     * @return the table model. May be <tt>null</tt>
     */
    protected ScheduleTableModel getModel() {
        return model;
    }

    /**
     * Returns the table.
     *
     * @return the table. May be <tt>null</tt>
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
        notifyQueryListeners();
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

    private void doQueryWithView(boolean reselect) {
        Set<Entity> lastSchedules = (results != null) ? results.keySet() : null;
        results = query.query();

        ScheduleEventGrid grid = createEventGrid(query.getDate(), results);
        int lastRow = -1;
        int lastColumn = -1;
        IMObjectReference lastEventId = null;
        if (model != null) {
            lastRow = model.getSelectedRow();
            lastColumn = model.getSelectedColumn();
            lastEventId = getEventReference(lastColumn, lastRow);
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

            // if the schedules and date haven't changed and there was no
            // previously selected object or the object hasn't changed,
            // reselect the selected cell
            if (lastRow != -1 && lastColumn != -1 && sameSchedules && lastColumn < model.getColumnCount()) {
                IMObjectReference eventId = getEventReference(lastColumn, lastRow);
                if (ObjectUtils.equals(selectedDate, query.getDate())
                    && (lastEventId == null || ObjectUtils.equals(lastEventId, eventId))) {
                    model.setSelectedCell(lastColumn, lastRow);
                }
            }

            updateCutSelection();
        }
    }

    /**
     * Updates the cut selection.
     */
    private void updateCutSelection() {
        boolean found = false;
        if (cut != null) {
            IMObjectReference scheduleRef = cut.getReference(ScheduleEvent.SCHEDULE_REFERENCE);
            IMObjectReference eventRef = cut.getReference(ScheduleEvent.ACT_REFERENCE);
            ScheduleTableModel model = getModel();
            int column = model.getColumn(scheduleRef);
            if (column != -1) {
                Schedule schedule = model.getSchedule(column);
                int row = model.getRow(schedule, eventRef);
                if (row != -1) {
                    model.setCutCell(column, row);
                    found = true;
                }
            }
        }
        if (!found) {
            model.setCutCell(-1, -1);
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
        if (isDoubleClick()) {
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
                for (QueryBrowserListener<PropertySet> listener : getQueryListeners()) {
                    if (listener instanceof ScheduleBrowserListener) {
                        ((ScheduleBrowserListener) listener).create();
                    }
                }
            } else {
                for (QueryBrowserListener<PropertySet> listener : getQueryListeners()) {
                    if (listener instanceof ScheduleBrowserListener) {
                        ((ScheduleBrowserListener) listener).edit(selected);
                    }
                }
            }
        } else {
            notifySelected(selected);
        }

        // deselect the row
        table.getSelectionModel().clearSelection();
    }

    /**
     * Returns the reference of the event at the speciifed column and row.
     *
     * @param column the column
     * @param row    the row
     * @return the corresponding event reference, or <tt>null</tt> if none exists
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

    /**
     * Determines if the mouse has been clicked twice on the table.
     * <p/>
     * This implementation returns <tt>true</tt> if the table has been clicked twice within 2 seconds
     *
     * @return <tt>true</tt> if the mouse has been clicked twice
     */
    private boolean isDoubleClick() {
        boolean result;
        Date now = new Date();
        result = (lastClick != null && (lastClick.getTime() + 2000) >= now.getTime());
        lastClick = now;
        return result;
    }

}
