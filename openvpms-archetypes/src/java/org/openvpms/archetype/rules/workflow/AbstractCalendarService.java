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

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.cache.EhcacheManager;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.ParticipationConstraint;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.and;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.gt;
import static org.openvpms.component.system.common.query.Constraints.in;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.lt;
import static org.openvpms.component.system.common.query.Constraints.lte;
import static org.openvpms.component.system.common.query.Constraints.not;
import static org.openvpms.component.system.common.query.Constraints.sort;
import static org.openvpms.component.system.common.query.ParticipationConstraint.Field.ActShortName;

/**
 * A {@link ScheduleService} where events occur at specific dates and times.
 *
 * @author Tim Anderson
 */
public class AbstractCalendarService extends AbstractScheduleService {

    /**
     * Constructs an {@link AbstractCalendarService}.
     *
     * @param eventArchetypes the event act archetypes
     * @param service         the archetype service
     * @param cacheManager    the cache manager
     * @param cacheName       the cache name
     * @param factory         the event factory
     */
    public AbstractCalendarService(String[] eventArchetypes, IArchetypeService service, EhcacheManager cacheManager,
                                   String cacheName, ScheduleEventFactory factory) {
        super(eventArchetypes, service, cacheManager, cacheName, factory);
    }

    /**
     * Determines if there are acts that overlap with an event.
     *
     * @param event the event
     * @return the first overlapping event times, or {@code null} if none are found
     * @throws OpenVPMSException for any error
     */
    public Times getOverlappingEvent(Act event) {
        Times result = null;
        IMObjectBean bean = getService().getBean(event);
        Reference schedule = bean.getTargetRef("schedule");
        Times times = Times.create(event);
        if (schedule != null && times != null) {
            result = getOverlappingEvent(times, schedule);
        }
        return result;
    }

    /**
     * Determines if there are acts that overlap with an appointment.
     * <p>
     * This checks the cache first for overlaps before making a more expensive database query.
     * <p>
     * Note that due to race conditions, it is possible that an event may be saved in another thread that
     * won't be seen in the cache. This may result in double booking of events for schedules that don't support
     * it.
     * While not ideal, the likelihood is small, and needs to be weighed against:
     * <ul>
     * <li>issuing a database query each time</li>
     * <li>changing the architecture to route all event updates directly through this service first
     * to keep the cache in sync.</li>
     * </ul>
     *
     * @param times    the event times
     * @param schedule the schedule
     * @return the first overlapping event times, or {@code null} if none are found
     * @throws OpenVPMSException for any error
     */
    public Times getOverlappingEvent(Times times, Reference schedule) {
        Times result = null;
        Date startTime = times.getStartTime();
        Date endTime = times.getEndTime();
        Date fromDay = DateRules.getDate(startTime);
        Date toDay = DateRules.getDate(endTime);
        boolean incomplete = false;

        // first check to see if the days that the event spans are already cached.
        while (fromDay.compareTo(toDay) <= 0) {
            List<PropertySet> cached = getCached(schedule, fromDay);
            if (cached != null) {
                result = getOverlap(times, cached);
                if (result != null) {
                    break;
                }
            } else {
                incomplete = true; //  day is not cached
            }
            fromDay = DateRules.getDate(fromDay, 1, DateUnits.DAYS);
        }
        if (result == null && incomplete) {
            // need to hit the database
            List<Times> list = Collections.singletonList(times);
            result = getOverlap(list, schedule);
        }
        return result;
    }

    /**
     * Returns the event that overlaps the specified events.
     *
     * @param events   the event times
     * @param schedule the schedule
     * @return the first event, or {@code null} if none exists
     */
    public Times getOverlappingEvent(List<Times> events, Entity schedule) {
        return getOverlappingEvent(events, schedule.getObjectReference());
    }

    /**
     * Returns the first event that overlaps the specified events.
     *
     * @param events   the event times
     * @param schedule the schedule
     * @return the first event, or {@code null} if none exists
     */
    public Times getOverlappingEvent(List<Times> events, Reference schedule) {
        Times result;
        if (events.isEmpty()) {
            result = null;
        } else if (events.size() == 1) {
            result = getOverlappingEvent(events.get(0), schedule);
        } else {
            result = getOverlap(events, schedule);
        }
        return result;
    }

