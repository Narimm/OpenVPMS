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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.archetype.util;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.test.TestHelper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link DateRules} class.
 *
 * @author Tim Anderson
 */
public class DateRulesTestCase {

    /**
     * Tests the {@link DateRules#getToday()} method.
     */
    @Test
    public void testGetToday() {
        Date expected = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
        assertEquals(expected, DateRules.getToday());
    }

    /**
     * Tests the {@link DateRules#getTomorrow()} method.
     */
    @Test
    public void testGetTomorrow() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        Date expected = DateUtils.truncate(calendar.getTime(), Calendar.DAY_OF_MONTH);
        assertEquals(expected, DateRules.getTomorrow());
    }

    /**
     * Tests the {@link DateRules#getNextDate(Date)}.
     */
    @Test
    public void testGetNextDate() {
        Date date = TestHelper.getDatetime("2012-01-05 12:34:32");
        Date expected = TestHelper.getDate("2012-01-06");
        assertEquals(expected, DateRules.getNextDate(date));
    }
}
