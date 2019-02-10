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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.roster;

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
import org.openvpms.archetype.rules.workflow.roster.RosterService;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.LayoutContext;
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
import org.openvpms.web.system.ServiceHelper;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Roster browser.
 *
 * @author Tim Anderson
 */
abstract class RosterBrowser extends AbstractBrowser<PropertySet> {

    /**
     * The roster query.
     */
    private final RosterQuery query;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The roster service.
     */
    private final RosterService service;

    /**
     * Used to determine if there has been a double click.
     */
    private final DoubleClickMonitor click = new DoubleClickMonitor();

    /**
     * The event grid.
     */
    private RosterEventGrid grid;

    /**
     * The roster event table model.
     */
    private RosterTableModel model;

    /**
     * The roster event table.
     */
    private TableEx table;

    /**
     * The browser component.
     */
    private Component component;

    /**
     * The selected roster event.
     */
    private PropertySet selected;

    /**
     * The event marked for cut/copy.
     */
    private PropertySet marked;

    /**
     * Constructs a {@link RosterBrowser}.
     *
     * @param query   the query
     * @param context the layout context
     */
    RosterBrowser(RosterQuery query, LayoutContext context) {
        this.query = query;
        this.context = context.getContext();
        service = ServiceHelper.getBean(RosterService.class);

        query.addQueryListener(this::query);
    }

    /**
     * Refreshes the display, if the events have changed.
     */
    public void refresh() {
        if (updated()) {
            query();
        }
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
     * Returns the selected object.
     *
     * @return the selected object, or {@code null} if none has been selected.
     */
    @Override
    public PropertySet getSelected() {
        return selected;
    }

    /**
     * Select an object.
     *
     * @param object the object to select. May be {@code null} to deselect the current selection
     * @return {@code true} if the object was selected, {@code false} if it doesn't exist in the current view
     */
    @Override
    public boolean setSelected(PropertySet object) {
        boolean found = false;
        selected = object;
        if (selected != null) {
            Cell cell = model.getCell(object);
            if (cell != null) {
                model.setSelected(cell);
                found = true;
            }
        }
        if (!found) {
            model.setSelected(null);
            table.getSelectionModel().clearSelection();
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
        boolean found = false;
        if (model != null) {
            if (marked != null) {
                Cell cell = model.getCell(marked);
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
     * Returns the selected entity.
     *
     * @return the selected entity. May be {@code null}
     */
    public Entity getSelectedEntity() {
        return model != null ? model.getSelectedEntity() : null;
    }

    /**
     * Returns the selected date.
     *
     * @return the selected date. May be {@code null}
     */
    public Date getSelectedDate() {
        return model != null ? model.getSelectedDate() : null;
    }

    /**
     * Sets focus on the results.
     */
    @Override
    public void setFocusOnResults() {

    }

    /**
     * Query using the specified criteria, and populate the browser with matches.
     */
    @Override
    public void query() {
        Cell cell = (model != null) ? model.getSelected() : null;
        grid = query(query);
        model = createTableModel(grid);
        if (table == null) {
            table = createTable(model);
            addTable(table, component);
        } else {
            table.setModel(model);
            table.setColumnModel(model.getColumnModel());
        }
        if (selected != null) {
            setSelected(selected);
        } else if (cell != null) {
            setSelectedCell(cell);
        }
    }

    /**
     * Returns the objects matching the query.
     *
     * @return the objects matching the query.
     */
    @Override
    public List<PropertySet> getObjects() {
        return Collections.emptyList();
    }

    /**
     * Returns the act associated with a roster event.
     *
     * @param event the roster event
     * @return the corresponding act. May be {@code null}
     */
    public Act getAct(PropertySet event) {
        return (Act) IMObjectHelper.getObject(event.getReference(ScheduleEvent.ACT_REFERENCE));
    }

    /**
     * Returns the modification hash for the specified schedule and date range.
     *
     * @param entity    the entity
     * @param events    the events
     * @param startDate the start date
     * @param endDate   the end date
     * @param service   the roster service
     * @return the modification hash, or {@code -1} if the entity and range are not cached
     */
    protected abstract long getModHash(Entity entity, ScheduleEvents events, Date startDate, Date endDate,
                                       RosterService service);

    /**
     * Returns the table model.
     *
     * @return the table model, or {@code null} if no query has executed
     */
    protected RosterTableModel getModel() {
        return model;
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
     * Executes the query, returning the results in a grid.
     *
     * @param query the query
     * @return the results
     */
    protected abstract RosterEventGrid query(RosterQuery query);

    /**
     * Lays out the component.
     *
     * @return a new component
     */
    protected Component doLayout() {
        Component column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, layoutQuery());
        SplitPane component = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "ScheduleBrowser", column);
        if (table != null) {
            addTable(table, component);
        }
        return component;
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
     * Lays out the query component.
     *
     * @return the query component
     */
    protected Component layoutQuery() {
        Row row = RowFactory.create(Styles.CELL_SPACING);
        FocusGroup group = getFocusGroup();
        row.add(query.getComponent());
        group.add(query.getFocusGroup());
        ButtonRow buttons = new ButtonRow(group);
        buttons.addButton("query", new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });
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
     * Creates a new table.
     *
     * @param model the model
     * @return a new table
     */
    protected TableEx createTable(RosterTableModel model) {
        TableEx table = new TableEx(model, model.getColumnModel());
        table.setStyleName("ScheduleTable");
        table.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onSelected((TableActionEventEx) event);
            }
        });
        return table;
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
        if (click.isDoubleClick() && column != RosterTableModel.NAME_INDEX && model.isSelected(column, row)) {
            // need to select the same cell to get double click
            doubleClick = true;
        }
        setSelectedCell(new Cell(column, row));
        if (doubleClick) {
            if (selected == null) {
                for (BrowserListener<PropertySet> listener : getBrowserListeners()) {
                    if (listener instanceof RosterBrowserListener) {
                        ((RosterBrowserListener) listener).create();
                    }
                }
            } else {
                for (BrowserListener<PropertySet> listener : getBrowserListeners()) {
                    if (listener instanceof RosterBrowserListener) {
                        ((RosterBrowserListener<PropertySet>) listener).edit(selected);
                    }
                }
            }
        } else if (selected != null) {
            notifySelected(selected);
        }

        // deselect the row
        table.getSelectionModel().clearSelection();
    }

    /**
     * Selects a cell.
     *
     * @param cell the cell to select
     */
    protected void setSelectedCell(Cell cell) {
        model.setSelected(cell);
        selected = model.getEvent(cell);
    }

    /**
     * Creates a table model.
     *
     * @param grid the roster event grid
     * @return a new table model
     */
    protected abstract RosterTableModel createTableModel(RosterEventGrid grid);

    /**
     * Determines if any of the events have been updated.
     * <p>
     * This assumes that none of the query criteria have changed since the events were returned.
     *
     * @return {@code true} if any of the events have been updated, otherwise {@code false}
     */
    private boolean updated() {
        boolean result = false;
        if (grid != null) {
            Map<Entity, ScheduleEvents> events = grid.getEvents();
            Date from = grid.getStartDate();
            Date to = grid.getEndDate();
            for (Map.Entry<Entity, ScheduleEvents> entry : events.entrySet()) {
                ScheduleEvents entityEvents = entry.getValue();
                long hash = getModHash(entry.getKey(), entityEvents, from, to, service);
                if (hash == -1 || hash != entityEvents.getModHash()) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

}
