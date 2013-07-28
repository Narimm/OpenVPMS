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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.system.common.jxpath;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Date formatting functions for use in xpath expressions.
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
     * The thread-specific locale to use to format dates.
     */
    private static ThreadLocal<Locale> threadLocale = new ThreadLocal<Locale>();

    /**
     * The thread-specific time zone to use to format dates.
     */
    private static ThreadLocal<TimeZone> threadZone = new ThreadLocal<TimeZone>();

    /**
     * Formats a date according to a {@code SimpleDateFormat} pattern.
     *
     * @param date    the date. May be {@code null}
     * @param pattern the format pattern. May be {@code null}
     * @return the formatted date, or {@code null} if the date is null
     */
    public static String format(Date date, String pattern) {
        String result = null;
        if (date != null) {
            if (pattern == null) {
                result = formatDateTime(date);
            } else {
                DateFormat format = new SimpleDateFormat(pattern, getLocale());
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
     * @return the formatted date string, or <code>null</code> if no date was
     *         passed
     */
    public static String formatDate(Date date) {
        return formatDate(date, MEDIUM);
    }

    /**
     * Returns a formatted string for a date, using the locale associated
     * with the current thread, or the system default if none is specified.
     *
     * @param date  the date
     * @param style the date style. May be "short", "medium", or "long"
     * @return the formatted date string, or <code>null</code> if no date was
     *         passed
     */
    public static String formatDate(Date date, String style) {
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
     * @return the formatted time string or <code>null</code> if no time was
     *         passed
     */
    public static String formatTime(Date time) {
        return formatTime(time, MEDIUM);
    }

    /**
     * Returns a formatted string for a time, using the locale associated
     * with the current thread, or the system default if none is specified.
     *
     * @param time  the time
     * @param style the time style. May be "short", "medium", or "long"
     * @return the formatted time string or <code>null</code> if no time was
     *         passed
     */
    public static String formatTime(Date time, String style) {
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
     * @return a formatted date/time string or <code>null</code> if no date/time
     *         was passed
     */
    public static String formatDateTime(Date dateTime) {
        return formatDateTime(dateTime, MEDIUM);
    }

    /**
     * Returns a formatted string for a date/time, using the locale associated
     * with the current thread, or the system default if none is specified.
     *
     * @param dateTime the date/time
     * @param style    the style. May be "short", "medium", or "long"
     * @return a formatted date/time string or <code>null</code> if no date/time
     *         was passed
     */
    public static String formatDateTime(Date dateTime, String style) {
        return formatDateTime(dateTime, style, style);
    }

    /**
     * Returns a formatted string for a date/time, using the locale associated
     * with the current thread, or the system default if none is specified.
     *
     * @param dateTime  the date/time
     * @param dateStyle the date style. May be "short", "medium", or "long"
     * @param timeStyle the time style. May be "short", "medium", or "long"
     * @return a formatted date/time string or <code>null</code> if no date/time
     *         was passed
     */
    public static String formatDateTime(Date dateTime, String dateStyle,
                                        String timeStyle) {
        if (dateTime != null) {
            DateFormat format = DateFormat.getDateTimeInstance(
                    getStyle(dateStyle), getStyle(timeStyle), getLocale());
            format.setTimeZone(getTimeZone());
            return format.format(dateTime);
        }
        return null;
    }

    /**
     * Sets the locale for the current thread.
     *
     * @param locale the locale. May be <code>null</code>
     */
    public static void setLocale(Locale locale) {
        threadLocale.set(locale);
    }

    /**
     * Sets the time zone for the current thread.
     *
     * @param zone the time zone. May be <tt>null</tt>
     */
    public static void setTimeZone(TimeZone zone) {
        threadZone.set(zone);
    }

    /**
     * Helper to convert a style string to the {@link DateFormat} equivalent.
     *
     * @param style the style. One of "short", "medium", or "long"
     * @return the date format style, or {@link DateFormat#LONG} if the style
     *         is invalid
     */
    private static int getStyle(String style) {
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

    /**
     * Returns the locale to use for formatting.
     *
     * @return the locale
     */
    private static Locale getLocale() {
        Locale locale = threadLocale.get();
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return locale;
    }

    /**
     * Returns the timezone to use for formatting.
     *
     * @return the time zone
     */
    private static TimeZone getTimeZone() {
        TimeZone zone = threadZone.get();
        if (zone == null) {
            zone = TimeZone.getDefault();
        }
        return zone;
    }

}
