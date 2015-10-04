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

package org.openvpms.archetype.function.date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openvpms.archetype.function.date.DateFunctions.LONG;
import static org.openvpms.archetype.function.date.DateFunctions.MEDIUM;
import static org.openvpms.archetype.function.date.DateFunctions.SHORT;


/**
 * Tests the {@link DateFunctions} class.
 *
 * @author Tim Anderson
 */
public class DateFunctionsTestCase {

    /**
     * The functions with AU locale.
     */
    private DateFunctions functions;

    /**
     * The test date/time.
     */
    private Date dateTime;

    /**
     * The time zone.
     */
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    /**
     * Tests the {@link DateFunctions#add(Date, String)} method.
     */
    @Test
    public void testAdd() {
        assertEquals(DateRules.getDate(dateTime, 1, DateUnits.DAYS), functions.add(dateTime, "1d"));
        assertEquals(DateRules.getDate(dateTime, -1, DateUnits.DAYS), functions.add(dateTime, "-1d"));
        assertEquals(DateRules.getDate(dateTime, 1, DateUnits.WEEKS), functions.add(dateTime, "1w"));
        assertEquals(DateRules.getDate(dateTime, -1, DateUnits.WEEKS), functions.add(dateTime, "-1w"));

        // test nulls
        assertNull(functions.add(null, "1d"));
        assertNull(functions.add(dateTime, null));
        assertNull(functions.add(null, null));
    }

    /**
     * Tests the {@link DateFunctions#format(Date, String)} method.
     */
    @Test
    public void testFormat() {
        assertEquals("Sep 2006", functions.format(dateTime, "MMM yyyy"));
        assertEquals("September 2006", functions.format(dateTime, "MMMM yyyy"));

        assertEquals("5:54 PM", functions.format(dateTime, "h:mm a"));

        assertEquals("17:54 Sep 2006", functions.format(dateTime, "H:mm MMM yyyy"));

        // test null handling
        assertNull("September 2006", functions.format(null, null));
        assertNull("September 2006", functions.format(null, "MMM yyyy"));
        String value = functions.format(dateTime, null); // falls back to medium format when pattern is null
        assertTrue("20/09/2006 17:54:22".equals(value) || "20/09/2006 5:54:22 PM".equals(value));
    }

    /**
     * Tests the {@link DateFunctions#formatDate methods.
     */
    @Test
    public void testFormatDate() {
        assertEquals("20/09/2006", functions.formatDate(dateTime));
        assertEquals("20/09/06", functions.formatDate(dateTime, SHORT));
        assertEquals("20/09/2006", functions.formatDate(dateTime, MEDIUM));
        assertEquals("20 September 2006", functions.formatDate(dateTime, LONG));

        // change the locale
        DateFunctions functions2 = new DateFunctions(Locale.UK, GMT);
        assertEquals("20-Sep-2006", functions2.formatDate(dateTime));
        assertEquals("20/09/06", functions2.formatDate(dateTime, SHORT));
        assertEquals("20-Sep-2006", functions2.formatDate(dateTime, MEDIUM));
        assertEquals("20 September 2006", functions2.formatDate(dateTime, LONG));
    }

