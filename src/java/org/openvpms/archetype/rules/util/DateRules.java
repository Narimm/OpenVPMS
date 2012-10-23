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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.util;

import org.apache.commons.lang.time.DateUtils;

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
        return DateRules.getDate(getToday(), 1, DateUnits.DAYS);
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
     * Helper to compare two dates.
     * <p/>
     * This is functionally equivalent to the {@link Date#compareTo(Date)}
     * method, except that it doesn't throw {@code ClassCastExceptions}
     * if {@code lhs} is an instance of a {@link Timestamp Timestamp} and
     * {@code rhs} isn't.
     * <p/>
     * For timestamps, the nanoseconds are ignored.
     *
     * @param lhs the date
     * @param rhs the date to compare with
     * @return {@code 0} if the {@code lhs} is equal to {@code rhs};
     *         a value less than {@code 0} if {@code lhs} is before
     *         {@code rhs}; and a value greater than
     *         {@code 0} if {@code lhs} is after {@code rhs}.
     */
    public static int compareTo(Date lhs, Date rhs) {
        long lhsTime = lhs.getTime();
        long rhsTime = rhs.getTime();
        return (lhsTime < rhsTime ? -1 : (lhsTime == rhsTime ? 0 : 1));
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
     * @param from1 the start of the first date range
     * @param to1   the end of the first date range
     * @param from2 the start of the second date range
     * @param to2   the end of the second date range
     * @return {@code true} if the date ranges intersect
     */
    public static boolean intersects(Date from1, Date to1,
                                     Date from2, Date to2) {
        int f1f2 = compareTo(from1, from2);
        int t1t2 = compareTo(to1, to2);
        return (f1f2 < 0 && compareTo(to1, from2) > 0)
               || (compareTo(from1, to2) < 0 && t1t2 > 0)
               || (f1f2 >= 0 && t1t2 <= 0);
    }

    /**
     * Determines if a date falls between two dates.
     *
     * @param date       the date to compare
     * @param lowerBound the lower bound
     * @param upperBound the upper bound
     * @return {@code true} if the date falls between the lower and upper bounds, otherwise {@code false}
     */
    public static boolean between(Date date, Date lowerBound, Date upperBound) {
        return (compareTo(date, lowerBound) >= 0 && compareTo(date, upperBound) <= 0);
    }
}
