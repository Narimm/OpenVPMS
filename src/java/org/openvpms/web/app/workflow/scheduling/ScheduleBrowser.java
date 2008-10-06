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
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.ObjectSet;
import static org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid.Availability;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.query.AbstractBrowser;
import org.openvpms.web.component.im.query.QueryBrowserListener;
import org.openvpms.web.component.im.query.QueryListener;
import org.openvpms.web.component.table.DefaultTableHeaderRenderer;
import org.openvpms.web.component.table.EvenOddTableCellRenderer;
import org.openvpms.web.component.util.ButtonRow;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Schedule browser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class ScheduleBrowser extends AbstractBrowser<ObjectSet> {

    /**
     * The query.
     */
    private ScheduleQuery query;

    /**
     * The schedule events, keyed on schedule.
     */
    private Map<Entity, List<ObjectSet>> results;

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
    private ObjectSet selected;

    /**
     * The selected time. May be <tt>null</tt>.
     */
    private Date selectedTime;

    /**
     * The selected schedule. May be <tt>null</tt>
     */
    private Entity selectedSchedule;

    /**
     * Highlight selector, to change colour of display items.
     */
    private SelectField highlightSelector;

    /**
     * Clinician selector.
     */
    private SelectField clinicianSelector;


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
     * @return the selected object, or <code>null</code> if none has been
     *         selected.
     */
    public ObjectSet getSelected() {
        return selected;
    }

    /**
     * Select an object.
     *
     * @param object the object to select
     */
    public void setSelected(ObjectSet object) {
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
    public List<ObjectSet> getObjects() {
        if (results == null) {
            query();
        }
        List<ObjectSet> result = new ArrayList<ObjectSet>();
        for (List<ObjectSet> list : results.values()) {
            for (ObjectSet set : list) {
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
            createEventGrid(Date date, Map<Entity, List<ObjectSet>> events);

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
        addQueryButton(row);

        Component component = ColumnFactory.create("WideCellSpacing", row);
        if (table != null) {
            component.add(table);
        }
        return component;
    }

    /**
     * Lays out the query row, adding highlight and clinician selectors.
     *
     * @param row the container
     */
    protected void layoutQueryRow(Row row) {
        FocusGroup group = getFocusGroup();

        SelectField highlight = getHighlightSelector();
        SelectField clinician = getClinicianSelector();

        row.add(query.getComponent());
        row.add(LabelFactory.create("workflow.scheduling.highlight"));
        row.add(highlight);
        row.add(LabelFactory.create("clinician"));
        row.add(clinician);

        group.add(query.getFocusGroup());
        group.add(highlight);
        group.add(clinician);

    }

    protected void addQueryButton(Row row) {
        FocusGroup group = getFocusGroup();
        ButtonRow buttons = new ButtonRow(group);
        buttons.addButton("query", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onQuery();
            }
        });
        row.add(buttons);
    }

    /**
     * Returns the highlight selector.
     *
     * @return the highlight selector
     */
    protected SelectField getHighlightSelector() {
        if (highlightSelector == null) {
            highlightSelector = createHighlightSelector();
        }
        return highlightSelector;
    }

    /**
     * Returns the clinician selector.
     *
     * @return the clinician selector
     */
    protected SelectField getClinicianSelector() {
        if (clinicianSelector == null) {
            clinicianSelector = createClinicianSelector();
        }
        return clinicianSelector;
    }

    /**
     * Creates a new highlight selector.
     *
     * @return a new highlight selector
     */
    protected SelectField createHighlightSelector() {
        String[] highlightSelectorItems = {
                Messages.get("workflow.scheduling.highlight.event"),
                Messages.get("workflow.scheduling.highlight.clinician"),
                Messages.get("workflow.scheduling.highlight.status")};

        SelectField result = SelectFieldFactory.create(highlightSelectorItems);
        result.setSelectedItem(highlightSelectorItems[0]);
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onHighlightChanged();
            }
        });
        return result;
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

        Set<Entity> lastSchedules = (results != null) ? results.keySet() : null;
        results = query.query();

        ScheduleEventGrid grid = createEventGrid(query.getDate(), results);
        int lastRow = -1;
        int lastColumn = -1;
        if (model != null) {
            lastRow = model.getSelectedRow();
            lastColumn = model.getSelectedColumn();
        }
        model = createTableModel(grid);
        if (table == null) {
            table = createTable(model);
            table.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onSelected((TableActionEventEx) event);
                }
            });
            component.add(table);
        } else {
            table.setModel(model);
            table.setColumnModel(model.getColumnModel());
        }

        if (reselect) {
            // if the the schedules and date haven't changed, reselect the
            // selected cell
            Date selectedDate = (selectedTime != null)
                    ? DateRules.getDate(selectedTime) : null;
            if (lastRow != -1 && lastColumn != -1) {
                if (ObjectUtils.equals(lastSchedules, results.keySet())
                        && ObjectUtils.equals(selectedDate, query.getDate())) {
                    model.setSelectedCell(lastColumn, lastRow);
                }
            }
        }
    }

    /**
     * Invoked when the view changes.
     */
    private void onHighlightChanged() {
        int index = highlightSelector.getSelectedIndex();
        switch (index) {
            case 0:
                model.setHighlight(ScheduleTableModel.Highlight.EVENT);
                break;
            case 1:
                model.setHighlight(ScheduleTableModel.Highlight.CLINICIAN);
                break;
            default:
                model.setHighlight(ScheduleTableModel.Highlight.STATUS);
        }
    }

    /**
     * Invoked when the clinician changes.
     */
    private void onClinicianChanged() {
        User clinician = (User) clinicianSelector.getSelectedItem();
        if (clinician != null) {
            model.setClinician(clinician.getObjectReference());
        } else {
            model.setClinician(null);
        }
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
                for (QueryBrowserListener<ObjectSet> listener
                        : getQueryListeners()) {
                    if (listener instanceof ScheduleBrowserListener) {
                        ((ScheduleBrowserListener) listener).create();
                    }
                }
            } else {
                for (QueryBrowserListener<ObjectSet> listener
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

    /**
     * Creates a new dropdown to select clinicians.
     *
     * @return a new clinician selector
     */
    private SelectField createClinicianSelector() {
        UserRules rules = new UserRules();
        List<IMObject> clinicians = new ArrayList<IMObject>();
        ArchetypeQuery query = new ArchetypeQuery("security.user", true, true);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        Iterator<User> iter = new IMObjectQueryIterator<User>(query);
        while (iter.hasNext()) {
            User user = iter.next();
            if (rules.isClinician(user)) {
                clinicians.add(user);
            }
        }
        IMObjectListModel model
                = new IMObjectListModel(clinicians, true, false);
        SelectField result = SelectFieldFactory.create(model);
        result.setCellRenderer(IMObjectListCellRenderer.INSTANCE);

        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onClinicianChanged();
            }
        });

        return result;
    }

}
