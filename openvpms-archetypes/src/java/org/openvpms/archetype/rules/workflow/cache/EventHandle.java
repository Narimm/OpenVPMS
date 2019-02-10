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

import org.openvpms.component.system.common.util.PropertySet;

import java.util.Date;

/**
 * A handle to an {@link Event}.
 * <p>
 * This holds the modCount of the Event when the handle was constructed, in order to detect changes to the Event.
 * <p>
 * Note that access to EventHandle is single-threaded via DayCache, so it doesn't need its own synchronization.
 */
class EventHandle {

    /**
     * The event.
     */
    private final Event event;

    /**
     * The modification count of the event, when the handle was constructed.
     */
    private int modCount;

    /**
     * Constructs an {@link EventHandle}.
     *
     * @param event    the event
     * @param modCount the event modification count
     */
    EventHandle(Event event, int modCount) {
        this.event = event;
        this.modCount = modCount;
    }

    /**
     * Returns the event.
     * <p>
     * If the event has been changed since the handle was constructed, the following may occur;
     * <ul>
     * <li>the event may no longer be for the entityId and date range. In this case {@code null} is returned.</li>
     * <li>the event is still applicable. In this case, {@link #setModCount(int) modCount} is updated.</li>
     * </ul>
     *
     * @param entityId the entity id the event is expected to be for
     * @param from     the start of the date range
     * @param to       the end of the date range
     * @return the event, or {@code null} if the event is not for the expected entity and day
     */
    public PropertySet getEvent(long entityId, Date from, Date to) {
        return event.getEvent(entityId, from, to, this);
    }

    /**
     * Returns the event modification count.
     *
     * @return the modification count.
     */
    int getModCount() {
        return modCount;
    }

    /**
     * Updates the modification count.
     *
     * @param modCount the modification count
     */
    void setModCount(int modCount) {
        this.modCount = modCount;
    }
}
