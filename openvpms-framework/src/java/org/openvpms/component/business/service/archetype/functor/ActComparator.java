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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.functor;

import org.apache.commons.collections4.comparators.ReverseComparator;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.util.DateHelper;

import java.util.Comparator;

/**
 * Comparator for {@link Act}s.
 *
 * @author Tim Anderson
 */
public class ActComparator<T extends Act> implements Comparator<T> {


    /**
     * Comparator to order acts on start time, oldest first.
     */
    private static final Comparator ASCENDING = new ActComparator();

    /**
     * Comparator to order acts on start time, most recent first.
     */
    @SuppressWarnings("unchecked")
    private static final Comparator DESCENDING = new ReverseComparator(ASCENDING);

    /**
     * Default constructor.
     */
    private ActComparator() {

    }

    /**
     * Returns a comparator to order acts on start time, oldest first.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Act> Comparator<T> ascending() {
        return (ActComparator<T>) ASCENDING;
    }

    /**
     * Returns a comparator to sort acts on descending start time.
     *
     * @return the comparator
     */
    @SuppressWarnings("unchecked")
    public static <T extends Act> Comparator<T> descending() {
        return (Comparator<T>) DESCENDING;
    }

    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * first argument is less than, equal to, or greater than the
     * second.
     * @throws NullPointerException if an argument is null and this
     *                              comparator does not permit null arguments
     * @throws ClassCastException   if the arguments' types prevent them from
     *                              being compared by this comparator.
     */
    @Override
    public int compare(T o1, T o2) {
        int result = DateHelper.compareTo(o1.getActivityStartTime(), o2.getActivityStartTime(), false);
        if (result == 0) {
            result = Long.compare(o1.getId(), o2.getId());
        }
        return result;
    }

}
