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

import net.sf.ehcache.Ehcache;
import org.openvpms.component.business.service.archetype.IArchetypeService;

/**
 * Implementation of the {@link ScheduleService} for calendar events.
 *
 * @author Tim Anderson
 */
public class CalendarService extends AbstractCalendarService {

    /**
     * Constructs a {@link CalendarService}.
     *
     * @param service the archetype service
     * @param cache   the event cache
     */
    public CalendarService(IArchetypeService service, Ehcache cache) {
        super(new String[]{ScheduleArchetypes.CALENDAR_EVENT}, service, cache, new CalendarEventFactory(service));
    }
}
