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
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.act.Participation;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Date;

/**
 * A factory for tasks.
 *
 * @author Tim Anderson
 */
class TaskFactory extends ScheduleEventFactory {

    /**
     * Constructs a {@link TaskFactory}.
     *
     * @param service the archetype service
     * @param lookups the lookup service
     */
    TaskFactory(IArchetypeService service, ILookupService lookups) {
        super(ScheduleArchetypes.TASK, service, lookups);
    }

    /**
     * Assembles an {@link PropertySet PropertySet} from a source act.
     *
     * @param target the target set
     * @param source the source act
     */
    @Override
    protected void assemble(PropertySet target, IMObjectBean source) {
        super.assemble(target, source);

        Participation schedule = source.getObject("worklist", Participation.class);
        populate(target, schedule, "schedule");

        Participation taskType = source.getObject("taskType", Participation.class);
        populate(target, taskType, "scheduleType");

        String reason = ((Act) source.getObject()).getReason();
        target.set(ScheduleEvent.ACT_REASON, reason);
        target.set(ScheduleEvent.ACT_REASON_NAME, reason);

        target.set(ScheduleEvent.CONSULT_START_TIME, source.getDate(ScheduleEvent.CONSULT_START_TIME));
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
        return new TaskQuery(entity, startTime, endTime, getStatusNames(), getService());
    }
}
