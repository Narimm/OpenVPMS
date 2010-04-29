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

package org.openvpms.component.system.service.jxpath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.system.common.jxpath.DateFunctions;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Tests the {@link DateFunctions} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DateFunctionsTestCase {

    /**
     * The test date/time.
     */
    private Date dateTime;

    /**
     * The AU locale.
     */
    private static final Locale AU = new Locale("en", "AU");


    /**
     * Tests the {@link DateFunctions#formatDate methods.
     */
    @Test
    public void testFormatDate() {
        DateFunctions.setLocale(AU);
        assertEquals("20/09/2006", DateFunctions.formatDate(dateTime));
        assertEquals("20/09/06", DateFunctions.formatDate(dateTime, "short"));
        assertEquals("20/09/2006",
                     DateFunctions.formatDate(dateTime, "medium"));
        assertEquals("20 September 2006",
                     DateFunctions.formatDate(dateTime, "long"));

        // change the locale
        DateFunctions.setLocale(Locale.UK);
        assertEquals("20-Sep-2006", DateFunctions.formatDate(dateTime));
        assertEquals("20/09/06", DateFunctions.formatDate(dateTime, "short"));
        assertEquals("20-Sep-2006",
                     DateFunctions.formatDate(dateTime, "medium"));
        assertEquals("20 September 2006",
                     DateFunctions.formatDate(dateTime, "long"));
    }

    /**
     * Tests the {@link DateFunctions#formatTime} methods.
     * <p/>
     * TODO - this is a bit tedious, as different JDK versions have different formats for short, medium and long
     * time formats.
     */
    @Test
    public void testFormatTime() {
        DateFunctions.setLocale(AU);
        String defaultValue = DateFunctions.formatTime(dateTime);
        assertTrue("17:54:22".equals(defaultValue) || "5:54:22 PM".equals(defaultValue));

        String shortValue = DateFunctions.formatTime(dateTime, "short");
        assertTrue("17:54".equals(shortValue) || "5:54 PM".equals(shortValue));

        String mediumValue = DateFunctions.formatTime(dateTime, "medium");
        assertTrue("17:54:22".equals(mediumValue) || "5:54:22 PM".equals(mediumValue));

        String longValue = DateFunctions.formatTime(dateTime, "long");
        assertTrue("17:54:22".equals(longValue) || "5:54:22 PM".equals(longValue));

        // override the local and time zone
        DateFunctions.setLocale(Locale.UK);
        DateFunctions.setTimeZone(TimeZone.getTimeZone("GMT+4"));

        defaultValue = DateFunctions.formatTime(dateTime);
        assertTrue("21:54:22".equals(defaultValue) || "9:54:22 PM".equals(defaultValue));

        shortValue = DateFunctions.formatTime(dateTime, "short");
        assertTrue("21:54".equals(shortValue) || "5:54 PM".equals(shortValue));

        mediumValue = DateFunctions.formatTime(dateTime, "medium");
        assertTrue("21:54:22".equals(mediumValue) || "9:54:22 PM".equals(mediumValue));

        assertEquals("21:54:22 GMT+04:00", DateFunctions.formatTime(dateTime, "long"));
    }

    /**
     * Tests the {@link DateFunctions#formatDateTime} methods.
     * <p/>
     * TODO - this is a bit tedious, as different JDK versions have different formats for short, medium and long
     * time formats.
     */
    @Test
    public void testFormatDateTime() {
        DateFunctions.setLocale(AU);
        String defaultValue = DateFunctions.formatDateTime(dateTime);
        assertTrue("20/09/2006 17:54:22".equals(defaultValue) || "20/09/2006 5:54:22 PM".equals(defaultValue));

        String shortValue = DateFunctions.formatDateTime(dateTime, "short");
        assertTrue("20/09/06 17:54".equals(shortValue) || "20/09/06 5:54 PM".equals(shortValue));

        String mediumValue = DateFunctions.formatDateTime(dateTime, "medium");
        assertTrue("20/09/2006 17:54:22".equals(mediumValue) || "20/09/2006 5:54:22 PM".equals(mediumValue));

        String longValue = DateFunctions.formatDateTime(dateTime, "long");
        assertTrue("20 September 2006 17:54:22".equals(longValue)
                   || "20 September 2006 5:54:22 PM".equals(longValue));

        shortValue = DateFunctions.formatDateTime(dateTime, "short", "short");
        assertTrue("20/09/06 17:54".equals(shortValue) || "20/09/06 5:54 PM".equals(shortValue));

        mediumValue = DateFunctions.formatDateTime(dateTime, "medium", "medium");
        assertTrue("20/09/2006 17:54:22".equals(mediumValue) || "20/09/2006 5:54:22 PM".equals(mediumValue));

        longValue = DateFunctions.formatDateTime(dateTime, "long", "long");
        assertTrue("20 September 2006 17:54:22".equals(longValue)
                   || "20 September 2006 5:54:22 PM".equals(longValue));

        DateFunctions.setLocale(Locale.UK);
        assertEquals("20-Sep-2006 17:54:22",
                     DateFunctions.formatDateTime(dateTime));
        assertEquals("20/09/06 17:54",
                     DateFunctions.formatDateTime(dateTime, "short"));
        assertEquals("20-Sep-2006 17:54:22", DateFunctions.formatDateTime(
                dateTime, "medium"));
        assertEquals("20 September 2006 17:54:22 GMT",
                     DateFunctions.formatDateTime(dateTime, "long"));

        assertEquals("20/09/06 17:54",
                     DateFunctions.formatDateTime(dateTime, "short", "short"));
        assertEquals("20-Sep-2006 17:54:22", DateFunctions.formatDateTime(
                dateTime, "medium", "medium"));
        assertEquals("20 September 2006 17:54:22 GMT",
                     DateFunctions.formatDateTime(dateTime, "long", "long"));
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        Calendar calendar = new GregorianCalendar(2006, 8, 20, 17, 54, 22);
        dateTime = calendar.getTime();

        // use the default time zone for formatting
        DateFunctions.setTimeZone(TimeZone.getDefault());
    }
}

