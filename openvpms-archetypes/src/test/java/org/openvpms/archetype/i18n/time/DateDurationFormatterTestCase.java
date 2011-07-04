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
import static org.openvpms.archetype.test.TestHelper.getDate;

import java.util.Date;

/**
 * Tests {@link DateDurationFormatter} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class DateDurationFormatterTestCase {

    /**
     * Tests formatting where only the years are shown.
     */
    @Test
    public void testYears() {
        Date from = getDate("2008-01-01");
        Date to1 = getDate("2008-12-31");
        Date to2 = getDate("2009-01-01");
        Date to3 = getDate("2011-05-01");
        checkFormat("0 Years", from, from, true, false, false, false);
        checkFormat("0 Years", from, to1, true, false, false, false);
        checkFormat("1 Year", from, to2, true, false, false, false);
        checkFormat("3 Years", from, to3, true, false, false, false);
    }

    /**
     * Tests formatting where only the months are shown.
     */
    @Test
    public void testMonths() {
        Date from = getDate("2008-06-01");
        Date to1 = getDate("2008-12-31");
        Date to2 = getDate("2009-01-01");
        Date to3 = getDate("2010-06-01");
        checkFormat("0 Months", from, from, false, true, false, false);
        checkFormat("6 Months", from, to1, false, true, false, false);
        checkFormat("7 Months", from, to2, false, true, false, false);
        checkFormat("24 Months", from, to3, false, true, false, false);
    }

    /**
     * Tests formatting where only the weeks are shown, where the starting and ending weeks are in the same year.
     */
    @Test
    public void testWeeksForCurrentYear() {
        Date from = getDate("2011-01-01");
        Date to1 = getDate("2011-02-01");
        Date to2 = getDate("2011-03-01");
        Date to3 = getDate("2011-04-01");
        Date to4 = getDate("2011-05-01");
        checkFormat("0 Weeks", from, from, false, false, true, false);
        checkFormat("4 Weeks", from, to1, false, false, true, false);
        checkFormat("8 Weeks", from, to2, false, false, true, false);
        checkFormat("12 Weeks", from, to3, false, false, true, false);
        checkFormat("17 Weeks", from, to4, false, false, true, false);
    }

    /**
     * Tests formatting where only the weeks are shown, where the starting and ending weeks are in different years.
     */
    @Test
    public void testWeeksForPastYear() {
        Date from = getDate("2008-05-01");
        Date year1 = getDate("2009-05-01");
        Date year2 = getDate("2010-05-01");
        Date year3 = getDate("2011-05-01");
        Date to2 = getDate("2009-04-01");
        Date to3 = getDate("2010-04-01");
        checkFormat("52 Weeks", from, year1, false, false, true, false);
        checkFormat("52 Weeks", year1, year2, false, false, true, false);
        checkFormat("104 Weeks", from, year2, false, false, true, false);
        checkFormat("52 Weeks", year2, year3, false, false, true, false);
        checkFormat("156 Weeks", from, year3, false, false, true, false);
        checkFormat("47 Weeks", from, to2, false, false, true, false);
        checkFormat("100 Weeks", from, to3, false, false, true, false);
    }

    /**
     * Tests formatting where only the days are shown.
     */
    @Test
    public void testDays() {
        Date from = getDate("2008-05-01");
        Date date1 = getDate("2008-06-01");
        Date date2 = getDate("2009-05-01");
        Date date3 = getDate("2009-05-02");
        checkFormat("0 Days", from, from, false, false, false, true);
        checkFormat("31 Days", from, date1, false, false, false, true);
        checkFormat("365 Days", from, date2, false, false, false, true);
        checkFormat("366 Days", from, date3, false, false, false, true);
    }

    /**
     * Tests formatting where years and months are shown.
     */
    @Test
    public void testYearsMonths() {
        Date from = getDate("2008-05-01");
        Date to1 = getDate("2009-06-01");
        Date to2 = getDate("2010-06-01");
        Date to3 = getDate("2010-04-01");
        checkFormat("0 Months", from, from, true, true, false, false);
        checkFormat("1 Year 1 Month", from, to1, true, true, false, false);
        checkFormat("2 Years 1 Month", from, to2, true, true, false, false);
        checkFormat("1 Year 11 Months", from, to3, true, true, false, false);
    }

    /**
     * Tests formatting where years, months and weeks are shown.
     */
    @Test
    public void testYearsMonthsWeeks() {
        Date from = getDate("2008-05-01");
        Date to1 = getDate("2009-06-08");
        Date to2 = getDate("2010-06-07");
        Date to3 = getDate("2010-04-22");
        checkFormat("0 Weeks", from, from, true, true, true, false);
        checkFormat("1 Year 1 Month 1 Week", from, to1, true, true, true, false);
        checkFormat("2 Years 1 Month", from, to2, true, true, true, false);
        checkFormat("1 Year 11 Months 3 Weeks", from, to3, true, true, true, false);
    }

    /**
     * Tests formatting where years, months, weeks and days are shown.
     */
    @Test
    public void testYearsMonthsWeeksDays() {
        Date from = getDate("2008-05-01");
        Date to1 = getDate("2009-06-08");
        Date to2 = getDate("2010-06-07");
        Date to3 = getDate("2010-04-22");
        checkFormat("0 Days", from, from, true, true, true, true);
        checkFormat("1 Year 1 Month 1 Week 1 Day", from, to1, true, true, true, true);
        checkFormat("2 Years 1 Month", from, to2, true, true, true, true);
        checkFormat("1 Year 11 Months 3 Weeks 1 Day", from, to3, true, true, true, true);
    }

    /**
     * Tests formatting where months, and weeks are shown.
     */
    @Test
    public void testMonthsWeeks() {
        Date from = getDate("2010-07-16");
        Date to1 = getDate("2011-07-03");
        Date to2 = getDate("2011-08-03");
        Date to3 = getDate("2011-08-16");
        Date to4 = getDate("2011-08-23");
        checkFormat("0 Weeks", from, from, false, true, true, false);
        checkFormat("11 Months 2 Weeks", from, to1, false, true, true, false);
        checkFormat("12 Months 2 Weeks", from, to2, false, true, true, false);
        checkFormat("13 Months", from, to3, false, true, true, false);
        checkFormat("13 Months 1 Week", from, to4, false, true, true, false);
    }

    /**
     * Tests formatting where months, weeks and days are shown.
     */
    @Test
    public void testMonthsWeeksDays() {
        Date from = getDate("2008-05-01");
        Date to1 = getDate("2009-06-08");
        Date to2 = getDate("2010-06-07");
        Date to3 = getDate("2010-04-22");
        checkFormat("0 Days", from, from, false, true, true, true);
        checkFormat("13 Months 1 Week 1 Day", from, to1, false, true, true, true);
        checkFormat("25 Months", from, to2, false, true, true, true);
        checkFormat("23 Months 3 Weeks 1 Day", from, to3, false, true, true, true);
    }

    /**
     * Tests behaviour when nothing is shown.
     */
    @Test
    public void testEmpty() {
        Date from = getDate("2008-01-01");
        checkFormat("", from, from, false, false, false, false);
    }

    /**
     * Verifies that a format matches that expected.
     *
     * @param expected   the expected result
     * @param from       the from date
     * @param to         the to date
     * @param showYears  determines if years are shown
     * @param showMonths determines if months are shown
     * @param showWeeks  determines if weeks are shown
     * @param showDays   determines if days are shown
     */
    private void checkFormat(String expected, Date from, Date to, boolean showYears, boolean showMonths,
                             boolean showWeeks, boolean showDays) {
        DateDurationFormatter formatter = new DateDurationFormatter(showYears, showMonths, showWeeks, showDays);
        String result = formatter.format(from, to);
        assertEquals(expected, result);
    }

}
