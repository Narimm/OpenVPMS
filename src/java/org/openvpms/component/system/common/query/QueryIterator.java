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

package org.openvpms.component.system.common.query;

import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * Iterator for the results of an {@link IArchetypeQuery}.
 * This uses paging to limit the no. of results retrieved.
 *
 * @author Tim Anderson
 */
public abstract class QueryIterator<T> implements Iterator<T> {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The query.
     */
    private IArchetypeQuery query;

    /**
     * The current page.
     */
    private IPage<T> page;

    /**
     * Iterator over the current page.
     */
    private Iterator<T> iterator;


    /**
     * Constructs a new {@code QueryIterator}.
     *
     * @param query   the query
     * @param service the archetype service
     */
    public QueryIterator(IArchetypeService service, IArchetypeQuery query) {
        this.service = service;
        this.query = query;
    }

    /**
     * Returns {@code true} if the iteration has more elements. (In other words, returns {@code true} if {@code next}
     * would return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iterator has more elements.
     * @throws ArchetypeServiceException if a query fails
     */
    public boolean hasNext() {
        if (page == null || !iterator.hasNext()) {
            if (page == null || (query.getMaxResults() != IArchetypeQuery.ALL_RESULTS
                                 && page.getResults().size() >= query.getMaxResults())) {
                page = getPage(service, query);
                if (query.getMaxResults() != IArchetypeQuery.ALL_RESULTS) {
                    int first = query.getFirstResult() + query.getMaxResults();
                    query.setFirstResult(first);
                }
                iterator = page.getResults().iterator();
            }
        }
        return iterator.hasNext();
    }

    /**
     * Returns the next element in the iteration.  Calling this method
     * repeatedly until the {@link #hasNext()} method returns false will
     * return each element in the underlying collection exactly once.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     */
    public T next() {
        return iterator.next();
    }

    /**
     * Removes from the underlying collection the last element returned by the iterator (optional operation).
     *
     * @throws UnsupportedOperationException if invoked
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the next page.
     *
     * @return the next page
     * @throws ArchetypeServiceException if the query fails
     */
    protected abstract IPage<T> getPage(IArchetypeService service, IArchetypeQuery query);

}
