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

package org.openvpms.web.workspace.workflow.scheduling;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.openvpms.archetype.test.TestHelper.getDatetime;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class SchedulingHelperTestCase {

    /**
     * Tests the {@link SchedulingHelper#getSlotTime} method.
     */
    @Test
    public void testGetSlotTime() {
        Date date1 = getDatetime("2015-03-05 09:00:00");
        assertEquals(date1, SchedulingHelper.getSlotTime(date1, 15, false));
        assertEquals(date1, SchedulingHelper.getSlotTime(date1, 15, true));

        Date date2 = getDatetime("2015-03-05 09:05:00");
        assertEquals(getDatetime("2015-03-05 09:00:00"), SchedulingHelper.getSlotTime(date2, 15, false));
        assertEquals(getDatetime("2015-03-05 09:15:00"), SchedulingHelper.getSlotTime(date2, 15, true));

        Date date3 = getDatetime("2015-03-05 12:15:00");
        assertEquals(getDatetime("2015-03-05 12:00:00"), SchedulingHelper.getSlotTime(date3, 30, false));
        assertEquals(getDatetime("2015-03-05 12:30:00"), SchedulingHelper.getSlotTime(date3, 30, true));
    }
}
