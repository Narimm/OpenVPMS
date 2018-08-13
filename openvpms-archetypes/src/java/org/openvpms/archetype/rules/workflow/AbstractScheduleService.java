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
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.util.PropertySet;
import org.springframework.beans.factory.DisposableBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Abstract implementation of the {@link ScheduleService}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractScheduleService implements ScheduleService, DisposableBean {

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
     * @param cache           the event cache
     * @param factory         the event factory
     */
    public AbstractScheduleService(String[] eventArchetypes, IArchetypeService service, Ehcache cache,
                                   ScheduleEventFactory factory) {
        this.service = service;
        this.factory = factory;
        this.cache = new ScheduleEventCache(cache, factory);

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
        HashCodeBuilder builder = new HashCodeBuilder();
        Date fromDay = DateRules.getDate(from);
        Date toDay = DateRules.getDate(to);
        List<PropertySet> results = new ArrayList<>();
        while (fromDay.compareTo(toDay) <= 0) {
            ScheduleEvents events = getScheduleEvents(schedule, fromDay);
            builder.append(events.getModHash());
            for (PropertySet event : events.getEvents()) {
                Date startTime = event.getDate(ScheduleEvent.ACT_START_TIME);
                Date endTime = event.getDate(ScheduleEvent.ACT_END_TIME);
                if (DateRules.intersects(startTime, endTime, from, to)) {
                    results.add(event);
                } else if (DateRules.compareTo(startTime, to) >= 0) {
                    break;
                }
            }
            fromDay = DateRules.getDate(fromDay, 1, DateUnits.DAYS);
        }
        return new ScheduleEvents(results, builder.toHashCode()); // 64 bit to 32 bit not ideal
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
        HashCodeBuilder builder = new HashCodeBuilder();
        Date fromDay = DateRules.getDate(from);
        Date toDay = DateRules.getDate(to);
        while (fromDay.compareTo(toDay) <= 0) {
            long modHash = getModHash(schedule, fromDay);
            if (modHash == -1) {
                return -1;
            }
            builder.append(modHash);
            fromDay = DateRules.getDate(fromDay, 1, DateUnits.DAYS);
        }
        return builder.toHashCode();
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
