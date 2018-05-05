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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.date;

import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.jxpath.ObjectFunctions;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link DateFunctions} class.
 *
 * @author Tim Anderson
 */
public class DateFunctionsTestCase {

    /**
     * The test date/time.
     */
    private Date dateTime;

    /**
     * The default context.
     */
    private JXPathContext context;

    /**
     * The time zone.
     */
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");


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
        this.context = createContext(new DateFunctions(locale, GMT));
    }

    /**
     * Tests the {@link DateFunctions#now()} method.
     */
    @Test
    public void testNow() {
        Date before = new Date();
        Date now = evaluate("date:now()", context);
        Date after = new Date();
        assertTrue(before.compareTo(now) <= 0 && after.compareTo(now) >= 0);
    }

    /**
     * Tests the {@link DateFunctions#today()} method.
     */
    @Test
    public void testToday() {
        assertEquals(DateRules.getToday(), evaluate("date:today()", context));
    }

    /**
     * Tests the {@link DateFunctions#tomorrow()} method.
     */
    @Test
    public void testTomorrow() {
        assertEquals(DateRules.getTomorrow(), evaluate("date:tomorrow()", context));
    }

    /**
     * Tests the {@link DateFunctions#yesterday()} method.
     */
    @Test
    public void testYesterday() {
        assertEquals(DateRules.getYesterday(), evaluate("date:yesterday()", context));
    }

    /**
     * Tests the {@link DateFunctions#add(Date, String)} method.
     */
    @Test
    public void testAdd() {
        assertEquals(DateRules.getDate(dateTime, 1, DateUnits.DAYS), evaluate("date:add(., '1d')", context));
        assertEquals(DateRules.getDate(dateTime, -1, DateUnits.DAYS), evaluate("date:add(., '-1d')", context));
        assertEquals(DateRules.getDate(dateTime, 1, DateUnits.WEEKS), evaluate("date:add(., '1w')", context));
        assertEquals(DateRules.getDate(dateTime, -1, DateUnits.WEEKS), evaluate("date:add(., '-1w')", context));
        assertEquals(DateRules.getDate(dateTime, 1, DateUnits.HOURS), evaluate("date:add(., '1h')", context));
        assertEquals(DateRules.getDate(dateTime, -1, DateUnits.HOURS), evaluate("date:add(., '-1h')", context));

        // test nulls
        DateFunctions functions = new DateFunctions();
        assertNull(functions.add(null, "1d"));
        assertNull(functions.add(dateTime, null));
        assertNull(functions.add(null, null));
    }

    /**
     * Tests the {@link DateFunctions#format(Date, String)} method.
     */
    @Test
    public void testFormat() {
        assertEquals("Sep 2006", evaluate("date:format(., 'MMM yyyy')", context));
        assertEquals("September 2006", evaluate("date:format(., 'MMMM yyyy')", context));

        assertEquals("5:54 PM", evaluate("date:format(., 'h:mm a')", context));

        assertEquals("17:54 Sep 2006", evaluate("date:format(., 'H:mm MMM yyyy')", context));

        // test null handling
        DateFunctions functions = new DateFunctions(new Locale("en", "AU"), GMT);
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
        assertEquals("20/09/2006", evaluate("date:formatDate(.)", context));
        assertEquals("20/09/06", evaluate("date:formatDate(., 'short')", context));
        assertEquals("20/09/2006", evaluate("date:formatDate(., 'medium')", context));
        assertEquals("20 September 2006", evaluate("date:formatDate(., 'long')", context));

        // change the locale
        JXPathContext context2 = createContext(new DateFunctions(Locale.UK, GMT));
        assertEquals("20-Sep-2006", evaluate("date:formatDate(.)", context2));
        assertEquals("20/09/06", evaluate("date:formatDate(., 'short')", context2));
        assertEquals("20-Sep-2006", evaluate("date:formatDate(., 'medium')", context2));
        assertEquals("20 September 2006", evaluate("date:formatDate(., 'long')", context2));
    }

    /**
     * Tests the {@link DateFunctions#formatTime} methods.
     * <p/>
     * TODO - this is a bit tedious, as different JDK versions have different formats for short, medium and long
     * time formats.
     */
    @Test
    public void testFormatTime() {
        String defaultValue = evaluate("date:formatTime(.)", context);
        assertTrue("17:54:22".equals(defaultValue) || "5:54:22 PM".equals(defaultValue));

        String shortValue = evaluate("date:formatTime(., 'short')", context);
        assertTrue("17:54".equals(shortValue) || "5:54 PM".equals(shortValue));

        String mediumValue = evaluate("date:formatTime(., 'medium')", context);
        assertTrue("17:54:22".equals(mediumValue) || "5:54:22 PM".equals(mediumValue));

        String longValue = evaluate("date:formatTime(., 'long')", context);
        assertTrue("17:54:22".equals(longValue) || "5:54:22 PM".equals(longValue));

        // override the local and time zone
        JXPathContext context2 = createContext(new DateFunctions(Locale.UK, TimeZone.getTimeZone("GMT+4")));

        defaultValue = evaluate("date:formatTime(.)", context2);
        assertTrue("21:54:22".equals(defaultValue) || "9:54:22 PM".equals(defaultValue));

        shortValue = evaluate("date:formatTime(., 'short')", context2);
        assertTrue("21:54".equals(shortValue) || "5:54 PM".equals(shortValue));

        mediumValue = evaluate("date:formatTime(., 'medium')", context2);
        assertTrue("21:54:22".equals(mediumValue) || "9:54:22 PM".equals(mediumValue));

        assertEquals("21:54:22 GMT+04:00", evaluate("date:formatTime(., 'long')", context2));
    }

    /**
     * Tests the {@link DateFunctions#formatDateTime} methods.
     * <p/>
     * TODO - this is a bit tedious, as different JDK versions have different formats for short, medium and long
     * time formats.
     */
    @Test
    public void testFormatDateTime() {
        String defaultValue = evaluate("date:formatDateTime(.)", context);
        assertTrue("20/09/2006 17:54:22".equals(defaultValue) || "20/09/2006 5:54:22 PM".equals(defaultValue));

        String shortValue = evaluate("date:formatDateTime(., 'short')", context);
        assertTrue("20/09/06 17:54".equals(shortValue) || "20/09/06 5:54 PM".equals(shortValue));

        String mediumValue = evaluate("date:formatDateTime(., 'medium')", context);
        assertTrue("20/09/2006 17:54:22".equals(mediumValue) || "20/09/2006 5:54:22 PM".equals(mediumValue));

        String longValue = evaluate("date:formatDateTime(., 'long')", context);
        assertTrue("20 September 2006 17:54:22".equals(longValue) || "20 September 2006 5:54:22 PM".equals(longValue));

        shortValue = evaluate("date:formatDateTime(., 'short', 'short')", context);
        assertTrue("20/09/06 17:54".equals(shortValue) || "20/09/06 5:54 PM".equals(shortValue));

        mediumValue = evaluate("date:formatDateTime(., 'medium', 'medium')", context);
        assertTrue("20/09/2006 17:54:22".equals(mediumValue) || "20/09/2006 5:54:22 PM".equals(mediumValue));

        longValue = evaluate("date:formatDateTime(., 'long', 'long')", context);
        assertTrue("20 September 2006 17:54:22".equals(longValue) || "20 September 2006 5:54:22 PM".equals(longValue));

        JXPathContext context2 = createContext(new DateFunctions(Locale.UK, GMT));
        assertEquals("20-Sep-2006 17:54:22", evaluate("date:formatDateTime(.)", context2));
        assertEquals("20/09/06 17:54", evaluate("date:formatDateTime(., 'short')", context2));
        assertEquals("20-Sep-2006 17:54:22", evaluate("date:formatDateTime(., 'medium')", context2));
        assertEquals("20 September 2006 17:54:22 GMT", evaluate("date:formatDateTime(., 'long')", context2));

        assertEquals("20/09/06 17:54", evaluate("date:formatDateTime(., 'short', 'short')", context2));
        assertEquals("20-Sep-2006 17:54:22", evaluate("date:formatDateTime(., 'medium', 'medium')", context2));
        assertEquals("20 September 2006 17:54:22 GMT", evaluate("date:formatDateTime(., 'long', 'long')", context2));
    }

    @SuppressWarnings("unchecked")
    private <T> T evaluate(String expression, JXPathContext context) {
        return (T) context.getValue(expression);
    }

    private JXPathContext createContext(DateFunctions functions) {
        FunctionLibrary library = new FunctionLibrary();
        library.addFunctions(new ObjectFunctions(functions, "date"));
        return JXPathHelper.newContext(dateTime, library);
    }

}

