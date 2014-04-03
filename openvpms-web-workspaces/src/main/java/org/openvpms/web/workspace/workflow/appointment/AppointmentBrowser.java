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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import echopointng.TableEx;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.layout.ColumnLayoutData;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.i18n.format.DateFormatter;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.scheduling.IntersectComparator;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleBrowser;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Appointment browser. Renders blocks of appointments in different hours a
 * different colour.
 *
 * @author Tim Anderson
 */
public class AppointmentBrowser extends ScheduleBrowser {

    /**
     * Displays the selected schedule view, schedule and date above the
     * appointments.
     */
    private Label title;

    /**
     * The last time range.
     */
    private AppointmentQuery.TimeRange lastTimeRange;

    /**
     * The appointment rules.
     */
    private AppointmentRules rules;


    /**
     * Constructs an {@code AppointmentBrowser}.
     *
     * @param location the practice location. May be {@code null}
     * @param context  the context
     */
    public AppointmentBrowser(Party location, Context context) {
        super(new AppointmentQuery(location), context);
        rules = ServiceHelper.getBean(AppointmentRules.class);
    }

    /**
     * Query using the specified criteria, and populate the table with matches.
     */
    public void query() {
        AppointmentQuery.TimeRange timeRange = getQuery().getTimeRange();
        boolean reselect = true;
        if (lastTimeRange == null || !timeRange.equals(lastTimeRange)) {
            reselect = false;
        }
        lastTimeRange = timeRange;
        doQuery(reselect);
        updateTitle();
    }

    /**
     * Returns the query.
     *
     * @return the query
     */
    @Override
    protected AppointmentQuery getQuery() {
        return (AppointmentQuery) super.getQuery();
    }

    /**
     * Creates a new grid for a set of events.
     *
     * @param date   the query date
     * @param events the events
     */
    protected ScheduleEventGrid createEventGrid(
            Date date, Map<Entity, List<PropertySet>> events) {
        Set<Entity> schedules = events.keySet();
        AppointmentGrid grid;
        if (schedules.size() == 1) {
            Party schedule = (Party) schedules.iterator().next();
            List<PropertySet> sets = events.get(schedule);
            if (!hasOverlappingEvents(sets, schedule)) {
                grid = new SingleScheduleGrid(getScheduleView(), date, schedule, sets, rules);
            } else {
                // there are overlapping appointments, so display them in multiple schedules
                grid = new MultiScheduleGrid(getScheduleView(), date, events, rules);
            }
        } else {
            grid = new MultiScheduleGrid(getScheduleView(), date, events, rules);
        }
        AppointmentQuery.TimeRange range = getQuery().getTimeRange();
        return createGridView(grid, range);
    }

    /**
     * Creates a new table model.
     *
     * @param grid the schedule event grid
     * @return the table model
     */
    protected ScheduleTableModel createTableModel(ScheduleEventGrid grid) {
        if (grid.getSchedules().size() == 1) {
            return new SingleScheduleTableModel((AppointmentGrid) grid, getContext());
        }
        return new MultiScheduleTableModel((AppointmentGrid) grid, getContext());
    }

    /**
     * Creates a new table.
     *
     * @param model the model
     * @return a new table
     */
    @Override
    protected TableEx createTable(ScheduleTableModel model) {
        TableEx table = super.createTable(model);
        table.setDefaultRenderer(new AppointmentTableCellRenderer());
        return table;
    }

    /**
     * Lays out the component.
     */
    @Override
    protected Component doLayout() {
        title = LabelFactory.create(null, "bold");
        ColumnLayoutData layout = new ColumnLayoutData();
        layout.setAlignment(Alignment.ALIGN_CENTER);
        title.setLayoutData(layout);

        Row row = RowFactory.create("CellSpacing");
        layoutQueryRow(row);

        Component column = ColumnFactory.create("WideCellSpacing", title, row);
        TableEx table = getTable();
        SplitPane component = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL,
                "AppointmentBrowser", column);
        if (getScheduleView() != null && table != null) {
            addTable(table, component);
        }
        return component;
    }

    /**
     * Updates the title based on the current selection.
     */
    private void updateTitle() {
        DateFormat format = DateFormatter.getFullDateFormat();
        String date = format.format(getDate());
        Entity view = getScheduleView();
        Entity schedule = getQuery().getSchedule();
        String viewName = (view != null) ? view.getName() : null;
        String schedName = (schedule != null) ? schedule.getName() : null;

        String text;
        if (viewName != null && schedName != null) {
            text = Messages.format(
                    "workflow.scheduling.appointment.viewscheduledate",
                    viewName, schedName, date);
        } else if (viewName != null) {
            text = Messages.format(
                    "workflow.scheduling.appointment.viewdate", viewName, date);
        } else {
            text = Messages.format("workflow.scheduling.appointment.date", date);
        }
        title.setText(text);
    }

    /**
     * Creates a new view of the appointments.
     *
     * @param grid      the underlying appointment grid
     * @param timeRange the time range to view
     * @return view a new grid view, based on the time range
     */
    private AppointmentGrid createGridView(
            AppointmentGrid grid,
            AppointmentQuery.TimeRange timeRange) {
        int startMins = timeRange.getStartMins();
        int endMins = timeRange.getEndMins();
        if (startMins < grid.getStartMins()) {
            startMins = grid.getStartMins();
        }
        if (endMins > grid.getEndMins()) {
            endMins = grid.getEndMins();
        }
        if (startMins > endMins) {
            startMins = endMins;
        }
        return new AppointmentGridView(grid, startMins, endMins, rules);
    }

    /**
     * Determines if one or more events have overlapping times.
     *
     * @param events   the events
     * @param schedule the schedule, used to determine the slot size
     * @return {@code true} if one or more events have overlapping times
     */
    private boolean hasOverlappingEvents(List<PropertySet> events, Party schedule) {
        AppointmentRules rules = ServiceHelper.getBean(AppointmentRules.class);
        int slotSize = AbstractAppointmentGrid.getSlotSize(schedule, rules);
        IntersectComparator comparator = new IntersectComparator(slotSize);
        List<PropertySet> list = new ArrayList<PropertySet>();

        for (PropertySet event : events) {
            if (Collections.binarySearch(list, event, comparator) >= 0) {
                return true;
            }
            list.add(event);
        }
        return false;
    }

}
