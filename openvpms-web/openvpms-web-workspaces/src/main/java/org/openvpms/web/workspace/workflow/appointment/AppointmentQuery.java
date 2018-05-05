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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.joda.time.DateTime;
import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleService;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.im.query.DateNavigator;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleServiceQuery;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel.Highlight;

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

        private final int startMins;

        private final int endMins;

        TimeRange(int startHour, int endHour) {
            this.startMins = startHour * 60;
            this.endMins = endHour * 60;
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

        /**
         * Returns one of {@link #AM}, or {@link #PM} based on the supplied time.
         *
         * @param time the time
         * @return the corresponding time range
         */
        public static TimeRange getAMorPM(DateTime time) {
            return time.getHourOfDay() < 12 ? AM : PM;
        }

        /**
         * Returns one of {@link #MORNING}, {@link #AFTERNOON} or {@link #EVENING} based on the supplied time.
         *
         * @param time the time
         * @return the corresponding time range
         */
        public static TimeRange getMorningOrAfternoonOrEvening(DateTime time) {
            int hour = time.getHourOfDay();
            if (hour < 12) {
                return MORNING;
            } else if (hour < 15) {
                return AFTERNOON;
            }
            return EVENING;
        }

        public int getStartMins() {
            return startMins;
        }

        public int getEndMins() {
            return endMins;
        }
    }

    public enum DateRange {
        DAY, WEEK, FORTNIGHT, MONTH
    }

    public enum Show {
        ALL, CAGE, SUMMARY, CHECKIN, CHECKOUT
    }

    /**
     * The container for the Dates label.
     */
    private final Component datesLabelContainer = new Row();

    /**
     * The container for the Dates selector.
     */
    private final Component datesContainer = new Row();

    /**
     * The container for the Show label.
     */
    private final Component showLabelContainer = new Row();

    /**
     * The container for the show selector.
     */
    private final Component showContainer = new Row();

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
     * The selected show type.
     */
    private Show show = Show.ALL;

    /**
     * The show selector.
     */
    private SelectField showSelector;

    /**
     * Constructs an {@link AppointmentQuery}.
     *
     * @param location the practice location. May be {@code null}
     * @param prefs    the user preferences
     */
    public AppointmentQuery(Party location, Preferences prefs) {
        super(ServiceHelper.getAppointmentService(), new AppointmentSchedules(location, prefs), prefs);
        String showPref = prefs.getString(PreferenceArchetypes.SCHEDULING, "show", Show.ALL.toString());
        try {
            show = Show.valueOf(showPref);
        } catch (Exception exception) {
            show = Show.ALL;
        }
    }

    /**
     * Sets the selected schedule view.
     *
     * @param view the schedule view
     */
    @Override
    public void setScheduleView(Entity view) {
        super.setScheduleView(view);
        updateDatesFilter();
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
     * Returns the selected show type.
     *
     * @return the show type
     */
    public Show getShow() {
        return show;
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
        timeSelector = createTimeSelector();

        String[] dwm = {Messages.get("workflow.scheduling.dates.day"),
                        Messages.get("workflow.scheduling.dates.week"),
                        Messages.get("workflow.scheduling.dates.fortnight"),
                        Messages.get("workflow.scheduling.dates.month")};
        dateSelector = SelectFieldFactory.create(dwm);
        dateSelector.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onDatesChanged();
            }
        });

        String[] show = {Messages.get("workflow.scheduling.show.all"),
                         Messages.get("workflow.scheduling.show.cage"),
                         Messages.get("workflow.scheduling.show.summary"),
                         Messages.get("workflow.scheduling.show.checkin"),
                         Messages.get("workflow.scheduling.show.checkout")
        };
        showSelector = SelectFieldFactory.create(show);
        showSelector.setSelectedIndex(this.show.ordinal());

        showSelector.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onShowChanged();
            }
        });

        container.add(LabelFactory.create("workflow.scheduling.time"));
        container.add(timeSelector);
        getFocusGroup().add(timeSelector);
        container.add(datesLabelContainer, 6);
        container.add(datesContainer, 7);
        container.add(showLabelContainer, 14);
        container.add(showContainer, 15);
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
     * Returns the default clinician.
     *
     * @return the default clinician, or {@code null} to indicate all clinicians.
     */
    @Override
    protected IMObjectReference getDefaultClinician() {
        return getPreferences().getReference(PreferenceArchetypes.SCHEDULING, "clinician", null);
    }

    /**
     * Returns the default highlight.
     *
     * @return the default highlight, or {@code null} if there is none
     */
    @Override
    protected Highlight getDefaultHighlight() {
        Highlight result = null;
        String highlight = getPreferences().getString(PreferenceArchetypes.SCHEDULING, "highlight", null);
        if (highlight != null) {
            try {
                result = Highlight.valueOf(highlight);
            } catch (IllegalArgumentException exception) {
                // do nothing
            }
        }
        return result;
    }

    /**
     * Invoked when the schedule view changes.
     * <p>
     * Notifies any listener to perform a query.
     */
    @Override
    protected void onViewChanged() {
        updateDatesFilter();
        super.onViewChanged();
    }

    /**
     * Invoked to update the view schedules.
     */
    @Override
    protected void updateViewSchedules() {
        super.updateViewSchedules();
        updateShowSelector();
    }

    /**
     * Invoked when the date changes.
     * <p>
     * This implementation invokes {@link #onQuery()}.
     */
    @Override
    protected void onDateChanged() {
        updateDays(getDate(), dateRange);
        super.onDateChanged();
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
        getPreferences().setPreference(PreferenceArchetypes.SCHEDULING, "dates", range.toString());
        onQuery();
    }

    /**
     * Creates a seletor for the time range.
     *
     * @return a new selector
     */
    private SelectField createTimeSelector() {
        // the order of the items must correspond to the order of TimeRange values.
        String[] timeSelectorItems = {
                Messages.get("workflow.scheduling.time.all"),
                Messages.get("workflow.scheduling.time.morning"),
                Messages.get("workflow.scheduling.time.afternoon"),
                Messages.get("workflow.scheduling.time.evening"),
                Messages.get("workflow.scheduling.time.AM"),
                Messages.get("workflow.scheduling.time.PM")};

        SelectField field = SelectFieldFactory.create(timeSelectorItems);
        String select = getPreferences().getString(PreferenceArchetypes.SCHEDULING, "time", "ALL");
        DateTime now = new DateTime();
        TimeRange range;
        switch (select) {
            case "AM_PM":
                range = TimeRange.getAMorPM(now);
                break;
            case "M_A_E":
                range = TimeRange.getMorningOrAfternoonOrEvening(now);
                break;
            default:
                range = TimeRange.ALL;
        }
        int index = range.ordinal();
        if (index > timeSelectorItems.length) {
            index = 0;
        }
        field.setSelectedItem(timeSelectorItems[index]);
        field.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });
        return field;
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
            case FORTNIGHT:
                setDateNavigator(DateNavigator.FORTNIGHT);
                break;
            case MONTH:
                setDateNavigator(DateNavigator.MONTH);
                break;
        }
        updateDays(getDate(), dateRange);
    }

    /**
     * Invoked when the show selector changes.
     */
    private void onShowChanged() {
        int index = showSelector.getSelectedIndex();
        if (index >= 0 && index < Show.values().length) {
            show = Show.values()[index];
            getPreferences().setPreference(PreferenceArchetypes.SCHEDULING, "show", show.toString());
            onQuery();
        }
    }

    /**
     * Invoked to update the show selector when the view changes.
     */
    private void updateShowSelector() {
        boolean hasCageType = false;
        if (show == Show.CAGE) {
            // if the schedule doesn't have any cages, don't enable the cage view by default
            for (Entity schedule : getSelectedSchedules()) {
                IMObjectBean bean = new IMObjectBean(schedule);
                if (bean.getNodeTargetObjectRef("cageType") != null) {
                    hasCageType = true;
                    break;
                }
            }
            if (!hasCageType) {
                show = Show.ALL;
                showSelector.setSelectedIndex(show.ordinal());
            }
        }
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
            case FORTNIGHT:
                days = 14;
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
            String datesPref = getPreferences().getString(PreferenceArchetypes.SCHEDULING, "dates",
                                                          DateRange.FORTNIGHT.toString());
            try {
                range = DateRange.valueOf(datesPref);
            } catch (Exception exception) {
                range = DateRange.FORTNIGHT;
            }

            if (datesContainer.getComponentCount() == 0) {
                datesLabelContainer.add(LabelFactory.create("workflow.scheduling.dates"));
                datesContainer.add(dateSelector);
            }
            if (showContainer.getComponentCount() == 0) {
                showLabelContainer.add(LabelFactory.create("workflow.scheduling.show"));
                showContainer.add(showSelector);
            }
        } else {
            range = DateRange.DAY;
            datesLabelContainer.removeAll();
            datesContainer.removeAll();
            showLabelContainer.removeAll();
            showContainer.removeAll();
        }
        setDateRange(range);
        dateSelector.setSelectedIndex(dateRange.ordinal());
    }
}
