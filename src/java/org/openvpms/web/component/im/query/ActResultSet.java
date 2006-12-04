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
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.Arrays;
import java.util.Date;


/**
 * Result set for {@link Act}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActResultSet extends AbstractActResultSet<Act> {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ActResultSet.class);


    /**
     * Construct a new <code>ActResultSet</code>.
     *
     * @param participant the participant constraint
     * @param archetypes  the act archetype constraint
     * @param from        the act start-from date. May be <code>null</code>
     * @param to          the act start-to date. May be <code>null</code>
     * @param statuses    the act statuses. If empty, indicates all acts
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be <code>null</code>
     */
    public ActResultSet(ParticipantConstraint participant,
                        BaseArchetypeConstraint archetypes, Date from, Date to,
                        String[] statuses, int pageSize,
                        SortConstraint[] sort) {
        super(participant, archetypes, from, to, statuses, false, null,
              pageSize, sort);
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
     * @param pageSize    the maximum no. of results per page
     * @param sort        the sort criteria. May be <code>null</code>
     */
    public ActResultSet(ParticipantConstraint participant,
                        BaseArchetypeConstraint archetypes, Date from, Date to,
                        String[] statuses, boolean exclude,
                        IConstraint constraints, int pageSize,
                        SortConstraint[] sort) {
        super(new ParticipantConstraint[]{participant}, archetypes, from, to,
              statuses, exclude, constraints, pageSize, sort);
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
     * @param pageSize     the maximum no. of results per page
     * @param sort         the sort criteria. May be <code>null</code>
     */
    public ActResultSet(ParticipantConstraint[] participants,
                        BaseArchetypeConstraint archetypes, Date from, Date to,
                        String[] statuses, boolean exclude,
                        IConstraint constraints, int pageSize,
                        SortConstraint[] sort) {
        super(participants, archetypes, from, to, statuses, exclude,
              constraints, pageSize, sort);
    }

    /**
     * Construct a new <code>ActResultSet</code>.
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
    public ActResultSet(ParticipantConstraint[] participants,
                        BaseArchetypeConstraint archetypes,
                        IConstraint times, String[] statuses, boolean exclude,
                        IConstraint constraints, int pageSize,
                        SortConstraint[] sort) {
        super(participants, archetypes, times, statuses, exclude, constraints,
              pageSize, sort);
    }

    /**
     * Returns the specified page.
     *
     * @param firstResult the first result of the page to retrieve
     * @param maxResults  the maximun no of results in the page
     * @return the page corresponding to <code>firstResult</code>, or
     *         <code>null</code> if none exists
     */
    protected IPage<Act> getPage(int firstResult, int maxResults) {
        IPage<Act> result = null;
        try {
            ArchetypeQuery query = getQuery(firstResult, maxResults);
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            String[] nodes = getNodes();
            IPage<IMObject> page;
            if (nodes == null || nodes.length == 0) {
                page = service.get(query);
            } else {
                page = service.get(query, Arrays.asList(nodes));
            }
            result = convert(page);
        } catch (OpenVPMSException exception) {
            log.error(exception, exception);
        }
        return result;
    }

}
