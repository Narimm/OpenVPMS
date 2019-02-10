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

import org.openvpms.archetype.rules.workflow.ScheduleEventFactory;
import org.openvpms.archetype.rules.workflow.cache.AbstractEventCache;
import org.openvpms.archetype.rules.workflow.cache.Event;
import org.openvpms.component.business.service.cache.EhCacheable;
import org.openvpms.component.business.service.cache.EhcacheManager;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.util.PropertySet;

/**
 * Caches roster events by user.
 * <p/>
 * This caches date ranges.
 *
 * @author Tim Anderson
 */
class RosterUserCache extends AbstractEventCache implements EhCacheable {

    /**
     * Constructs a {@link RosterUserCache}.
     *
     * @param cacheFactory the cache factory
     * @param cacheName    the cache name
     * @param factory      the event factory
     */
    RosterUserCache(EhcacheManager cacheFactory, String cacheName, ScheduleEventFactory factory) {
        super(cacheFactory, cacheName, factory, false);
    }

    /**
     * Creates an event.
     *
     * @param set the underlying property set
     * @return a new event
     */
    @Override
    protected Event createEvent(PropertySet set) {
        return new Event(set) {
            /**
             * Returns the entity identifier for the entity referenced by the event.
             *
             * @param event the event
             * @return the entity identifier, or {@code -1} if no entity is referenced
             */
            @Override
            protected long getEntityId(PropertySet event) {
                Reference reference = event.getReference(RosterEvent.USER_REFERENCE);
                return reference != null ? reference.getId() : -1;
            }
        };
    }
}