    /**
     * Tests the {@link DateFunctions#formatTime} methods.
     * <p/>
     * TODO - this is a bit tedious, as different JDK versions have different formats for short, medium and long
     * time formats.
     */
    @Test
    public void testFormatTime() {
        String defaultValue = functions.formatTime(dateTime);
        assertTrue("17:54:22".equals(defaultValue) || "5:54:22 PM".equals(defaultValue));

        String shortValue = functions.formatTime(dateTime, SHORT);
        assertTrue("17:54".equals(shortValue) || "5:54 PM".equals(shortValue));

        String mediumValue = functions.formatTime(dateTime, MEDIUM);
        assertTrue("17:54:22".equals(mediumValue) || "5:54:22 PM".equals(mediumValue));

        String longValue = functions.formatTime(dateTime, LONG);
        assertTrue("17:54:22".equals(longValue) || "5:54:22 PM".equals(longValue));

        // override the local and time zone
        DateFunctions functions2 = new DateFunctions(Locale.UK, TimeZone.getTimeZone("GMT+4"));

        defaultValue = functions2.formatTime(dateTime);
        assertTrue("21:54:22".equals(defaultValue) || "9:54:22 PM".equals(defaultValue));

        shortValue = functions2.formatTime(dateTime, SHORT);
        assertTrue("21:54".equals(shortValue) || "5:54 PM".equals(shortValue));

        mediumValue = functions2.formatTime(dateTime, MEDIUM);
        assertTrue("21:54:22".equals(mediumValue) || "9:54:22 PM".equals(mediumValue));

        assertEquals("21:54:22 GMT+04:00", functions2.formatTime(dateTime, LONG));
    }

    /**
     * Tests the {@link DateFunctions#formatDateTime} methods.
     * <p/>
     * TODO - this is a bit tedious, as different JDK versions have different formats for short, medium and long
     * time formats.
     */
    @Test
    public void testFormatDateTime() {
        String defaultValue = functions.formatDateTime(dateTime);
        assertTrue("20/09/2006 17:54:22".equals(defaultValue) || "20/09/2006 5:54:22 PM".equals(defaultValue));

        String shortValue = functions.formatDateTime(dateTime, SHORT);
        assertTrue("20/09/06 17:54".equals(shortValue) || "20/09/06 5:54 PM".equals(shortValue));

        String mediumValue = functions.formatDateTime(dateTime, MEDIUM);
        assertTrue("20/09/2006 17:54:22".equals(mediumValue) || "20/09/2006 5:54:22 PM".equals(mediumValue));

        String longValue = functions.formatDateTime(dateTime, LONG);
        assertTrue("20 September 2006 17:54:22".equals(longValue) || "20 September 2006 5:54:22 PM".equals(longValue));

        shortValue = functions.formatDateTime(dateTime, SHORT, SHORT);
        assertTrue("20/09/06 17:54".equals(shortValue) || "20/09/06 5:54 PM".equals(shortValue));

        mediumValue = functions.formatDateTime(dateTime, MEDIUM, MEDIUM);
        assertTrue("20/09/2006 17:54:22".equals(mediumValue) || "20/09/2006 5:54:22 PM".equals(mediumValue));

        longValue = functions.formatDateTime(dateTime, LONG, LONG);
        assertTrue("20 September 2006 17:54:22".equals(longValue) || "20 September 2006 5:54:22 PM".equals(longValue));

        // override the local and time zone
        DateFunctions functions2 = new DateFunctions(Locale.UK, GMT);
        assertEquals("20-Sep-2006 17:54:22", functions2.formatDateTime(dateTime));
        assertEquals("20/09/06 17:54", functions2.formatDateTime(dateTime, SHORT));
        assertEquals("20-Sep-2006 17:54:22", functions2.formatDateTime(dateTime, MEDIUM));
        assertEquals("20 September 2006 17:54:22 GMT", functions2.formatDateTime(dateTime, LONG));

        assertEquals("20/09/06 17:54", functions2.formatDateTime(dateTime, SHORT, SHORT));
        assertEquals("20-Sep-2006 17:54:22", functions2.formatDateTime(dateTime, MEDIUM, MEDIUM));
        assertEquals("20 September 2006 17:54:22 GMT", functions2.formatDateTime(dateTime, LONG, LONG));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        // use Joda to set up a GMT based date/time to avoid GregorianCalendar timezone oddities
        final ISOChronology chronology = ISOChronology.getInstance(DateTimeZone.forTimeZone(GMT));
        DateTime time = new DateTime(2006, 9, 20, 17, 54, 22, 0, chronology);
        dateTime = time.toDate();

        final Locale locale = new Locale("en", "AU");
        functions = new DateFunctions(locale, GMT);
    }
}

