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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment;

import echopointng.TabbedPane;
import echopointng.table.TableActionEventEx;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.Table;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.ColumnLayoutData;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.Slot;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.AbstractBrowserListener;
import org.openvpms.web.component.im.query.TabbedBrowserListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.factory.TabbedPaneFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.tabpane.TabPaneModel;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.appointment.boarding.CageScheduleGrid;
import org.openvpms.web.workspace.workflow.appointment.boarding.CageSummaryTableModel;
import org.openvpms.web.workspace.workflow.appointment.boarding.CageTableModel;
import org.openvpms.web.workspace.workflow.appointment.boarding.CheckInOutTableModel;
import org.openvpms.web.workspace.workflow.appointment.boarding.CheckInScheduleGrid;
import org.openvpms.web.workspace.workflow.appointment.boarding.CheckInTableModel;
import org.openvpms.web.workspace.workflow.appointment.boarding.CheckOutScheduleGrid;
import org.openvpms.web.workspace.workflow.appointment.boarding.CheckOutTableModel;
import org.openvpms.web.workspace.workflow.appointment.boarding.DefaultCageTableModel;
import org.openvpms.web.workspace.workflow.scheduling.Cell;
import org.openvpms.web.workspace.workflow.scheduling.IntersectComparator;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleBrowser;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleColours;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openvpms.web.echo.style.Styles.BOLD;
import static org.openvpms.web.echo.style.Styles.INSET;
import static org.openvpms.web.echo.style.Styles.WIDE_CELL_SPACING;
import static org.openvpms.web.workspace.workflow.appointment.AppointmentQuery.DateRange.DAY;
import static org.openvpms.web.workspace.workflow.appointment.AppointmentQuery.DateRange.WEEK;


/**
 * Appointment browser.
 * <p>
 * This provides two tabs:
 * <ol><li>Appointments<br/>
 * Provides a query to select appointments, and renders blocks of appointments in different hours a
 * different colour.
 * </li>
 * <li>Free Appointment Slots<br/>
 * Provides a query to find free appointment slots
 * </li>
 * </ol>
 * The Appointments tab is the primary tab; all {@link ScheduleBrowser} methods will be directed to this.
 *
 * @author Tim Anderson
 */
public class AppointmentBrowser extends ScheduleBrowser {

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The colour cache.
     */
    private final ScheduleColours colours;

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
     * The tab pane model.
     */
    private TabPaneModel model;

    /**
     * The tabbed pane.
     */
    private TabbedPane tab;

    /**
     * The free slot query.
     */
    private FreeAppointmentSlotQuery freeSlotQuery;

    /**
     * The free slot browser.
     */
    private FreeAppointmentSlotBrowser freeSlotBrowser;

    /**
     * The tab listener.
     */
    private TabbedBrowserListener listener;

    /**
     * The appointments tab index.
     */
    private int appointmentsTab;

    /**
     * The free slots tab index.
     */
    private int freeSlotsTab;

    /**
     * The last selected tab, to avoid redundant updates.
     */
    private int lastTab;


    /**
     * Constructs an {@link AppointmentBrowser}.
     *
     * @param location the practice location. May be {@code null}
     * @param context  the context
     */
    public AppointmentBrowser(Party location, LayoutContext context) {
        this(new AppointmentQuery(location, context.getPreferences()), context);
    }

    /**
     * Constructs an {@link AppointmentBrowser}.
     *
     * @param query   the query
     * @param context the context
     */
    public AppointmentBrowser(AppointmentQuery query, LayoutContext context) {
        super(query, context.getContext());
        this.context = context;
        rules = ServiceHelper.getBean(AppointmentRules.class);
        colours = ServiceHelper.getBean(ScheduleColours.class);
    }

    /**
     * Sets the tab listener.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setListener(TabbedBrowserListener listener) {
        this.listener = listener;
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
     * Returns if the appointments tab is selected.
     *
     * @return {@code true} if the appointments tab is selected, {@code false} if the free slot tab is selected
     */
    public boolean isAppointmentsSelected() {
        return tab.getSelectedIndex() == appointmentsTab;
    }

