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

package org.openvpms.component.system.common.util;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Date helper methods.
 *
 * @author Tim Anderson
 */
public class DateHelper {

    /**
     * Determines if a time is between two dates.
     * <ul>
     * <li>if both {@code from} and {@code to} are non-null, {@code time} is between
     * if {@code from <= time and to >= time}.</li>
     * <li>if {@code from} is {@code null}, then {@code time} is between if {@code to >= time}</li>
     * <li>if {@code to} is {@code null}, then {@code time} is between if {@code from <= time}</li>
     * <li>if {@code from} and {@code to} are both {@code null}, this always returns {@code true}</li>
     * </ul>
     * <p/>
     * NOTE: if both {@code from} and {@code to} are specified, {@code to} must be {@code >= from}
     *
     * @param time the time
     * @param from the from date. If {@code null}, indicates that the date is unbounded
     * @param to   the to date. If {@code null}, indicates that the date is unbounded
     * @return {@code true} if the date falls between {@code from} and {@code to}, otherwise {@code false}
     */
    public static boolean between(Date time, Date from, Date to) {
        return (from == null || compareTo(from, time) <= 0) && (to == null || compareTo(to, time) >= 0);
    }

    /**
     * Determines if two date ranges intersect.
     * <p/>
     * NOTE: if both {@code fromN} and {@code toN} are specified, {@code toN} must be {@code >= fromN}
     *
     * @param from1 the start of the first date range. May be {@code null}
     * @param to1   the end of the first date range. May be {@code null}
     * @param from2 the start of the second date range. May be {@code null}
     * @param to2   the end of the second date range. May be {@code null}
     * @return {@code true} if the date ranges intersect
     */
    public static boolean intersects(Date from1, Date to1, Date from2, Date to2) {
        if (from1 == null && to1 == null) {
            return true;
        } else if (from1 == null) {
            return from2 == null || compareTo(to1, from2) > 0;
        } else if (to1 == null) {
            return to2 == null || compareTo(from1, to2) < 0;
        } else if (from2 == null && to2 == null) {
            return true;
        } else if (from2 == null) {
            return compareTo(from1, to2) < 0;
        } else if (to2 == null) {
            return compareTo(to1, from2) > 0;
        }
        return compareTo(from2, to1) < 0 && compareTo(to2, from1) > 0;
    }

    /**
     * Helper to compare two dates.
     * <p/>
     * This is functionally equivalent to the {@link Date#compareTo(Date)} method, except that it doesn't throw
     * {@code ClassCastExceptions} if {@code lhs} is an instance of a {@link Timestamp Timestamp} and {@code rhs} isn't.
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

}
