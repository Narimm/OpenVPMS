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

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Comparator;
import java.util.Date;

/**
 * Comparator used to locate intersecting events.
 * <p/>
 * Two events are considered to intersect if their start and end times overlap, adjusted for the slot size.
 *
 * @author Tim Anderson
 */
public class IntersectComparator implements Comparator<PropertySet> {

    /**
     * The slot size.
     */
    private final int slotSize;


    /**
     * Constructs an {@link IntersectComparator}.
     *
     * @param slotSize the slot size
     */
    public IntersectComparator(int slotSize) {
        this.slotSize = slotSize;
    }

    /**
     * Compares its two arguments for order.  Returns a negative integer, zero, or a positive integer as the first
     * argument is less than, equal to, or greater than the second.
     *
     * @param o1 the first object to be compared
     * @param o2 the second object to be compared
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
     *         than the second.
     * @throws ClassCastException if the arguments' types prevent them from being compared by this Comparator
     */
    public int compare(PropertySet o1, PropertySet o2) {
        Date start1 = SchedulingHelper.getSlotTime(o1.getDate(ScheduleEvent.ACT_START_TIME), slotSize, false);
        Date end1 = SchedulingHelper.getSlotTime(o1.getDate(ScheduleEvent.ACT_END_TIME), slotSize, true);
        Date start2 = SchedulingHelper.getSlotTime(o2.getDate(ScheduleEvent.ACT_START_TIME), slotSize, false);
        Date end2 = SchedulingHelper.getSlotTime(o2.getDate(ScheduleEvent.ACT_END_TIME), slotSize, true);
        if (DateRules.compareTo(start1, start2) < 0 && DateRules.compareTo(end1, start2) <= 0) {
            return -1;
        }
        if (DateRules.compareTo(start1, end2) >= 0 && DateRules.compareTo(end1, end2) > 0) {
            return 1;
        }
        return 0;
    }

}
