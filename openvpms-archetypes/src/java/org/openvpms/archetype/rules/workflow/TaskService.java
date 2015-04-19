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

import net.sf.ehcache.Ehcache;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;


/**
 * Implementation of the {@link ScheduleService} for tasks.
 *
 * @author Tim Anderson
 */
public class TaskService extends AbstractScheduleService {

    /**
     * Constructs a {@link TaskService}.
     *
     * @param service the archetype service
     * @param cache   the cache
     */
    public TaskService(IArchetypeService service, ILookupService lookupService, Ehcache cache) {
        super(ScheduleArchetypes.TASK, service, cache, new TaskFactory(service, lookupService));
    }

}
