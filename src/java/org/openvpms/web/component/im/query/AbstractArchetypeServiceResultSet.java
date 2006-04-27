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

import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;


/**
 * Abstract implementation of the {@link ResultSet} interface for result sets
 * that query the {@link IArchetypeService}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractArchetypeServiceResultSet<T>
        extends AbstractResultSet<T> {

    /**
     * Additional constraints to associate with the query. May be
     * <code>null</code>
     */
    private final IConstraint _constraints;

    /**
     * The sort criteria. May be <code>null</code>.
     */
    private SortConstraint[] _sort;


    /**
     * Construct a new <code>AbstractArchetypeServiceResultSet</code>.
     *
     * @param rows the maximum no. of rows per page
     * @param sort the sort criteria. May be <code>null</code>
     */
    public AbstractArchetypeServiceResultSet(int rows, SortConstraint[] sort) {
        this(null, rows, sort);
    }

    /**
     * Construct a new <code>AbstractArchetypeServiceResultSet</code>.
     *
     * @param constraints query constraints. May be <code>null</code>
     * @param rows        the maximum no. of rows per page
     * @param sort        the sort criteria. May be <code>null</code>
     */
    public AbstractArchetypeServiceResultSet(IConstraint constraints,
                                             int rows, SortConstraint[] sort) {
        super(rows);
        _constraints = constraints;
        setSortConstraint(sort);
    }

    /**
     * Sort the set. This resets the iterator.
     *
     * @param sort the sort criteria
     */
    public void sort(SortConstraint[] sort) {
        setSortConstraint(sort);
        reset();
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <code>true</code> if the node is sorted ascending;
     *         <code>false</code> if it is sorted descending
     */
    public boolean isSortedAscending() {
        return (_sort.length == 0 || _sort[0].isAscending());
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
     * Sets the sort criteria.
     *
     * @param sort the sort criteria. May be <code>null</code>
     */
    protected void setSortConstraint(SortConstraint[] sort) {
        _sort = (sort != null) ? sort : new SortConstraint[0];
    }

    /**
     * Returns the query constraints.
     *
     * @return the query constraints
     */
    protected IConstraint getConstraints() {
        return _constraints;
    }

    /**
     * Helper to convert a page from one type to another. <strong>Use with
     * caution</strong>: No attempt is made to verify the contents of the page.
     *
     * @param page the page to convert
     * @return the converted page
     */
    @SuppressWarnings("unchecked")
    protected final <K, T> IPage<K> convert(IPage<T> page) {
        return (IPage<K>) page;
    }
}
