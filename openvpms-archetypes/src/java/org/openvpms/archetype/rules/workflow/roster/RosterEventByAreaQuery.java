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

import java.util.Date;


/**
 * Queries <em>act.rosterEvent</em> acts by area, returning a limited set of data for display purposes.
 *
 * @author Tim Anderson
 */
class RosterEventByAreaQuery extends RosterEventQuery {

    /**
     * Constructs an {@link RosterEventByAreaQuery}.
     *
     * @param area    the roster area
     * @param from    the 'from' start time
     * @param to      the 'to' start time
     * @param service the archetype service
     */
    RosterEventByAreaQuery(Entity area, Date from, Date to, IArchetypeService service) {
        super(area, from, to, service);
    }

    /**
     * Returns the name of the named query to execute.
     *
     * @return the name of the named query
     */
    protected String getQueryName() {
        return "rosterEventsByArea";
    }

}
