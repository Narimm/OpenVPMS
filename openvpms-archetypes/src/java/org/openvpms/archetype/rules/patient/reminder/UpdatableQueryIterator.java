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

package org.openvpms.archetype.rules.patient.reminder;

import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Iterator for the results of an {@link IArchetypeQuery}. This uses paging to limit the no. of results retrieved,
 * and can be reset if results are updated. <p/>
 * If reset, any items that have been returned previously will be excluded.
 *
 * @author Tim Anderson
 */
public abstract class UpdatableQueryIterator<T> implements Iterator<T> {

    /**
     * The query.
     */
    private ArchetypeQuery query;

    /**
     * The current page.
     */
    private IPage<T> page;

    /**
     * Determines if the underlying results have updated.
     */
    private boolean updated;

    /**
     * The iterator over the page.
     */
    private PageIterator iterator;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link UpdatableQueryIterator}.
     *
     * @param query    the query
     * @param pageSize the page size
     * @param service  the archetype service
     */
    public UpdatableQueryIterator(ArchetypeQuery query, int pageSize, IArchetypeService service) {
        query.setMaxResults(pageSize);
        this.query = query;
        iterator = new PageIterator();
        this.service = service;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        advance();
        return iterator.hasNext();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public T next() {
        advance();
        return iterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Indicates to the iterator that the underlying result set has been updated.
     * <p>
     * This will force the next database access to start from the beginning, to ensure objects are not skipped.
     * Any objects that have been returned by the iterator previously will be discarded.
     * <p>
     * Note that iteration over the current page will complete before any query is issued.
     */
    public void updated() {
        updated = true;
    }

    /**
     * Returns the next page.
     *
     * @param query   the query
     * @param service the archetype service
     * @return the next page
     */
    protected abstract IPage<T> getNext(ArchetypeQuery query, IArchetypeService service);

    /**
     * Returns a unique identifier for the object.
     *
     * @param object the object
     * @return a unique identifier for the object
     */
    protected abstract long getId(T object);

    /**
     * Returns the next page.
     *
     * @param reset    if {@code true}, reset the query to the start, otherwise goes from the next page
     * @param lastPage the last page. May be {@code null}
     * @return the next page
     */
    protected IPage<T> getNextPage(boolean reset, IPage<T> lastPage) {
        if (reset) {
            query.setFirstResult(0);
        } else if (lastPage != null) {
            query.setFirstResult(lastPage.getFirstResult() + lastPage.getResults().size());
        }
        return getNext(query, service);
    }

    /**
     * Advances to the next item in the list.
     */
    protected void advance() {
        if (!iterator.hasNext()) {
            boolean done = false;
            while (!done) {
                page = getNextPage(updated, page);
                iterator.setPage(page);
                updated = false;
                if (page.getResults().isEmpty() || iterator.hasNext()) {
                    done = true;
                }
            }
        }
    }

    /**
     * An iterator the excludes items that it has already seen.
     * <p>
     * This is necessary as paging is altered by reminder updates, and some reminders may seen more than once if
     * they fail to be updated.
     */
    private class PageIterator implements Iterator<T> {

        /**
         * Seen identifiers.
         */
        private Set<Long> ids = new HashSet<>();

        /**
         * The underlying iterator.
         */
        private Iterator<T> iterator;

        /**
         * The next object.
         */
        private T next;

        /**
         * Default constructor.
         */
        public PageIterator() {
        }

        /**
         * Sets the page to iterate over.
         *
         * @param page the page
         */
        public void setPage(IPage<T> page) {
            this.iterator = page.getResults().iterator();
        }

        /**
         * Returns {@code true} if the iteration has more elements.
         * (In other words, returns {@code true} if {@link #next} would
         * return an element rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            if (iterator != null && next == null) {
                while (iterator.hasNext()) {
                    T object = iterator.next();
                    long id = getId(object);
                    if (ids.add(id)) {
                        next = object;
                        break;
                    }
                }
            }
            return next != null;
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public T next() {
            if (next == null) {
                hasNext();
            }
            if (next == null) {
                throw new NoSuchElementException();
            }
            T result = next;
            next = null;
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
