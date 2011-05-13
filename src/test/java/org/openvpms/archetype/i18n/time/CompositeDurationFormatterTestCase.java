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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.archetype.i18n.time;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateUnits;
import static org.openvpms.archetype.test.TestHelper.getDate;

import java.util.Date;


/**
 * Tests the {@link CompositeDurationFormatter}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CompositeDurationFormatterTestCase {

    /**
     * Tests formatting when multiple formatters are registered to handle different durations.
     */
    @Test
    public void testFormat() {
        CompositeDurationFormatter formatter = new CompositeDurationFormatter();
        formatter.add(7, DateUnits.DAYS, new DateDurationFormatter(false, false, false, true)); // show days
        formatter.add(90, DateUnits.DAYS, new DateDurationFormatter(false, false, true, false)); // weeks
        formatter.add(1, DateUnits.YEARS, new DateDurationFormatter(false, true, false, false)); // months
        formatter.add(2, DateUnits.YEARS, new DateDurationFormatter(true, true, false, false)); // years, months

        Date from = getDate("2011-01-01");
        Date to1 = getDate("2011-01-07");
        Date to2 = getDate("2011-01-08");
        Date to3 = getDate("2011-01-09");
        Date to4 = getDate("2012-01-01");
        Date to5 = getDate("2013-02-01");
        checkFormat("6 Days", from, to1, formatter);
        checkFormat("7 Days", from, to2, formatter);
        checkFormat("1 Week", from, to3, formatter);
        checkFormat("12 Months", from, to4, formatter);
        checkFormat("2 Years 1 Month", from, to5, formatter);
    }

    /**
     * Verifies that a default formatter is registered that formats years.
     */
    @Test
    public void testDefault() {
        CompositeDurationFormatter formatter = new CompositeDurationFormatter();
        Date from = getDate("2008-01-01");
        Date to1 = getDate("2008-12-31");
        Date to2 = getDate("2009-01-01");
        Date to3 = getDate("2011-05-01");
        checkFormat("0 Years", from, to1, formatter);
        checkFormat("1 Year", from, to2, formatter);
        checkFormat("3 Years", from, to3, formatter);
    }

    /**
     * Verifies that the default formatter can be overridden.
     */
    @Test
    public void testDefaultOverride() {
        DurationFormatter override = new DateDurationFormatter(false, false, false, true);
        CompositeDurationFormatter formatter = new CompositeDurationFormatter();
        formatter.setDefaultFormatter(override);
        Date from = getDate("2008-01-01");
        Date to1 = getDate("2008-12-31");
        Date to2 = getDate("2009-01-01");
        Date to3 = getDate("2011-05-01");
        checkFormat("365 Days", from, to1, formatter);   // leap year
        checkFormat("366 Days", from, to2, formatter);
        checkFormat("1216 Days", from, to3, formatter);
    }


    /**
     * Verifies a format matches that expected.
     *
     * @param expected  the expected result
     * @param from      the from date
     * @param to        the to date
     * @param formatter the formatter to use
     */
    private void checkFormat(String expected, Date from, Date to, DurationFormatter formatter) {
        String result = formatter.format(from, to);
        assertEquals(expected, result);
    }
}
