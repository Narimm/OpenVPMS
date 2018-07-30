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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow;

import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.entity.Entity;

import java.util.Collections;
import java.util.Date;


/**
 * Queries <em>act.calendarEvent</em> acts, returning a limited set of data for display purposes.
 *
 * @author Tim Anderson
 */
class CalendarEventQuery extends ScheduleEventQuery {

    /**
     * Constructs an {@link CalendarEventQuery}.
     *
     * @param schedule the schedule
     * @param from     the 'from' start time
     * @param to       the 'to' start time
     * @param service  the archetype service
     */
    public CalendarEventQuery(Entity schedule, Date from, Date to, IArchetypeService service) {
        super(schedule, from, to, Collections.emptyMap(), Collections.emptyMap(), service);
    }

    /**
     * Returns the name of the named query to execute.
     *
     * @return the name of the named query
     */
    protected String getQueryName() {
        return "calendarEvents";
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
