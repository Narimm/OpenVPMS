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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.date;

import org.openvpms.archetype.rules.util.DateRules;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Date functions for use in xpath expressions.
 *
 * @author Tim Anderson
 */
public class DateFunctions {

    /**
     * Short format style.
     */
    public static final String SHORT = "short";

    /**
     * Medium format style.
     */
    public static final String MEDIUM = "medium";

    /**
     * Long format style.
     */
    public static final String LONG = "long";

    /**
     * The locale.
     */
    private final Locale locale;

    /**
     * The timezone.
     */
    private final TimeZone zone;

    /**
     * The relative date parser.
     */
    private static final RelativeDateParser PARSER = new RelativeDateParser();

    /**
     * Default constructor.
     * <p/>
     * This uses the default locale and timezone.
     */
    public DateFunctions() {
        this(Locale.getDefault(), TimeZone.getDefault());
    }

    /**
     * Constructs a {@link DateFunctions}.
     *
     * @param locale the locale
     * @param zone   the time zone
     */
    public DateFunctions(Locale locale, TimeZone zone) {
        this.locale = locale;
        this.zone = zone;
    }

    /**
     * Returns the current date/time.
     *
     * @return the current date/time
     */
    public Date now() {
        return new Date();
    }

    /**
     * Returns today's date.
     *
     * @return today's date
     */
    public Date today() {
        return DateRules.getToday();
    }

    /**
     * Returns tomorrow's date.
     *
     * @return tomorrow's date
     */
    public Date tomorrow() {
        return DateRules.getTomorrow();
    }

    /**
     * Returns yesterday's date.
     *
     * @return yesterday's date
     */
    public Date yesterday() {
        return DateRules.getYesterday();
    }

    /**
     * Adds a period to the specified date.
     * <p/>
     * The period must conform to the format specified by {@link RelativeDateParser}.
     *
     * @param date   the date. If {@code null}, the current date/time is used
     * @param period the relative date string
     * @return the relative date, or {@code null} if the {@code date} is {@code null} or {@code period} is invalid
     */
    public Date add(Date date, String period) {
        Date result = null;
        if (date != null && period != null) {
            result = PARSER.parse(period, date);
        }
        return result;
    }

    /**
     * Formats a date according to a {@code SimpleDateFormat} pattern.
     *
     * @param date    the date. May be {@code null}
     * @param pattern the format pattern. May be {@code null}
     * @return the formatted date, or {@code null} if the date is null
     */
    public String format(Date date, String pattern) {
        String result = null;
        if (date != null) {
            if (pattern == null) {
                result = formatDateTime(date);
            } else {
                DateFormat format = new SimpleDateFormat(pattern, getLocale());
                format.setTimeZone(getTimeZone());
                result = format.format(date);
            }
        }
        return result;
    }

    /**
     * Returns a formatted string for a date, using the locale associated
     * with the current thread, or the system default if none is specified.
     *
     * @param date the date
     * @return the formatted date string, or {@code null} if no date was passed
     */
    public String formatDate(Date date) {
        return formatDate(date, MEDIUM);
    }

    /**
     * Returns a formatted string for a date, using the locale associated
     * with the current thread, or the system default if none is specified.
     *
     * @param date  the date
     * @param style the date style. May be {@link #SHORT}, {@link #MEDIUM} or {@link #LONG}
     * @return the formatted date string, or {@code null} if no date was passed
     */
    public String formatDate(Date date, String style) {
        if (date != null) {
            int type = getStyle(style);
            DateFormat format = DateFormat.getDateInstance(type, getLocale());
            format.setTimeZone(getTimeZone());
            return format.format(date);
        }
        return null;
    }

    /**
     * Returns a formatted string for a time, using the locale associated
     * with the current thread, or the system default if none is specified.
     *
     * @param time the time
     * @return the formatted time string or {@code null} if no time was passed
     */
    public String formatTime(Date time) {
        return formatTime(time, MEDIUM);
    }

    /**
     * Returns a formatted string for a time, using the locale associated
     * with the current thread, or the system default if none is specified.
     *
     * @param time  the time
     * @param style the time style. May be {@link #SHORT}, {@link #MEDIUM} or {@link #LONG}
     * @return the formatted time string or {@code null} if no time was passed
     */
    public String formatTime(Date time, String style) {
        if (time != null) {
            int type = getStyle(style);
            DateFormat format = DateFormat.getTimeInstance(type, getLocale());
            format.setTimeZone(getTimeZone());
            return format.format(time);
        }
        return null;
    }

    /**
     * Returns a formatted string for a date/time, using the locale associated
     * with the current thread, or the system default if none is specified.
     *
     * @param dateTime the date/time
     * @return a formatted date/time string or {@code null} if no date/time was passed
     */
    public String formatDateTime(Date dateTime) {
        return formatDateTime(dateTime, MEDIUM);
    }

    /**
     * Returns a formatted string for a date/time, using the locale associated
     * with the current thread, or the system default if none is specified.
     *
     * @param dateTime the date/time
     * @param style    the style. May be {@link #SHORT}, {@link #MEDIUM}, or {@link #LONG}
     * @return a formatted date/time string or {@code null} if no date/time was passed
     */
    public String formatDateTime(Date dateTime, String style) {
        return formatDateTime(dateTime, style, style);
    }

    /**
     * Returns a formatted string for a date/time, using the locale associated
     * with the current thread, or the system default if none is specified.
     *
     * @param dateTime  the date/time
     * @param dateStyle the date style. May be {@link #SHORT}, {@link #MEDIUM}, or {@link #LONG}
     * @param timeStyle the time style. May be {@link #SHORT}, {@link #MEDIUM}, or {@link #LONG}
     * @return a formatted date/time string or {@code null} if no date/time was passed
     */
    public String formatDateTime(Date dateTime, String dateStyle, String timeStyle) {
        if (dateTime != null) {
            DateFormat format = DateFormat.getDateTimeInstance(getStyle(dateStyle), getStyle(timeStyle), getLocale());
            format.setTimeZone(getTimeZone());
            return format.format(dateTime);
        }
        return null;
    }

    /**
     * Returns the timezone.
     *
     * @return the time zone
     */
    protected TimeZone getTimeZone() {
        return zone;
    }

    /**
     * Returns the locale.
     *
     * @return the locale
     */
    protected Locale getLocale() {
        return locale;
    }

    /**
     * Helper to convert a style string to the {@link DateFormat} equivalent.
     *
     * @param style the style. One of {@link #SHORT}, {@link #MEDIUM}, or {@link #LONG}
     * @return the date format style, or {@link #LONG} if the style is invalid
     */
    private int getStyle(String style) {
        int result;
        if (SHORT.equals(style)) {
            result = DateFormat.SHORT;
        } else if (MEDIUM.equals(style)) {
            result = DateFormat.MEDIUM;
        } else {
            result = DateFormat.LONG;
        }
        return result;
    }

}