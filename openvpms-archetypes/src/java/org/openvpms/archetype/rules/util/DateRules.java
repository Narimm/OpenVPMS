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
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Date rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DateRules {

    /**
     * Day units.
     */
    public static final String DAYS = "DAYS";

    /**
     * Week units.
     */
    public static final String WEEKS = "WEEKS";

    /**
     * Month units.
     */
    public static final String MONTHS = "MONTHS";

    /**
     * Year units.
     */
    public static final String YEARS = "YEARS";


    /**
     * Calculates a date given a start time, interval and the date units.
     *
     * @param startTime the start time
     * @param interval  the time interval. May be negative to calculate a date
     *                  in the past
     * @param units     the interval units
     * @return the date for a reminder
     */
    public static Date getDate(Date startTime, int interval, String units) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(startTime);
        if (units != null) {
            units = units.toUpperCase();
            if (YEARS.equals(units)) {
                calendar.add(Calendar.YEAR, interval);
            } else if (MONTHS.equals(units)) {
                calendar.add(Calendar.MONTH, interval);
            } else if (WEEKS.equals(units)) {
                calendar.add(Calendar.DAY_OF_YEAR, interval * 7);
            } else if (DAYS.equals(units)) {
                calendar.add(Calendar.DAY_OF_YEAR, interval);
            }
        }
        return calendar.getTime();
    }
}
