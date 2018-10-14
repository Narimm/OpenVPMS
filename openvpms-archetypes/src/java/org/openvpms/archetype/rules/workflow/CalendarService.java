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
import org.openvpms.component.business.service.cache.EhcacheManager;

/**
 * Implementation of the {@link ScheduleService} for calendar events.
 * <p>
 * This uses the supplied {@link EhcacheManager} to create a cache named "calendarCache".
 *
 * @author Tim Anderson
 */
public class CalendarService extends AbstractCalendarService {

    /**
     * Constructs a {@link CalendarService}.
     *
     * @param service      the archetype service
     * @param cacheManager the cache manager
     */
    public CalendarService(IArchetypeService service, EhcacheManager cacheManager) {
        super(new String[]{ScheduleArchetypes.CALENDAR_EVENT}, service, cacheManager, "calendarCache",
              new CalendarEventFactory(service));
    }
}
