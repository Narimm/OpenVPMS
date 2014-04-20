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

package org.openvpms.archetype.rules.workflow;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator over free slots for a schedule.
 *
 * @author Tim Anderson
 */
class FreeSlotIterator implements Iterator<Slot> {

    /**
     * The query iterator.
     */
    private final Iterator<ObjectSet> iterator;

    /**
     * Constructs an {@link FreeSlotIterator}.
     *
     * @param schedule the schedule
     * @param fromDate the date to query from
     * @param toDate   the date to query to
     * @param service  the archetype service
     */
    public FreeSlotIterator(Entity schedule, Date fromDate, Date toDate, IArchetypeService service) {
        NamedQuery query = new NamedQuery("findFreeSlots", Arrays.asList("scheduleId", "startTime", "endTime"));
        query.setParameter("from", fromDate);
        query.setParameter("to", toDate);
        query.setParameter("scheduleId", schedule.getId());
        iterator = new ObjectSetQueryIterator(service, query);
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public Slot next() {
        ObjectSet set = iterator.next();
        return new Slot(set.getLong("scheduleId"), set.getDate("startTime"), set.getDate("endTime"));
    }

    /**
     * Removes from the underlying collection the last element returned
     * by this iterator (optional operation).
     *
     * @throws UnsupportedOperationException if the {@code remove} operation is not supported by this iterator
     * @throws IllegalStateException         if the {@code next} method has not yet been called, or the {@code remove}
     *                                       method has already been called after the last call to the {@code next}
     *                                       method
     */
    @Override
    public void remove() {
        iterator.remove();
    }
}
