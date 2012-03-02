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
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.functor;

import org.apache.commons.collections.Predicate;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.common.PeriodRelationship;

import java.util.Date;


/**
 * Predicate to determine if an {@link IMObjectRelationship} or {@link PeriodRelationship PeriodRelationship} is active
 * or not.
 * 
 * If the relationship is an {@link PeriodRelationship}, it is active as per {@link PeriodRelationship#isActive(Date)},
 * else it is active as per {@link IMObjectRelationship#isActive()}.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-09-04 06:34:35Z $
 */
public class IsActiveRelationship implements Predicate {

    /**
     * Predicate that determines if relationships are active as at the time
     * of evaluation.
     */
    public static final Predicate ACTIVE_NOW = new IsActiveRelationship();


    /**
     * The time to compare with. If <tt>-1</tt>, indicates to use the
     * system time at the time of comparison.
     */
    private final long time;


    /**
     * Creates a new <tt>IsActiveRelationship</tt> that evaluates <tt>true</tt>
     * for all relationships that are active as of the time of evaluation.
     */
    public IsActiveRelationship() {
        this(-1);
    }

    /**
     * Creates a new <tt>IsActiveRelationship</tt> that evaluates <tt>true</tt>
     * for all relationships that are active at the specified time.
     *
     * @param time the time to compare against
     */
    public IsActiveRelationship(Date time) {
        this(time.getTime());
    }

    /**
     * Creates a new <tt>IsActiveRelationship</tt> that evaluates <tt>true</tt>
     * for all relationships that are active at the specified time.
     *
     * @param time the time to compare against. If <tt>-1</tt>, indicates
     *             to use the system time at evaluation
     */
    public IsActiveRelationship(long time) {
        this.time = time;
    }

    /**
     * Determines if a relationship is active.
     *
     * @param object the object to evaluate. Must be an <tt>IMObjectRelationship</tt> or <tt>PeriodRelationship</tt>
     * @return <tt>true</tt> if the relationship is active, otherwise <tt>false</tt>
     * @throws ClassCastException if the input is the wrong class
     */
    public boolean evaluate(Object object) {
        boolean result;
        if (object instanceof PeriodRelationship) {
            PeriodRelationship relationship = (PeriodRelationship) object;
            result =  (time == -1) ? relationship.isActive() : relationship.isActive(time);
        } else {
            IMObjectRelationship relationship = (IMObjectRelationship) object;
            result = relationship.isActive();
        }
        return result;
    }

}
