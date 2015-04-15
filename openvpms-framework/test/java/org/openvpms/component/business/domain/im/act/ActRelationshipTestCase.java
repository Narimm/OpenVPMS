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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.domain.im.act;

import org.junit.Test;
import org.openvpms.component.business.domain.im.common.PeriodRelationship;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ActRelationship} class.
 *
 * @author Tim Anderson
 */
public class ActRelationshipTestCase {

    /**
     * Tests the {@link PeriodRelationship#isActive()} method.
     */
    @Test
    public void testActive() {
        ActRelationship relationship = new ActRelationship();
        assertNull(relationship.getActiveStartTime());
        assertNull(relationship.getActiveEndTime());

        long now = System.currentTimeMillis();
        assertTrue(relationship.isActive());

        relationship.setActiveStartTime(new Date(now - 2000));
        assertTrue(relationship.isActive());

        relationship.setActiveEndTime(new Date(now - 1000));

        assertFalse(relationship.isActive());
    }

    /**
     * Tests the {@link PeriodRelationship#isActive(Date)} method.
     */
    @Test
    public void testIsActiveForDate() {
        Date date1 = getDate("2013-08-21");
        Date date2 = getDate("2013-08-22");
        Date date3 = getDate("2013-08-23");
        Date date4 = getDate("2013-08-24");

        ActRelationship relationship = new ActRelationship();
        assertNull(relationship.getActiveStartTime());
        assertNull(relationship.getActiveEndTime());

        assertTrue(relationship.isActive(date2));

        relationship.setActiveStartTime(date2);
        assertFalse(relationship.isActive(date1));
        assertTrue(relationship.isActive(date2));
        assertTrue(relationship.isActive(date3));

        relationship.setActiveEndTime(date3);
        assertFalse(relationship.isActive(date1));
        assertTrue(relationship.isActive(date2));
        assertTrue(relationship.isActive(date3));
        assertFalse(relationship.isActive(date4));
    }

    /**
     * Tests the {@link PeriodRelationship#isActive(Date, Date)} method.
     */
    @Test
    public void testIsActiveRange() {
        Date date1 = getDate("2013-08-21");
        Date date2 = getDate("2013-08-23");

        ActRelationship relationship = new ActRelationship();
        assertNull(relationship.getActiveStartTime());
        assertNull(relationship.getActiveEndTime());

        checkActive(true, relationship, (String) null, null);
        checkActive(true, relationship, date1, null);
        checkActive(true, relationship, null, date2);
        checkActive(true, relationship, date1, date2);

        relationship.setActiveStartTime(date1);
        checkActive(true, relationship, (Date) null, null);
        checkActive(true, relationship, date1, null);
        checkActive(true, relationship, null, date2);
        checkActive(true, relationship, date1, date2);
        checkActive(true, relationship, "2013-08-20", null);
        checkActive(false, relationship, null, "2013-08-21");
        checkActive(true, relationship, null, "2013-08-24");

        relationship.setActiveEndTime(date2);
        checkActive(true, relationship, (Date) null, null);
        checkActive(true, relationship, date1, null);
        checkActive(true, relationship, null, date2);
        checkActive(true, relationship, date1, date2);
        checkActive(true, relationship, "2013-08-20", null);
        checkActive(false, relationship, null, "2013-08-21");
        checkActive(true, relationship, null, "2013-08-24");
        checkActive(true, relationship, "2013-08-20", "2013-08-22"); // overlap start
        checkActive(true, relationship, "2013-08-22", "2013-08-24"); // overlap end
        checkActive(true, relationship, "2013-08-20", "2013-08-24"); // overlap both
        checkActive(false, relationship, "2013-08-20", "2013-08-21"); // completely before
        checkActive(false, relationship, "2013-08-23", "2013-08-24"); // completely after
    }

    /**
     * Verifies that a relationship is active/inactive for a date range.
     *
     * @param expected     the expected active status
     * @param relationship the relationship
     * @param from         the from date. May be {@code null}
     * @param to           the to date. May be {@code null}
     */
    private static void checkActive(boolean expected, ActRelationship relationship, String from, String to) {
        assertEquals(expected, relationship.isActive(getDate(from), getDate(to)));
    }

    /**
     * Verifies that a relationship is active/inactive for a date range.
     *
     * @param expected     the expected active status
     * @param relationship the relationship
     * @param from         the from date. May be {@code null}
     * @param to           the to date. May be {@code null}
     */
    private static void checkActive(boolean expected, ActRelationship relationship, Date from, Date to) {
        assertEquals(expected, relationship.isActive(from, to));
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
