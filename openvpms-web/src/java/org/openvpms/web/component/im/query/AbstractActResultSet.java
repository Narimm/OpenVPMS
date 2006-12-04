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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IConstraintContainer;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.Date;


/**
 * Result set for performing queries on {@link Act}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractActResultSet<T>
        extends AbstractArchetypeServiceResultSet<T> {

    /**
     * The act archetype criteria.
     */
    private final BaseArchetypeConstraint archetypes;

    /**
     * The participant constraints.
     */
    private ParticipantConstraint[] participants;

    /**
     * The status criteria. May be <code>null</code>.
     */
    private final IConstraint statuses;

    /**
     * The time criteria. May be <code>null</code>.
     */
    private final IConstraint times;


    /**
     * Construct a new <code>AbstractActResultSet</code>.
     *
     * @param participant the participant constraint
     * @param archetypes  the act archetype constraint
     * @param from        the act start-from date. May be <code>null</code>
     * @param to          the act start-to date. May be <code>null</code>
     * @param statuses    the act statuses. If empty, indicates all acts
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be <code>null</code>
     */
    public AbstractActResultSet(ParticipantConstraint participant,
                                BaseArchetypeConstraint archetypes, Date from,
                                Date to,
                                String[] statuses, int pageSize,
                                SortConstraint[] sort) {
        this(participant, archetypes, from, to, statuses, false, null, pageSize,
             sort);
    }

    /**
     * Construct a new <code>AbstractActResultSet</code>.
     *
     * @param participant the participant constraint
     * @param archetypes  the act archetype constraint
     * @param from        the act start-from date. May be <code>null</code>
     * @param to          the act start-to date. May be <code>null</code>
     * @param statuses    the act statuses. If empty, indicates all acts
     * @param exclude     if <code>true</code> exclude acts with status in
     *                    <code>statuses</code>; otherwise include them.
     * @param constraints additional query constraints. May be
     *                    <code<null</code>
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be <code>null</code>
     */
    public AbstractActResultSet(ParticipantConstraint participant,
                                BaseArchetypeConstraint archetypes, Date from,
                                Date to,
                                String[] statuses, boolean exclude,
                                IConstraint constraints, int pageSize,
                                SortConstraint[] sort) {
        this(new ParticipantConstraint[]{participant}, archetypes, from, to,
             statuses, exclude, constraints, pageSize, sort);
    }

    /**
     * Construct a new <code>AbstractActResultSet</code>.
     *
     * @param participants the participant constraints
     * @param archetypes   the act archetype constraint
     * @param from         the act start-from date. May be <code>null</code>
     * @param to           the act start-to date. May be <code>null</code>
     * @param statuses     the act statuses. If empty, indicates all acts
     * @param exclude      if <code>true</code> exclude acts with status in
     *                     <code>statuses</code>; otherwise include them.
     * @param constraints  additional query constraints. May be
     *                     <code<null</code>
     * @param pageSize     the maximum no. of results per page
     * @param sort         the sort criteria. May be <code>null</code>
     */
    public AbstractActResultSet(ParticipantConstraint[] participants,
                                BaseArchetypeConstraint archetypes, Date from,
                                Date to,
                                String[] statuses, boolean exclude,
                                IConstraint constraints, int pageSize,
                                SortConstraint[] sort) {
        this(participants, archetypes, createTimeConstraint(from, to),
             statuses, exclude, constraints, pageSize, sort);
    }

    /**
     * Construct a new <code>AbstractActResultSet</code>.
     *
     * @param participants the participant constraints
     * @param archetypes   the act archetype constraint
     * @param times        the time constraints. May be <code>null</code>
     * @param statuses     the act statuses. If empty, indicates all acts
     * @param exclude      if <code>true</code> exclude acts with status in
     *                     <code>statuses</code>; otherwise include them.
     * @param constraints  additional query constraints. May be
     *                     <code<null</code>
     * @param pageSize     the maximum no. of results per page
     * @param sort         the sort criteria. May be <code>null</code>
     */
    public AbstractActResultSet(ParticipantConstraint[] participants,
                                BaseArchetypeConstraint archetypes,
                                IConstraint times, String[] statuses,
                                boolean exclude,
                                IConstraint constraints, int pageSize,
                                SortConstraint[] sort) {
        super(constraints, pageSize, sort);
        this.participants = participants;
        this.archetypes = archetypes;

        if (statuses.length > 1) {
            IConstraintContainer constraint;
            RelationalOp op;
            if (exclude) {
                constraint = new AndConstraint();
                op = RelationalOp.NE;
            } else {
                constraint = new OrConstraint();
                op = RelationalOp.EQ;
            }
            for (String status : statuses) {
                constraint.add(new NodeConstraint("status", op, status));
            }
            this.statuses = constraint;
        } else if (statuses.length == 1) {
            RelationalOp op = RelationalOp.EQ;
            if (exclude) {
                op = RelationalOp.NE;
            }
            this.statuses = new NodeConstraint("status", op, statuses[0]);
        } else {
            this.statuses = null;
        }
        this.times = times;
    }

    /**
     * Returns the query.
     *
     * @param firstResult the first result of the page to retrieve
     * @param maxResults  the maximun no of results in the page
     * @return the query
     */
    protected ArchetypeQuery getQuery(int firstResult, int maxResults) {
        ArchetypeQuery query = new ArchetypeQuery(archetypes);
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        query.setDistinct(isDistinct());
        query.setCountResults(true);

        if (statuses != null) {
            query.add(statuses);
        }
        if (times != null) {
            query.add(times);
        }

        for (ParticipantConstraint participant : participants) {
            query.add(participant);
        }

        IConstraint constraints = getConstraints();
        if (constraints != null) {
            query.add(constraints);
        }

        for (SortConstraint sort : getSortConstraints()) {
            query.add(sort);
        }

        return query;
    }

    /**
     * Helper to create a constraint on startTime, if the from and to dates
     * are non-null.
     *
     * @param from the act start-from date. May be <code>null</code>
     * @param to   the act start-to date. May be <code>null</code>
     * @return a new constraint, if both dates are non-null, otherwise
     *         <code>null</code>
     */
    private static NodeConstraint createTimeConstraint(Date from, Date to) {
        if (from != null && to != null) {
            return new NodeConstraint("startTime", RelationalOp.BTW, from, to);
        }
        return null;
    }
}
