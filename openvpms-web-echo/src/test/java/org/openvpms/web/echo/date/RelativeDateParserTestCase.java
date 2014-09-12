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

package org.openvpms.web.echo.date;

import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * Tests the {@link RelativeDateParser} class.
 *
 * @author Tim Anderson
 * @author Tim Gething
 */
public class RelativeDateParserTestCase {

    /**
     * The parser.
     */
    private final RelativeDateParser parser = new RelativeDateParser();

    /**
     * Tests the {@link RelativeDateParser#parse(String, Date)} method.
     */
    @Test
    public void testParse() {
        checkEquals("1d", 1, 0, 0, 0);
        checkEquals("1w", 0, 1, 0, 0);
        checkEquals("1m", 0, 0, 1, 0);
        checkEquals("1y", 0, 0, 0, 1);
        checkEquals("1q", 0, 0, 3, 0);

        checkEquals("2D", 2, 0, 0, 0);
        checkEquals("3W", 0, 3, 0, 0);
        checkEquals("4M", 0, 0, 4, 0);
        checkEquals("5Y", 0, 0, 0, 5);
        checkEquals("3Q", 0, 0, 9, 0);

        checkEquals("-1d", -1, 0, 0, 0);
        checkEquals("-1w", 0, -1, 0, 0);
        checkEquals("-1m ", 0, 0, -1, 0);
        checkEquals(" -1y", 0, 0, 0, -1);
        checkEquals(" -1q", 0, 0, -3, 0);

        checkEquals("1d 2w 3m 4y ", 1, 2, 3, 4);

        checkEquals(" -30D-52W-12M-100Y", -30, -52, -12, -100);
        checkEquals(" -30D+52W-12M+100Y", -30, 52, -12, 100);

        // verify 6m is treated as -6m due to leading minus
        checkEquals("-3y6m", 0, 0, -6, -3);
        checkEquals("-3y 6m", 0, 0, -6, -3);
        checkEquals("-3y-6m", 0, 0, -6, -3);
        // verify +6m is treated as +6m in spite of leading minus
        checkEquals("-3y+6m", 0, 0, 6, -3);
    }

    /**
     * Verifies that date parsing works as expected for leap years.
     * <p/>
     * The results can vary if years are processed before months;.
     */
    @Test
    public void testLeapYear() {
        Date expected1 = TestHelper.getDate("2008-02-29");
        Date expected2 = TestHelper.getDate("2008-02-28");
        Date source = TestHelper.getDate("2011-08-30");

        RelativeDateParser parser = new RelativeDateParser();
        assertEquals(expected1, parser.parse("-3y 6m", source));
        assertEquals(expected2, parser.parse("-6m 3y", source));
    }

    /**
     * Verifies that start and end variants work.
     */
    @Test
    public void testStartEnd() {
        Date date = TestHelper.getDate("2014-08-25");
        String tests[][] = {
                {"0ds", "2014-08-25"},
                {"0de", "2014-08-25"},
                {"0ws", "2014-08-25"},
                {"0we", "2014-08-29"},
                {"0ms", "2014-08-01"},
                {"0me", "2014-08-31"},
                {"0qs", "2014-07-01"},
                {"0qe", "2014-09-30"},
                {"0ys", "2014-01-01"},
                {"0ye", "2014-12-31"},
                {"-3ws", "2014-08-04"},
                {"-3ms", "2014-05-01"},
                {"-1qs", "2014-04-01"},
                {"-2qs", "2014-01-01"},
                {"-3qs", "2013-10-01"},
                {"-4qs", "2013-07-01"},
                {"-3ys", "2011-01-01"},
                {"-4qs+2d", "2013-07-03"},
                {"-3ys+4d", "2011-01-05"},
                {"-3ys+4d+2m", "2011-03-05"},
        };

        for (String[] test : tests) {
            checkParse(test[1], date, test[0]);
        }
    }

