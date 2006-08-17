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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.AndConstraint;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IConstraintContainer;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.spring.ServiceHelper;

import java.util.Date;


/**
 * Result set for {@link Act}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActResultSet extends AbstractArchetypeServiceResultSet<Act> {

    /**
     * The act archetype criteria.
     */
    private final BaseArchetypeConstraint _archetypes;

    /**
     * The participant constraints.
     */
    private ParticipantConstraint[] _participants;

    /**
     * The status criteria. May be <code>null</code>.
     */
    private final IConstraint _statuses;

    /**
     * The start-time criteria. May be <code>null</code>.
     */
    private final NodeConstraint _startTime;

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(ActResultSet.class);


    /**
     * Construct a new <code>ActResultSet</code>.
     *
     * @param participant the participant constraint
     * @param archetypes  the act archetype constraint
     * @param from        the act start-from date. May be <code>null</code>
     * @param to          the act start-to date. May be <code>null</code>
     * @param statuses    the act statuses. If empty, indicates all acts
     * @param rows        the maximum no. of rows per page
     * @param sort        the sort criteria. May be <code>null</code>
     */
    public ActResultSet(ParticipantConstraint participant,
                        BaseArchetypeConstraint archetypes, Date from, Date to,
                        String[] statuses, int rows, SortConstraint[] sort) {
        this(participant, archetypes, from, to, statuses, false, null, rows,
             sort);
    }

    /**
     * Construct a new <code>ActResultSet</code>.
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
     * @param rows        the maximum no. of rows per page
     * @param sort        the sort criteria. May be <code>null</code>
     */
    public ActResultSet(ParticipantConstraint participant,
                        BaseArchetypeConstraint archetypes, Date from, Date to,
                        String[] statuses, boolean exclude,
                        IConstraint constraints, int rows,
                        SortConstraint[] sort) {
        this(new ParticipantConstraint[]{participant}, archetypes, from, to,
             statuses, exclude, constraints, rows, sort);
    }

    /**
     * Construct a new <code>ActResultSet</code>.
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
     * @param rows         the maximum no. of rows per page
     * @param sort         the sort criteria. May be <code>null</code>
     */
    public ActResultSet(ParticipantConstraint[] participants,
                        BaseArchetypeConstraint archetypes, Date from, Date to,
                        String[] statuses, boolean exclude,
                        IConstraint constraints, int rows,
                        SortConstraint[] sort) {
        super(constraints, rows, sort);
        _participants = participants;
        _archetypes = archetypes;

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
            _statuses = constraint;
        } else if (statuses.length == 1) {
            RelationalOp op = RelationalOp.EQ;
            if (exclude) {
                op = RelationalOp.NE;
            }
            _statuses = new NodeConstraint("status", op, statuses[0]);
        } else {
            _statuses = null;
        }

        if (from != null && to != null) {
            _startTime = new NodeConstraint("startTime", RelationalOp.BTW, from,
                                            to);
        } else {
            _startTime = null;
        }
    }

    /**
     * Returns the specified page.
     *
     * @param firstRow the first row of the page to retrieve
     * @param maxRows  the maximun no of rows in the page
     * @return the page corresponding to <code>firstRow</code>, or
     *         <code>null</code> if none exists
     */
    protected IPage<Act> getPage(int firstRow, int maxRows) {
        IPage<Act> result = null;
        try {
            ArchetypeQuery query = getQuery(firstRow, maxRows);
            for (SortConstraint sort : getSortConstraints()) {
                query.add(sort);
            }

            IArchetypeService service = ServiceHelper.getArchetypeService();
            IPage<IMObject> page = service.get(query);
            result = convert(page);
        } catch (OpenVPMSException exception) {
            _log.error(exception, exception);
        }
        return result;
    }

    /**
     * Returns the query.
     *
     * @param firstRow the first row of the page to retrieve
     * @param maxRows  the maximun no of rows in the page
     * @return the query
     */
    protected ArchetypeQuery getQuery(int firstRow, int maxRows) {
        ArchetypeQuery query = new ArchetypeQuery(_archetypes);
        query.setFirstRow(firstRow);
        query.setNumOfRows(maxRows);
        query.setDistinct(isDistinct());

        if (_statuses != null) {
            query.add(_statuses);
        }
        if (_startTime != null) {
            query.add(_startTime);
        }

        for (ParticipantConstraint participant : _participants) {
            query.add(participant);
        }

        IConstraint constraints = getConstraints();
        if (constraints != null) {
            query.add(constraints);
        }
        return query;
    }

}
