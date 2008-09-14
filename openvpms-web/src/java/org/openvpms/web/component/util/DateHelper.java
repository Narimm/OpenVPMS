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

package org.openvpms.web.component.util;

import nextapp.echo2.app.ApplicationInstance;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.openvpms.web.resource.util.Messages;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;


/**
 * Date helper methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DateHelper {

    /**
     * Date edit pattern.
     */
    private static final String DATE_EDIT_PATTERN;

    /**
     * Date view pattern.
     */
    private static final String DATE_VIEW_PATTERN;

    /**
     * Full date pattern.
     */
    private static final String FULL_DATE_PATTERN;

    /**
     * Time edit pattern.
     */
    private static final String TIME_EDIT_PATTERN;

    /**
     * Time view pattern.
     */
    private static final String TIME_VIEW_PATTERN;

    /**
     * Date/time to generate a maximum width (in en locales).
     */
    private static final Date WIDE_DATE;


    /**
     * Compares the date portion of two date/times. Any time component is
     * ignored.
     *
     * @param d1 the first date/time
     * @param d2 the second date/time
     * @return the <tt>0</tt> if <tt>d1</tt> is equal to this <tt>d2</tt>;
     *         a value less than <tt>0</tt> if <tt>d1</tt>  is before the
     *         <tt>d2</tt>; and a value greater than <tt>0</tt> if
     *         <tt>d1</tt> is after <tt>d2</tt>.
     */
    public static int compareDates(Date d1, Date d2) {
        d1 = getDayMonthYear(d1);
        d2 = getDayMonthYear(d2);
        return d1.compareTo(d2);
    }

    /**
     * Returns the current date/time if the date falls on the
     * current date, otherwise returns the date unchanged.
     *
     * @param date the date
     * @return the current date/time if <tt>date</tt> falls on the current date.
     *         If not, returns <tt>date</tt> unchanged.
     */
    public static Date getDatetimeIfToday(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar d = Calendar.getInstance();
        d.setTime(date);
        if (now.get(Calendar.DAY_OF_YEAR) == d.get(Calendar.DAY_OF_YEAR)
                && now.get(Calendar.YEAR) == d.get(Calendar.YEAR)) {
            return now.getTime();
        }
        return date;
    }

    /**
     * Format a date.
     *
     * @param date the date to format
     * @param edit if <code>true</code> format the number for editing
     * @return the formatted date
     */
    public static String formatDate(Date date, boolean edit) {
        return getDateFormat(edit).format(date);
    }

    /**
     * Returns a date format.
     *
     * @param edit if <code>true</code> return a format for editing otherwise
     *             return a format for viewing dates
     * @return a date format
     */
    public static DateFormat getDateFormat(boolean edit) {
        DateFormat format;
        Locale locale = ApplicationInstance.getActive().getLocale();
        String pattern = (edit) ? DATE_EDIT_PATTERN : DATE_VIEW_PATTERN;
        if (pattern == null) {
            if (edit) {
                // specify SHORT style when parsing, so that 2 digit years
                // are handled correctly
                format = DateFormat.getDateInstance(DateFormat.SHORT, locale);
            } else {
                format = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
            }
        } else {
            format = new SimpleDateFormat(pattern, locale);
        }
        return format;
    }

    /**
     * Returns the full date format for the current locale.
     * <p/>
     * This can be overridden by specifying the <em>date.format.full</em>
     * property in <tt>messages.properties</tt>.
     *
     * @return the full date format
     */
    public static DateFormat getFullDateFormat() {
        Locale locale = ApplicationInstance.getActive().getLocale();
        if (FULL_DATE_PATTERN != null) {
            return new SimpleDateFormat(FULL_DATE_PATTERN, locale);
        }
        return DateFormat.getDateInstance(DateFormat.FULL, locale);
    }

    /**
     * Returns the day/month/year part of a date-time.
     *
     * @param datetime the date/time
     * @return the day/month/year part of the date
     */
    public static Date getDayMonthYear(Date datetime) {
        return DateUtils.truncate(datetime, Calendar.DAY_OF_MONTH);
    }

    /**
     * Format a time.
     *
     * @param time the time to format
     * @param edit if <code>true</code> format the number for editing
     * @return the formatted date
     */
    public static String formatTime(Date time, boolean edit) {
        return getTimeFormat(edit).format(time);
    }

    /**
     * Returns a time format.
     *
     * @param edit if <code>true</code> return a format for editing otherwise
     *             return a format for viewing dates
     * @return a date format
     */
    public static DateFormat getTimeFormat(boolean edit) {
        DateFormat format;
        Locale locale = ApplicationInstance.getActive().getLocale();
        String pattern = (edit) ? TIME_EDIT_PATTERN : TIME_VIEW_PATTERN;
        if (pattern == null) {
            if (edit) {
                // specify SHORT style when parsing, so that 2 digit years
                // are handled correctly
                format = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
            } else {
                format = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale);
            }
        } else {
            format = new SimpleDateFormat(pattern, locale);
        }
        return format;
    }

    /**
     * Returns a date-time format.
     *
     * @param edit if <code>true</code> return a format for editing otherwise
     *             return a format for viewing date-times.
     * @return a date-time format
     */
    public static DateFormat getDateTimeFormat(boolean edit) {
        DateFormat format;
        Locale locale = ApplicationInstance.getActive().getLocale();
        if (edit) {
            // specify SHORT style for dates when parsing, so that 2 digit years
            // are handled correctly
            format = DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                                    DateFormat.SHORT, locale);
        } else {
            format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                                                    DateFormat.SHORT, locale);
        }
        return format;
    }

    /**
     * Format a date-time.
     *
     * @param dateTime the date-time to format
     * @param edit     if <code>true</code> format the date-time for editing
     * @return the formatted date
     */
    public static String formatDateTime(Date dateTime, boolean edit) {
        return getDateTimeFormat(edit).format(dateTime);
    }

    /**
     * Helper to determine the no. of characters to display a date format.
     *
     * @param format the format
     * @return the (approximate) no. of characters required to display a date
     *         in the format
     */
    public static int getLength(DateFormat format) {
        return format.format(WIDE_DATE).length();
    }

    /**
     * Formats a time difference in hours and minutes.
     *
     * @param from the start time
     * @param to   the end time
     * @return the difference between the time, in hours and minutes
     */
    public static String formatTimeDiff(Date from, Date to) {
        long diff = to.getTime() - from.getTime();
        if (diff < 0) {
            diff = 0;
        }
        return formatTime(diff);
    }

    /**
     * Format a time in hours and minutes.
     *
     * @param time the time in milliseconds
     * @return the time in hours and minutes
     */
    public static String formatTime(long time) {
        long hours = time / DateUtils.MILLIS_PER_HOUR;
        long mins = (time % DateUtils.MILLIS_PER_HOUR)
                / DateUtils.MILLIS_PER_MINUTE;
        return Messages.get("time.format.abs", hours, mins);
    }


    static {
        String dateEdit = Messages.get("date.format.edit", true);
        DATE_EDIT_PATTERN = (!StringUtils.isEmpty(dateEdit)) ? dateEdit : null;

        String dateView = Messages.get("date.format.view", true);
        DATE_VIEW_PATTERN = (!StringUtils.isEmpty(dateView)) ? dateView : null;

        String fullDate = Messages.get("date.format.full", true);
        FULL_DATE_PATTERN = (!StringUtils.isEmpty(fullDate)) ? fullDate : null;

        String timeEdit = Messages.get("time.format.edit", true);
        TIME_EDIT_PATTERN = (!StringUtils.isEmpty(timeEdit)) ? timeEdit : null;

        String timeView = Messages.get("time.format.view", true);
        TIME_VIEW_PATTERN = (!StringUtils.isEmpty(timeView)) ? timeView : null;

        Calendar calendar = new GregorianCalendar(2006, 12, 30, 12, 59, 59);
        WIDE_DATE = calendar.getTime();
    }

}
