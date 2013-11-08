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

package org.openvpms.archetype.rules.util;

import org.apache.commons.lang.time.DateUtils;
import org.openvpms.component.system.common.util.DateHelper;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Date rules.
 *
 * @author Tim Anderson
 */
public class DateRules {

    /**
     * Calculates a date given a start time, interval and the date units.
     *
     * @param startTime the start time
     * @param interval  the time interval. May be negative to calculate a date in the past
     * @param units     the interval units
     * @return the date
     */
    public static Date getDate(Date startTime, int interval, DateUnits units) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(startTime);
        if (units != null) {
            switch (units) {
                case YEARS:
                    calendar.add(Calendar.YEAR, interval);
                    break;
                case MONTHS:
                    calendar.add(Calendar.MONTH, interval);
                    break;
                case WEEKS:
                    calendar.add(Calendar.DAY_OF_YEAR, interval * 7);
                    break;
                case DAYS:
                    calendar.add(Calendar.DAY_OF_YEAR, interval);
                    break;
                case HOURS:
                    calendar.add(Calendar.HOUR_OF_DAY, interval);
                    break;
                case MINUTES:
                    calendar.add(Calendar.MINUTE, interval);
                    break;
            }
        }
        return calendar.getTime();
    }

    /**
     * Returns today's date, minus any time component.
     *
     * @return today's date
     */
    public static Date getToday() {
        return getDate(new Date());
    }

    /**
     * Returns tomorrow's date, minus any time component.
     *
     * @return tomorrow's date
     */
    public static Date getTomorrow() {
        return getNextDate(getToday());
    }

    /**
     * Returns yesterday's date, minus any time component.
     *
     * @return yesterday's date
     */
    public static Date getYesterday() {
        return getDate(getToday(), -1, DateUnits.DAYS);
    }

    /**
     * Returns the date part of a date-time, zero-ing out any time component.
     *
     * @param datetime the date/time. May be {@code null}
     * @return the date part of {@code datetime}, or {@code null} if {@code datetime} is null
     */
    public static Date getDate(Date datetime) {
        if (datetime == null) {
            return null;
        }
        return DateUtils.truncate(datetime, Calendar.DAY_OF_MONTH);
    }

    /**
     * Returns the next date given the supplied date, zero-ing out any time component.
     *
     * @param datetime the date/time. May be {@code null}
     * @return the date part of {@code datetime}, or {@code null} if {@code datetime} is null
     */
    public static Date getNextDate(Date datetime) {
        if (datetime == null) {
            return null;
        }
        return getDate(getDate(datetime), 1, DateUnits.DAYS);
    }

    /**
     * Helper to compare two dates.
     * <p/>
     * Null dates are treated as greater than non-null dates.
     * <p/>
     * This is functionally equivalent to the {@link Date#compareTo(Date)}
     * method, except that it doesn't throw {@code ClassCastExceptions}
     * if {@code lhs} is an instance of a {@link Timestamp Timestamp} and
     * {@code rhs} isn't.
     * <p/>
     * For timestamps, the nanoseconds are ignored.
     *
     * @param lhs the date. May be {@code null}
     * @param rhs the date to compare with. May be {@code null}
     * @return {@code 0} if the {@code lhs} is equal to {@code rhs};
     *         a value less than {@code 0} if {@code lhs} is before
     *         {@code rhs}; and a value greater than
     *         {@code 0} if {@code lhs} is after {@code rhs}.
     */
    public static int compareTo(Date lhs, Date rhs) {
        return compareDateTime(lhs, rhs, true);
    }

    /**
     * Helper to compare two dates.
     * <p/>
     * This is functionally equivalent to the {@link Date#compareTo(Date)}
     * method, except that it doesn't throw {@code ClassCastExceptions}
     * if {@code lhs} is an instance of a {@link Timestamp Timestamp} and
     * {@code rhs} isn't.
     * <p/>
     * For timestamps, the nanoseconds are ignored.
     *
     * @param lhs          the date
     * @param rhs          the date to compare with
     * @param ignoreMillis if {@code true}, ignore milliseconds
     * @return {@code 0} if the {@code lhs} is equal to {@code rhs};
     *         a value less than {@code 0} if {@code lhs} is before
     *         {@code rhs}; and a value greater than
     *         {@code 0} if {@code lhs} is after {@code rhs}.
     */
    public static int compareTo(Date lhs, Date rhs, boolean ignoreMillis) {
        if (ignoreMillis) {
            lhs = DateUtils.truncate(lhs, Calendar.SECOND);
            rhs = DateUtils.truncate(rhs, Calendar.SECOND);
        }
        return compareTo(lhs, rhs);
    }

    /**
     * Determines if two date ranges intersect.
     *
     * @param from1 the start of the first date range. May be {@code null}
     * @param to1   the end of the first date range. May be {@code null}
     * @param from2 the start of the second date range. May be {@code null}
     * @param to2   the end of the second date range. May be {@code null}
     * @return {@code true} if the date ranges intersect
     */
    public static boolean intersects(Date from1, Date to1, Date from2, Date to2) {
        return DateHelper.intersects(from1, to1, from2, to2);
    }

    /**
     * Determines if a date falls between two dates.
     *
     * @param date       the date to compare
     * @param lowerBound the from date. If {@code null}, indicates that the date is unbounded
     * @param upperBound the to date. If {@code null}, indicates that the date is unbounded
     * @return {@code true} if the date falls between the lower and upper bounds, otherwise {@code false}
     */
    public static boolean between(Date date, Date lowerBound, Date upperBound) {
        return DateHelper.between(date, lowerBound, upperBound);
    }

    /**
     * Determines if a date falls between two dates, inclusive.
     * <p/>
     * Any time component of the specified dates is ignored.
     *
     * @param date the date to compare
     * @param from the lower bound. If {@code null}, indicates there is no lower bound
     * @param to   the upper bound. If {@code null}, indicates there is no upper bound
     * @return {@code true} if the date falls between the two dates, inclusive; otherwise {@code false}
     */
    public static boolean betweenDates(Date date, Date from, Date to) {
        date = getDate(date);
        from = getDate(from);
        to = getDate(to);
        return (from == null || DateRules.compareTo(from, date) <= 0)
               && (to == null || DateRules.compareTo(to, date) >= 0);
    }

    /**
     * Adds a date and time.
     *
     * @param date the date part
     * @param time the time to add
     * @return the date+time
     */
    public static Date addDateTime(Date date, Date time) {
        GregorianCalendar dateCal = new GregorianCalendar();
        dateCal.setTime(date);
        GregorianCalendar timeCal = new GregorianCalendar();
        timeCal.setTime(time);

        dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        dateCal.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
        return dateCal.getTime();
    }

    /**
     * Compares the date portion of two date/times. Any time component is ignored.
     * <p/>
     * Null dates are treated as greater than non-null dates.
     *
     * @param d1 the first date/time. May be {@code null}
     * @param d2 the second date/time. May be {@code null}
     * @return the {@code 0} if {@code d1} is equal to this {@code d2};
     *         a value less than {@code 0} if {@code d1}  is before the {@code d2};
     *         and a value greater than {@code 0} if {@code d1} is after {@code d2}.
     */
    public static int compareDates(Date d1, Date d2) {
        return compareDate(d1, d2, true);
    }

    /**
     * Compares the date portion of two date/times. Any time component is ignored.
     *
     * @param d1       the first date/time. May be {@code null}
     * @param d2       the second date/time. May be {@code null}
     * @param nullHigh if {@code true} nulls are considered greater than any date, else they are lower
     * @return the {@code 0} if {@code d1} is equal to this {@code d2};
     *         a value less than {@code 0} if {@code d1}  is before the {@code d2};
     *         and a value greater than {@code 0} if {@code d1} is after {@code d2}.
     */
    public static int compareDate(Date d1, Date d2, boolean nullHigh) {
        d1 = getDate(d1);
        d2 = getDate(d2);
        return compareDateTime(d1, d2, nullHigh);
    }

    /**
     * Compares two date/times.
     *
     * @param d1       the first date/time. May be {@code null}
     * @param d2       the second date/time. May be {@code null}
     * @param nullHigh if {@code true} nulls are considered greater than any date, else they are lower
     * @return the {@code 0} if {@code d1} is equal to this {@code d2};
     *         a value less than {@code 0} if {@code d1}  is before the {@code d2};
     *         and a value greater than {@code 0} if {@code d1} is after {@code d2}.
     */
    public static int compareDateTime(Date d1, Date d2, boolean nullHigh) {
        if (d1 == null || d2 == null) {
            if (d1 == null && d2 == null) {
                return 0;
            } else if (d1 == null) {
                return nullHigh ? 1 : -1;
            } else {
                return nullHigh ? -1 : 1;
            }
        }
        return DateHelper.compareTo(d1, d2);
    }

    /**
     * Compares the date portion of a date with today's date. Any time component is ignored.
     *
     * @param date the date
     * @return the {@code 0} if {@code date} is equal to today's date;
     *         a value less than {@code 0} if {@code date} is before today's date;
     *         and a value greater than {@code 0} if {@code date} is after today's date
     */
    public static int compareDateToToday(Date date) {
        return getDate(date).compareTo(getToday());
    }

    /**
     * Determines if two dates are equal.
     * <p/>
     * This handles nulls and ignores any time component.
     *
     * @param date1 the first date. May be {@code null}
     * @param date2 the second date. May be {@code null}
     * @return {@code true} if the dates are equal
     */
    public static boolean dateEquals(Date date1, Date date2) {
        boolean result;
        if (date1 == null || date2 == null) {
            result = (date1 == null && date2 == null);
        } else {
            result = compareDates(date1, date2) == 0;
        }
        return result;
    }
}
