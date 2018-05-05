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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory for appointments.
 *
 * @author Tim Anderson
 */
class AppointmentFactory extends ScheduleEventFactory {

    /**
     * Cache of visit reason lookup names, keyed on code.
     */
    private final Map<String, String> reasonNames = Collections.synchronizedMap(new HashMap<String, String>());

    /**
     * Constructs an {@link AppointmentFactory}.
     *
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public AppointmentFactory(IArchetypeService service, ILookupService lookups) {
        super(ScheduleArchetypes.APPOINTMENT, service, lookups);
        Map<String, String> map = LookupHelper.getNames(service, lookups, ScheduleArchetypes.APPOINTMENT, "reason");
        reasonNames.putAll(map);
    }

    /**
     * Caches a visit reason.
     *
     * @param reason the reason to cache
     * @return {@code true} if the reason was already cached
     */
    public boolean addReason(Lookup reason) {
        return reasonNames.put(reason.getCode(), reason.getName()) != null;
    }

    /**
     * Removes a cached visit reason.
     *
     * @param reason the reason to remove
     * @return {@code true} if the reason was removed
     */
    public boolean removeReason(Lookup reason) {
        return reasonNames.remove(reason.getCode()) != null;
    }

    /**
     * Assembles an {@link PropertySet PropertySet} from a source act.
     *
     * @param target the target set
     * @param source the source act
     */
    @Override
    protected void assemble(PropertySet target, ActBean source) {
        super.assemble(target, source);

        Participation schedule = source.getParticipation(ScheduleArchetypes.SCHEDULE_PARTICIPATION);
        IMObjectReference scheduleRef = (schedule != null) ? schedule.getEntity() : null;
        String scheduleName = getName(scheduleRef);
        target.set(ScheduleEvent.SCHEDULE_REFERENCE, scheduleRef);
        target.set(ScheduleEvent.SCHEDULE_NAME, scheduleName);
        target.set(ScheduleEvent.SCHEDULE_PARTICIPATION_VERSION, (schedule != null) ? schedule.getVersion() : -1);

        if (source.isA(ScheduleArchetypes.APPOINTMENT)) {
            String reason = source.getAct().getReason();
            target.set(ScheduleEvent.ACT_REASON, reason);
            target.set(ScheduleEvent.ACT_REASON_NAME, reasonNames.get(reason));

            IMObjectReference typeRef = source.getNodeParticipantRef("appointmentType");
            String typeName = getName(typeRef);
            target.set(ScheduleEvent.SCHEDULE_TYPE_REFERENCE, typeRef);
            target.set(ScheduleEvent.SCHEDULE_TYPE_NAME, typeName);
            target.set(ScheduleEvent.SEND_REMINDER, source.getBoolean(ScheduleEvent.SEND_REMINDER));
            target.set(ScheduleEvent.REMINDER_SENT, source.getDate(ScheduleEvent.REMINDER_SENT));
            target.set(ScheduleEvent.REMINDER_ERROR, source.getString(ScheduleEvent.REMINDER_ERROR));
            target.set(ScheduleEvent.ARRIVAL_TIME, source.getDate(ScheduleEvent.ARRIVAL_TIME));
            target.set(ScheduleEvent.ONLINE_BOOKING, source.getBoolean(ScheduleEvent.ONLINE_BOOKING));
            target.set(ScheduleEvent.BOOKING_NOTES, source.getString(ScheduleEvent.BOOKING_NOTES));
        } else {
            IMObjectReference typeRef = source.getNodeParticipantRef("type");
            String typeName = getName(typeRef);
            target.set(ScheduleEvent.ACT_NAME, source.getAct().getName());
            target.set(ScheduleEvent.SCHEDULE_TYPE_REFERENCE, typeRef);
            target.set(ScheduleEvent.SCHEDULE_TYPE_NAME, typeName);
        }
    }

    /**
     * Creates a query to query events for a particular schedule and day.
     *
     * @param schedule the schedule
     * @param day      the day
     * @return a new query
     */
    @Override
    protected ScheduleEventQuery createQuery(Entity schedule, Date day) {
        return new AppointmentQuery(schedule, DateRules.getDate(day), getEnd(day), getService(), getLookups());
    }
}
