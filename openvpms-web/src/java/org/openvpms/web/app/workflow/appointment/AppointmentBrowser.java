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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.workflow.appointment;

import echopointng.TableEx;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.layout.ColumnLayoutData;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.app.workflow.scheduling.ScheduleBrowser;
import org.openvpms.web.app.workflow.scheduling.ScheduleEventGrid;
import org.openvpms.web.app.workflow.scheduling.ScheduleTableModel;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.DateHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Appointment browser. Renders blocks of appointments in different hours a
 * different colour.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * Creates a new <tt>AppointmentBrowser</tt>.
     */
    public AppointmentBrowser() {
        super(new AppointmentQuery());
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
     * Performs a query.
     *
     * @param reselect if <tt>true</tt> try and reselect the selected cell
     */
    @Override
    protected void doQuery(boolean reselect) {
        getComponent();
        super.doQuery(reselect);
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
            Date date, Map<Entity, List<ObjectSet>> events) {
        Set<Entity> schedules = events.keySet();
        AppointmentGrid grid;
        if (schedules.size() == 1) {
            Party schedule = (Party) schedules.iterator().next();
            grid = new SingleScheduleGrid(date, schedule, events.get(schedule));
        } else {
            grid = new MultiScheduleGrid(date, events);
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
            return new SingleScheduleTableModel((AppointmentGrid) grid);
        }
        return new MultiScheduleTableModel((AppointmentGrid) grid);
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

        Component component = ColumnFactory.create("WideCellSpacing",
                                                   title, row);
        TableEx table = getTable();
        if (table != null) {
            component.add(table);
        }
        return component;
    }

    /**
     * Updates the title based on the current selection.
     */
    private void updateTitle() {
        DateFormat format = DateHelper.getFullDateFormat();
        String date = format.format(getDate());
        Entity view = getScheduleView();
        Entity schedule = getQuery().getSchedule();
        String viewName = (view != null) ? view.getName() : null;
        String schedName = (schedule != null) ? schedule.getName() : null;

        String text;
        if (viewName != null && schedName != null) {
            text = Messages.get(
                    "workflow.scheduling.appointment.viewscheduledate",
                    viewName, schedName, date);
        } else if (viewName != null) {
            text = Messages.get(
                    "workflow.scheduling.appointment.viewdate", viewName, date);
        } else {
            text = Messages.get("workflow.scheduling.appointment.date", date);
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
        return new AppointmentGridView(grid, startMins, endMins);
    }

}
