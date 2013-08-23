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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.functor;

import org.apache.commons.collections.Predicate;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.common.PeriodRelationship;

import java.util.Date;


/**
 * Predicate to determine if an {@link IMObjectRelationship} or {@link PeriodRelationship PeriodRelationship} is active
 * or not.
 * <p/>
 * If the relationship is an {@link PeriodRelationship}, it is active as per {@link PeriodRelationship#isActive(Date)},
 * else it is active as per {@link IMObjectRelationship#isActive()}.
 *
 * @author Tim Anderson
 */
public abstract class IsActiveRelationship implements Predicate {

    /**
     * Predicate that determines if relationships are active as at the time
     * of evaluation.
     */
    private static final Predicate ACTIVE_NOW = new IsActive();


    /**
     * Returns a predicate that determines if relationships are active as at the time of evaluation.
     *
     * @return a predicate that returns {@code true} if a relationship is active at time of evaluation
     */
    public static Predicate isActiveNow() {
        return ACTIVE_NOW;
    }

    /**
     * Creates a new predicate that evaluates {@code true} if an {@link IMObjectRelationship} or
     * {@link PeriodRelationship} is active.
     * The {@link PeriodRelationship} must be active at the specified time.
     *
     * @param time the time
     * @return a new predicate
     */
    public static Predicate isActive(Date time) {
        return new IsActive(time);
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
    public static Predicate isActive(Date from, Date to) {
        return new IsActiveRange(from, to);
    }

    /**
     * Determines if a relationship is active.
     *
     * @param object the object to evaluate. Must be an {@link IMObjectRelationship}
     * @return {@code true} if the relationship is active, otherwise {@code false}
     * @throws ClassCastException if the input is the wrong class
     */
    public boolean evaluate(Object object) {
        IMObjectRelationship relationship = (IMObjectRelationship) object;
        return relationship.isActive();
    }

    private static class IsActive extends IsActiveRelationship {

        /**
         * The time to compare with. If {@code -1}, indicates to use the
         * system time at the time of comparison.
         */
        private final long time;

        /**
         * Constructs an {@link IsActive} that evaluates {@code true} for all relationships that are active as of the
         * time of evaluation.
         */
        public IsActive() {
            this(-1);
        }

        /**
         * Constructs an {@link IsActive} that evaluates {@code true} for all relationships that are active at the
         * specified time.
         *
         * @param time the time to compare against
         */
        public IsActive(Date time) {
            this(time.getTime());
        }

        /**
         * Creates a new {@code IsActiveRelationship} that evaluates {@code true}
         * for all relationships that are active at the specified time.
         *
         * @param time the time to compare against. If {@code -1}, indicates to use the system time at evaluation
         */
        public IsActive(long time) {
            this.time = time;
        }

        /**
         * Determines if a relationship is active.
         *
         * @param object the object to evaluate. Must be an {@code IMObjectRelationship} or {@code PeriodRelationship}
         * @return {@code true} if the relationship is active, otherwise {@code false}
         * @throws ClassCastException if the input is the wrong class
         */
        @Override
        public boolean evaluate(Object object) {
            boolean result;
            if (object instanceof PeriodRelationship) {
                PeriodRelationship relationship = (PeriodRelationship) object;
                result = (time == -1) ? relationship.isActive() : relationship.isActive(time);
            } else {
                result = super.evaluate(object);
            }
            return result;
        }
    }

    private static class IsActiveRange extends IsActiveRelationship {

        /**
         * The from date. May be {@code null}
         */
        private final Date from;

        /**
         * The to date. May be {@code null}
         */
        private final Date to;

        /**
         * Constructs an {@link IsActiveRange}.
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
         * @param object the object to evaluate. Must be an {@link IMObjectRelationship} or {@link PeriodRelationship}
         * @return {@code true} if the relationship is active, otherwise {@code false}
         * @throws ClassCastException if the input is the wrong class
         */
        public boolean evaluate(Object object) {
            boolean result;
            if (object instanceof PeriodRelationship) {
                PeriodRelationship relationship = (PeriodRelationship) object;
                result = relationship.isActive(from, to);
            } else {
                result = super.evaluate(object);
            }
            return result;
        }

    }

}
