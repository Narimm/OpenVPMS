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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.system.common.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Chain of {@link Iterable}.
 *
 * @author Tim Anderson
 */
public class IterableChain<T> implements Iterable<T> {

    /**
     * The iterables.
     */
    private final Iterable<T>[] iterables;

    /**
     * Constructs an {@link IterableChain}.
     *
     * @param iterables the iterables
     */
    @SafeVarargs
    public IterableChain(Iterable<T>... iterables) {
        this.iterables = iterables;
    }

    /**
     * Constructs an {@link IterableChain} for a singleton and an iterable. The singleton is returned first.
     *
     * @param singleton a singleton
     * @param iterable  the iterable
     */
    public IterableChain(T singleton, Iterable<T> iterable) {
        this(Collections.singletonList(singleton), iterable);
    }

    /**
     * Constructs an {@link IterableChain} for an iterable and a singleton. The singleton is returned last.
     *
     * @param iterable  the iterable
     * @param singleton a singleton
     */
    public IterableChain(Iterable<T> iterable, T singleton) {
        this(iterable, Collections.singletonList(singleton));
    }

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     */
    public Iterator<T> iterator() {
        return new ChainedIterator();
    }

    private class ChainedIterator implements Iterator<T> {

        private int index = 0;

        private Iterator<T> iter;

        public ChainedIterator() {
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements.
         *
         * @return <tt>true</tt> if the iterator has more elements.
         */
        public boolean hasNext() {
            if (iter == null || !iter.hasNext()) {
                advance();
            }
            return (iter != null && iter.hasNext());
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @throws NoSuchElementException iteration has no more elements.
         */
        public T next() {
            if (iter == null || !iter.hasNext()) {
                advance();
                if (iter == null || !iter.hasNext()) {
                    throw new NoSuchElementException();
                }
            }
            return iter.next();
        }

        /**
         * Not supported.
         *
         * @throws UnsupportedOperationException if invoked
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void advance() {
            while (iter == null || !iter.hasNext()) {
                if (index < iterables.length) {
                    iter = iterables[index].iterator();
                    ++index;
                } else {
                    return;
                }
            }
        }
    }
}
