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

package org.openvpms.web.workspace.admin.job;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openvpms.web.workspace.admin.job.CronHelper.DAY_OF_MONTH;
import static org.openvpms.web.workspace.admin.job.CronHelper.DAY_OF_WEEK;
import static org.openvpms.web.workspace.admin.job.CronHelper.HOURS;
import static org.openvpms.web.workspace.admin.job.CronHelper.MINUTES;
import static org.openvpms.web.workspace.admin.job.CronHelper.MONTH;


/**
 * Tests the {@link CronHelper} class.
 *
 * @author Tim Anderson
 */
public class CronHelperTestCase {

    /**
     * Tests the {@link CronHelper#MINUTES} pattern.
     */
    @Test
    public void testMinutes() {
        checkValid("*", MINUTES);
        checkValid("0", MINUTES);
        checkValid("0-59", MINUTES);
        checkValid("0,1,2,3,4,5", MINUTES);
        checkValid("0,1,2,3,4,5-10", MINUTES);
        checkValid("*/5", MINUTES); // every 5 minutes

        checkInvalid("?", MINUTES);
        checkInvalid("-1", MINUTES);
        checkInvalid("60", MINUTES);
    }

    /**
     * Tests the {@link CronHelper#HOURS} pattern.
     */
    @Test
    public void testHours() {
        checkValid("*", HOURS);
        checkValid("0", HOURS);
        checkValid("0-23", HOURS);
        checkValid("0,1,2,3,4,5", HOURS);
        checkValid("0,1,2,3,4,5-10", HOURS);
        checkValid("*/2", HOURS); // every 2 hours

        checkInvalid("?", HOURS);
        checkInvalid("-1", HOURS);
        checkInvalid("24", HOURS);
    }

    /**
     * Tests the {@link CronHelper#DAY_OF_MONTH} pattern.
     */
    @Test
    public void testDayOfMonth() {
        checkValid("*", DAY_OF_MONTH);
        checkValid("?", DAY_OF_MONTH);
        checkValid("1", DAY_OF_MONTH);
        checkValid("1-31", DAY_OF_MONTH);
        checkValid("01,02,9,10-24", DAY_OF_MONTH);
        checkValid("*/5", DAY_OF_MONTH); // every 5 days

        checkInvalid("0", DAY_OF_MONTH);
        checkInvalid("-1", DAY_OF_MONTH);
        checkInvalid("32", DAY_OF_MONTH);
    }

    /**
     * Tests the {@link CronHelper#MONTH} pattern.
     */
    @Test
    public void testMonth() {
        checkValid("*", MONTH);
        checkValid("1", MONTH);
        checkValid("1-12", MONTH);
        checkValid("jan,feb,mar,apr,may,jun,jul,aug,sep,oct,nov,dec", MONTH);
        checkValid("jan-dec", MONTH);
        checkValid("MAR,AUG", MONTH);
        checkValid("*/2", MONTH); // every 2 months

        checkInvalid("?", MONTH);
        checkInvalid("0", MONTH);
        checkInvalid("13", MONTH);
    }

    /**
     * Tests the {@link CronHelper#DAY_OF_WEEK} pattern.
     */
    @Test
    public void testDayOfWeek() {
        checkValid("*", DAY_OF_WEEK);
        checkValid("?", DAY_OF_WEEK);
        checkValid("1", DAY_OF_WEEK);
        checkValid("1-7", DAY_OF_WEEK);
        checkValid("sun,mon,tue,wed,thu,fri,sat", DAY_OF_WEEK);
        checkValid("sun-sat", DAY_OF_WEEK);
        checkValid("*/2", DAY_OF_WEEK); // every 2 days

        checkInvalid("0", DAY_OF_WEEK);
        checkInvalid("8", DAY_OF_WEEK);
    }

    /**
     * Verifies that a value matches a pattern.
     *
     * @param value   the value
     * @param pattern the pattern
     */
    private void checkValid(String value, Pattern pattern) {
        assertTrue(pattern.matcher(value).matches());
    }

    /**
     * Verifies that a value doesn't match a pattern.
     *
     * @param value   the value
     * @param pattern the pattern
     */
    private void checkInvalid(String value, Pattern pattern) {
        assertFalse(value, pattern.matcher(value).matches());
    }
}
