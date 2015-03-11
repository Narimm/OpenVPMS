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

package org.openvpms.web.component.im.act;

import org.apache.commons.collections4.comparators.ReverseComparator;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;

import java.util.Comparator;
import java.util.List;

/**
 * Filters the children of an act.
 *
 * @author Tim Anderson
 */
public abstract class ActFilter<T extends Act> {

    /**
     * Comparator to order acts on start time, oldest first.
     */
    private static final Comparator<Act> ASCENDING = new Comparator<Act>() {
        @Override
        public int compare(Act o1, Act o2) {
            return DateRules.compareTo(o1.getActivityStartTime(), o2.getActivityStartTime());
        }
    };

    /**
     * Comparator to order acts on start time, most recent first.
     */
    private static final ReverseComparator<Act> DESCENDING = new ReverseComparator<Act>(ASCENDING);

    /**
     * Returns the immediate children of an act, after applying filters.
     *
     * @param root the root of the tree
     * @return the immediate children of the root, or an empty list if they have been filtered
     */
    public List<T> filter(T root) {
        return filter(root, root);
    }

    /**
     * Returns the immediate children of an act, after applying filters.
     *
     * @param act  the act
     * @param root the root of the tree
     * @return the immediate children of the act, or an empty list if they have been filtered
     */
    public abstract List<T> filter(T act, T root);

    /**
     * Returns a comparator to sort the children of an act.
     *
     * @param act the parent act
     * @return the comparator to sort the act's children
     */
    public abstract Comparator<T> getComparator(T act);

    /**
     * Returns a comparator to sort acts on start time.
     *
     * @param ascending if {@code true}, sort items on ascending times
     * @return the comparator
     */
    @SuppressWarnings("unchecked")
    protected Comparator<T> getComparator(boolean ascending) {
        Comparator result = (ascending) ? ASCENDING : DESCENDING;
        return (Comparator<T>) result;
    }

}
