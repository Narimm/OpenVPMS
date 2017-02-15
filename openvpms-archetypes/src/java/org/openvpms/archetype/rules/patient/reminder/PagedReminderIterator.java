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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An iterator over items returned queries created by {@link ReminderItemQueryFactory}.
 *
 * @author Tim Anderson
 */
public class PagedReminderIterator implements Iterator<ObjectSet> {

    /**
     * The query.
     */
    private ArchetypeQuery query;

    /**
     * The current page.
     */
    private IPage<ObjectSet> page;

    /**
     * Determines if the underlying results have updated.
     */
    private boolean updated;

    /**
     * The iterator over the page.
     */
    private ReminderIterator iterator;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link PagedReminderIterator}.
     *
     * @param factory  the reminder item query factory
     * @param pageSize the page size
     * @param service  the archetype service
     */
    public PagedReminderIterator(ReminderItemQueryFactory factory, int pageSize, IArchetypeService service) {
        ArchetypeQuery query = factory.createQuery();
        query.setMaxResults(pageSize);
        this.query = query;
        iterator = new ReminderIterator();
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
    public ObjectSet next() {
        advance();
        return iterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the next page.
     *
     * @param reset    if {@code true}, reset the query to the start, otherwise goes from the next page
     * @param lastPage the last page. May be {@code null}
     * @return the next page
     */
    protected IPage<ObjectSet> getNextPage(boolean reset, IPage<ObjectSet> lastPage) {
        if (reset) {
            query.setFirstResult(0);
        } else if (lastPage != null) {
            query.setFirstResult(lastPage.getFirstResult() + lastPage.getResults().size());
        }
        return service.getObjects(query);
    }

    /**
     * Indicates to the iterator that the underlying result set has been updated.
     * <p/>
     * This will force the next database access to start from the beginning, to ensure items are not skipped.
     * Any items that have been returned by the iterator previously will be discarded.
     */
    public void updated() {
        updated = true;
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
     * An iterator the excludes reminders that it has already seen.
     * <p/>
     * This is necessary as paging is altered by reminder updates, and some reminders may seen more than once if
     * they fail to be updated.
     */
    private class ReminderIterator implements Iterator<ObjectSet> {

        /**
         * Seen reminder identifiers.
         */
        private Set<Long> ids = new HashSet<>();

        /**
         * The underlying iterator.
         */
        private Iterator<ObjectSet> iterator;

        /**
         * The next object.
         */
        private ObjectSet next;

        /**
         * Default constructor.
         */
        public ReminderIterator() {
        }

        /**
         * Sets the page to iterate over.
         *
         * @param page the page
         */
        public void setPage(IPage<ObjectSet> page) {
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
                    ObjectSet set = iterator.next();
                    Act reminder = (Act) set.get("reminder");
                    long id = reminder.getId();
                    if (!ids.contains(id)) {
                        next = set;
                        ids.add(id);
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
        public ObjectSet next() {
            if (next == null) {
                hasNext();
            }
            if (next == null) {
                throw new NoSuchElementException();
            }
            ObjectSet result = next;
            next = null;
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
