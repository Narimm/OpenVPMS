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

import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.iterators.FilterIterator;
import org.apache.commons.collections4.iterators.PeekingIterator;
import org.openvpms.archetype.rules.util.DateRules;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * An iterator that wraps one or more {@link FreeSlotIterator}s, returning the minimum dated slot from each,
 * on each call to {@link #next()}.
 *
 * @author Tim Anderson
 */
class FreeSlotIterators implements Iterator<Slot> {

    /**
     * The free slot iterators.
     */
    private List<PeekingIterator<Slot>> iterators;

    /**
     * The next iterator to use.
     */
    private Iterator<Slot> next;


    /**
     * Constructs a {@link FreeSlotIterators}.
     *
     * @param iterators the underlying iterators
     * @param predicate the predicate, used to filter slots
     */
    public FreeSlotIterators(List<FreeSlotIterator> iterators, Predicate<Slot> predicate) {
        this.iterators = new ArrayList<PeekingIterator<Slot>>();
        for (FreeSlotIterator iterator : iterators) {
            Iterator<Slot> filtered = new FilterIterator<Slot>(iterator, predicate);
            this.iterators.add(new PeekingIterator<Slot>(filtered));
        }
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
        if (next == null) {
            getNext();
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
    public Slot next() {
        if (next == null) {
            getNext();
        }
        if (next == null) {
            throw new NoSuchElementException();
        }
        Slot slot = next.next();
        next = null;
        return slot;
    }

    /**
     * Removes from the underlying collection the last element returned
     * by this iterator (optional operation).
     *
     * @throws UnsupportedOperationException if invoked
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the next iterator.
     */
    private void getNext() {
        next = null;
        ListIterator<PeekingIterator<Slot>> listIterator = iterators.listIterator();
        Date minDate = null;
        while (listIterator.hasNext()) {
            PeekingIterator<Slot> iterator = listIterator.next();
            if (!iterator.hasNext()) {
                listIterator.remove();
            } else {
                Slot slot = iterator.peek();
                if (minDate == null || DateRules.compareTo(minDate, slot.getStartTime()) > 0) {
                    minDate = slot.getStartTime();
                    next = iterator;
                }
            }
        }
    }

}
