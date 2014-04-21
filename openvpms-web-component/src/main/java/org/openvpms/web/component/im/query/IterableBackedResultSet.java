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

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link ResultSet} backed by an {@code Iterable}.
 *
 * @author Tim Anderson
 */
public class IterableBackedResultSet<T> extends AbstractCachingResultSet<T> {

    /**
     * The underlying iterable.
     */
    private final Iterable<T> iterable;

    /**
     * The iterator.
     */
    private Iterator<T> iterator;

    /**
     * The last index returned; used to track if the iterator can be re-used, or if needs to be reset.
     */
    private int lastIndex;

    /**
     * Constructs an {@link IterableBackedResultSet}.
     *
     * @param pageSize the maximum no. of results per page
     */
    public IterableBackedResultSet(Iterable<T> iterable, int pageSize) {
        super(pageSize);
        this.iterable = iterable;
        iterator = iterable.iterator();
    }

    /**
     * Reset the iterator.
     */
    @Override
    public void reset() {
        super.reset();
        lastIndex = 0;
        iterator = iterable.iterator();
    }

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be {@code null}
     */
    @Override
    public void sort(SortConstraint[] sort) {
        reset();
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return {@code true} if the node is sorted ascending or no sort constraint was specified; {@code false} if it is
     *         sorted descending
     */
    @Override
    public boolean isSortedAscending() {
        return true;
    }

    /**
     * Returns the sort criteria.
     *
     * @return the sort criteria. Never null
     */
    @Override
    public SortConstraint[] getSortConstraints() {
        return new SortConstraint[0];
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @param distinct if true, remove duplicate results
     */
    @Override
    public void setDistinct(boolean distinct) {
        // no-op
    }

    /**
     * Determines if duplicate results should be filtered.
     *
     * @return {@code true} if duplicate results should be removed; otherwise {@code false}
     */
    @Override
    public boolean isDistinct() {
        return false;
    }

    /**
     * Performs a query.
     *
     * @param firstResult the first result of the page to retrieve
     * @param maxResults  the maximum no. of results in the page
     * @return the page, or {@code null}
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    protected IPage<T> query(int firstResult, int maxResults) {
        if (firstResult < lastIndex) {
            reset();
        }
        while (lastIndex < firstResult && iterator.hasNext()) {
            iterator.next();
            lastIndex++;
        }
        List<T> matches = new ArrayList<T>();
        for (int i = 0; i < maxResults && iterator.hasNext(); ++i) {
            matches.add(iterator.next());
        }
        return new Page<T>(matches, firstResult, getPageSize(), -1);
    }

    /**
     * Counts the no. of results matching the query criteria.
     *
     * @return the total number of results
     */
    @Override
    protected int countResults() {
        int count = 0;
        for (T match : iterable) {
            ++count;
        }
        return count;
    }
}
