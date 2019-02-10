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

import org.openvpms.archetype.rules.workflow.ScheduleEventQuery;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.entity.Entity;

import java.util.Date;

/**
 * A factory for <em>act.rosterEvent</em> that queries events by roster area.
 *
 * @author Tim Anderson
 */
class RosterEventByAreaFactory extends RosterEventFactory {

    /**
     * Constructs an {@link RosterEventByAreaFactory}.
     *
     * @param service the archetype service
     */
    RosterEventByAreaFactory(IArchetypeService service) {
        super(service);
    }

    /**
     * Creates a query to query events for a particular entity between two times.
     *
     * @param entity    the entity
     * @param startTime the start time, inclusive
     * @param endTime   the end time, exclusive
     * @return a new query
     */
    @Override
    protected ScheduleEventQuery createQuery(Entity entity, Date startTime, Date endTime) {
        return new RosterEventByAreaQuery(entity, startTime, endTime, getService());
    }

}