    /**
     * Selects the cell for the specified schedule and slot start time.
     *
     * @param schedule  the schedule
     * @param startTime the slot start time
     */
    public void setSelected(Entity schedule, Date startTime) {
        Cell cell = getModel().getCell(schedule.getObjectReference(), startTime);
        if (cell != null) {
            setSelectedCell(cell);
        }
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
    protected ScheduleEventGrid createEventGrid(Date date, Map<Entity, List<PropertySet>> events) {
        Set<Entity> schedules = events.keySet();
        ScheduleEventGrid grid;
        Entity scheduleView = getScheduleView();
        AppointmentQuery query = getQuery();
        AppointmentQuery.DateRange dateRange = query.getDateRange();
        if (dateRange != DAY && AppointmentHelper.isMultiDayView(scheduleView)) {
            int days = query.getDays();
            AppointmentQuery.Show show = query.getShow();
            if (show == AppointmentQuery.Show.CAGE
                || show == AppointmentQuery.Show.SUMMARY) {
                grid = new CageScheduleGrid(scheduleView, date, days, events, rules);
            } else if (show == AppointmentQuery.Show.CHECKIN) {
                grid = new CheckInScheduleGrid(scheduleView, date, days, events, rules);
            } else if (show == AppointmentQuery.Show.CHECKOUT) {
                grid = new CheckOutScheduleGrid(scheduleView, date, days, events, rules);
            } else {
                grid = new MultiDayScheduleGrid(scheduleView, date, days, events, rules);
            }
        } else {
            if (schedules.size() == 1) {
                Party schedule = (Party) schedules.iterator().next();
                List<PropertySet> sets = events.get(schedule);
                if (!hasOverlappingEvents(sets, schedule)) {
                    grid = new SingleScheduleGrid(scheduleView, date, schedule, sets, rules);
                } else {
                    // there are overlapping appointments, so display them in multiple schedules
                    grid = new MultiScheduleGrid(scheduleView, date, events, rules);
                }
            } else {
                grid = new MultiScheduleGrid(getScheduleView(), date, events, rules);
            }
            AppointmentQuery.TimeRange range = query.getTimeRange();
            grid = createGridView((AppointmentGrid) grid, range);
        }
        return grid;
    }

    /**
     * Creates a new table model.
     *
     * @param grid the schedule event grid
     * @return the table model
     */
    protected ScheduleTableModel createTableModel(ScheduleEventGrid grid) {
        ScheduleTableModel model;
        if (grid instanceof CageScheduleGrid) {
            CageScheduleGrid cageGrid = (CageScheduleGrid) grid;
            if (getQuery().getShow() == AppointmentQuery.Show.CAGE) {
                model = new DefaultCageTableModel(cageGrid, getContext(), colours);
            } else {
                model = new CageSummaryTableModel(cageGrid, getContext(), colours);
            }
        } else if (grid instanceof MultiDayScheduleGrid) {
            model = new MultiDayTableModel((MultiDayScheduleGrid) grid, getContext(), colours);
        } else if (grid instanceof CheckInScheduleGrid) {
            model = new CheckInTableModel((CheckInScheduleGrid) grid, getContext(), colours);
        } else if (grid instanceof CheckOutScheduleGrid) {
            model = new CheckOutTableModel((CheckOutScheduleGrid) grid, getContext(), colours);
        } else if (grid.getSchedules().size() == 1) {
            model = new SingleScheduleTableModel((AppointmentGrid) grid, getContext(), colours);
        } else {
            model = new MultiScheduleTableModel((AppointmentGrid) grid, getContext(), colours);
        }
        return model;
    }

    /**
     * Lays out the component.
     */
    @Override
    protected Component doLayout() {
        Component tabContainer = new Column();
        model = new TabPaneModel(tabContainer);
        tab = TabbedPaneFactory.create(model);
        title = LabelFactory.create(null, BOLD);
        ColumnLayoutData layout = new ColumnLayoutData();
        layout.setAlignment(Alignment.ALIGN_CENTER);
        title.setLayoutData(layout);

        Component column = ColumnFactory.create(INSET, ColumnFactory.create(WIDE_CELL_SPACING, title, layoutQuery()));

        appointmentsTab = addTab(Messages.get("workflow.scheduling.appointment.title"), column);
        freeSlotsTab = addTab(Messages.get("workflow.scheduling.appointment.find.title"),
                              ColumnFactory.create(Styles.INSET));
        tab.setSelectedIndex(appointmentsTab);
        lastTab = appointmentsTab;

        Table table = getTable();
        tabContainer.add(tab);
        SplitPane component = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "AppointmentBrowser",
                                                      tabContainer);
        if (getScheduleView() != null && table != null) {
            addTable(table, component);
        }
        tab.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                onBrowserChanged();
            }
        });

        return component;
    }

    /**
     * Invoked when a cell is selected.
     * <p>
     * Notifies listeners of the selection.
     *
     * @param event the event
     */
    @Override
    protected void onSelected(TableActionEventEx event) {
        ScheduleTableModel model = getModel();
        if (model instanceof CageTableModel) {
            CageTableModel cageModel = (CageTableModel) model;
            if (cageModel.isCageType(event.getRow())) {
                cageModel.toggle(event.getRow());
                getTable().getSelectionModel().clearSelection();
            } else {
                super.onSelected(event);
            }
        } else if (model instanceof CheckInOutTableModel) {
            CheckInOutTableModel checkInOutTableModel = (CheckInOutTableModel) model;
            if (!checkInOutTableModel.isEmpty()) {
                super.onSelected(event);
            } else {
                getTable().getSelectionModel().clearSelection();
            }
        } else {
            super.onSelected(event);
        }
    }

    /**
     * Updates the title based on the current selection.
     */
    private void updateTitle() {
        Entity view = getScheduleView();
        AppointmentQuery.DateRange dateRange = getQuery().getDateRange();
        Entity schedule = getQuery().getSchedule();
        String viewName = (view != null) ? view.getName() : null;
        String schedName = (schedule != null) ? schedule.getName() : null;

        String date;
        String text;
        if (view != null && schedule != null) {
            ScheduleEventGrid grid = getModel().getGrid();
            if (dateRange == DAY) {
                date = Messages.format("workflow.scheduling.appointment.day", grid.getStartDate());
            } else {
                Date from = grid.getStartDate();
                Date to = grid.getEndDate();
                boolean sameMonth = DateRules.getMonthStart(from).equals(DateRules.getMonthStart(to));
                if (dateRange == WEEK) {
                    if (sameMonth) {
                        date = Messages.format("workflow.scheduling.appointment.week.samemonth", from, to);
                    } else {
                        date = Messages.format("workflow.scheduling.appointment.week.diffmonth", from, to);
                    }
                } else {
                    if (sameMonth) {
                        date = Messages.format("workflow.scheduling.appointment.month.samemonth", from, to);
                    } else {
                        date = Messages.format("workflow.scheduling.appointment.month.diffmonth", from, to);
                    }
                }
            }
        } else {
            date = Messages.format("workflow.scheduling.appointment.day", getDate());
        }
        if (viewName != null && schedName != null) {
            text = Messages.format("workflow.scheduling.appointment.viewscheduledate", viewName, schedName, date);
        } else if (viewName != null) {
            text = Messages.format("workflow.scheduling.appointment.viewdate", viewName, date);
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
    private AppointmentGrid createGridView(AppointmentGrid grid, AppointmentQuery.TimeRange timeRange) {
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
        IntersectComparator comparator = new IntersectComparator(slotSize, rules);
        List<PropertySet> list = new ArrayList<>();

        for (PropertySet event : events) {
            if (Collections.binarySearch(list, event, comparator) >= 0) {
                return true;
            }
            list.add(event);
        }
        return false;
    }

    /**
     * Removes the existing browser, if required.
     */
    private void removeBrowser() {
        Component parent = getComponent();
        if (parent.getComponentCount() == 2) {
            parent.remove(parent.getComponents()[1]);
        }
    }

    /**
     * Invoked when a browser tab is selected.
     */
    private void onBrowserChanged() {
        int index = tab.getSelectedIndex();
        if (index != lastTab) {
            lastTab = index;
            if (index == appointmentsTab) {
                onAppointmentsSelected();
            } else {
                onFreeSlotsSelected();
            }
            if (listener != null) {
                listener.onBrowserChanged();
            }
        }
    }

    /**
     * Invoked when the appointments tab is selected.
     */
    private void onAppointmentsSelected() {
        Component parent = getComponent();
        removeBrowser();

        Table table = getTable();
        if (getScheduleView() != null && table != null) {
            addTable(table, parent);
        }
    }

    /**
     * Invoked when the free slots tab is selected.
     */
    private void onFreeSlotsSelected() {
        if (freeSlotQuery == null) {
            Party location = getContext().getLocation();
            freeSlotQuery = new FreeAppointmentSlotQuery(location, getScheduleView(), getSelectedSchedule(),
                                                         getQuery().getDate(), context.getPreferences());
            freeSlotBrowser = new FreeAppointmentSlotBrowser(freeSlotQuery, context);
            freeSlotBrowser.addBrowserListener(new AbstractBrowserListener<Slot>() {
                @Override
                public void selected(Slot slot) {
                    onFreeSlotSelected(slot);
                }
            });
            Component query = layoutQuery(freeSlotQuery, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    freeSlotBrowser.query();
                }
            });
            model.getTabContentAt(freeSlotsTab).add(query);
        }
        Component parent = getComponent();
        removeBrowser();
        parent.add(freeSlotBrowser.getComponent());
    }

    private void onFreeSlotSelected(Slot slot) {
        Entity scheduleView = freeSlotBrowser.getScheduleView();
        Entity schedule = freeSlotBrowser.getSchedule(slot);
        if (scheduleView != null && schedule != null) {
            setScheduleView(scheduleView);
            Date startTime = slot.getStartTime();
            setDate(startTime);
            getQuery().setTimeRange(AppointmentQuery.TimeRange.getRange(startTime));
            query();
            setSelected(schedule, startTime);
            tab.setSelectedIndex(appointmentsTab);
        }
    }

    /**
     * Adds a browser tab.
     *
     * @param displayName the tab name
     * @param component   the component
     * @return the tab index
     */
    private int addTab(String displayName, Component component) {
        int result = model.size();
        int shortcut = result + 1;
        String text = "&" + shortcut + " " + displayName;
        component = ColumnFactory.create(Styles.INSET, component);
        model.addTab(text, component);
        return result;
    }

}
