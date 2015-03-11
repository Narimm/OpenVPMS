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

package org.openvpms.archetype.i18n.time;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * A {@link DurationFormatter} that formats date durations.
 *
 * @author Tim Anderson
 */
public class DateDurationFormatter implements DurationFormatter {

    /**
     * A duration formatter that formats durations as years.
     */
    public static final DurationFormatter YEAR;

    /**
     * A duration formatter that formats durations as months.
     */
    public static final DurationFormatter MONTH;

    /**
     * A duration formatter that formats durations as weeks.
     */
    public static final DurationFormatter WEEK;

    /**
     * A duration formatter that formats durations as days.
     */
    public static final DurationFormatter DAY;

    /**
     * Determines if years should be displayed.
     */
    private final boolean showYears;

    /**
     * Determines if months should be displayed.
     */
    private final boolean showMonths;

    /**
     * Determines if weeks should be displayed.
     */
    private final boolean showWeeks;

    /**
     * Determines if days should be displayed.
     */
    private final boolean showDays;

    /**
     * Determines if hours should be displayed.
     */
    private final boolean showHours;

    /**
     * Determines if minutes should be displayed.
     */
    private final boolean showMinutes;

    /**
     * The period formatter.
     */
    private final PeriodFormatter formatter;

    /**
     * Singular minute suffix.
     */
    private static String MINUTE_SUFFIX;

    /**
     * Plural minute suffix.
     */
    private static String MINUTES_SUFFIX;

    /**
     * Singular hour suffix.
     */
    private static String HOUR_SUFFIX;

    /**
     * Plural hour suffix.
     */
    private static String HOURS_SUFFIX;

    /**
     * Singular day suffix.
     */
    private static String DAY_SUFFIX;

    /**
     * Plural day suffix.
     */
    private static String DAYS_SUFFIX;

    /**
     * Singular week suffix.
     */
    private static String WEEK_SUFFIX;

    /**
     * Plural week suffix.
     */
    private static String WEEKS_SUFFIX;

    /**
     * Singular month suffix.
     */
    private static String MONTH_SUFFIX;

    /**
     * Plural month suffix.
     */
    private static String MONTHS_SUFFIX;

    /**
     * Singular year suffix.
     */
    private static String YEAR_SUFFIX;

    /**
     * Plural year suffix.
     */
    private static String YEARS_SUFFIX;

    static {
        ResourceBundle bundle = ResourceBundle.getBundle(DurationFormatter.class.getName(), Locale.getDefault());
        HOUR_SUFFIX = " " + bundle.getString("hour");
        HOURS_SUFFIX = " " + bundle.getString("hours");
        MINUTE_SUFFIX = " " + bundle.getString("minute");
        MINUTES_SUFFIX = " " + bundle.getString("minutes");
        DAY_SUFFIX = " " + bundle.getString("day");
        DAYS_SUFFIX = " " + bundle.getString("days");
        WEEK_SUFFIX = " " + bundle.getString("week");
        WEEKS_SUFFIX = " " + bundle.getString("weeks");
        MONTH_SUFFIX = " " + bundle.getString("month");
        MONTHS_SUFFIX = " " + bundle.getString("months");
        YEAR_SUFFIX = " " + bundle.getString("year");
        YEARS_SUFFIX = " " + bundle.getString("years");
        YEAR = new DateDurationFormatter(true, false, false, false, false, false);
        MONTH = new DateDurationFormatter(false, true, false, false, false, false);
        WEEK = new DateDurationFormatter(false, false, true, false, false, false);
        DAY = new DateDurationFormatter(false, false, false, true, false, false);
    }

    /**
     * Constructs a {@link DateDurationFormatter}.
     *
     * @param showYears  determines if years should be displayed
     * @param showMonths determines if months should be displayed
     * @param showWeeks  determines if weeks should be displayed
     * @param showDays   determines if days should be displayed
     */
    protected DateDurationFormatter(boolean showYears, boolean showMonths, boolean showWeeks, boolean showDays) {
        this(showYears, showMonths, showWeeks, showDays, false, false);
    }

