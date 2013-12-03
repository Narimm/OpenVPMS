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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.scheduling;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Comparator;
import java.util.Date;

/**
 * Comparator used to locate intersecting events.
 *
 * @author Tim Anderson
 */
public class IntersectComparator implements Comparator<PropertySet> {

    /**
     * The singleton instance.
     */
    public static final IntersectComparator INSTANCE = new IntersectComparator();

    /**
     * Default constructor.
     */
    private IntersectComparator() {

    }

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second.
     * @throws ClassCastException if the arguments' types prevent them from
     *                            being compared by this Comparator.
     */
    public int compare(PropertySet o1, PropertySet o2) {
        Date start1 = o1.getDate(ScheduleEvent.ACT_START_TIME);
        Date end1 = o1.getDate(ScheduleEvent.ACT_END_TIME);
        Date start2 = o2.getDate(ScheduleEvent.ACT_START_TIME);
        Date end2 = o2.getDate(ScheduleEvent.ACT_END_TIME);
        if (DateRules.compareTo(start1, start2) < 0
            && DateRules.compareTo(end1, start2) <= 0) {
            return -1;
        }
        if (DateRules.compareTo(start1, end2) >= 0
            && DateRules.compareTo(end1, end2) > 0) {
            return 1;
        }
        return 0;
    }
}
