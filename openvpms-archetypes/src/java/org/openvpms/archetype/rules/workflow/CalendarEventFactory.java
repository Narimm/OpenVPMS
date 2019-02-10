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
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Collections;
import java.util.Date;

/**
 * A factory for <em>act.calendarEvents</em>.
 *
 * @author Tim Anderson
 */
class CalendarEventFactory extends ScheduleEventFactory {

    /**
     * Constructs a {@link CalendarEventFactory}.
     *
     * @param service the archetype service
     */
    public CalendarEventFactory(IArchetypeService service) {
        super(Collections.emptyMap(), service);
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
        return new CalendarEventQuery(entity, startTime, endTime, getService());
    }

    /**
     * Assembles a {@link PropertySet PropertySet} from a source act.
     *
     * @param target the target set
     * @param source the source act
     */
    @Override
    protected void assemble(PropertySet target, IMObjectBean source) {
        populate(target, source, "schedule");
        target.set(ScheduleEvent.ACT_NAME, source.getObject().getName());

        super.assemble(target, source);
    }
}
