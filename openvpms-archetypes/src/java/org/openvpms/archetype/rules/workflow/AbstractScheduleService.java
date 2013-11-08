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

import net.sf.ehcache.Ehcache;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
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
     * The archetype short name of the events that this service caches.
     */
    private final String eventShortName;

    /**
     * Listener for event updates.
     */
    private final IArchetypeServiceListener listener;


    /**
     * Constructs an {@link AbstractScheduleService}.
     *
     * @param eventShortName the event act archetype short name
     * @param service        the archetype service
     * @param cache          the event cache
     * @param factory        the event factory
     */
    public AbstractScheduleService(String eventShortName, IArchetypeService service, Ehcache cache,
                                   ScheduleEventFactory factory) {
        this.service = service;
        this.factory = factory;
        this.cache = new ScheduleEventCache(cache, factory);

        this.eventShortName = eventShortName;

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
        service.addListener(eventShortName, listener);
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
        cache.clear();
        service.removeListener(eventShortName, listener);
    }

    /**
     * Returns all events for the specified schedule and day.
     * Events are represented by {@link PropertySet PropertySets}.
     *
     * @param schedule the schedule
     * @param day      the day
     * @return a list of events
     */
    public List<PropertySet> getEvents(Entity schedule, Date day) {
        return cache.getEvents(schedule, day);
    }

    /**
     * Returns all events for the specified schedule, and time range.
     *
     * @param schedule the schedule
     * @param from     the from time
     * @param to       the to time
     * @return a list of events
     */
    public List<PropertySet> getEvents(Entity schedule, Date from, Date to) {
        Date fromDay = DateRules.getDate(from);
        Date toDay = DateRules.getDate(to);
        List<PropertySet> results = new ArrayList<PropertySet>();
        while (fromDay.compareTo(toDay) <= 0) {
            for (PropertySet event : getEvents(schedule, fromDay)) {
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

        return results;
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