    /**
     * Constructs a {@link DateDurationFormatter}.
     *
     * @param showYears   determines if years should be displayed
     * @param showMonths  determines if months should be displayed
     * @param showWeeks   determines if weeks should be displayed
     * @param showDays    determines if days should be displayed
     * @param showHours   determines if hours should be displayed
     * @param showMinutes determines if minutes should be displayed
     */
    protected DateDurationFormatter(boolean showYears, boolean showMonths, boolean showWeeks, boolean showDays,
                                    boolean showHours, boolean showMinutes) {
        this.showYears = showYears;
        this.showMonths = showMonths;
        this.showWeeks = showWeeks;
        this.showDays = showDays;
        this.showHours = showHours;
        this.showMinutes = showMinutes;

        PeriodFormatterBuilder builder = new PeriodFormatterBuilder();
        if (showYears) {
            builder = builder.appendYears().appendSuffix(YEAR_SUFFIX, YEARS_SUFFIX).appendSeparator(" ");
        }
        if (showMonths) {
            builder = builder.appendMonths().appendSuffix(MONTH_SUFFIX, MONTHS_SUFFIX).appendSeparator(" ");
        }
        if (showWeeks) {
            builder = builder.appendWeeks().appendSuffix(WEEK_SUFFIX, WEEKS_SUFFIX).appendSeparator(" ");
        }
        if (showDays) {
            builder = builder.appendDays().appendSuffix(DAY_SUFFIX, DAYS_SUFFIX).appendSeparator(" ");
        }
        if (showHours) {
            builder = builder.appendHours().appendSuffix(HOUR_SUFFIX, HOURS_SUFFIX).appendSeparator(" ");
        }
        if (showMinutes) {
            builder = builder.appendMinutes().appendSuffix(MINUTE_SUFFIX, MINUTES_SUFFIX).appendSeparator(" ");
        }

        formatter = builder.toFormatter();
    }

    /**
     * Creates a new duration formatter.
     *
     * @param showYears  determines if years should be displayed
     * @param showMonths determines if months should be displayed
     * @param showWeeks  determines if weeks should be displayed
     * @param showDays   determines if days should be displayed
     * @return a new formatter
     */
    public static DurationFormatter create(boolean showYears, boolean showMonths, boolean showWeeks, boolean showDays) {
        return create(showYears, showMonths, showWeeks, showDays, false, false);
    }

    /**
     * Creates a new duration formatter.
     *
     * @param showYears   determines if years should be displayed
     * @param showMonths  determines if months should be displayed
     * @param showWeeks   determines if weeks should be displayed
     * @param showDays    determines if days should be displayed
     * @param showHours   determines if hours should be displayed
     * @param showMinutes determines if minutes should be displayed
     * @return a new formatter
     */
    public static DurationFormatter create(boolean showYears, boolean showMonths, boolean showWeeks,
                                           boolean showDays, boolean showHours, boolean showMinutes) {
        if (showYears && !showMonths && !showWeeks && !showDays) {
            return YEAR;
        } else if (!showYears && showMonths && !showWeeks && !showDays) {
            return MONTH;
        } else if (!showYears && !showMonths && showWeeks && !showDays) {
            return WEEK;
        } else if (!showYears && !showMonths && !showWeeks && showDays) {
            return DAY;
        }
        return new DateDurationFormatter(showYears, showMonths, showWeeks, showDays, showHours, showMinutes);
    }

    /**
     * Formats the duration between two timestamps.
     *
     * @param from the starting time
     * @param to   the ending time
     * @return the formatted duration
     */
    public String format(Date from, Date to) {
        return format(new DateTime(from.getTime()), new DateTime(to.getTime()));
    }

    /**
     * Formats the duration between two timestamps.
     * <p/>
     * NOTE: this currently doesn't do anything sensible for from > to. Possible solution would be to simply
     * reverse the times, and then prepend a - between each field using  the
     *
     * @param from the starting time
     * @param to   the ending time
     * @return the formatted duration
     */
    protected String format(DateTime from, DateTime to) {
        int years = 0;
        int months = 0;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;

        DateTime start = from;
        if (showYears) {
            years = Years.yearsBetween(start, to).getYears();
            start = start.plusYears(years);
        }
        if (showMonths) {
            months = Months.monthsBetween(start, to).getMonths();
            start = start.plusMonths(months);
        }
        if (showWeeks) {
            weeks = Weeks.weeksBetween(start, to).getWeeks();
            start = start.plusWeeks(weeks);
        }
        if (showDays) {
            days = Days.daysBetween(start, to).getDays();
            start = start.plusDays(days);
        }
        if (showHours) {
            hours = Hours.hoursBetween(start, to).getHours();
            start = start.plusHours(hours);
        }
        if (showMinutes) {
            minutes = Minutes.minutesBetween(start, to).getMinutes();
        }

        Period period = new Period(years, months, weeks, days, hours, minutes, 0, 0);
        return formatter.print(period);
    }
}
