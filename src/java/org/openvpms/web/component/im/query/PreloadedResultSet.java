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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.openvpms.web.component.im.util.IMObjectSorter;

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.ArrayList;
import java.util.List;


/**
 * Paged result set where the results are pre-loaded.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PreloadedResultSet<T extends IMObject>
        extends AbstractResultSet<T> {

    /**
     * The query objects.
     */
    private final List<T> _objects;

    /**
     * The sort criteria.
     */
    private SortConstraint[] _sort = new SortConstraint[0];

    /**
     * Determines if the set is sorted ascending or descending.
     */
    private boolean _sortAscending = true;


    /**
     * Construct a new <code>PreloadedResultSet</code>.
     *
     * @param objects the objects
     * @param rows    the maximum no. of rows per page
     */
    public PreloadedResultSet(List<T> objects, int rows) {
        super(rows);
        _objects = objects;

        reset();
    }

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be <code>null</code>
     */
    public void sort(SortConstraint[] sort) {
        if (sort != null && !_objects.isEmpty()) {
            IMObjectSorter.sort(_objects, sort);
            _sortAscending = sort[0].isAscending();
            _sort = sort;
        }
        reset();
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <code>true</code> if the node is sorted ascending or no sort
     *         constraint was specified; <code>false</code> if it is sorted
     *         descending
     */
    public boolean isSortedAscending() {
        return _sortAscending;
    }

    /**
     * Returns the sort criteria.
     *
     * @return the sort criteria. Never null
     */
    public SortConstraint[] getSortConstraints() {
        return _sort;
    }

    /**
     * Determines if duplicate rows should be filtered.
     *
     * @param distinct if true, remove duplicate rows
     */
    public void setDistinct(boolean distinct) {
        // no-op
    }

    /**
     * Determines if duplicate rows should be filtered.
     *
     * @return <code>true</code> if duplicate rows should be removed;
     *         otherwise <code>false</code>
     */
    public boolean isDistinct() {
        return false;
    }

    /**
     * Returns the specified page.
     *
     * @param firstRow
     * @param maxRows
     * @return the page corresponding to <code>page</code>, or <code>null</code>
     *         if none exists
     */
    protected IPage<T> getPage(int firstRow, int maxRows) {
        int to;
        if (maxRows == ArchetypeQuery.ALL_ROWS
                || ((firstRow + maxRows) >= _objects.size())) {
            to = _objects.size();
        } else {
            to = firstRow + maxRows;
        }
        List<T> rows = new ArrayList<T>(_objects.subList(firstRow, to));
        return new Page<T>(rows, firstRow, maxRows, _objects.size());
    }

}
