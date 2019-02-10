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
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.ehcache.Cache;
import org.ehcache.spi.loaderwriter.CacheLoaderWriter;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.archetype.rules.workflow.ScheduleEventFactory;
import org.openvpms.archetype.rules.workflow.ScheduleEvents;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.cache.EhcacheManager;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.HARD;
import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.WEAK;

/**
 * The {@link AbstractEventCache} caches events for schedules.
 * <p>
 * It uses an underlying {@code Ehcache} to manage {@link DayCache} instances.
 * <p>
 * While events are being read from the database, updated instances of the same events may be added via
 * {@link #addEvent(Act)} and {@link #removeEvent(Act)}. This will check versions to ensure that the latest version
 * of the event is used.
 * <p>
 * NOTE: as the cache can be updated from multiple threads, there is a very small possibility that an event deletion
 * could be received before the event addition. In this case, the deletion will be ignored and the addition will be
 * cached. A workaround for this is to enable timeToLive on the underlying Ehcache.
 * <p>
 * Another reason for setting a timeToLive on the underlying Ehcache is to ensure that entity names (e.g. customer and
 * patient) reflect those in the database. If the underlying cache is eternal, then changes to names won't be reflected
 * in the cached entries.
 *
 * @author Tim Anderson
 */
public abstract class AbstractEventCache {

    /**
     * The underlying cache. This is used to cache {@link DayCache} instances.
     */
    private final Cache<Key, DayCache> cache;

    /**
     * The event factory.
     */
    private final ScheduleEventFactory factory;

    /**
     * If {@code true}, events are cached on a daily basis, otherwise they are cached on a date range basis.
     */
    private final boolean dailyCache;

    /**
     * A map of schedule ids to {@link DayCaches} instances. Schedule instances can be reclaimed by the garbage
     * collector when no {@link DayCache} references them.
     */
    private final Map<Long, DayCaches> cachesMap = Collections.synchronizedMap(new ReferenceMap<>(HARD, WEAK));

    /**
     * A map of act {@link Reference} to {@link Event}. Event instances can be reclaimed by the garbage
     * collector when no {@link DayCache} references them.
     * <p>
     * Events spanning multiple days may be shared by multiple {@link DayCache} instances.
     */
    private final ReferenceMap<Reference, Event> events = new ReferenceMap<>(HARD, WEAK);

    /**
     * Constructs a {@link AbstractEventCache}.
     *
     * @param cacheFactory the cache factory
     * @param cacheName    the cache name
     * @param factory      the event factory
     * @param dailyCache   if {@code true}, events are cached on a daily basis, otherwise they are cached on a date
     *                     range basis.
     */
    public AbstractEventCache(EhcacheManager cacheFactory, String cacheName, ScheduleEventFactory factory,
                              boolean dailyCache) {
        this.cache = cacheFactory.create(cacheName, Key.class, DayCache.class, new DayCacheLoader());
        this.factory = factory;
        this.dailyCache = dailyCache;
    }

    /**
     * Returns all events for an entity on the given day.
     * <p>
     * If the events are not cached, they will be read in from the database. Whilst this occurs, updates to the
     * events may be received via {@link #addEvent(Act)} and {@link #removeEvent(Act)}.
     *
     * @param entity the entity
     * @param day    the day
     * @return all events on the specified day for the entity
     */
    public ScheduleEvents getEvents(Entity entity, Date day) {
        Date from = DateRules.getDate(day);
        Date to = DateRules.getNextDate(from);
        return getScheduleEvents(entity, from, to);
    }

