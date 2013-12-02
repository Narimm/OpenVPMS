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

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;
import org.apache.commons.collections.map.ReferenceMap;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
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
 * The {@link ScheduleEventCache} caches events for schedules.
 * <p/>
 * It uses an underlying {@code Ehcache} to manage {@link ScheduleDay} instances.
 * <p/>
 * While events are being read from the database, updated instances of the same events may be added via
 * {@link #addEvent(Act)} and {@link #removeEvent(Act)}. This will check versions to ensure that the latest version
 * of the event is used.
 * <p/>
 * NOTE: as the cache can be updated from multiple threads, there is a very small possibility that an event deletion
 * could be received before the event addition. In this case, the deletion will be ignored and the addition will be
 * cached. A workaround for this is to enable timeToLive on the underlying Ehcache.
 * <p/>
 * Another reason for setting a timeToLive on the underlying Ehcache is to ensure that customer and patient names
 * reflect those in the database. If the underlying cache is eternal, then changes to customer and patient names won't
 * be reflected in the cached entries.
 *
 * @author Tim Anderson
 */
class ScheduleEventCache {

    /**
     * The underlying cache. This is used to cache {@link ScheduleDay} instances.
     */
    private final BlockingCache cache;

    /**
     * The query factory.
     */
    private final ScheduleEventFactory factory;

    /**
     * A map of schedule ids to {@link Schedule} instances. Schedule instances can be reclaimed by the garbage
     * collector when no {@link ScheduleDay} references them.
     */
    @SuppressWarnings("unchecked")
    private final Map<Long, Schedule> schedules
            = Collections.<Long, Schedule>synchronizedMap(new ReferenceMap(ReferenceMap.HARD, ReferenceMap.WEAK));

    /**
     * A map of act {@link IMObjectReference} to {@link Event}. Event instances can be reclaimed by the garbage
     * collector when no {@link ScheduleDay} references them.
     * <p/>
     * Events spanning multiple days may be shared by multiple {@link ScheduleDay} instances.
     */
    private final ReferenceMap events = new ReferenceMap(ReferenceMap.HARD, ReferenceMap.WEAK);

    /**
     * Constructs a {@link ScheduleEventCache}.
     *
     * @param cache   the underlying cache
     * @param factory the event query factory
     */
    public ScheduleEventCache(Ehcache cache, ScheduleEventFactory factory) {
        this.cache = new BlockingCache(cache);
        this.factory = factory;
    }

    /**
     * Returns all events for a schedule on the given day.
     * <p/>
     * If the events are not cached, they will be read in from the database. Whilst this occurs, updates to the
     * events may be received via {@link #addEvent(Act)} and {@link #removeEvent(Act)}.
     *
     * @param schedule the schedule
     * @param day      the day
     * @return all events on the specified day for the schedule
     */
    public List<PropertySet> getEvents(Entity schedule, Date day) {
        ScheduleDay result;
        day = DateRules.getDate(day);
        Key key = new Key(schedule.getId(), day);
        try {
            // if null will lock here
            Element element = cache.get(key);
            if (element == null) {
                // Value not cached, so query

                result = new ScheduleDay(getSchedule(schedule.getId()), day);
                // NOTE: ScheduleDay can be now updated via addEvent and removeEvent

                List<PropertySet> sets = factory.getEvents(schedule, day);
                List<Event> events = addEvents(sets);
                result.setEvents(events);
                element = new Element(key, result);
                cache.put(element);
            } else {
                result = (ScheduleDay) element.getObjectValue();
            }
        } catch (LockTimeoutException exception) {
            // do not release the lock, as it was not acquired
            String message = "Timeout waiting on another thread to fetch object for cache entry \"" + key + "\".";
            throw new LockTimeoutException(message, exception);
        } catch (final Throwable throwable) {
            // Could not fetch - ditch the entry from the cache and rethrow.
            cache.put(new Element(key, null));  // releases the lock
            throw new CacheException("Could not fetch object for cache entry with key \"" + key + "\".", throwable);
        }
        return result.getEvents();
    }

    /**
     * Adds an event to the cache.
     *
     * @param event the event act
     */
    public void addEvent(Act event) {
        addEvent(factory.createEvent(event));
    }

    /**
     * Removes an event from the cache.
     *
     * @param event the event to remove
     */
    public void removeEvent(Act event) {
        Event e = removeEvent(event.getObjectReference());
        if (e != null) {
            Schedule schedule = schedules.get(e.getScheduleId());
            if (schedule != null) {
                schedule.removeEvent(e);
            }
        }
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        cache.removeAll();
        schedules.clear();
        synchronized (events) {
            events.clear();
        }
    }

    /**
     * Adds an event to the cache.
     *
     * @param set the {@code PropertySet} representation of the event
     */
    private void addEvent(PropertySet set) {
        Event[] events = update(set);
        Event oldEvent = events[0];
        Event newEvent = events[1];
        if (oldEvent != null) {
            Schedule oldSchedule = schedules.get(oldEvent.getScheduleId());
            if (oldSchedule != null) {
                oldSchedule.removeEvent(oldEvent);
            }
        }
        Schedule newSchedule = schedules.get(newEvent.getScheduleId());
        if (newSchedule != null) {
            newSchedule.addEvent(newEvent);
        }
    }

    /**
     * Returns a {@link Schedule} given its id, creating it if it doesn't exist.
     *
     * @param id the schedule identifier
     * @return the schedule
     */
    private Schedule getSchedule(long id) {
        Schedule result;
        synchronized (schedules) {
            result = schedules.get(id);
            if (result == null) {
                result = new Schedule(id);
                schedules.put(id, result);
            }
        }
        return result;
    }

    /**
     * Creates {@link Event} instances for the supplied {@link PropertySet}s.
     * <p/>
     * If an event already exists for a property set, it will be updated.
     *
     * @param events the event property sets
     * @return the new events
     */
    private List<Event> addEvents(List<PropertySet> events) {
        List<Event> result = new ArrayList<Event>();
        for (PropertySet set : events) {
            Event event = update(set)[1];
            result.add(event);
        }
        return result;
    }

    /**
     * Updates the locally cached copy of an {@link Event} if it exists, otherwise creates a new version.
     *
     * @param set the set representation of the event
     * @return a two element array containing the old event, if present, and the new event
     */
    private Event[] update(PropertySet set) {
        Event[] result = new Event[2];
        IMObjectReference act = set.getReference(ScheduleEvent.ACT_REFERENCE);
        synchronized (events) {
            Event event = (Event) events.get(act);
            if (event == null) {
                event = new Event(set);
                events.put(act, event);
            } else {
                result[0] = new Event(event.getEvent()); // copy the old event
                event.update(set);
            }
            result[1] = event;
        }
        return result;
    }

    /**
     * Removes an event, if it exists.
     *
     * @param act the event act reference
     * @return the corresponding event, or {@code null} if it doesn't exist
     */
    private Event removeEvent(IMObjectReference act) {
        Event result;
        synchronized (events) {
            result = (Event) events.remove(act);
        }
        return result;
    }

    /**
     * Cache key.
     */
    private static class Key {

        /**
         * The schedule identifier.
         */
        private final long scheduleId;

        /**
         * The schedule day.
         */
        private final Date day;

        /**
         * Cached hash code.
         */
        private final int hashCode;


        /**
         * Constructs an {@link Key}.
         *
         * @param scheduleId the schedule identifier
         * @param day        the schedule day
         */
        public Key(long scheduleId, Date day) {
            this.scheduleId = scheduleId;
            this.day = day;
            hashCode = ((int) (scheduleId ^ (scheduleId >>> 32))) ^ day.hashCode();
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param obj the reference object with which to compare.
         * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Key) {
                Key other = (Key) obj;
                return (scheduleId == other.scheduleId && day.equals(other.day));
            }
            return false;
        }

        /**
         * Returns a hash code value for the object.
         *
         * @return a hash code value for this object.
         */
        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    /**
     * Tracks {@link ScheduleDay} instances held by the underlying cache.
     * <p/>
     * It only holds weak references so they can be discarded when the cache no longer has them.
     * <p/>
     * Each {@link Schedule} is only weakly referenced by {@link ScheduleEventCache} so they can be garbage collected
     * when there is no {@link ScheduleDay} instance referencing them.
     */
    private static class Schedule {

        /**
         * A map of Date to {@link ScheduleDay} instances.
         */
        private final ReferenceMap days = new ReferenceMap(ReferenceMap.HARD, ReferenceMap.WEAK);

        /**
         * The schedule identifier.
         */
        private long id;

        /**
         * Constructs an {@link Schedule}.
         *
         * @param id the schedule identifier
         */
        public Schedule(long id) {
            this.id = id;
        }

        /**
         * Returns the schedule identifier.
         *
         * @return the schedule identifier
         */
        public long getId() {
            return id;
        }

        /**
         * Adds a {@link ScheduleDay}.
         *
         * @param day the day to add
         */
        public void add(ScheduleDay day) {
            synchronized (days) {
                days.put(day.getDay(), day);
            }
        }

        /**
         * Adds an event.
         *
         * @param event the event to add
         */
        public void addEvent(Event event) {
            synchronized (days) {
                for (Object value : days.values()) {
                    ScheduleDay day = (ScheduleDay) value;
                    if (event.intersects(day.getDay())) {
                        day.add(event);
                    }
                }
            }
        }

        /**
         * Removes an event.
         *
         * @param event the event to remove
         */
        public void removeEvent(Event event) {
            Object[] values;
            synchronized (days) {
                values = days.values().toArray();
            }
            for (Object value : values) {
                ScheduleDay day = (ScheduleDay) value;
                if (event.intersects(day.getDay())) {
                    day.remove(event);
                }
            }
        }

    }

    private static class ScheduleDay {


        /**
         * The events, keyed on act identifier. Note that the PropertySets that these refer to may change.
         */
        private Map<Long, EventHandle> map;

        /**
         * The schedule day.
         */
        private final Date day;

        /**
         * Changes that may need to be applied the schedule, after its events are populated.
         * <p/>
         * This is required as events may be updated whilst they are being fetched from the database.
         */
        private List<Change> changes = new ArrayList<Change>();

        /**
         * The owning schedule. This reference is kept to ensure that the Schedule instance isn't garbage collected
         * while the underlying cache has an instance of this.
         */
        private final Schedule schedule;

        /**
         * Constructs an {@link ScheduleDay}.
         *
         * @param schedule the schedule
         * @param day      the schedule day
         */
        public ScheduleDay(Schedule schedule, Date day) {
            this.day = day;
            this.schedule = schedule;
            schedule.add(this);
        }

        /**
         * Returns the schedule day.
         *
         * @return the schedule day
         */
        public Date getDay() {
            return day;
        }

        /**
         * Registers events.
         * <p/>
         * The events can change at any time, and may have changed between being read from the database and this
         * method being called.
         *
         * @param events the events to register
         */
        public synchronized void setEvents(List<Event> events) {
            map = new HashMap<Long, EventHandle>();
            long scheduleId = schedule.getId();
            for (Event event : events) {
                EventHandle handle = event.getHandle(scheduleId, day);
                if (handle != null) {
                    map.put(event.getId(), handle);
                }
            }
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
         * Returns a shallow copy of the events.
         * <p/>
         * Note that this sorts on each access. Would be better to sort on Event, and resort if any Event changes.
         *
         * @return the events
         */
        public synchronized List<PropertySet> getEvents() {
            if (map == null) {
                // shouldn't occur. The ScheduleDay isn't available until it has events registered.
                return Collections.emptyList();
            }
            long id = schedule.getId();
            List<PropertySet> result = new ArrayList<PropertySet>();
            for (Iterator<EventHandle> iterator = map.values().iterator(); iterator.hasNext(); ) {
                EventHandle handle = iterator.next();
                PropertySet event = handle.getEvent(id, day);
                if (event != null) {
                    result.add(new ObjectSet(event)); // shallow copy
                } else {
                    // handle is out of date
                    iterator.remove();
                }
            }
            Collections.sort(result, EventComparator.INSTANCE);
            return result;
        }

        /**
         * Adds an event.
         *
         * @param event the event to add
         */
        public synchronized void add(Event event) {
            if (map != null) {
                EventHandle handle = event.getHandle(schedule.getId(), day);
                if (handle != null) {
                    map.put(event.getId(), handle);
                }
            } else {
                // queue the addition
                changes.add(new Change(event, true));
            }
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
        }

        /**
         * Tracks a change to the schedule.
         */
        private static class Change {

            private final boolean add;

            private final Event event;

            public Change(Event event, boolean add) {
                this.event = event;
                this.add = add;
            }
        }

    }

    private static class Event {

        /**
         * The event.
         */
        private PropertySet event;

        /**
         * The event identifier.
         */
        private final long id;

        /**
         * The event version.
         */
        private long version;

        /**
         * The event schedule.
         */
        private long scheduleId;

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
                                                ScheduleEvent.CLINICIAN_PARTICIPATION_VERSION};

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
         * Determine if the event falls on the specified day.
         *
         * @param day the day
         * @return {@code true} if the event falls on te specified day
         */
        public synchronized boolean intersects(Date day) {
            Date startTime = DateRules.getDate(event.getDate(ScheduleEvent.ACT_START_TIME));
            Date endTime = DateRules.getDate(event.getDate(ScheduleEvent.ACT_END_TIME));
            return DateRules.between(day, startTime, endTime);
        }

        /**
         * Returns the schedule identifier.
         *
         * @return the schedule identifier
         */
        public synchronized long getScheduleId() {
            return scheduleId;
        }

        /**
         * Creates a new handle for the event, if it is for the specified schedule and day.
         *
         * @param scheduleId the schedule identifier
         * @param day        the day
         * @return a new handle, or {@code null} if the event isn't for the schedule or day
         */
        public synchronized EventHandle getHandle(long scheduleId, Date day) {
            return isFor(scheduleId, day) ? new EventHandle(this, modCount) : null;
        }

        /**
         * Returns the event, if it is for the specified schedule and day.
         * <p/>
         * If the handle modCount is not up-to-date, but the event still applies, it will be updated.
         *
         * @param scheduleId the schedule identifier
         * @param day        the day
         * @param handle     the event handle, used for quick determination of event applicability
         * @return the event, or {@code null} if the event no longer applies
         */
        public synchronized PropertySet getEvent(long scheduleId, Date day, EventHandle handle) {
            if (modCount == handle.getModCount()) {
                return event;
            } else if (isFor(scheduleId, day)) {
                handle.setModCount(modCount);  // update the modCount
            }
            return null; // handle is out date
        }

        /**
         * Determines if this event belongs to a particular schedule and day.
         *
         * @param scheduleId the schedule identifier
         * @param day        the day
         * @return {@code true} if the event belongs to the schedule and day
         */
        private boolean isFor(long scheduleId, Date day) {
            return this.scheduleId == scheduleId && intersects(day);
        }

        /**
         * Compares versions.
         *
         * @param key   the version key
         * @param other the set to compare with
         * @return {@code -1} if this has has older version than {@code other}, {@code 0} if they are equal, {@code 1}
         *         if this has a newer version
         */
        private int compareVersions(String key, PropertySet other) {
            long version = event.getLong(key);
            long otherVersion = other.getLong(key);
            return (version < otherVersion) ? -1 : ((version == otherVersion) ? 0 : 1);
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
            this.scheduleId = event.getReference(ScheduleEvent.SCHEDULE_REFERENCE).getId();
            ++modCount;
        }
    }

    /**
     * A handle to an {@link Event}.
     * <p/>
     * This holds the modCount of the Event when the handle was constructed, in order to detect changes to the Event.
     * <p/>
     * Note that access to EventHandle is single-threaded via ScheduleDay, so it doesn't need its own synchronization.
     */
    private static class EventHandle {

        /**
         * The event.
         */
        private final Event event;

        /**
         * The modification count of the event, when the handle was constructed.
         */
        private int modCount;

        /**
         * Constructs a new {@link EventHandle}.
         *
         * @param event    the event
         * @param modCount the event modification count
         */
        public EventHandle(Event event, int modCount) {
            this.event = event;
            this.modCount = modCount;
        }

        /**
         * Returns the event.
         * <p/>
         * If the event has been changed since the handle was constructed, the following may occur;
         * <ul>
         * <li>the event may no longer be for the scheduleId and day. In this case {@code null} is returned.</li>
         * <li>the event is still applicable. In this case, {@link #setModCount(int) modCount} is updated.</li>
         * </ul>
         *
         * @param scheduleId the schedule id the event is expected to be for
         * @param day        the day the event is expected to be for
         * @return the event, or {@code null} if the event is not for the expected schedule and day
         */
        public PropertySet getEvent(long scheduleId, Date day) {
            return event.getEvent(scheduleId, day, this);
        }

        /**
         * Returns the event modification count.
         *
         * @return the modification count.
         */
        public int getModCount() {
            return modCount;
        }

        /**
         * Updates the modification count.
         *
         * @param modCount the modification count
         */
        public void setModCount(int modCount) {
            this.modCount = modCount;
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
         * <p/>
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or
         *         greater than the second.
         */
        public int compare(PropertySet o1, PropertySet o2) {
            Date startTime1 = o1.getDate(ScheduleEvent.ACT_START_TIME);
            Date startTime2 = o2.getDate(ScheduleEvent.ACT_START_TIME);
            int result = DateRules.compareTo(startTime1, startTime2);
            if (result == 0) {
                IMObjectReference ref1 = o1.getReference(ScheduleEvent.ACT_REFERENCE);
                IMObjectReference ref2 = o2.getReference(ScheduleEvent.ACT_REFERENCE);
                if (ref1.getId() < ref2.getId()) {
                    result = -1;
                } else if (ref1.getId() == ref2.getId()) {
                    result = 0;
                } else {
                    result = 1;
                }
            }
            return result;
        }
    }

}
