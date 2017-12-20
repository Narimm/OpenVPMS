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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.common.PeriodRelationship;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.Date;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * Predicates for {@link IMObjectRelationship} instances.
 *
 * @author Tim Anderson
 */
public final class Predicates {

    /**
     * Predicate that determines if relationships are active as at the time
     * of evaluation.
     */
    private static final Predicate ACTIVE_NOW = new IsActiveAt();

    /**
     * Returns a predicate that determines if relationships are active as at the time of evaluation.
     *
     * @return a predicate that returns {@code true} if a relationship is active at time of evaluation
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObjectRelationship> Predicate<T> activeNow() {
        return (Predicate<T>) ACTIVE_NOW;
    }

    /**
     * Creates a new predicate that evaluates {@code true} if an {@link IMObjectRelationship} or
     * {@link PeriodRelationship} is active.
     * The {@link PeriodRelationship} must be active at the specified time.
     *
     * @param time the time
     * @return a new predicate
     */
    public static <T extends IMObjectRelationship> Predicate<T> activeAt(Date time) {
        return new IsActiveAt<>(time);
    }

    /**
     * Creates a new predicate that evaluates {@code true} if an {@link IMObjectRelationship} or
     * {@link PeriodRelationship} is active.
     * The {@link PeriodRelationship} must be active within the specified time range.
     *
     * @param from the from date. May be {@code null}
     * @param to   the to date. May be {@code null}
     * @return a new predicate
     */
    public static <T extends IMObjectRelationship> Predicate<T> active(Date from, Date to) {
        return new IsActiveRange<>(from, to);
    }

    /**
     * Returns a predicate that determines if the source of an {@link IMObjectRelationship} is that of the supplied
     * object.
     *
     * @param object the object. May be {@code null}
     * @return a predicate
     */
    public static <T extends IMObjectRelationship> Predicate<T> sourceEquals(IMObject object) {
        return new RefEquals<>(object, IMObjectRelationship::getSource);
    }

    /**
     * Returns a predicate that determines if the source of an {@link IMObjectRelationship} is that of the supplied
     * reference.
     *
     * @param object the object. May be {@code null}
     * @return a predicate
     */
    public static <T extends IMObjectRelationship> Predicate<T> sourceEquals(IMObjectReference object) {
        return new RefEquals<>(object, IMObjectRelationship::getSource);
    }

    /**
     * Returns a predicate that determines if the target of an {@link IMObjectRelationship} is that of the supplied
     * object.
     *
     * @param object the object. May be {@code null}
     * @return a predicate
     */
    public static <T extends IMObjectRelationship> Predicate<T> targetEquals(IMObject object) {
        return new RefEquals<>(object, IMObjectRelationship::getTarget);
    }

    /**
     * Returns a predicate that determines if an object is one of the specified set of archetypes.
     *
     * @param archetypes the archetypes
     * @return a new predicate
     */
    public static <T extends IMObject> Predicate<T> isA(String... archetypes) {
        return object -> TypeHelper.isA(object, archetypes);
    }

    /**
     * Returns a predicate that determines if the target of an {@link IMObjectRelationship} is that of the supplied
     * reference.
     *
     * @param object the object. May be {@code null}
     * @return a predicate
     */
    public static <T extends IMObjectRelationship> Predicate<T> targetEquals(IMObjectReference object) {
        return new RefEquals<>(object, IMObjectRelationship::getTarget);
    }

    private static class IsActive<T extends IMObjectRelationship> implements Predicate<T> {
        /**
         * Determines if a relationship is active.
         *
         * @param relationship the relationship
         * @return {@code true} if the relationship matches the predicate, otherwise {@code false}
         */
        @Override
        public boolean test(T relationship) {
            return relationship.isActive();
        }

    }

    private static class IsActiveAt<T extends IMObjectRelationship> extends IsActive<T> {

        /**
         * The time to compare with. If {@code -1}, indicates to use the
         * system time at the time of comparison.
         */
        private final long time;

        /**
         * Constructs an {@link IsActiveAt} that evaluates {@code true} for all relationships that are active as of the
         * time of evaluation.
         */
        public IsActiveAt() {
            this(-1);
        }

        /**
         * Constructs an {@link IsActiveAt} that evaluates {@code true} for all relationships that are active at the
         * specified time.
         *
         * @param time the time to compare against
         */
        public IsActiveAt(Date time) {
            this(time.getTime());
        }

        /**
         * Creates a new {@code Predicates} that evaluates {@code true}
         * for all relationships that are active at the specified time.
         *
         * @param time the time to compare against. If {@code -1}, indicates to use the system time at evaluation
         */
        public IsActiveAt(long time) {
            this.time = time;
        }


        /**
         * Determines if a relationship is active.
         *
         * @param relationship the object to evaluate. Must be an {@code IMObjectRelationship}
         *                     or {@code PeriodRelationship}
         * @return {@code true} if the relationship is active, otherwise {@code false}
         */
        @Override
        public boolean test(T relationship) {
            boolean result;
            if (relationship instanceof PeriodRelationship) {
                PeriodRelationship period = (PeriodRelationship) relationship;
                result = (time == -1) ? period.isActive() : period.isActive(time);
            } else {
                result = super.test(relationship);
            }
            return result;
        }
    }

    private static class IsActiveRange<T extends IMObjectRelationship> extends IsActive<T> {

        /**
         * The from date. May be {@code null}
         */
        private final Date from;

        /**
         * The to date. May be {@code null}
         */
        private final Date to;

        /**
         * Constructs an {@link Predicates.IsActiveRange}.
         *
         * @param from the from date. May be {@code null}
         * @param to   the to date. May be {@code null}
         */
        public IsActiveRange(Date from, Date to) {
            this.from = from;
            this.to = to;
        }

        /**
         * Determines if a relationship is active.
         *
         * @param relationship the relationship
         * @return {@code true} if the relationship matches the predicate, otherwise {@code false}
         */
        @Override
        public boolean test(T relationship) {
            boolean result;
            if (relationship instanceof PeriodRelationship) {
                PeriodRelationship period = (PeriodRelationship) relationship;
                result = period.isActive(from, to);
            } else {
                result = super.test(relationship);
            }
            return result;
        }

    }

    private static class RefEquals<T extends IMObjectRelationship> implements Predicate<T> {
        /**
         * The reference to compare.
         */
        private final IMObjectReference ref;

        private Function<T, IMObjectReference> accessor;

        public RefEquals(IMObject object, Function<T, IMObjectReference> accessor) {
            this(object != null ? object.getObjectReference() : null, accessor);
        }

        public RefEquals(IMObjectReference ref, Function<T, IMObjectReference> accessor) {
            this.ref = ref;
            this.accessor = accessor;
        }

        /**
         * Evaluates this predicate on the given argument.
         *
         * @param relationship the input argument
         * @return {@code true} if the input argument matches the predicate,
         * otherwise {@code false}
         */
        @Override
        public boolean test(T relationship) {
            return ObjectUtils.equals(ref, accessor.apply(relationship));
        }
    }

}