    /**
     * Returns all events for the specified entity, and date range range.
     *
     * @param entity the entity
     * @param from   the from date, inclusive
     * @param to     the to date, exclusive
     * @return the events
     */
    public ScheduleEvents getEvents(Entity entity, Date from, Date to) {
        ScheduleEvents result;
        if (dailyCache) {
            // need to iterate through each day
            HashCodeBuilder builder = new HashCodeBuilder();
            Date fromDay = DateRules.getDate(from);
            Date toDay = DateRules.getDate(to);
            List<PropertySet> results = new ArrayList<>();
            while (fromDay.compareTo(toDay) <= 0) {
                ScheduleEvents events = getEvents(entity, fromDay);
                builder.append(events.getModHash());
                for (PropertySet event : events.getEvents()) {
                    Date actStart = event.getDate(ScheduleEvent.ACT_START_TIME);
                    Date actEnd = event.getDate(ScheduleEvent.ACT_END_TIME);
                    if (DateRules.intersects(actStart, actEnd, from, to)) {
                        results.add(event);
                    } else if (DateRules.compareTo(actStart, to) >= 0) {
                        break;
                    }
                }
                fromDay = DateRules.getDate(fromDay, 1, DateUnits.DAYS);
            }
            result = new ScheduleEvents(results, builder.toHashCode()); // 64 bit to 32 bit not ideal
        } else {
            // cache the entire date range
            result = getScheduleEvents(entity, from, to);
        }
        return result;
    }

    /**
     * Returns the modification hash for the specified entity and day.
     *
     * @param entity the entity
     * @param day    the day
     * @return the modification hash, or {@code -1} if they are not present in the cache
     */
    public long getModHash(Entity entity, Date day) {
        Date from = DateRules.getDate(day);
        Date to = DateRules.getNextDate(from);
        DayCache cached = getDayCache(entity.getObjectReference(), from, to, entity);
        return cached != null ? cached.getModHash() : -1;
    }

    /**
     * Returns the modification hash for the specified entity and date range.
     * <p>
     * This can be used to determine if any of the events have changed.
     *
     * @param entity the entity
     * @param from   the from time
     * @param to     the to time
     * @return the modification hash, or {@code -1} if the entity and range are not cached
     */
    public long getModHash(Entity entity, Date from, Date to) {
        long result;
        if (dailyCache) {
            // need to iterate through each day. If a day isn't cached, assume that events have changed
            HashCodeBuilder builder = new HashCodeBuilder();
            Date fromDay = DateRules.getDate(from);
            Date toDay = DateRules.getDate(to);
            boolean missing = false;
            while (fromDay.compareTo(toDay) <= 0) {
                long modHash = getModHash(entity, fromDay);
                if (modHash == -1) {
                    // day not cached
                    missing = true;
                    break;
                }
                builder.append(modHash);
                fromDay = DateRules.getDate(fromDay, 1, DateUnits.DAYS);
            }
            result = (missing) ? -1 : builder.toHashCode();
        } else {
            DayCache cached = getDayCache(entity.getObjectReference(), from, to, entity);
            result = (cached != null) ? cached.getModHash() : -1;
        }
        return result;
    }

