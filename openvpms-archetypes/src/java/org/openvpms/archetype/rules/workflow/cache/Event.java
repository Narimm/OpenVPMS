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

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Date;

/**
 * Manages updates to cached events.
 *
 * @author Tim Anderson
 */
public abstract class Event {

    /**
     * The event identifier.
     */
    private final long id;

    /**
     * The event.
     */
    private PropertySet event;

    /**
     * The event version.
     */
    private long version;

    /**
     * The event entity identity.
     */
    private long entityId;

    /**
     * The modification count.
     */
    private int modCount;

    /**
     * Keys for version properties.
     */
    private static String[] VERSION_KEYS = {ScheduleEvent.SCHEDULE_PARTICIPATION_VERSION,
                                            ScheduleEvent.CUSTOMER_PARTICIPATION_VERSION,
                                            ScheduleEvent.PATIENT_PARTICIPATION_VERSION,
                                            ScheduleEvent.CLINICIAN_PARTICIPATION_VERSION,
                                            ScheduleEvent.SCHEDULE_TYPE_PARTICIPATION_VERSION};

    /**
     * Constructs an {@link Event}.
     *
     * @param event the event properties
     */
    public Event(PropertySet event) {
        id = event.getReference(ScheduleEvent.ACT_REFERENCE).getId();
        setEvent(event, event.getLong(ScheduleEvent.ACT_VERSION));
    }

    /**
     * Updates the event with that supplied, if it is the same or newer.
     *
     * @param event the event
     * @return {@code true} if the event was updated
     */
    public synchronized boolean update(PropertySet event) {
        boolean update = false;
        long otherVersion = event.getLong(ScheduleEvent.ACT_VERSION);
        if (version < otherVersion) {
            update = true;
        } else if (version == otherVersion) {
            int value = 0;
            for (String key : VERSION_KEYS) {
                value = compareVersions(key, event);
                if (value < 0) {
                    update = true;
                    break;
                } else if (value > 0) {
                    break;
                }
            }
            if (!update && value == 0) {
                // all of the versions are identical, but an event update has been received. Can't determine
                // if its newer or not than the existing event, but update anyway, just in case it includes
                // name changes
                update = true;
            }
        }
        if (update) {
            setEvent(event, otherVersion);
        }
        return update;
    }

    /**
     * Returns the event identifier.
     *
     * @return the event identifier
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the event.
     *
     * @return the event
     */
    public synchronized PropertySet getEvent() {
        return event;
    }

    /**
     * Determines in the event falls in a date range.
     *
     * @param from the start of the range
     * @param to   the end of the range
     * @return {@code true} if the event occurs in the date range
     */
    public synchronized boolean intersects(Date from, Date to) {
        Date startTime = event.getDate(ScheduleEvent.ACT_START_TIME);
        Date endTime = event.getDate(ScheduleEvent.ACT_END_TIME);
        return DateRules.intersects(from, to, startTime, endTime);
    }

    /**
     * Returns the entity identifier.
     *
     * @return the entity identifier
     */
    public synchronized long getEntityId() {
        return entityId;
    }

    /**
     * Creates a new handle for the event, if it is for the specified entity and date range.
     *
     * @param entityId the entity identifier
     * @param from     the from date
     * @param to       the to date
     * @return a new handle, or {@code null} if the event isn't for the entity or range
     */
    public synchronized EventHandle getHandle(long entityId, Date from, Date to) {
        return isFor(entityId, from, to) ? new EventHandle(this, modCount) : null;
    }

    /**
     * Returns the event, if it is for the specified entity and day.
     * <p>
     * If the handle modCount is not up-to-date, but the event still applies, it will be updated.
     *
     * @param entityId the entity identifier
     * @param from     the from date
     * @param to       date the to date
     * @param handle   the event handle, used for quick determination of event applicability
     * @return the event, or {@code null} if the event no longer applies
     */
    public synchronized PropertySet getEvent(long entityId, Date from, Date to, EventHandle handle) {
        PropertySet result;
        if (modCount == handle.getModCount()) {
            result = event;
        } else if (isFor(entityId, from, to)) {
            handle.setModCount(modCount);  // update the modCount
            result = event;
        } else {
            result = null; // handle is out date
        }
        return result;
    }

    /**
     * Returns the entity identifier for the entity referenced by the event.
     *
     * @param event the event
     * @return the entity identifier, or {@code -1} if no entity is referenced
     */
    protected abstract long getEntityId(PropertySet event);

    /**
     * Determines if this event belongs to a particular entity and date range.
     *
     * @param entityId the entity identifier
     * @param from     the from date
     * @param to       the to date
     * @return {@code true} if the event belongs to the entity and day
     */
    private boolean isFor(long entityId, Date from, Date to) {
        return this.entityId == entityId && intersects(from, to);
    }

    /**
     * Compares versions.
     *
     * @param key   the version key
     * @param other the set to compare with
     * @return {@code -1} if this has older version than {@code other}, {@code 0} if they are equal, {@code 1}
     * if this has a newer version
     */
    private int compareVersions(String key, PropertySet other) {
        if (!event.exists(key) || !other.exists(key)) {
            // the version isn't present in one or both of the sets. If its missing from one and not the other,
            // then a relationship has been added or removed; cannot tell which, without looking at the versions
            // on the parent events (these should have different versions anyway). In this case, just return that
            // they are equal.
            return 0;
        }
        long version = event.getLong(key);
        long otherVersion = other.getLong(key);
        return Long.compare(version, otherVersion);
    }

    /**
     * Sets the event.
     *
     * @param event   the event
     * @param version the event version
     */
    private void setEvent(PropertySet event, long version) {
        this.event = event;
        this.version = version;
        this.entityId = getEntityId(event);

        ++modCount;
    }
}
