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

package org.openvpms.archetype.rules.workflow.cache;

import org.apache.commons.collections4.map.ReferenceMap;

import java.util.Date;

import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.HARD;
import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.WEAK;

/**
 * Manages multiple {@link DayCache} instances for an entity (e.g. a schedule).
 * <p>
 * It only holds weak references so they can be discarded when the EHCache no longer has them.
 * <p>
 * Each {@link DayCaches} is only weakly referenced by {@link AbstractEventCache} so they can be garbage collected
 * when there is no {@link DayCache} instance referencing them.
 */
class DayCaches {

    /**
     * A map of Date to {@link DayCache} instances.
     */
    private final ReferenceMap<Date, DayCache> days = new ReferenceMap<>(HARD, WEAK);

    /**
     * The entity identifier.
     */
    private long id;

    /**
     * Constructs an {@link DayCaches}.
     *
     * @param id the entity identifier
     */
    DayCaches(long id) {
        this.id = id;
    }

    /**
     * Returns the entity identifier.
     *
     * @return the entity identifier
     */
    public long getId() {
        return id;
    }

    /**
     * Adds a {@link DayCache}.
     *
     * @param day the day to add
     */
    public void add(DayCache day) {
        synchronized (days) {
            day.setOwner(this);
            days.put(day.getFrom(), day);
        }
    }

    /**
     * Adds an event.
     *
     * @param event the event to add
     */
    public void addEvent(Event event) {
        synchronized (days) {
            for (DayCache cache : days.values()) {
                cache.addIfIntersects(event);
            }
        }
    }

    /**
     * Removes an event.
     *
     * @param event the event to remove
     */
    public void removeEvent(Event event) {
        DayCache[] caches;
        synchronized (days) {
            caches = days.values().toArray(new DayCache[0]);
        }
        for (DayCache cache : caches) {
            cache.removeIfIntersects(event);
        }
    }

}
