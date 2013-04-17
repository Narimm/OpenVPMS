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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.act;

import org.apache.commons.collections.Predicate;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;


/**
 * This class enables supports iteration over the first level of an act
 * heirarchy, optionally filtering child acts.
 * e.g, given a heirarchy of:
 * <ul>
 * <li>event1</li>
 * <ul><li>note1</li><li>problem1</li><li>weight1</li></ul>
 * <li>event2</li>
 * <ul><li>note2</li><li>problem2</li></ul>
 * </ul>
 * and filtering out all child acts bar <em>act.patientNote</em> and
 * <em>act.patientClinicalProblem</em>, the following acts would be returned:
 * {@code event1, note1, problem1, event2, note2, problem2}.
 * <br/>
 * Note that the child acts are ordered on increasing start time.
 *
 * @author Tim Anderson
 */
public class ActHierarchyIterator<T extends Act> implements Iterable<T> {

    /**
     * The top level acts.
     */
    private Iterable<T> acts;

    /**
     * The predicate to filter relationships.
     */
    private ActHierarchyFilter<T> filter;

    /**
     * The maximum depth to iterate to. Use {@code -1} to not limit the depth.
     */
    private int maxDepth;

    /**
     * Constructs an {@code ActHierarchyIterator}.
     *
     * @param acts    the collection of acts
     * @param context the context
     */
    public ActHierarchyIterator(Iterable<T> acts, Context context) {
        this(acts, (Predicate) null, -1, context);
    }

    /**
     * Constructs an {@code ActHierarchyIterator}.
     *
     * @param acts       the collection of acts
     * @param shortNames the child short names to include
     * @param context    the context
     */
    public ActHierarchyIterator(Iterable<T> acts, String[] shortNames, Context context) {
        this(acts, shortNames, true, -1, context);
    }

    /**
     * Constructs an {@code ActHierarchyIterator}.
     *
     * @param acts       the collection of acts
     * @param shortNames the child short names to include
     * @param maxDepth   the maximum depth to iterate to, or {@code -1} to have unlimited depth
     * @param context the context
     */
    public ActHierarchyIterator(Iterable<T> acts, String[] shortNames, int maxDepth, Context context) {
        this(acts, shortNames, true, maxDepth, context);
    }

    /**
     * Constructs an {@code ActHierarchyIterator}.
     *
     * @param acts       the collection of acts
     * @param shortNames the child short names to include/exclude
     * @param include    if {@code true} include the acts, otherwise exclude them
     * @param maxDepth   the maximum depth to iterate to, or {@code -1} to have unlimited depth
     */
    public ActHierarchyIterator(Iterable<T> acts, String[] shortNames, boolean include, int maxDepth, Context context) {
        this(acts, new ActHierarchyFilter<T>(shortNames, include, context), maxDepth);
    }

    /**
     * Constructs an {@code ActHeirarchyFlattener}.
     *
     * @param acts      the collection of acts
     * @param predicate the predicate to select act relationships. If {@code null}, indicates to select all child acts
     * @param maxDepth  the maximum depth to iterate to, or {@code -1} to have unlimited depth
     */
    public ActHierarchyIterator(Iterable<T> acts, Predicate predicate, int maxDepth, Context context) {
        this(acts, new ActHierarchyFilter<T>(predicate, context), maxDepth);
    }

    /**
     * Constructs an {@code ActHeirarchyFlattener}.
     *
     * @param acts     the collection of acts
     * @param filter   the hierarchy flattener
     * @param maxDepth the maximum depth to iterate to, or {@code -1} to have unlimited depth
     */
    public ActHierarchyIterator(Iterable<T> acts, ActHierarchyFilter<T> filter, int maxDepth) {
        this.acts = acts;
        this.filter = filter;
        this.maxDepth = maxDepth;
    }

    /**
     * Returns an iterator over the acts.
     *
     * @return a new iterator
     */
    public Iterator<T> iterator() {
        return new ActIterator(maxDepth);
    }

    private class ActIterator implements Iterator<T> {

        /**
         * Stack of iterators, top level acts pushed first.
         */
        private Stack<Iterator<T>> stack = new Stack<Iterator<T>>();

        /**
         * The current act.
         */
        private T current;

        /**
         * The maximum depth in the heirarchy to descend to.
         */
        private int maxDepth;


        /**
         * Constructs an {@code ActIterator}.
         *
         * @param maxDepth the maximum depth in the heirarchy to descend to, or {@code -1} if there is no limit
         */
        public ActIterator(int maxDepth) {
            stack.push(acts.iterator());
            this.maxDepth = maxDepth;
        }

        /**
         * Returns {@code true} if the iteration has more elements.
         *
         * @return {@code true} if the iterator has more elements.
         */
        public boolean hasNext() {
            if (current == null) {
                advance();
            }
            return (current != null);
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @throws NoSuchElementException iteration has no more elements.
         */
        public T next() {
            if (current == null) {
                if (!advance()) {
                    throw new NoSuchElementException();
                }
            }
            T result = current;
            current = null;
            return result;
        }

        /**
         * Not supported.
         *
         * @throws UnsupportedOperationException if invoked
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Attempts to advance to the next top level act.
         *
         * @return {@code true} if the advance was successful.
         */
        private boolean advance() {
            current = null;
            while (!stack.isEmpty()) {
                Iterator<T> iterator = stack.peek();
                while (iterator.hasNext()) {
                    T act = iterator.next();
                    if (filter.include(act)) {
                        current = act;
                        if (maxDepth == -1 || stack.size() < maxDepth) {
                            iterator = filter.filter(current).iterator();
                            if (iterator.hasNext()) {
                                stack.push(iterator);
                            }
                        }
                        return true;
                    }
                }
                stack.pop();
            }
            return false;
        }

    }
}
