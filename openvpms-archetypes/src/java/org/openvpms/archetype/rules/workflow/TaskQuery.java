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

package org.openvpms.archetype.rules.workflow;

import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Collections;
import java.util.Date;
import java.util.Map;


/**
 * Queries <em>act.customerTask</em> acts, returning a limited set of data for display purposes.
 *
 * @author Tim Anderson
 */
class TaskQuery extends ScheduleEventQuery {

    /**
     * Constructs a {@link TaskQuery}.
     *
     * @param workList    the schedule
     * @param from        the 'from' start time
     * @param to          the 'to' start time
     * @param statusNames the status names, keyed on status code
     * @param service     the archetype service
     */
    TaskQuery(Entity workList, Date from, Date to, Map<String, String> statusNames, IArchetypeService service) {
        super(workList, from, to, statusNames, Collections.emptyMap(), service);
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
        boolean result;
        if (ScheduleArchetypes.WORKLIST_PARTICIPATION.equals(archetype)) {
            set.set(ScheduleEvent.SCHEDULE_REFERENCE, entityRef);
            set.set(ScheduleEvent.SCHEDULE_NAME, entityName);
            set.set(ScheduleEvent.SCHEDULE_PARTICIPATION_VERSION, version);
            result = true;
        } else {
            result = super.populateParticipation(set, archetype, entityRef, entityName, version);
        }
        return result;
    }

    /**
     * Returns the name of the named query to execute.
     *
     * @return the name of the named query
     */
    protected String getQueryName() {
        return "taskEvents";
    }

    /**
     * Returns the archetype of the schedule type.
     *
     * @param eventArchetype the event archetype
     * @return the archetype of the schedule type
     */
    protected String getScheduleType(String eventArchetype) {
        return ScheduleArchetypes.TASK_TYPE;
    }

    /**
     * Creates a new {@link ObjectSet} representing a scheduled event.
     *
     * @param actRef the reference of the event act
     * @param set    the source set
     * @return a new event
     */
    @Override
    protected ObjectSet createEvent(Reference actRef, ObjectSet set) {
        ObjectSet event = super.createEvent(actRef, set);
        event.set(ScheduleEvent.CONSULT_START_TIME, null);
        return event;
    }

}
