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

package org.openvpms.web.component.property;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.test.TestHelper;

import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link DateTimePropertyTransformer} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class DateTimePropertyTransformerTestCase {

    /**
     * Populates the property from a string, verifying that the transformer transforms the value appropriately.
     */
    @Test
    public void setTestDate() {
        SimpleProperty property = new SimpleProperty("date", Date.class);
        DateTimePropertyTransformer transformer = new DateTimePropertyTransformer(property);
        property.setTransformer(transformer);

        assertNull(property.getValue());

        // populate using a date
        Date date = TestHelper.getDate("1992-02-23");
        assertTrue(property.setValue(date));
        assertEquals(date, property.getValue());

        // populate using a date/time, and verify that any time component is preserved
        Date timestamp1 = TestHelper.getDatetime("1980-01-01 12:39:38");
        assertTrue(property.setValue(timestamp1));
        assertEquals(timestamp1, property.getValue());

        // populate using a date/time, this time flagging seconds to be removed
        Date timestamp2 = TestHelper.getDatetime("1980-01-01 12:39:40");
        transformer.setKeepSeconds(false);
        property.setValue(timestamp2);
        assertEquals(TestHelper.getDatetime("1980-01-01 12:39:00"), property.getValue());
    }

    /**
     * Populates the property from a string, verifying that the transformer transforms the value appropriately.
     */
    @Test
    public void testSetString() {
        Locale.setDefault(new Locale("en", "AU"));

        SimpleProperty property = new SimpleProperty("date", Date.class);
        property.setTransformer(new DateTimePropertyTransformer(property));
        assertNull(property.getValue());

        assertFalse(property.setValue("31/1/1970")); // can only set time portion using strings
        assertNull(property.getValue());

        assertTrue(property.setValue("10:30"));
        Date expected1 = TestHelper.getDatetime("1970-01-01 10:30:00");
        assertEquals(expected1, property.getValue());

        Date date = TestHelper.getDate("2010-01-01");
        assertTrue(property.setValue(date));                 // replaces the time
        assertEquals(date, property.getValue());

        assertTrue(property.setValue("12:30"));              // adds the time
        Date expected2 = TestHelper.getDatetime("2010-01-01 12:30:00");
        assertEquals(expected2, property.getValue());

        assertTrue(property.setValue(""));
        assertNull(property.getValue());
    }

    /**
     * Verifies that dates can be made to fall within a date range.
     */
    @Test
    public void testDateRange() {
        // create a date property with a restricted date range
        SimpleProperty property = new SimpleProperty("date", Date.class);
        Date min = TestHelper.getDate("2003-08-01");
        Date max = TestHelper.getDate("2003-09-01");
        property.setTransformer(new DateTimePropertyTransformer(property, min, max));

        // test population using dates
        assertFalse(property.setValue(TestHelper.getDatetime("2003-07-31 23:59:59")));
        assertTrue(property.setValue(min));
        assertFalse(property.setValue(TestHelper.getDate("2003-09-02")));
        assertTrue(property.setValue(max));

        // test population using strings
        assertFalse(property.setValue("10:30"));
        assertTrue(property.setValue("00:00"));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        Locale.setDefault(new Locale("en", "AU")); // ensure appropriate date format is used
    }
}