    /**
     * Returns events that overlap those supplied.
     *
     * @param events   the events to check
     * @param schedule the schedule
     * @param limit    the maximum no. of events to return
     * @return the overlapping events, or {@code null} if no events overlap
     */
    public List<Times> getOverlappingEvents(List<Times> events, Entity schedule, int limit) {
        List<Times> result = new ArrayList<>();
        ObjectSetQueryIterator iterator = createOverlappingEventIterator(events, schedule.getObjectReference(), limit);
        while (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            result.add(createTimes(set));
        }
        return !result.isEmpty() ? result : null;
    }

    /**
     * Creates an iterator that returns events that overlap those supplied.
     *
     * @param events     the events
     * @param schedule   the schedule
     * @param maxResults the maximum no. of results to return
     * @return the iterator
     */
    protected ObjectSetQueryIterator createOverlappingEventIterator(List<Times> events, Reference schedule,
                                                                    int maxResults) {
        return createOverlappingEventIterator(events, "schedule", schedule, maxResults);
    }

    /**
     * Creates an iterator that returns events that overlap those supplied.
     *
     * @param events            the events
     * @param participationNode the participation node name
     * @param entity            the entity to restrict events to
     * @param maxResults        the maximum no. of results to return
     * @return the iterator
     */
    protected ObjectSetQueryIterator createOverlappingEventIterator(List<Times> events, String participationNode,
                                                                    Reference entity, int maxResults) {
        List<Long> ids = getIds(events);
        String[] archetypes = getEventArchetypes();
        ArchetypeQuery query = new ArchetypeQuery(archetypes, false, false);
        query.getArchetypeConstraint().setAlias("act");
        query.add(new ObjectRefSelectConstraint("act"));
        query.add(new NodeSelectConstraint("startTime"));
        query.add(new NodeSelectConstraint("endTime"));
        JoinConstraint participation = join(participationNode);
        participation.add(eq("entity", entity));

        // to encourage mysql to use the correct index
        OrConstraint participationActs = new OrConstraint();
        for (String archetype : archetypes) {
            participationActs.add(new ParticipationConstraint(ActShortName, archetype));
        }
        participation.add(participationActs);
        query.add(participation);
        if (!ids.isEmpty()) {
            query.add(not(in("id", ids.toArray())));
        }
        OrConstraint or = new OrConstraint();
        for (Times times : events) {
            Date startTime = times.getStartTime();
            if (times.isInstant()) {
                or.add(and(lte("startTime", startTime), gt("endTime", startTime)));
            } else {
                or.add(and(lt("startTime", times.getEndTime()), gt("endTime", startTime)));
            }
        }
        query.add(or);
        query.add(sort("startTime"));
        query.setMaxResults(maxResults);
        IArchetypeService service = getService();
        return new ObjectSetQueryIterator(service, query);
    }

    /**
     * Creates a {@link Times} from an object set.
     *
     * @param set the set
     * @return the new times
     */
    protected Times createTimes(ObjectSet set) {
        return new Times(set.getReference("act.reference"), set.getDate("act.startTime"), set.getDate("act.endTime"));
    }

    /**
     * Returns the first event that overlaps the specified events.
     * <p>
     * This implementation queries the database.
     *
     * @param events   the event times
     * @param schedule the schedule
     * @return the first event, or {@code null} if none exists
     */
    private Times getOverlap(List<Times> events, Reference schedule) {
        Times result = null;
        ObjectSetQueryIterator iterator = createOverlappingEventIterator(events, schedule, 1);
        if (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            result = createTimes(set);
        }
        return result;
    }

    /**
     * Returns the event identifiers.
     *
     * @param events the events
     * @return the event identifiers
     */
    private List<Long> getIds(List<Times> events) {
        List<Long> ids = new ArrayList<>();
        for (Times times : events) {
            if (times.getId() != -1) {
                ids.add(times.getId());
            }
        }
        return ids;
    }

    /**
     * Find the first appointment that overlaps the specified times.
     *
     * @param times  the times
     * @param events the appointments
     * @return the overlapping appointment times, or {@code null} if none is found
     */
    private Times getOverlap(Times times, List<PropertySet> events) {
        Times result = null;
        boolean isNew = times.getId() == -1;
        for (PropertySet event : events) {
            Reference ref = event.getReference(ScheduleEvent.ACT_REFERENCE);
            if (isNew || ref.getId() != times.getId()) {
                Date startTime2 = event.getDate(ScheduleEvent.ACT_START_TIME);
                Date endTime2 = event.getDate(ScheduleEvent.ACT_END_TIME);
                if (times.intersects(startTime2, endTime2)) {
                    result = new Times(ref, startTime2, endTime2);
                    break;
                }
            }
        }
        return result;
    }

}
