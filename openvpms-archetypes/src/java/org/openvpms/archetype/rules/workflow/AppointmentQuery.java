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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Date;


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
     * @param schedule the schedule
     * @param from     the 'from' start time
     * @param to       the 'to' start time
     * @param service  the archetype service
     * @param lookups  the lookup service
     */
    public AppointmentQuery(Entity schedule, Date from, Date to, IArchetypeService service, ILookupService lookups) {
        super(schedule, from, to, ScheduleArchetypes.APPOINTMENT, service, lookups);
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
     * Returns the archetype short name of the schedule type.
     *
     * @param eventShortName the event short name
     * @return the short name of the schedule type
     */
    protected String getScheduleType(String eventShortName) {
        return ScheduleArchetypes.APPOINTMENT.equals(eventShortName) ?
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
    protected ObjectSet createEvent(IMObjectReference actRef, ObjectSet set) {
        ObjectSet event = super.createEvent(actRef, set);
        if (TypeHelper.isA(actRef, ScheduleArchetypes.APPOINTMENT)) {
            event.set(ScheduleEvent.ARRIVAL_TIME, null);
            event.set(ScheduleEvent.SEND_REMINDER, null);
            event.set(ScheduleEvent.REMINDER_SENT, null);
            event.set(ScheduleEvent.REMINDER_ERROR, null);
        }
        return event;
    }
}
