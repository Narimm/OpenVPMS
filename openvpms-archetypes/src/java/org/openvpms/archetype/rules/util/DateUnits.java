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

package org.openvpms.archetype.rules.util;


import org.joda.time.Period;

/**
 * Date units.
 *
 * @author Tim Anderson
 */
public enum DateUnits {
    MINUTES, HOURS, DAYS, WEEKS, MONTHS, YEARS;

    /**
     * Converts a value in the units to a {@code Period}.
     *
     * @param value the value
     * @return a new period
     */
    public Period toPeriod(int value) {
        switch (this) {
            case MINUTES:
                return Period.minutes(value);
            case HOURS:
                return Period.hours(value);
            case DAYS:
                return Period.days(value);
            case WEEKS:
                return Period.weeks(value);
            case MONTHS:
                return Period.months(value);
            default:
                return Period.years(value);
        }
    }

    /**
     * Converts a string to a date unit, ignoring nulls.
     *
     * @param value the string value. May be {@code null}
     * @return the corresponding unit, or {@code null} if {@code value} is {@code null}
     */
    public static DateUnits fromString(String value) {
        return value != null ? valueOf(value) : null;
    }

    /**
     * Converts a string to a date unit, ignoring nulls.
     *
     * @param value        the string value. May be {@code null}
     * @param defaultValue the default value if {@code value} is {@code null}
     * @return the corresponding unit, or {@code defaultValue} if {@code value} is {@code null}
     */
    public static DateUnits fromString(String value, DateUnits defaultValue) {
        return (value != null) ? fromString(value) : defaultValue;
    }
}
