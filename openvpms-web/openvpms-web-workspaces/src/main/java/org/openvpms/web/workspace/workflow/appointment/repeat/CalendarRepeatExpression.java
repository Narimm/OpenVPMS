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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.repeat;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

/**
 * A {@link RepeatExpression} that calculates dates using calendar intervals.
 *
 * @author Tim Anderson
 */
public class CalendarRepeatExpression implements RepeatExpression {

    /**
     * The interval.
     */
    private final int interval;

    /**
     * The interval units.
     */
    private DateUnits units;

    /**
     * Constructs an {@link CalendarRepeatExpression}.
     *
     * @param interval the interval
     * @param units    the interval units
     */
    public CalendarRepeatExpression(int interval, DateUnits units) {
        this.interval = interval;
        this.units = units;
    }

    /**
     * Returns the interval.
     *
     * @return the interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Returns the interval units.
     *
     * @return the interval units
     */
    public DateUnits getUnits() {
        return units;
    }

    /**
     * Returns a string representation of this expression.
     *
     * @return a formatted string
     */
    @Override
    public String toString() {
        String result;
        if (interval == 1) {
            result = RepeatHelper.toString(getType());
        } else {
            StringBuilder buffer = new StringBuilder();
            buffer.append(Messages.get("workflow.scheduling.appointment.every"));
            buffer.append(" ");
            buffer.append(interval);
            buffer.append(" ");
            buffer.append(RepeatHelper.toString(units));
            result = buffer.toString();
        }
        return result;
    }

    /**
     * Returns the type of the expression.
     *
     * @return the type
     */
    @Override
    public Type getType() {
        if (interval == 1) {
            switch (units) {
                case DAYS:
                    return Type.DAILY;
                case WEEKS:
                    return Type.WEEKLY;
                case MONTHS:
                    return Type.MONTHLY;
                case YEARS:
                    return Type.YEARLY;
            }
        }
        return Type.CUSTOM;
    }

    /**
     * Returns the next repeat time after the specified time.
     *
     * @param time      the time
     * @param condition the condition to evaluate for each date
     * @return the next repeat time, or {@code null} if there are no more repeats, or the predicate returns
     *         {@code false}
     */
    @Override
    public Date getRepeatAfter(Date time, Predicate<Date> condition) {
        Date date = DateRules.getDate(time, interval, units);
        return condition.evaluate(date) ? date : null;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof CalendarRepeatExpression) {
            CalendarRepeatExpression other = (CalendarRepeatExpression) obj;
            return interval == other.interval && units == other.units;
        }
        return false;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        return builder.append(interval).append(units).hashCode();
    }

}