    /**
     * Verifies processing of the "q", "qs", and "qe" modifiers.
     */
    @Test
    public void testQuarters() {
        // +0 quarter
        checkParse("2014-07-01", "2014-07-01", "0q");
        checkParse("2014-08-25", "2014-08-25", "0q");
        checkParse("2014-09-30", "2014-09-30", "0q");

        // -1 quarter
        checkParse("2014-04-01", "2014-07-01", "-1q");
        checkParse("2014-05-25", "2014-08-25", "-1q");
        checkParse("2014-06-30", "2014-09-30", "-1q");

        // -4 quarter
        checkParse("2013-07-01", "2014-07-01", "-4q");
        checkParse("2013-08-25", "2014-08-25", "-4q");
        checkParse("2013-09-30", "2014-09-30", "-4q");

        // +1 quarter
        checkParse("2014-10-01", "2014-07-01", "1q");
        checkParse("2014-11-25", "2014-08-25", "1q");
        checkParse("2014-12-30", "2014-09-30", "1q");

        // +4 quarter
        checkParse("2015-07-01", "2014-07-01", "4q");
        checkParse("2015-08-25", "2014-08-25", "4q");
        checkParse("2015-09-30", "2014-09-30", "4q");

        // current quarter start
        checkParse("2014-07-01", "2014-07-01", "0qs");
        checkParse("2014-07-01", "2014-08-25", "0qs");
        checkParse("2014-07-01", "2014-09-30", "0qs");

        // current quarter end
        checkParse("2014-09-30", "2014-07-01", "0qe");
        checkParse("2014-09-30", "2014-08-25", "0qe");
        checkParse("2014-09-30", "2014-09-30", "0qe");

        // -1 quarter start
        checkParse("2014-04-01", "2014-07-01", "-1qs");
        checkParse("2014-04-01", "2014-08-25", "-1qs");
        checkParse("2014-04-01", "2014-09-30", "-1qs");

        // -1 quarter end
        checkParse("2014-06-30", "2014-07-01", "-1qe");
        checkParse("2014-06-30", "2014-08-25", "-1qe");
        checkParse("2014-06-30", "2014-09-30", "-1qe");

        // -4 quarter start
        checkParse("2013-07-01", "2014-07-01", "-4qs");
        checkParse("2013-07-01", "2014-08-25", "-4qs");
        checkParse("2013-07-01", "2014-09-30", "-4qs");

        // -4 quarter end
        checkParse("2013-09-30", "2014-07-01", "-4qe");
        checkParse("2013-09-30", "2014-08-25", "-4qe");
        checkParse("2013-09-30", "2014-09-30", "-4qe");

        // +1 quarter start
        checkParse("2014-10-01", "2014-07-01", "1qs");
        checkParse("2014-10-01", "2014-08-25", "1qs");
        checkParse("2014-10-01", "2014-09-30", "1qs");

        // +1 quarter end
        checkParse("2014-12-31", "2014-07-01", "1qe");
        checkParse("2014-12-31", "2014-08-25", "1qe");
        checkParse("2014-12-31", "2014-09-30", "1qe");

        // +4 quarter start
        checkParse("2015-07-01", "2014-07-01", "4qs");
        checkParse("2015-07-01", "2014-08-25", "4qs");
        checkParse("2015-07-01", "2014-09-30", "4qs");

        // +4 quarter end
        checkParse("2015-09-30", "2014-07-01", "4qe");
        checkParse("2015-09-30", "2014-08-25", "4qe");
        checkParse("2015-09-30", "2014-09-30", "4qe");
    }

    /**
     * Tests the {@link RelativeDateParser#parse(String, Date)} method when
     * supplied with invalid strings.
     */
    @Test
    public void testInvalidParse() {
        assertNull(parser.parse(null));
        assertNull(parser.parse(""));
        assertNull(parser.parse("  "));
        assertNull(parser.parse("-"));
        assertNull(parser.parse("-1"));
        assertNull(parser.parse(" d"));
        assertNull(parser.parse("-1dy"));
        assertNull(parser.parse("-1z"));
        assertNull(parser.parse("1d1z"));
        assertNull(parser.parse("1qw"));
    }

    /**
     * Parses a string relative to date, and verifies the result matches that expected.
     *
     * @param expected the expected date string
     * @param date     the date to parse relative to
     * @param source   the string to parse
     */
    private void checkParse(String expected, String date, String source) {
        checkParse(expected, TestHelper.getDate(date), source);
    }

    /**
     * Parses a string relative to date, and verifies the result matches that expected.
     *
     * @param expected the expected date string
     * @param date     the date to parse relative to
     * @param source   the string to parse
     */
    private void checkParse(String expected, Date date, String source) {
        Date value = parser.parse(source, date);
        assertEquals("Failure for " + source, TestHelper.getDate(expected), value);
    }

    /**
     * Tests the {@link RelativeDateParser#parse(String, Date)} method.
     *
     * @param source the string to parse
     * @param day    the expected day change
     * @param week   the expected week change
     * @param month  the expected month change
     * @param year   the expected year change
     */
    private void checkEquals(String source, int day, int week,
                             int month, int year) {
        Date date = TestHelper.getDate("2011-04-10");
        Date relative = parser.parse(source, date);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, day);
        calendar.add(Calendar.WEEK_OF_YEAR, week);
        calendar.add(Calendar.MONTH, month);
        calendar.add(Calendar.YEAR, year);
        assertEquals(relative, calendar.getTime());
    }
}
