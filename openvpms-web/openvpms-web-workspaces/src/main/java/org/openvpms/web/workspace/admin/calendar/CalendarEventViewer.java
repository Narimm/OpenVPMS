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

package org.openvpms.web.workspace.admin.calendar;

import echopointng.TableEx;
import echopointng.table.TableActionEventEx;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.CalendarService;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.im.query.DateNavigator;
import org.openvpms.web.component.im.query.DateSelector;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.Cell;
import org.openvpms.web.echo.util.DoubleClickMonitor;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.appointment.TimeRange;
import org.openvpms.web.workspace.workflow.appointment.TimeRangeSelector;

import java.util.Date;
import java.util.List;

import static org.openvpms.web.echo.style.Styles.BOLD;

/**
 * Displays calendar events.
 *
 * @author Tim Anderson
 */
public class CalendarEventViewer {

    /**
     * The calendar.
     */
    private final Entity calendar;

    /**
     * The calendar service.
     */
    private final CalendarService service;

    /**
     * Used to determine if there has been a double click.
     */
    private final DoubleClickMonitor click = new DoubleClickMonitor();

    /**
     * The date navigator.
     */
    private final DateSelector navigator;

    /**
     * The time range selector.
     */
    private final TimeRangeSelector timeSelector;

    /**
     * Label to display the selected date.
     */
    private final Label selectedDate;

    /**
     * The calendar component.
     */
    private Component component;

    /**
     * The calendar table model.
     */
    private CalendarTableModel model;

    /**
     * The table.
     */
    private TableEx table;

    /**
     * Listener for calendar create, edit events.
     */
    private CalendarListener listener;

    /**
     * Constructs a {@link CalendarEventViewer}.
     *
     * @param calendar the calendar
     */
    public CalendarEventViewer(Entity calendar) {
        this.calendar = calendar;
        service = ServiceHelper.getBean(CalendarService.class);
        navigator = new DateSelector();
        timeSelector = new TimeRangeSelector();
        selectedDate = LabelFactory.create(null, BOLD);
        navigator.setNavigator(DateNavigator.MONTH);
        ActionListener refresh = new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                refresh();
            }
        };
        navigator.setListener(refresh);
        timeSelector.addActionListener(refresh);
    }

    /**
     * Returns the component.
     *
     * @return the component.
     */
    public Component getComponent() {
        if (component == null) {
            component = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM, "Calendar.View");
            doLayout(component);
        }
        return component;
    }

    /**
     * Refreshes the display.
     */
    public void refresh() {
        if (component == null) {
            getComponent();
        } else {
            component.removeAll();
            doLayout(component);
        }
    }

    /**
     * Registers a listener to receive create and edit events.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setListener(CalendarListener listener) {
        this.listener = listener;
    }

    /**
     * Lays out the component in the supplied container.
     *
     * @param container the container
     */
    private void doLayout(Component container) {
        Date startDate = navigator.getDate();
        int daysInMonth = DateRules.getDaysInMonth(startDate);
        Date endDate = DateRules.getDate(startDate, daysInMonth, DateUnits.DAYS);
        List<PropertySet> events = service.getEvents(calendar, startDate, endDate);
        AppointmentRules rules = ServiceHelper.getBean(AppointmentRules.class);
        CalendarGrid grid = new DefaultCalendarGrid(calendar, startDate, daysInMonth, events, rules);
        TimeRange range = timeSelector.getSelected();
        if (range != TimeRange.ALL) {
            grid = new CalendarGridView(grid, range.getStartMins(), range.getEndMins());
        }
        model = new CalendarTableModel(grid);
        table = new TableEx(model, model.getColumnModel());
        table.setStyleName("ScheduleTable");
        table.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                TableActionEventEx ex = (TableActionEventEx) event;
                onSelected(new Cell(ex.getColumn(), ex.getRow()));
            }
        });

        selectedDate.setText(Messages.format("calendar.date", startDate));
        Row time = RowFactory.create(Styles.CELL_SPACING, LabelFactory.create("workflow.scheduling.show"),
                                     timeSelector);
        Row row = RowFactory.create(Styles.WIDE_CELL_SPACING, navigator.getComponent(), time, selectedDate);
        container.add(ColumnFactory.create(Styles.INSET, row));
        container.add(ColumnFactory.create(Styles.INSET, table));
    }

    /**
     * Invoked when a cell is selected.
     *
     * @param cell the selected cell
     */
    private void onSelected(Cell cell) {
        table.getSelectionModel().clearSelection(); // need to deselect the cell
        if (cell.getColumn() != CalendarTableModel.TIME_COLUMN) {
            model.setSelected(cell);
            if (listener != null && click.isDoubleClick(cell)) {
                PropertySet event = model.getEvent(cell);
                if (event != null) {
                    IMObjectReference actRef = event.getReference(ScheduleEvent.ACT_REFERENCE);
                    Act act = (Act) IMObjectHelper.getObject(actRef);
                    if (act != null) {
                        listener.edit(act);
                    } else {
                        refresh();
                    }
                } else {
                    listener.create(model.getDatetime(cell));
                }
            }
        }
    }
}