    /**
     * Returns events for the specified entity and day, if they are in the cache.
     *
     * @param entity the entity
     * @param day    the day
     * @return the events, or {@code null} if they are not in the cache
     */
    public List<PropertySet> getCached(Reference entity, Date day) {
        Date from = DateRules.getDate(day);
        Date to = DateRules.getNextDate(from);
        DayCache cached = getDayCache(entity, day, to, null);
        return (cached != null) ? cached.getEvents() : null;
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
            DayCaches view = cachesMap.get(e.getEntityId());
            if (view != null) {
                view.removeEvent(e);
            }
        }
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        cache.clear();
        cachesMap.clear();
        synchronized (events) {
            events.clear();
        }
    }

    /**
     * Returns the underlying cache.
     *
     * @return the cache
     */
    public Cache getCache() {
        return cache;
    }

    /**
     * Creates an event.
     *
     * @param set the underlying property set
     * @return a new event
     */
    protected abstract Event createEvent(PropertySet set);

    /**
     * Returns all events for the specified entity, and date range.
     * <p/>
     * This loads and caches the results if they are not already cached.
     *
     * @param entity the entity
     * @param from   the start date, inclusive
     * @param to     the end date, exclusive
     * @return the events
     */
    protected ScheduleEvents getScheduleEvents(Entity entity, Date from, Date to) {
        Key key = new Key(entity.getObjectReference(), from, to, entity);
        DayCache result = cache.get(key);
        return result.getScheduleEvents();
    }

    /**
     * Returns the cached {@link DayCache} for the given entity and date range.
     *
     * @param reference the entity reference.
     * @param from      the start date, inclusive
     * @param to        the end date, exclusive
     * @param entity    the corresponding entity. May be {@code null}
     * @return the corresponding {@link DayCache} or {@code null} if it is not cached
     */
    private DayCache getDayCache(Reference reference, Date from, Date to, Entity entity) {
        DayCache result = null;
        Key key = new Key(reference, from, to, entity);
        if (cache.containsKey(key)) {
            result = cache.get(key); // small potential for the day to be discarded and then loaded back in here
        }
        return result;
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
            DayCaches oldView = cachesMap.get(oldEvent.getEntityId());
            if (oldView != null) {
                oldView.removeEvent(oldEvent);
            }
        }
        DayCaches caches = cachesMap.get(newEvent.getEntityId());
        if (caches != null) {
            caches.addEvent(newEvent);
        }
    }

    /**
     * Returns a {@link DayCaches} given its reference, creating it if it doesn't exist.
     *
     * @param reference the schedule reference
     * @return the schedule
     */
    private DayCaches getEntity(Reference reference) {
        DayCaches result;
        synchronized (cachesMap) {
            long id = reference.getId();
            result = cachesMap.get(id);
            if (result == null) {
                result = new DayCaches(id);
                cachesMap.put(id, result);
            }
        }
        return result;
    }

    /**
     * Creates {@link Event} instances for the supplied {@link PropertySet}s.
     * <p>
     * If an event already exists for a property set, it will be updated.
     *
     * @param events the event property sets
     * @return the new events
     */
    private List<Event> addEvents(List<PropertySet> events) {
        List<Event> result = new ArrayList<>();
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
        Reference act = set.getReference(ScheduleEvent.ACT_REFERENCE);
        synchronized (events) {
            Event event = events.get(act);
            if (event == null) {
                event = createEvent(set);
                events.put(act, event);
            } else {
                result[0] = createEvent(event.getEvent()); // copy the old event
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
    private Event removeEvent(Reference act) {
        Event result;
        synchronized (events) {
            result = events.remove(act);
        }
        return result;
    }

    private class DayCacheLoader implements CacheLoaderWriter<Key, DayCache> {

        @Override
        public DayCache load(Key key) {
            Entity entity = key.entity;
            Date from = key.from;
            Date to = key.to;
            Reference reference = key.reference;

            DayCaches caches = getEntity(reference);
            DayCache result = new DayCache(reference.getId(), from, to);
            caches.add(result);
            // NOTE: DayCache can be now updated via addEvent and removeEvent, before it is populated from
            // the database. This is to avoid losing events while the load is in progress

            // load the events
            List<PropertySet> sets = (entity != null) ? factory.getEvents(entity, from, to)
                                                      : factory.getEvents(reference, from, to);
            List<Event> events = addEvents(sets);
            result.setEvents(events);
            return result;
        }

        @Override
        public void write(Key key, DayCache value) {

        }

        @Override
        public void delete(Key key) {

        }
    }

    /**
     * Cache key.
     */
    private static class Key {

        /**
         * The entity reference.
         */
        private final Reference reference;

        /**
         * The start date.
         */
        private final Date from;

        /**
         * The end date.
         */
        private final Date to;

        /**
         * The entity. May be {@code null}
         */
        private final Entity entity;

        /**
         * Cached hash code.
         */
        private final int hashCode;


        /**
         * Constructs a {@link Key}.
         *
         * @param reference the entity reference
         * @param from      the start date
         * @param to        the end date
         * @param entity    the entity. May be {@code null}
         */
        Key(Reference reference, Date from, Date to, Entity entity) {
            this.reference = reference;
            this.from = from;
            this.to = to;
            this.entity = entity;
            long id = reference.getId();
            hashCode = ((int) (id ^ (id >>> 32))) ^ from.hashCode() ^ to.hashCode();
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
                return (reference.getId() == other.reference.getId() && from.equals(other.from)
                        && to.equals(other.to));
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

}
