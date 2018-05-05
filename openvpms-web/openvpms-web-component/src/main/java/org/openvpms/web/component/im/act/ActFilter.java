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

package org.openvpms.web.component.im.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.functor.ActComparator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Filters the children of an act.
 *
 * @author Tim Anderson
 */
public abstract class ActFilter<T extends Act> {

    /**
     * Returns the immediate children of an act, after applying filters.
     *
     * @param root the root of the tree
     * @return the immediate children of the root, or an empty list if they have been filtered
     */
    public List<T> filter(T root) {
        return filter(root, root, new HashMap<IMObjectReference, T>());
    }

    /**
     * Returns the immediate children of an act, after applying filters.
     *
     * @param act  the act
     * @param root the root of the tree
     * @param acts the set of visited acts, keyed on reference
     * @return the immediate children of the act, or an empty list if they have been filtered
     */
    public abstract List<T> filter(T act, T root, Map<IMObjectReference, T> acts);

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
    protected Comparator<T> getComparator(boolean ascending) {
        return ascending ? ActComparator.<T>ascending() : ActComparator.<T>descending();
    }

}
