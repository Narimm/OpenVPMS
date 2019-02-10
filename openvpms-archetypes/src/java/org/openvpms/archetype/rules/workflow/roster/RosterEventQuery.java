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

import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleEventQuery;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Collections;
import java.util.Date;


/**
 * Queries <em>act.rosterEvent</em> acts, returning a limited set of data for display purposes.
 *
 * @author Tim Anderson
 */
abstract class RosterEventQuery extends ScheduleEventQuery {

    /**
     * Constructs an {@link RosterEventQuery}.
     *
     * @param schedule the schedule
     * @param from     the 'from' start time
     * @param to       the 'to' start time
     * @param service  the archetype service
     */
    RosterEventQuery(Entity schedule, Date from, Date to, IArchetypeService service) {
        super(schedule, from, to, Collections.emptyMap(), Collections.emptyMap(), service);
    }

    /**
     * Creates a new {@link ObjectSet ObjectSet} representing a scheduled event.
     *
     * @param actRef the reference of the event act
     * @param set    the source set
     * @return a new event
     */
    @Override
    protected ObjectSet createEvent(Reference actRef, ObjectSet set) {
        ObjectSet event = super.createEvent(actRef, set);
        event.set(RosterEvent.USER_REFERENCE, null);
        event.set(RosterEvent.USER_NAME, null);
        event.set(RosterEvent.LOCATION_REFERENCE, null);
        event.set(RosterEvent.LOCATION_NAME, null);
        return event;
    }

    /**
     * Populates a set with participation relationship details.
     *
     * @param set        the set to populate
     * @param archetype  the participation archetype
     * @param entityRef  the entity reference
     * @param entityName the entity name
     * @param version    the participation version
     * @return {@code true} if the set was populated
     */
    @Override
    protected boolean populateParticipation(ObjectSet set, String archetype, Reference entityRef, String entityName,
                                            long version) {
        boolean result = true;
        if (!super.populateParticipation(set, archetype, entityRef, entityName, version)) {
            switch (archetype) {
                case ScheduleArchetypes.AREA_PARTICIPATION:
                    populate(set, "schedule", entityRef, entityName, version);
                    break;
                case "participation.location":
                    populate(set, "location", entityRef, entityName, version);
                    break;
                case "participation.user":
                    populate(set, "user", entityRef, entityName, version);
                    break;
                default:
                    result = false;
                    break;
            }
        }
        return result;
    }

    /**
     * Returns the archetype of the schedule type.
     *
     * @param eventArchetype the event archetype
     * @return {@code null}
     */
    protected String getScheduleType(String eventArchetype) {
        return null;
    }

}
