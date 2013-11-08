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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
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
     * Cache of appointment reason lookup names, keyed on code.
     */
    private final Map<String, String> reasonNames = Collections.synchronizedMap(new HashMap<String, String>());

    /**
     * Constructs an {@link AppointmentFactory}.
     *
     * @param service       the archetype service
     * @param lookupService the lookup service
     */
    public AppointmentFactory(IArchetypeService service, ILookupService lookupService) {
        super(ScheduleArchetypes.APPOINTMENT, service, lookupService);
        Map<String, String> map = LookupHelper.getNames(service, lookupService, ScheduleArchetypes.APPOINTMENT,
                                                        "reason");
        reasonNames.putAll(map);
    }

    /**
     * Caches an appointment reason.
     *
     * @param reason the reason to cache
     * @return {@code true} if the reason was already cached
     */
    public boolean addReason(Lookup reason) {
        return reasonNames.put(reason.getCode(), reason.getName()) != null;
    }

    /**
     * Removes a cached appointment reason.
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

        String reason = source.getAct().getReason();
        target.set(ScheduleEvent.ACT_REASON, reason);
        target.set(ScheduleEvent.ACT_REASON_NAME, reasonNames.get(reason));

        Participation schedule = source.getParticipation(ScheduleArchetypes.SCHEDULE_PARTICIPATION);
        IMObjectReference scheduleRef = (schedule != null) ? schedule.getEntity() : null;
        String scheduleName = getName(scheduleRef);
        target.set(ScheduleEvent.SCHEDULE_REFERENCE, scheduleRef);
        target.set(ScheduleEvent.SCHEDULE_NAME, scheduleName);
        target.set(ScheduleEvent.SCHEDULE_PARTICIPATION_VERSION, (schedule != null) ? schedule.getVersion() : -1);

        IMObjectReference typeRef = source.getNodeParticipantRef("appointmentType");
        String typeName = getName(typeRef);
        target.set(ScheduleEvent.SCHEDULE_TYPE_REFERENCE, typeRef);
        target.set(ScheduleEvent.SCHEDULE_TYPE_NAME, typeName);
        target.set(ScheduleEvent.ARRIVAL_TIME, source.getDate(ScheduleEvent.ARRIVAL_TIME));
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
        return new AppointmentQuery(schedule, DateRules.getDate(day), getEnd(day), getService());
    }
}
