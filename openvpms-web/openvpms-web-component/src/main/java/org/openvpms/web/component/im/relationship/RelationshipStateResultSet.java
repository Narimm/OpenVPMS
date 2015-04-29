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

package org.openvpms.web.component.im.relationship;

import org.apache.commons.collections.Transformer;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractListResultSet;
import org.openvpms.web.component.im.util.IMObjectSorter;

import java.util.List;


/**
 * A result set for {@link RelationshipState} instances, that provides sorting.
 *
 * @author Tim Anderson
 */
class RelationshipStateResultSet extends AbstractListResultSet<RelationshipState> {

    /**
     * The sort criteria.
     */
    private SortConstraint[] sort = new SortConstraint[0];

    /**
     * Determines if the set is sorted ascending or descending.
     */
    private boolean sortAscending = true;


    /**
     * Constructs a {@link RelationshipStateResultSet}.
     *
     * @param objects  the objects
     * @param pageSize the maximum no. of results per page
     */
    public RelationshipStateResultSet(List<RelationshipState> objects, int pageSize) {
        super(objects, pageSize);
    }

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be {@code null}
     */
    public void sort(SortConstraint[] sort) {
        if (sort != null && sort.length != 0 && !getObjects().isEmpty()) {
            this.sort = sort;
            sortAscending = sort[0].isAscending();
            IMObjectSorter.sort(getObjects(), sort, new Transformer() {
                @Override
                public Object transform(Object input) {
                    return ((RelationshipState) input).getRelationship();
                }
            });
        }
        reset();
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return {@code true} if the node is sorted ascending or no sort constraint was specified; {@code false} if it is
     *         sorted descending
     */
    public boolean isSortedAscending() {
        return sortAscending;
    }

    /**
     * Returns the sort criteria.
     *
     * @return the sort criteria. Never null
     */
    public SortConstraint[] getSortConstraints() {
        return sort;
    }

}
