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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;

import java.util.Date;

/**
 * Used by {@link DateSelector} to navigate to the next/previous date.
 *
 * @author Tim Anderson
 */
public class DateNavigator {

    /**
     * Next/previous navigate +1/-1 day, Next/previous term navigates +1/-1 week.
     */
    public static final DateNavigator DAY = new DateNavigator();

    /**
     * Next/previous navigate +1/-1 day, Next/previous term navigates +1/-1 week.
     */
    public static final DateNavigator WEEK = DAY;

    /**
     * Next/previous navigate +1/-1 day, Next/previous term navigates +1/-1 week.
     */
    public static final DateNavigator FORTNIGHT = DAY;

    /**
     * Next/previous navigate +1/-1 day.<br/>
     * Next term navigates to the start of the next month.<br/>
     * Previous term navigates to the start of the month, or the previous month if at the start.
     */
    public static final DateNavigator MONTH = new DateNavigator() {

        @Override
        public Date getNextTerm(Date date) {
            return DateRules.getDate(DateRules.getMonthStart(date), 1, DateUnits.MONTHS);
        }

        @Override
        public Date getPreviousTerm(Date date) {
            Date result = DateRules.getMonthStart(date);
            if (DateRules.dateEquals(result, date)) {
                result = DateRules.getDate(result, -1, DateUnits.MONTHS);
            }
            return result;
        }
    };

    /**
     * Returns the current date.
     *
     * @return the current date
     */
    public Date getCurrent() {
        return DateRules.getToday();
    }

    /**
     * Returns the date to display, given a date.
     * <p/>
     * This can be used to for week views, to start on a particular day of the week for example.
     * <p/>
     * This implementation returns the date unchanged.
     *
     * @param date the date
     * @return the new date
     */
    public Date getDate(Date date) {
        return date;
    }

    /**
     * Returns the next date.
     *
     * @param date the starting date
     * @return the date after {@code date}
     */
    public Date getNext(Date date) {
        return DateRules.getNextDate(date);
    }

    /**
     * Returns the previous date.
     *
     * @param date the starting date
     * @return the date before {@code date}
     */
    public Date getPrevious(Date date) {
        return DateRules.getPreviousDate(date);
    }

    /**
     * Returns the next term.
     * <p/>
     * This implementation returns a week after the specified date.
     *
     * @param date the starting date
     * @return a week after {@code date}
     */
    public Date getNextTerm(Date date) {
        return DateRules.getDate(date, 1, DateUnits.WEEKS);
    }

    /**
     * Returns the next term.
     * <p/>
     * This implementation returns a week before the specified date.
     *
     * @param date the starting date
     * @return a week before {@code date}
     */
    public Date getPreviousTerm(Date date) {
        return DateRules.getDate(date, -1, DateUnits.WEEKS);
    }
}
