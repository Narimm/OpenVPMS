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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow.roster;

import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.user.User;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.NamedQuery;

import java.util.Date;


/**
 * Queries <em>act.rosterEvent</em> acts by user, returning a limited set of data for display purposes.
 *
 * @author Tim Anderson
 */
class RosterEventByUserQuery extends RosterEventQuery {

    /**
     * Constructs an {@link RosterEventByUserQuery}.
     *
     * @param user    the user
     * @param from    the 'from' start time
     * @param to      the 'to' start time
     * @param service the archetype service
     */
    RosterEventByUserQuery(User user, Date from, Date to, IArchetypeService service) {
        super(user, from, to, service);
    }

    /**
     * Returns the name of the named query to execute.
     *
     * @return the name of the named query
     */
    protected String getQueryName() {
        return "rosterEventsByUser";
    }

    /**
     * Creates a new query.
     *
     * @param schedule the schedule
     * @param from     the from date
     * @param to       the to date
     * @return the query
     */
    protected IArchetypeQuery createQuery(Entity schedule, Date from, Date to) {
        NamedQuery query = new NamedQuery(getQueryName(), NAMES);
        query.setParameter("userId", schedule.getId());
        query.setParameter("from", from);
        query.setParameter("to", to);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        return query;
    }
}
