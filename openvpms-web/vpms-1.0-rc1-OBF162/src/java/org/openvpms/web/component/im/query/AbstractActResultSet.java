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
     * The status criteria. May be <tt>null</tt>.
     */
    private final IConstraint statuses;

    /**
     * The time criteria. May be <tt>null</tt>.
     */
    private final IConstraint times;


    /**
     * Constructs a new <tt>AbstractActResultSet</tt>.
     *
     * @param archetypes the act archetype constraint
     * @param pageSize   the maximum no. of results per page
     * @param sort       the sort criteria. May be <tt>null</tt>
     */
    public AbstractActResultSet(BaseArchetypeConstraint archetypes,
                                int pageSize, SortConstraint[] sort) {
        this(archetypes, null, null, null, null, pageSize, sort);
    }

    /**
     * Constructs a new <tt>AbstractActResultSet</tt>.
     *
     * @param archetypes  the act archetype constraint
     * @param participant the participant constraint. May be <tt>null</tt>
     * @param from        the act start-from date. May be <tt>null</tt>
     * @param to          the act start-to date. May be <tt>null</tt>
     * @param statuses    the act statuses. If empty, indicates all acts
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be <tt>null</tt>
     */
    public AbstractActResultSet(BaseArchetypeConstraint archetypes,
                                ParticipantConstraint participant,
                                Date from,
                                Date to,
                                String[] statuses, int pageSize,
                                SortConstraint[] sort) {
        this(archetypes, participant, from, to, statuses, false, null, pageSize,
             sort);
    }

    /**
     * Constructs a new <tt>AbstractActResultSet</tt>.
     *
     * @param archetypes  the act archetype constraint
     * @param participant the participant constraint. May be <tt>null</tt>
     * @param from        the act start-from date. May be <tt>null</tt>
     * @param to          the act start-to date. May be <tt>null</tt>
     * @param statuses    the act statuses. If empty, indicates all acts
     * @param exclude     if <tt>true</tt> exclude acts with status in
     *                    <tt>statuses</tt>; otherwise include them.
     * @param constraints additional query constraints. May be
     *                    <code<null</tt>
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be <tt>null</tt>
     */
    public AbstractActResultSet(BaseArchetypeConstraint archetypes,
                                ParticipantConstraint participant,
                                Date from,
                                Date to,
                                String[] statuses, boolean exclude,
                                IConstraint constraints, int pageSize,
                                SortConstraint[] sort) {
        this(archetypes, (participant != null)
                ? new ParticipantConstraint[]{participant} : null,
             from, to, statuses, exclude, constraints, pageSize, sort);
    }

    /**
     * Constructs a new <tt>AbstractActResultSet</tt>.
     *
     * @param archetypes   the act archetype constraint
     * @param participants the participant constraints. May be <tt>null</tt>
     * @param from         the act start-from date. May be <tt>null</tt>
     * @param to           the act start-to date. May be <tt>null</tt>
     * @param statuses     the act statuses. If empty, indicates all acts
     * @param exclude      if <tt>true</tt> exclude acts with status in
     *                     <tt>statuses</tt>; otherwise include them.
     * @param constraints  additional query constraints. May be
     *                     <code<null</tt>
     * @param pageSize     the maximum no. of results per page
     * @param sort         the sort criteria. May be <tt>null</tt>
     */
    public AbstractActResultSet(BaseArchetypeConstraint archetypes,
                                ParticipantConstraint[] participants,
                                Date from,
                                Date to,
                                String[] statuses, boolean exclude,
                                IConstraint constraints, int pageSize,
                                SortConstraint[] sort) {
        this(archetypes, participants, createTimeConstraint(from, to),
             statuses, exclude, constraints, pageSize, sort);
    }

    /**
     * Constructs a new <tt>AbstractActResultSet</tt>.
     *
     * @param archetypes   the act archetype constraint
     * @param participants the participant constraints. May be <tt>null</tt>
     * @param times        the time constraints. May be <tt>null</tt>
     * @param statuses     the act statuses. If empty, indicates all acts
     * @param exclude      if <tt>true</tt> exclude acts with status in
     *                     <tt>statuses</tt>; otherwise include them.
     * @param constraints  additional query constraints. May be
     *                     <code<null</tt>
     * @param pageSize     the maximum no. of results per page
     * @param sort         the sort criteria. May be <tt>null</tt>
     */
    public AbstractActResultSet(BaseArchetypeConstraint archetypes,
                                ParticipantConstraint[] participants,
                                IConstraint times, String[] statuses,
                                boolean exclude, IConstraint constraints,
                                int pageSize, SortConstraint[] sort) {
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
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query = new ArchetypeQuery(archetypes);
        if (statuses != null) {
            query.add(statuses);
        }
        if (times != null) {
            query.add(times);
        }

        if (participants != null) {
            for (ParticipantConstraint participant : participants) {
                query.add(participant);
            }
        }

        return query;
    }

    /**
     * Helper to create a constraint on startTime, if the from and to dates
     * are non-null.
     *
     * @param from the act start-from date. May be <tt>null</tt>
     * @param to   the act start-to date. May be <tt>null</tt>
     * @return a new constraint, if both dates are non-null, otherwise
     *         <tt>null</tt>
     */
    private static NodeConstraint createTimeConstraint(Date from, Date to) {
        if (from != null && to != null) {
            return new NodeConstraint("startTime", RelationalOp.BTW, from, to);
        }
        return null;
    }
}
