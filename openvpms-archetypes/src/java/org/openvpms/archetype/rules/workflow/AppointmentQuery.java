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
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Date;
import java.util.Map;


/**
 * Queries <em>act.customerAppointment</em> and <em>act.calendarBlock</em> acts, returning a limited set of data for
 * display purposes.
 *
 * @author Tim Anderson
 */
class AppointmentQuery extends ScheduleEventQuery {

    /**
     * Constructs an {@link AppointmentQuery}.
     *
     * @param schedule    the schedule
     * @param from        the 'from' start time
     * @param to          the 'to' start time
     * @param statusNames the status names, keyed on status code
     * @param reasonNames the reason names, keyed on reason code
     * @param service     the archetype service
     */
    public AppointmentQuery(Entity schedule, Date from, Date to, Map<String, String> statusNames,
                            Map<String, String> reasonNames, IArchetypeService service) {
        super(schedule, from, to, statusNames, reasonNames, service);
    }

    /**
     * Returns the name of the named query to execute.
     *
     * @return the name of the named query
     */
    protected String getQueryName() {
        return "appointmentEvents";
    }

    /**
     * Returns the archetype of the schedule type.
     *
     * @param eventArchetype the event archetype
     * @return the archetype of the schedule type
     */
    protected String getScheduleType(String eventArchetype) {
        return ScheduleArchetypes.APPOINTMENT.equals(eventArchetype) ?
               ScheduleArchetypes.APPOINTMENT_TYPE : ScheduleArchetypes.CALENDAR_BLOCK_TYPE;
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
        if (actRef.isA(ScheduleArchetypes.APPOINTMENT)) {
            event.set(ScheduleEvent.ARRIVAL_TIME, null);
            event.set(ScheduleEvent.SEND_REMINDER, null);
            event.set(ScheduleEvent.REMINDER_SENT, null);
            event.set(ScheduleEvent.REMINDER_ERROR, null);
        }
        return event;
    }
}
