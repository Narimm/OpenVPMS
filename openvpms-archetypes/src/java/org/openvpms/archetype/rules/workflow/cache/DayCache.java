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

import org.apache.commons.lang.math.RandomUtils;
import org.ehcache.sizeof.annotations.IgnoreSizeOf;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.archetype.rules.workflow.ScheduleEvents;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Caches events related to a date range.
 *
 * @author Tim Anderson
 */
class DayCache {

    /**
     * The identifier for the entity that this cache is caching events for.
     */
    private final long entityId;

    /**
     * The from date, inclusive.
     */
    private final Date from;

    /**
     * The to date, exclusive.
     */
    private final Date to;

    /**
     * The owning instance. This reference is kept to ensure that the DayCaches instance isn't garbage collected
     * while the underlying cache has an instance of this.
     */
    @IgnoreSizeOf
    private DayCaches owner;

    /**
     * The events, keyed on act identifier. Note that the PropertySets that these refer to may change.
     */
    private Map<Long, EventHandle> map;

    /**
     * Changes that may need to be applied to the cache, after its events are populated.
     * <p>
     * This is required as events may be updated whilst they are being fetched from the database.
     */
    private List<Change> changes = new ArrayList<>();

    /**
     * The modification hash, used to determine if the object has changed.
     */
    private long modHash;

    /**
     * Constructs an {@link DayCache}.
     *
     * @param entityId the identifier for the entity that this cache is caching events for
     * @param from     the from date
     * @param to       the to date
     */
    DayCache(long entityId, Date from, Date to) {
        this.entityId = entityId;
        this.from = from;
        this.to = to;
        modHash = RandomUtils.nextLong();
    }

    /**
     * Returns the from date.
     *
     * @return the schedule day
     */
    public Date getFrom() {
        return from;
    }

    /**
     * Registers events.
     * <p>
     * The events can change at any time, and may have changed between being read from the database and this
     * method being called.
     *
     * @param events the events to register
     */
    public synchronized void setEvents(List<Event> events) {
        map = new HashMap<>();
        for (Event event : events) {
            EventHandle handle = event.getHandle(entityId, from, to);
            if (handle != null) {
                map.put(event.getId(), handle);
            }
        }
        modHash++;
        if (!changes.isEmpty()) {
            for (Change change : changes) {
                if (change.add) {
                    add(change.event);
                } else {
                    remove(change.event);
                }
            }
            changes.clear();
        }
    }

    /**
     * Adds an event if it intersects the date range.
     *
     * @param event the event
     */
    public void addIfIntersects(Event event) {
        if (event.intersects(from, to)) {
            add(event);
        }
    }

    /**
     * Removes an event if it intersects the date range.
     *
     * @param event the event
     */
    public void removeIfIntersects(Event event) {
        if (event.intersects(from, to)) {
            remove(event);
        }
    }

    /**
     * Returns a shallow copy of the events.
     * <p>
     * Note that this sorts on each access. Would be better to sort on Event, and resort if any Event changes.
     *
     * @return the events
     */
    public synchronized List<PropertySet> getEvents() {
        if (map == null) {
            // shouldn't occur. The DayCache isn't available until it has events registered.
            return Collections.emptyList();
        }
        long id = owner.getId();
        List<PropertySet> result = new ArrayList<>();
        for (Iterator<EventHandle> iterator = map.values().iterator(); iterator.hasNext(); ) {
            EventHandle handle = iterator.next();
            PropertySet event = handle.getEvent(id, from, to);
            if (event != null) {
                result.add(new ObjectSet(event)); // shallow copy
            } else {
                // handle is out of date
                modHash++;
                iterator.remove();
            }
        }
        result.sort(EventComparator.INSTANCE);
        return result;
    }

    /**
     * Returns the {@link ScheduleEvents} for the day.
     *
     * @return the events
     */
    public synchronized ScheduleEvents getScheduleEvents() {
        List<PropertySet> events = getEvents();
        return new ScheduleEvents(events, modHash);
    }

    /**
     * Returns the modification hash. This can be used to determine if the events have been modified since they
     * were last accessed.
     *
     * @return the modification hash
     */
    public synchronized long getModHash() {
        getEvents(); // applies changes
        return modHash;
    }

    /**
     * Adds an event.
     *
     * @param event the event to add
     */
    public synchronized void add(Event event) {
        if (map != null) {
            EventHandle handle = event.getHandle(owner.getId(), from, to);
            if (handle != null) {
                map.put(event.getId(), handle);
            }
        } else {
            // queue the addition
            changes.add(new Change(event, true));
        }
        modHash++;
    }

    /**
     * Removes an event.
     *
     * @param event the event to remove
     */
    public synchronized void remove(Event event) {
        if (map != null) {
            map.remove(event.getId());
        } else {
            // queue the removal
            changes.add(new Change(event, false));
        }
        modHash++;
    }

    /**
     * Sets the owner.
     *
     * @param owner the owner
     */
    void setOwner(DayCaches owner) {
        this.owner = owner;
    }

    /**
     * Tracks a change to the schedule.
     */
    private static class Change {

        private final boolean add;

        private final Event event;

        Change(Event event, boolean add) {
            this.event = event;
            this.add = add;
        }
    }

    /**
     * Compares event PropertySets, ordering them on {@link ScheduleEvent#ACT_START_TIME}.
     */
    private static class EventComparator implements Comparator<PropertySet> {

        /**
         * The singleton instance.
         */
        public static final EventComparator INSTANCE = new EventComparator();

        /**
         * Compares its two arguments for order.  Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.<p>
         * <p>
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or
         * greater than the second.
         */
        public int compare(PropertySet o1, PropertySet o2) {
            Date startTime1 = o1.getDate(ScheduleEvent.ACT_START_TIME);
            Date startTime2 = o2.getDate(ScheduleEvent.ACT_START_TIME);
            int result = DateRules.compareTo(startTime1, startTime2);
            if (result == 0) {
                Reference ref1 = o1.getReference(ScheduleEvent.ACT_REFERENCE);
                Reference ref2 = o2.getReference(ScheduleEvent.ACT_REFERENCE);
                result = Long.compare(ref1.getId(), ref2.getId());
            }
            return result;
        }
    }

}
