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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.relationship;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractListResultSet;
import org.openvpms.web.component.im.util.IMObjectSorter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A result set for {@link RelationshipState} instances, that provides
 * sorting.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class RelationshipStateResultSet
        extends AbstractListResultSet<RelationshipState> {

    /**
     * Determines if the parent of the relationship is the source or target.
     */
    private final boolean source;

    /**
     * The sort criteria.
     */
    private SortConstraint[] sort = new SortConstraint[0];

    /**
     * Determines if the set is sorted ascending or descending.
     */
    private boolean sortAscending = true;


    /**
     * Constructs a new <tt>RelationshipStateResultSet</tt>.
     *
     * @param objects  the objects
     * @param source   determines if the parent of the relationship is the
     *                 source or target
     * @param pageSize the maximum no. of results per page
     */
    public RelationshipStateResultSet(List<RelationshipState> objects,
                                      boolean source, int pageSize) {
        super(objects, pageSize);
        this.source = source;
    }

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     */
    public void sort(SortConstraint[] sort) {
        if (sort != null && sort.length != 0 && !getObjects().isEmpty()) {
            sortAscending = sort[0].isAscending();
            SortConstraint s = sort[0];
            if (s instanceof NodeSortConstraint) {
                NodeSortConstraint n = (NodeSortConstraint) s;
                String name = n.getNodeName();
                if (name.equals(RelationshipStateTableModel.NAME_NODE)
                        || name.equals(
                        RelationshipStateTableModel.DESCRIPTION_NODE)
                        || name.equals(RelationshipStateTableModel.DETAIL_NODE))
                {
                    Collections.sort(getObjects(), getComparator(n));
                } else {
                    Transformer transformer = new Transformer() {
                        public Object transform(Object input) {
                            return ((RelationshipState) input).getRelationship();
                        }
                    };
                    IMObjectSorter.sort(getObjects(), sort, transformer);
                }
            }
            this.sort = sort;
        }
        reset();
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <tt>true</tt> if the node is sorted ascending or no sort
     *         constraint was specified; <tt>false</tt> if it is sorted
     *         descending
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

    /**
     * Helper to return a comparator for a node sort constraint.
     *
     * @param sort the node sort constraint
     * @return a comparator for the constraint, or <tt>null</tt> if the
     *         node is not supported
     */
    private Comparator getComparator(NodeSortConstraint sort) {
        Transformer transformer = null;
        String name = sort.getNodeName();
        if (RelationshipStateTableModel.NAME_NODE.equals(name)) {
            transformer = new Transformer() {
                public Object transform(Object input) {
                    RelationshipState state = ((RelationshipState) input);
                    return (source) ? state.getTargetName()
                            : state.getSourceName();
                }
            };
        } else if (RelationshipStateTableModel.DESCRIPTION_NODE.equals(name)) {
            transformer = new Transformer() {
                public Object transform(Object input) {
                    RelationshipState state = ((RelationshipState) input);
                    return (source) ? state.getTargetDescription()
                            : state.getSourceDescription();
                }
            };
        } else if (RelationshipStateTableModel.DETAIL_NODE.equals(name)) {
            transformer = new Transformer() {
                public Object transform(Object input) {
                    RelationshipState state = ((RelationshipState) input);
                    return state.getRelationship().getDescription();
                }
            };
        }
        Comparator comparator = null;
        if (transformer != null) {
            comparator = IMObjectSorter.getComparator(sortAscending);
            comparator = new TransformingComparator(transformer, comparator);
        }
        return comparator;
    }

}
