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

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link DateHelper} class.
 *
 * @author Tim Anderson
 */
public class DateHelperTestCase {

    /**
     * Tests the {@link DateHelper#between(Date, Date, Date)} method.
     */
    @Test
    public void testBetween() {
        Date date1 = getDate("2014-01-01");
        Date date2 = getDate("2014-01-02");
        Date date3 = getDate("2014-01-03");

        assertTrue(DateHelper.between(date2, date1, date3));
        assertFalse(DateHelper.between(date2, date3, date1)); // date3 > date1 - TODO

        assertFalse(DateHelper.between(date1, date2, date3));
        assertFalse(DateHelper.between(date1, date3, date2));
        assertFalse(DateHelper.between(date3, date1, date2));
        assertFalse(DateHelper.between(date3, date2, date1));

        assertTrue(DateHelper.between(date1, date1, date2));
        assertFalse(DateHelper.between(date1, date2, date1));  // date2 > date1 - TODO
        assertTrue(DateHelper.between(date1, date1, date3));
        assertFalse(DateHelper.between(date1, date3, date1));   // date3 > date1 - TODO

        assertTrue(DateHelper.between(date2, date2, date2));
        assertFalse(DateHelper.between(date2, date1, date1));
        assertFalse(DateHelper.between(date2, date3, date3));

        assertTrue(DateHelper.between(date2, null, null));
        assertTrue(DateHelper.between(date2, null, date3));
        assertTrue(DateHelper.between(date2, date1, null));

        assertFalse(DateHelper.between(date2, date3, null));
        assertFalse(DateHelper.between(date2, null, date1));
    }

    /**
     * Tests the {@link DateHelper#intersects(Date, Date, Date, Date)} method.
     */
    @Test
    public void testIntersects() {
        // range1 before range2
        checkIntersects(false, "2014-01-01", "2014-01-10", "2014-01-10", "2014-01-20");
        checkIntersects(false, "2014-01-01", "2014-01-10", "2014-01-11", "2014-01-20");
        checkIntersects(false, "2014-01-01", "2014-01-10", "2014-01-10", null);
        checkIntersects(false, null, "2014-01-10", "2014-01-10", "2014-01-20");
        checkIntersects(false, null, "2014-01-10", "2014-01-11", "2014-01-20");
        checkIntersects(false, null, "2014-01-10", "2014-01-10", null);
        checkIntersects(false, null, "2014-01-10", "2014-01-11", null);

        // range1 after range2
        checkIntersects(false, "2014-01-10", "2014-01-20", "2014-01-01", "2014-01-10");
        checkIntersects(false, "2014-01-11", "2014-01-20", "2014-01-01", "2014-01-10");
        checkIntersects(false, "2014-01-10", null, "2014-01-01", "2014-01-10");
        checkIntersects(false, "2014-01-11", null, "2014-01-01", "2014-01-10");
        checkIntersects(false, "2014-01-10", "2014-01-20", null, "2014-01-10");
        checkIntersects(false, "2014-01-10", null, null, "2014-01-10");
        checkIntersects(false, "2014-01-11", null, null, "2014-01-10");

        // range1 overlaps start of range2
        checkIntersects(true, "2014-01-01", "2014-01-11", "2014-01-10", "2014-01-20");

        // range1 overlaps end of range2
        checkIntersects(true, "2014-01-09", "2014-01-20", "2014-01-01", "2014-01-10");

        // range1 == range2
        checkIntersects(true, "2014-01-01", "2014-01-20", "2014-01-01", "2014-01-20");

        // range1 within range2
        checkIntersects(true, "2014-01-05", "2014-01-06", "2014-01-01", "2014-01-20");
        checkIntersects(true, "2014-01-01", "2014-01-05", "2014-01-01", "2014-01-20");
        checkIntersects(true, "2014-01-05", "2014-01-20", "2014-01-01", "2014-01-20");

        // range2 within range1
        checkIntersects(true, "2014-01-01", "2014-01-20", "2014-01-05", "2014-01-06");
        checkIntersects(true, "2014-01-01", "2014-01-20", "2014-01-01", "2014-01-05");
        checkIntersects(true, "2014-01-01", "2014-01-20", "2014-01-05", "2014-01-20");
    }

    /**
     * Verifies that an unbounded date range intersects everything.
     */
    @Test
    public void testIntersectsForUnboundedDateRange() {
        checkIntersects(true, null, null, null, null);
        checkIntersects(true, null, null, "2014-01-01", null);
        checkIntersects(true, null, null, "2014-01-01", "2014-01-31");
        checkIntersects(true, null, null, null, "2014-01-31");
        checkIntersects(true, "2014-01-01", null, null, null);
        checkIntersects(true, "2014-01-01", "2014-01-31", null, null);
        checkIntersects(true, null, "2014-01-31", null, null);
    }

    /**
     * Tests the {@link DateHelper#intersects(Date, Date, Date, Date)} method.
     *
     * @param intersects the expected result
     * @param from1      the start of the first date range. May be {@code null}
     * @param to1        the end of the first date range. May be {@code null}
     * @param from2      the start of the second date range. May be {@code null}
     * @param to2        the end of the second date range. May be {@code null}
     */
    private void checkIntersects(boolean intersects, String from1, String to1, String from2, String to2) {
        assertEquals(intersects, DateHelper.intersects(getDate(from1), getDate(to1), getDate(from2), getDate(to2)));
    }

    /**
     * Converts a string to date.
     *
     * @param date the date string. May be {@code null}
     * @return the converted date. May be {@code null}
     */
    private static Date getDate(String date) {
        return date != null ? java.sql.Date.valueOf(date) : null;
    }
}
