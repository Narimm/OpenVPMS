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

import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.archetype.rules.workflow.ScheduleEventFactory;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Collections;

/**
 * Roster event factory.
 *
 * @author Tim Anderson
 */
abstract class RosterEventFactory extends ScheduleEventFactory {

    /**
     * Constructs a {@link RosterEventFactory}.
     *
     * @param service the archetype service
     */
    RosterEventFactory(IArchetypeService service) {
        super(Collections.emptyMap(), service);
    }

    /**
     * Assembles a {@link PropertySet PropertySet} from a source act.
     *
     * @param target the target set
     * @param source the source act
     */
    @Override
    protected void assemble(PropertySet target, IMObjectBean source) {
        super.assemble(target, source);
        target.set(ScheduleEvent.ACT_NAME, source.getObject().getName());
        populate(target, source, "schedule");
        populate(target, source, "user");
        populate(target, source, "location");
    }
}
