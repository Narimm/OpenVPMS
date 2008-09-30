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
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
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
import org.openvpms.web.component.util.SelectFieldFactory;
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
     * Displays the selected date above the appointments.
     */
    private Label selectedDate;

    /**
     * Time range selector.
     */
    private SelectField timeSelector;


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
        super.query();
        DateFormat format = DateHelper.getFullDateFormat();
        selectedDate.setText(format.format(getDate()));
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
        TimeRange range = getSelectedTimeRange();
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
        selectedDate = LabelFactory.create(null, "bold");
        ColumnLayoutData layout = new ColumnLayoutData();
        layout.setAlignment(Alignment.ALIGN_CENTER);
        selectedDate.setLayoutData(layout);

        Row row = RowFactory.create("CellSpacing");
        layoutQueryRow(row);
        addQueryButton(row);

        Component component = ColumnFactory.create("WideCellSpacing",
                                                   selectedDate, row);
        TableEx table = getTable();
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
    @Override
    protected void layoutQueryRow(Row row) {
        super.layoutQueryRow(row);

        String[] timeSelectorItems = {
                Messages.get("workflow.scheduling.time.all"),
                Messages.get("workflow.scheduling.time.morning"),
                Messages.get("workflow.scheduling.time.afternoon"),
                Messages.get("workflow.scheduling.time.evening"),
                Messages.get("workflow.scheduling.time.AM"),
                Messages.get("workflow.scheduling.time.PM")};

        timeSelector = SelectFieldFactory.create(timeSelectorItems);
        timeSelector.setSelectedItem(timeSelectorItems[0]);
        timeSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onTimeRangeChanged();
            }
        });

        Label timeLabel = LabelFactory.create("workflow.scheduling.time");
        row.add(timeLabel);
        row.add(timeSelector);
    }

    /**
     * Invoked when the time range changed.
     */
    private void onTimeRangeChanged() {
        doQuery(false); // don't reselect the cell
    }

    /**
     * Returns the selected time range.
     *
     * @return the selected time range
     */
    private TimeRange getSelectedTimeRange() {
        int index = timeSelector.getSelectedIndex();
        TimeRange range;
        switch (index) {
            case 0:
                range = TimeRange.ALL;
                break;
            case 1:
                range = TimeRange.MORNING;
                break;
            case 2:
                range = TimeRange.AFTERNOON;
                break;
            case 3:
                range = TimeRange.EVENING;
                break;
            case 4:
                range = TimeRange.AM;
                break;
            case 5:
                range = TimeRange.PM;
                break;
            default:
                range = TimeRange.ALL;
        }
        return range;
    }

    /**
     * Creates a new view of the appointments.
     *
     * @param grid      the underlying appointment grid
     * @param timeRange the time range to view
     * @return view a new grid view, based on the time range
     */
    private AppointmentGrid createGridView(AppointmentGrid grid,
                                           TimeRange timeRange) {
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

    private enum TimeRange {
        ALL(0, 34), MORNING(8, 12), AFTERNOON(12, 17), EVENING(17, 24),
        AM(0, 12), PM(12, 24);

        TimeRange(int startHour, int endHour) {
            this.startMins = startHour * 60;
            this.endMins = endHour * 60;
        }

        public int getStartMins() {
            return startMins;
        }

        public int getEndMins() {
            return endMins;
        }

        private final int startMins;
        private final int endMins;
    }
}
