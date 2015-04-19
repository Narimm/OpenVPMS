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

package org.openvpms.web.workspace.workflow.scheduling;

import org.junit.Test;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.util.PropertySet;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link IntersectComparator} class.
 *
 * @author Tim Anderson
 */
public class IntersectComparatorTestCase {

    /**
     * Tests events that are as long as the slot size.
     */
    @Test
    public void testIntersectForEventLengthsEqualToSlotSize() {
        PropertySet event1 = createEvent("2014-03-01 10:00:00", "2014-03-01 10:15:00");
        PropertySet event2 = createEvent("2014-03-01 10:15:00", "2014-03-01 10:30:00");
        PropertySet event3 = createEvent("2014-03-01 10:30:00", "2014-03-01 10:45:00");

        IntersectComparator comparator = new IntersectComparator(15);
        assertEquals(0, comparator.compare(event1, event1));
        assertEquals(-1, comparator.compare(event1, event2));
        assertEquals(-1, comparator.compare(event1, event3));

        assertEquals(1, comparator.compare(event2, event1));
        assertEquals(0, comparator.compare(event2, event2));
        assertEquals(-1, comparator.compare(event2, event3));

        assertEquals(1, comparator.compare(event3, event1));
        assertEquals(1, comparator.compare(event3, event2));
        assertEquals(0, comparator.compare(event3, event3));
    }

    /**
     * Checks events that are shorter than the slot size.
     */
    @Test
    public void testIntersectForEventLengthsSmallerThanSlotSize() {
        PropertySet event1 = createEvent("2014-03-01 10:00:00", "2014-03-01 10:05:00");
        PropertySet event2 = createEvent("2014-03-01 10:05:00", "2014-03-01 10:10:00");
        PropertySet event3 = createEvent("2014-03-01 10:10:00", "2014-03-01 10:15:00");
        PropertySet event4 = createEvent("2014-03-01 10:15:00", "2014-03-01 10:20:00");

        IntersectComparator comparator = new IntersectComparator(10);
        assertEquals(0, comparator.compare(event1, event1));
        assertEquals(0, comparator.compare(event1, event2));
        assertEquals(0, comparator.compare(event2, event1));
        assertEquals(0, comparator.compare(event2, event2));

        assertEquals(-1, comparator.compare(event1, event3));
        assertEquals(-1, comparator.compare(event1, event4));
        assertEquals(-1, comparator.compare(event2, event3));
        assertEquals(-1, comparator.compare(event2, event4));

        assertEquals(0, comparator.compare(event3, event3));
        assertEquals(0, comparator.compare(event3, event4));
        assertEquals(0, comparator.compare(event4, event3));
        assertEquals(0, comparator.compare(event4, event4));

        assertEquals(1, comparator.compare(event3, event2));
        assertEquals(1, comparator.compare(event3, event1));
        assertEquals(1, comparator.compare(event4, event2));
        assertEquals(1, comparator.compare(event4, event1));
    }

    /**
     * Checks events that are greater than the slot size.
     */
    @Test
    public void testIntersectForEventLengthsLongerThanSlotSize() {
        PropertySet event1 = createEvent("2014-03-01 10:00:00", "2014-03-01 10:10:00");
        PropertySet event2 = createEvent("2014-03-01 10:10:00", "2014-03-01 10:20:00");
        PropertySet event3 = createEvent("2014-03-01 10:20:00", "2014-03-01 10:30:00");

        IntersectComparator comparator = new IntersectComparator(5);
        assertEquals(0, comparator.compare(event1, event1));
        assertEquals(-1, comparator.compare(event1, event2));
        assertEquals(-1, comparator.compare(event1, event3));

        assertEquals(1, comparator.compare(event2, event1));
        assertEquals(0, comparator.compare(event2, event2));
        assertEquals(-1, comparator.compare(event2, event3));

        assertEquals(1, comparator.compare(event3, event1));
        assertEquals(1, comparator.compare(event3, event2));
        assertEquals(0, comparator.compare(event3, event3));
    }

    /**
     * Creates an event.
     *
     * @param startTime the event start time
     * @param endTime   the event end time
     * @return a new event
     */
    private PropertySet createEvent(String startTime, String endTime) {
        PropertySet event = new ObjectSet();
        event.set(ScheduleEvent.ACT_START_TIME, TestHelper.getDatetime(startTime));
        event.set(ScheduleEvent.ACT_END_TIME, TestHelper.getDatetime(endTime));
        return event;
    }
}
