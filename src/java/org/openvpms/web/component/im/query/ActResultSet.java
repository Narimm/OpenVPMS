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
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.Date;


/**
 * Result set for {@link Act}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActResultSet<T extends Act> extends AbstractActResultSet<T> {

    /**
     * Constructs a new <tt>ActResultSet</tt>.
     *
     * @param archetypes  the act archetype constraint
     * @param participant the participant constraint
     * @param from        the act start-from date. May be <tt>null</tt>
     * @param to          the act start-to date. May be <tt>null</tt>
     * @param statuses    the act statuses. If empty, indicates all acts
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be <tt>null</tt>
     */
    public ActResultSet(ShortNameConstraint archetypes,
                        ParticipantConstraint participant,
                        Date from, Date to,
                        String[] statuses, int pageSize,
                        SortConstraint[] sort) {
        super(archetypes, participant, from, to, statuses, false, null,
              pageSize, sort);
    }

    /**
     * Constructs a new <tt>ActResultSet</tt>.
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
    public ActResultSet(ShortNameConstraint archetypes,
                        ParticipantConstraint participant,
                        Date from, Date to,
                        String[] statuses, boolean exclude,
                        IConstraint constraints, int pageSize,
                        SortConstraint[] sort) {
        super(archetypes, participant, from, to, statuses, exclude, constraints,
              pageSize, sort);
    }

    /**
     * Constructs a new <tt>ActResultSet</tt>.
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
    public ActResultSet(ShortNameConstraint archetypes,
                        ParticipantConstraint[] participants,
                        Date from, Date to,
                        String[] statuses, boolean exclude,
                        IConstraint constraints, int pageSize,
                        SortConstraint[] sort) {
        super(archetypes, participants, from, to, statuses, exclude,
              constraints, pageSize, sort);
    }

    /**
     * Constructs a new <tt>ActResultSet</tt>.
     *
     * @param archetypes   the act archetype constraint
     * @param participants the participant constraints. May be <tt>null</tt>
     * @param times        the time constraints. May be <tt>null</tt>
     * @param statuses     the act statuses. If empty, indicates all acts
     * @param exclude      if <tt>true</tt> exclude acts with status in
     *                     <tt>statuses</tt>; otherwise include them.
     * @param constraints  additional query constraints. May be <tt>null</tt>
     * @param pageSize     the maximum no. of results per page
     * @param sort         the sort criteria. May be <tt>null</tt>
     */
    public ActResultSet(ShortNameConstraint archetypes,
                        ParticipantConstraint[] participants,
                        IConstraint times, String[] statuses, boolean exclude,
                        IConstraint constraints, int pageSize,
                        SortConstraint[] sort) {
        super(archetypes, participants, times, statuses, exclude, constraints,
              pageSize, sort);
    }

}
