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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.util;

import org.junit.Test;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Tests the {@link RelativeDateParser} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RelativeDateParserTestCase {

    /**
     * Tests the {@link RelativeDateParser#parse(String, Date)} method.
     */
    @Test
    public void testParse() {
        checkEquals("1d", 1, 0, 0, 0);
        checkEquals("1w", 0, 1, 0, 0);
        checkEquals("1m", 0, 0, 1, 0);
        checkEquals("1y", 0, 0, 0, 1);

        checkEquals("2D", 2, 0, 0, 0);
        checkEquals("3W", 0, 3, 0, 0);
        checkEquals("4M", 0, 0, 4, 0);
        checkEquals("5Y", 0, 0, 0, 5);

        checkEquals("-1d", -1, 0, 0, 0);
        checkEquals("-1w", 0, -1, 0, 0);
        checkEquals("-1m ", 0, 0, -1, 0);
        checkEquals(" -1y", 0, 0, 0, -1);

        checkEquals("1d 2w 3m 4y ", 1, 2, 3, 4);

        checkEquals(" -30D-52W-12M-100Y", -30, -52, -12, -100);

        // verify 6m is treated as -6m due to leading minus
        checkEquals("-3y6m", 0, 0, -6, -3);
        checkEquals("-3y 6m", 0, 0, -6, -3);
        checkEquals("-3y-6m", 0, 0, -6, -3);
    }

    /**
     * Tests the {@link RelativeDateParser#parse(String, Date)} method when
     * supplied with invalid strings.
     */
    @Test
    public void testInvalidParse() {
        RelativeDateParser parser = new RelativeDateParser();
        assertNull(parser.parse(null));
        assertNull(parser.parse(""));
        assertNull(parser.parse("  "));
        assertNull(parser.parse("-"));
        assertNull(parser.parse("-1"));
        assertNull(parser.parse(" d"));
        assertNull(parser.parse("-1dy"));
        assertNull(parser.parse("-1z"));
        assertNull(parser.parse("1d1z"));
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
        RelativeDateParser parser = new RelativeDateParser();
        Date date = new Date();
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
