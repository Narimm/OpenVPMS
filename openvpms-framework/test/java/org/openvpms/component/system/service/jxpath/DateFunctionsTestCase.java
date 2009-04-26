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

import junit.framework.TestCase;
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
public class DateFunctionsTestCase extends TestCase {

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
     */
    public void testFormatTime() {
        DateFunctions.setLocale(AU);
        assertEquals("17:54:22", DateFunctions.formatTime(dateTime));
        assertEquals("17:54", DateFunctions.formatTime(dateTime, "short"));
        assertEquals("17:54:22", DateFunctions.formatTime(dateTime, "medium"));
        assertEquals("17:54:22", DateFunctions.formatTime(dateTime, "long"));

        // override the local and time zone
        DateFunctions.setLocale(Locale.UK);
        DateFunctions.setTimeZone(TimeZone.getTimeZone("GMT+4"));

        assertEquals("21:54:22", DateFunctions.formatTime(dateTime));
        assertEquals("21:54", DateFunctions.formatTime(dateTime, "short"));
        assertEquals("21:54:22", DateFunctions.formatTime(dateTime, "medium"));
        assertEquals("21:54:22 GMT+04:00",
                     DateFunctions.formatTime(dateTime, "long"));
    }

    /**
     * Tests the {@link DateFunctions#formatDateTime} methods.
     */
    public void testFormatDateTime() {
        DateFunctions.setLocale(AU);
        assertEquals("20/09/2006 17:54:22",
                     DateFunctions.formatDateTime(dateTime));

        assertEquals("20/09/06 17:54",
                     DateFunctions.formatDateTime(dateTime, "short"));
        assertEquals("20/09/2006 17:54:22",
                     DateFunctions.formatDateTime(dateTime, "medium"));
        assertEquals("20 September 2006 17:54:22",
                     DateFunctions.formatDateTime(dateTime, "long"));

        assertEquals("20/09/06 17:54",
                     DateFunctions.formatDateTime(dateTime, "short", "short"));
        assertEquals("20/09/2006 17:54:22",
                     DateFunctions.formatDateTime(dateTime, "medium",
                                                  "medium"));
        assertEquals("20 September 2006 17:54:22",
                     DateFunctions.formatDateTime(dateTime, "long", "long"));

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
    @Override
    protected void setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        Calendar calendar = new GregorianCalendar(2006, 8, 20, 17, 54, 22);
        dateTime = calendar.getTime();

        // use the default time zone for formatting
        DateFunctions.setTimeZone(TimeZone.getDefault());
    }
}

