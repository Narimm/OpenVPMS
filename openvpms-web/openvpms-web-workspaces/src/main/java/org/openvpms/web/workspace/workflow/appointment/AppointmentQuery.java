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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.joda.time.DateTime;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleService;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.im.query.DateNavigator;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleServiceQuery;

import java.util.Date;
import java.util.List;


/**
 * Appointment query.
 *
 * @author Tim Anderson
 */
class AppointmentQuery extends ScheduleServiceQuery {

    public enum TimeRange {

        ALL(0, 24), MORNING(8, 12), AFTERNOON(12, 17), EVENING(17, 24),
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

        /**
         * Returns the time range that the specified time falls into.
         *
         * @param time the time
         * @return the corresponding time range
         */
        public static TimeRange getRange(Date time) {
            DateTime dateTime = new DateTime(time);
            int hour = dateTime.getHourOfDay();
            if (hour < 8) {
                return AM;
            } else if (hour >= 8 && hour < 12) {
                return MORNING;
            } else if (hour >= 12 && hour < 17) {
                return AFTERNOON;
            } else if (hour >= 17) {
                return EVENING;
            }
            return ALL;
        }

        private final int startMins;

        private final int endMins;
    }

    public enum DateRange {
        DAY, WEEK, MONTH
    }

    /**
     * Time range selector.
     */
    private SelectField timeSelector;

    /**
     * Date range selector.
     */
    private SelectField dateSelector;

    /**
     * The selected date range.
     */
    private DateRange dateRange = DateRange.DAY;

    /**
     * The no. of days to query.
     */
    private int days = 1;

    /**
     * The container for the Dates label.
     */
    private Component labelContainer = new Row();

    /**
     * The container for the Dates selector.
     */
    private Component datesContainer = new Row();

    /**
     * Constructs an {@link AppointmentQuery}.
     *
     * @param location the practice location. May be {@code null}
     */
    public AppointmentQuery(Party location) {
        super(ServiceHelper.getAppointmentService(), new AppointmentSchedules(location));
    }

    /**
     * Returns the selected time range.
     *
     * @return the selected time range
     */
    public TimeRange getTimeRange() {
        int index = timeSelector.getSelectedIndex();
        return index >= 0 && index < TimeRange.values().length ? TimeRange.values()[index] : TimeRange.ALL;
    }

    /**
     * Sets the selected time range.
     *
     * @param range the time range
     */
    public void setTimeRange(TimeRange range) {
        timeSelector.setSelectedIndex(range.ordinal());
    }

    /**
     * Returns the selected date range.
     *
     * @return the date range
     */
    public DateRange getDateRange() {
        return dateRange;
    }

    /**
     * Returns the no. of days to query.
     *
     * @return the no of days
     */
    public int getDays() {
        return days;
    }

    /**
     * Creates a container to lay out the component.
     *
     * @return a new container
     */
    @Override
    protected Component createContainer() {
        return GridFactory.create(8);
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        // the order of the items must correspond to the order of TimeRange values.
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
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });

        String[] dwm = {Messages.get("workflow.scheduling.dates.day"),
                        Messages.get("workflow.scheduling.dates.week"),
                        Messages.get("workflow.scheduling.dates.month")};
        dateSelector = SelectFieldFactory.create(dwm);
        dateSelector.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onDatesChanged();
            }
        });

        container.add(LabelFactory.create("workflow.scheduling.time"));
        container.add(timeSelector);
        getFocusGroup().add(timeSelector);
        container.add(labelContainer, 6);
        container.add(datesContainer, 7);
        updateDatesFilter();
    }

    /**
     * Returns the events for a schedule and date.
     *
     * @param schedule the schedule
     * @param date     the date
     * @return the events
     */
    @Override
    protected List<PropertySet> getEvents(Entity schedule, Date date) {
        ScheduleService service = getService();
        if (days == 1) {
            return service.getEvents(schedule, date);
        } else {
            return service.getEvents(schedule, date, DateRules.getDate(date, days, DateUnits.DAYS));
        }
    }

    /**
     * Invoked when the schedule view changes.
     * <p/>
     * Notifies any listener to perform a query.
     */
    @Override
    protected void onViewChanged() {
        updateDatesFilter();
        super.onViewChanged();
    }

    /**
     * Invoked when the Dates filter changes.
     */
    private void onDatesChanged() {
        int index = dateSelector.getSelectedIndex();
        DateRange range;
        if (index >= 0 && index < DateRange.values().length) {
            range = DateRange.values()[index];
        } else {
            range = DateRange.DAY;
        }
        setDateRange(range);
        onQuery();
    }

    /**
     * Sets the date range.
     *
     * @param dateRange the date range
     */
    private void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
        switch (dateRange) {
            case DAY:
                setDateNavigator(DateNavigator.DAY);
                break;
            case WEEK:
                setDateNavigator(DateNavigator.WEEK);
                break;
            case MONTH:
                setDateNavigator(DateNavigator.MONTH);
                break;
        }
        updateDays(getDate(), dateRange);
    }

    /**
     * Invoked when the date changes.
     * <p/>
     * This implementation invokes {@link #onQuery()}.
     */
    @Override
    protected void onDateChanged() {
        updateDays(getDate(), dateRange);
        super.onDateChanged();
    }

    /**
     * Updates the no. of days to query.
     *
     * @param date  the date
     * @param range the date range
     */
    private void updateDays(Date date, DateRange range) {
        switch (range) {
            case DAY:
                days = 1;
                break;
            case WEEK:
                days = 7;
                break;
            default:
                days = DateRules.getDaysInMonth(date);
        }
    }

    /**
     * Updates the Dates filter.
     */
    private void updateDatesFilter() {
        DateRange range;
        Entity view = getScheduleView();
        if (AppointmentHelper.isMultiDayView(view)) {
            range = DateRange.MONTH;
            if (datesContainer.getComponentCount() == 0) {
                labelContainer.add(LabelFactory.create("workflow.scheduling.dates"));
                datesContainer.add(dateSelector);
            }
        } else {
            range = DateRange.DAY;
            labelContainer.removeAll();
            datesContainer.removeAll();
        }
        setDateRange(range);
        dateSelector.setSelectedIndex(dateRange.ordinal());
    }
}
