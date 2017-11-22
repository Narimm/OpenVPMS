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

package org.openvpms.component.business.domain.bean;

import org.openvpms.component.business.domain.im.common.IMObjectRelationship;

import java.util.Comparator;
import java.util.Date;
import java.util.function.Predicate;

/**
 * Policies for selecting relationships and retrieving objects when operating on nodes with an {@link IMObjectBean}.
 *
 * @author Tim Anderson
 */
public class Policies {

    /**
     * A policy that matches any relationship, and both active and inactive objects.
     */
    private static final Policy<IMObjectRelationship> ANY
            = new DefaultPolicy<>(Policy.State.ANY, IMObjectRelationship.class, null);

    /**
     * A policy that matches active relationships, and active objects.
     */
    private static final Policy<IMObjectRelationship> ACTIVE
            = new DefaultPolicy<>(Policy.State.ACTIVE, IMObjectRelationship.class, Predicates.activeNow());

    /**
     * Returns a policy that matches active relationships, and returns active objects.
     *
     * @return the policy
     */
    public static Policy<IMObjectRelationship> active() {
        return ACTIVE;
    }

    /**
     * Returns a policy that selects relationships active at the specified time and returns active objects.
     *
     * @param time the time
     * @return a new policy
     */
    public static Policy<IMObjectRelationship> active(Date time) {
        return active(time, true);
    }

    /**
     * Returns a policy that selects relationships using the supplied predicate.
     *
     * @param time   the time
     * @param active if {@code true}, returns active objects, otherwise returns active and inactive objects
     * @return a new policy
     */
    public static Policy<IMObjectRelationship> active(Date time, boolean active) {
        return new DefaultPolicy<>(active, IMObjectRelationship.class, Predicates.activeAt(time));
    }

    /**
     * Returns a policy that selects relationships using the supplied predicate, and returns active objects.
     *
     * @param predicate the predicate
     * @return a new policy
     */
    public static Policy<IMObjectRelationship> active(Predicate<IMObjectRelationship> predicate) {
        return new DefaultPolicy<>(Policy.State.ACTIVE, IMObjectRelationship.class, predicate);
    }

    /**
     * Returns a policy that selects all active relationships, and returns active objects.
     *
     * @param type the relationship type
     * @return a new policy
     */
    public static <R extends IMObjectRelationship> Policy<R> active(Class<R> type) {
        return new DefaultPolicy<>(Policy.State.ACTIVE, type, Predicates.activeNow());
    }

    /**
     * Returns a policy that selects all active relationships, and returns active objects.
     * <p>
     * The relationships will be ordered using the specified comparator.
     *
     * @param type       the relationship type
     * @param comparator the relationship comparator
     * @return a new policy
     */
    public static <R extends IMObjectRelationship> Policy<R> active(Class<R> type, Comparator<R> comparator) {
        return new DefaultPolicy<>(Policy.State.ACTIVE, type, Predicates.activeNow(), comparator);
    }

    /**
     * Returns a policy that selects relationships active at the specified time and returns active objects.
     * <p>
     * The relationships will be ordered using the specified comparator.
     *
     * @param type       the relationship type
     * @param comparator the relationship comparator
     * @return a new policy
     */
    public static <R extends IMObjectRelationship> Policy<R> active(Date time, Class<R> type,
                                                                    Comparator<R> comparator) {
        return new DefaultPolicy<>(Policy.State.ACTIVE, type, Predicates.activeAt(time), comparator);
    }

    /**
     * Returns a policy that matches any relationship, and both active and inactive objects.
     *
     * @return the policy
     */
    public static Policy<IMObjectRelationship> any() {
        return ANY;
    }

    /**
     * Returns a policy that selects all relationships, and returns active or inactive objects.
     *
     * @param type the relationship type
     * @return a new policy
     */
    public static <R extends IMObjectRelationship> Policy<R> any(Class<R> type) {
        return new DefaultPolicy<>(Policy.State.ANY, type, null);
    }

    /**
     * Returns a policy that selects relationships using the supplied predicate, and returns active or inactive objects.
     *
     * @param predicate the predicate
     * @return a new policy
     */
    public static Policy<IMObjectRelationship> any(Predicate<IMObjectRelationship> predicate) {
        return new DefaultPolicy<>(Policy.State.ANY, IMObjectRelationship.class, predicate);
    }

    /**
     * Returns a policy that matches the specified criteria.
     *
     * @param active    if {@code true}, objects must be active, otherwise they can be either active or inactive
     * @param predicate the predicate used to select relationships
     * @return a new policy
     */
    public static Policy<IMObjectRelationship> match(boolean active, Predicate<IMObjectRelationship> predicate) {
        return new DefaultPolicy<>(active, IMObjectRelationship.class, predicate);
    }

    /**
     * Returns a policy that matches the specified criteria.
     *
     * @param active    if {@code true}, objects must be active, otherwise they can be either active or inactive
     * @param predicate the predicate used to select relationships
     * @return a new policy
     */
    public static Policy<IMObjectRelationship> match(boolean active, Predicate<IMObjectRelationship> predicate,
                                                     Comparator<IMObjectRelationship> comparator) {
        return new DefaultPolicy<>(active, IMObjectRelationship.class, predicate, comparator);
    }

    /**
     * Returns a policy that matches the specified criteria.
     *
     * @param active    if {@code true}, objects must be active, otherwise they can be either active or inactive
     * @param predicate the predicate used to select relationships
     * @return a new policy
     */
    public static <R extends IMObjectRelationship> Policy<R> match(boolean active, Class<R> type,
                                                                   Predicate<R> predicate) {
        return new DefaultPolicy<>(active, type, predicate);
    }

    static class DefaultPolicy<R extends IMObjectRelationship> implements Policy<R> {

        private final Predicate<R> predicate;

        private final State state;

        private final Class<R> type;

        private final Comparator<R> comparator;

        public DefaultPolicy(boolean active, Class<R> type, Predicate<R> predicate) {
            this(active, type, predicate, null);
        }

        public DefaultPolicy(boolean active, Class<R> type, Predicate<R> predicate, Comparator<R> comparator) {
            this(active ? Policy.State.ACTIVE : Policy.State.ANY, type, predicate, comparator);
        }

        public DefaultPolicy(State state, Class<R> type, Predicate<R> predicate) {
            this(state, type, predicate, null);
        }

        public DefaultPolicy(State state, Class<R> type, Predicate<R> predicate, Comparator<R> comparator) {
            this.predicate = predicate;
            this.state = state;
            this.type = type;
            this.comparator = comparator;
        }

        @Override
        public Predicate<R> getPredicate() {
            return predicate;
        }

        @Override
        public State getState() {
            return state;
        }

        @Override
        public Comparator<R> getComparator() {
            return comparator;
        }

        @Override
        public Class<R> getType() {
            return type;
        }

    }
}
