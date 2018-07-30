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

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Collections;
import java.util.Date;

/**
 * A factory for appointments.
 *
 * @author Tim Anderson
 */
class CalendarEventFactory extends ScheduleEventFactory {

    /**
     * Constructs an {@link CalendarEventFactory}.
     *
     * @param service the archetype service
     */
    public CalendarEventFactory(IArchetypeService service) {
        super(Collections.emptyMap(), service);
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
        return new CalendarEventQuery(schedule, DateRules.getDate(day), getEnd(day), getService());
    }

    /**
     * Assembles a {@link PropertySet PropertySet} from a source act.
     *
     * @param target the target set
     * @param source the source act
     */
    @Override
    protected void assemble(PropertySet target, ActBean source) {
        Participation schedule = source.getParticipation(ScheduleArchetypes.SCHEDULE_PARTICIPATION);
        Reference scheduleRef = (schedule != null) ? schedule.getEntity() : null;
        String scheduleName = getName(scheduleRef);
        target.set(ScheduleEvent.ACT_NAME, source.getAct().getName());
        target.set(ScheduleEvent.SCHEDULE_REFERENCE, scheduleRef);
        target.set(ScheduleEvent.SCHEDULE_NAME, scheduleName);
        target.set(ScheduleEvent.SCHEDULE_PARTICIPATION_VERSION, (schedule != null) ? schedule.getVersion() : -1);

        super.assemble(target, source);
    }
}
