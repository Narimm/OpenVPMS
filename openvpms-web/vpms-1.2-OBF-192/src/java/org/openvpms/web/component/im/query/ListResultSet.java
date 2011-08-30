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

package org.openvpms.web.component.im.query;

import org.openvpms.component.system.common.query.SortConstraint;

import java.util.List;


/**
 * Paged result set where the results are pre-loaded from a list.
 * This implementation provides no sorting support.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-11-27 05:30:20Z $
 */
public class ListResultSet<T> extends AbstractListResultSet<T> {

    /**
     * The sort criteria.
     */
    private SortConstraint[] sort = new SortConstraint[0];


    /**
     * Constructs a new <tt>ListResultSet</tt>.
     *
     * @param objects  the objects
     * @param pageSize the maximum no. of results per page
     */
    public ListResultSet(List<T> objects, int pageSize) {
        super(objects, pageSize);
    }

    /**
     * This resets the iterator but does not do any sorting.
     *
     * @param sort the sort criteria. May be <code>null</code>
     */
    public void sort(SortConstraint[] sort) {
        reset();
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <code>false</code>
     */
    public boolean isSortedAscending() {
        return false;
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
