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

package org.openvpms.archetype.rules.workflow;

import org.ehcache.Cache;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.cache.EhCacheable;
import org.openvpms.component.business.service.cache.EhcacheManager;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.util.PropertySet;
import org.springframework.beans.factory.DisposableBean;

import java.util.Date;
import java.util.List;


/**
 * Abstract implementation of the {@link ScheduleService}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractScheduleService implements ScheduleService, DisposableBean, EhCacheable {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The event cache.
     */
    private final ScheduleEventCache cache;

    /**
     * The event factory.
     */
    private final ScheduleEventFactory factory;

    /**
     * The archetypes of the events that this service caches.
     */
    private final String[] eventArchetypes;

    /**
     * Listener for event updates.
     */
    private final IArchetypeServiceListener listener;


    /**
     * Constructs an {@link AbstractScheduleService}.
     *
     * @param eventArchetypes the event act archetype short names
     * @param service         the archetype service
     * @param cacheManager    the cache manager
     * @param cacheName       the cache name
     * @param factory         the event factory
     */
    public AbstractScheduleService(String[] eventArchetypes, IArchetypeService service, EhcacheManager cacheManager,
                                   String cacheName, ScheduleEventFactory factory) {
        this.service = service;
        this.factory = factory;
        this.cache = new ScheduleEventCache(cacheManager, cacheName, factory);

        this.eventArchetypes = eventArchetypes;

        // add a listener to receive notifications from the archetype service
        listener = new AbstractArchetypeServiceListener() {

            @Override
            public void saved(IMObject object) {
                addEvent((Act) object);
            }

            @Override
            public void removed(IMObject object) {
                removeEvent((Act) object);
            }
        };
        for (String shortName : eventArchetypes) {
            service.addListener(shortName, listener);
        }
    }

    /**
     * Returns the underlying cache.
     *
     * @return the underlying cache
     */
    @Override
    public Cache getCache() {
        return cache.getCache();
    }

    /**
     * Clears cached data, including the underlying cache.
     */
    @Override
    public void clear() {
        cache.clear();
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors.
     *                   Exceptions will get logged but not rethrown to allow
     *                   other beans to release their resources too.
     */
    @Override
    public void destroy() throws Exception {
        try {
            for (String shortName : eventArchetypes) {
                service.removeListener(shortName, listener);
            }
        } finally {
            cache.clear();
        }
    }

    /**
     * Returns the event archetypes.
     *
     * @return the event archetypes
     */
    public String[] getEventArchetypes() {
        return eventArchetypes;
    }

    /**
     * Returns all events for the specified schedule and day.
     * Events are represented by {@link PropertySet PropertySets}.
     *
     * @param schedule the schedule
     * @param day      the day
     * @return a list of events
     */
    @Override
    public List<PropertySet> getEvents(Entity schedule, Date day) {
        return cache.getEvents(schedule, day).getEvents();
    }

    /**
     * Returns all events for the specified schedule and day.
     * Events are represented by {@link PropertySet PropertySets}.
     *
     * @param schedule the schedule
     * @param day      the day
     * @return the events
     */
    @Override
    public ScheduleEvents getScheduleEvents(Entity schedule, Date day) {
        return cache.getEvents(schedule, day);
    }

    /**
     * Returns the modification hash for the specified schedule and day.
     * <p>
     * This can be used to determine if any of the events have changed.
     *
     * @param schedule the schedule
     * @param day      the schedule day
     * @return the modification hash, or {@code -1} if the schedule and day are not cached
     */
    @Override
    public long getModHash(Entity schedule, Date day) {
        return cache.getModHash(schedule, day);
    }

    /**
     * Returns all events for the specified schedule, and time range.
     *
     * @param schedule the schedule
     * @param from     the from time
     * @param to       the to time
     * @return a list of events
     */
    @Override
    public List<PropertySet> getEvents(Entity schedule, Date from, Date to) {
        return getScheduleEvents(schedule, from, to).getEvents();
    }

    /**
     * Returns all events for the specified schedule, and time range.
     *
     * @param schedule the schedule
     * @param from     the from time
     * @param to       the to time
     * @return the events
     */
    @Override
    public ScheduleEvents getScheduleEvents(Entity schedule, Date from, Date to) {
        return cache.getEvents(schedule, from, to);
    }

    /**
     * Returns the modification hash for the specified schedule and date range.
     * <p>
     * This can be used to determine if any of the events have changed.
     *
     * @param schedule the schedule
     * @param from     the from time
     * @param to       the to time
     * @return the modification hash, or {@code -1} if the schedule and range are not cached
     */
    @Override
    public long getModHash(Entity schedule, Date from, Date to) {
        return cache.getModHash(schedule, from, to);
    }

    /**
     * Returns all events for the specified schedule and day, if they are cached.
     *
     * @param schedule the schedule
     * @param day      the day
     * @return the events, or {@code null} if they are not in the cache
     */
    protected List<PropertySet> getCached(Reference schedule, Date day) {
        return cache.getCached(schedule, day);
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Returns the event factory.
     *
     * @return the event factory
     */
    protected ScheduleEventFactory getEventFactory() {
        return factory;
    }

    /**
     * Adds an event to the cache.
     *
     * @param event the event to add
     */
    protected void addEvent(Act event) {
        cache.addEvent(event);
    }

    /**
     * Removes an event from the cache.
     *
     * @param event the event to remove
     */
    protected void removeEvent(Act event) {
        cache.removeEvent(event);
    }

    /**
     * Clears the cache.
     */
    protected void clearCache() {
        cache.clear();
    }

}
