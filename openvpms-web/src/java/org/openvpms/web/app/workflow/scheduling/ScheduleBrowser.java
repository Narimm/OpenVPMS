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
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.system.common.util.PropertySet;
import static org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid.Availability;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.query.AbstractBrowser;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.query.QueryListener;
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
     * Creates a new <tt>ScheduleBrowser</tt>.
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
     * @param object the object to select
     */
    public void setSelected(PropertySet object) {
        selected = object;
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
     */
    protected abstract ScheduleEventGrid
            createEventGrid(Date date, Map<Entity, List<PropertySet>> events);

    /**
     * Creates a new table model.
     *
     * @param grid the schedule event grid
     * @return the table model
     */
    protected abstract ScheduleTableModel createTableModel(
            ScheduleEventGrid grid);

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
     */
    protected Component doLayout() {
        Row row = RowFactory.create("CellSpacing");
        layoutQueryRow(row);

        Component column = ColumnFactory.create("WideCellSpacing", row);
        SplitPane component = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL,
                "ScheduleBrowser", column);
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
            public void actionPerformed(ActionEvent event) {
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
            if (lastRow != -1 && lastColumn != -1) {
                PropertySet event = model.getEvent(lastColumn, lastRow);
                if (event != null) {
                    lastEventId = event.getReference(
                            ScheduleEvent.ACT_REFERENCE);
                }
            }
        }
        model = createTableModel(grid);
        if (table == null) {
            table = createTable(model);
            table.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
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
            // if the schedules and date haven't changed and there was no
            // previously selected object or the object hasn't changed,
            // reselect the selected cell
            if (lastRow != -1 && lastColumn != -1
                    && ObjectUtils.equals(lastSchedules, results.keySet())) {
                Date selectedDate = (selectedTime != null)
                        ? DateRules.getDate(selectedTime) : null;
                PropertySet event = model.getEvent(lastColumn, lastRow);
                IMObjectReference eventId = (event != null)
                        ? event.getReference(ScheduleEvent.ACT_REFERENCE)
                        : null;
                if (ObjectUtils.equals(selectedDate, query.getDate())
                        && (lastEventId == null
                        || ObjectUtils.equals(lastEventId, eventId))) {
                    model.setSelectedCell(lastColumn, lastRow);
                }
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
        component.add(ColumnFactory.create("Inset.Small", table));
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
                for (QueryBrowserListener<PropertySet> listener
                        : getQueryListeners()) {
                    if (listener instanceof ScheduleBrowserListener) {
                        ((ScheduleBrowserListener) listener).create();
                    }
                }
            } else {
                for (QueryBrowserListener<PropertySet> listener
                        : getQueryListeners()) {
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
     * Deselects the selected cell.
     */
    protected void clearSelection() {
        selectedTime = null;
        model.setSelectedCell(-1, -1);
    }

}
