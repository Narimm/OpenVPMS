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

package org.openvpms.archetype.rules.workflow.roster;

import org.openvpms.archetype.rules.workflow.AbstractCalendarService;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleEvents;
import org.openvpms.archetype.rules.workflow.Times;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.cache.EhCacheable;
import org.openvpms.component.business.service.cache.EhcacheManager;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.model.user.User;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.ParticipationConstraint;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.and;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.gt;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.lt;
import static org.openvpms.component.system.common.query.ParticipationConstraint.Field.ActShortName;

/**
 * Roster Service.
 *
 * @author Tim Anderson
 */
public class RosterService extends AbstractCalendarService {

    public static class UserEvent {

        private final Reference event;

        private final Reference area;

        private final Date startTime;

        private final Date endTime;

        UserEvent(Reference event, Reference area, Date startTime, Date endTime) {
            this.event = event;
            this.area = area;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public Reference getEvent() {
            return event;
        }

        public Reference getArea() {
            return area;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

    }

    /**
     * Caches roster events by user.
     */
    private final RosterUserCache userCache;

    /**
     * Constructs a {@link RosterService}.
     *
     * @param service      the archetype service
     * @param cacheManager the cache manager
     */
    public RosterService(IArchetypeService service, EhcacheManager cacheManager) {
        super(new String[]{ScheduleArchetypes.ROSTER_EVENT}, service, cacheManager, "rosterAreaCache",
              new RosterEventByAreaFactory(service));
        userCache = new RosterUserCache(cacheManager, "rosterUserCache", new RosterEventByUserFactory(service));
    }

    /**
     * Returns all of the events for a user between two times.
     *
     * @param user the the user
     * @param from the from date, inclusive
     * @param to   the to date, exclusive
     * @return the events
     */
    public ScheduleEvents getUserEvents(User user, Date from, Date to) {
        return userCache.getEvents(user, from, to);
    }

    /**
     * Returns the modification hash for the specified user and date range.
     * <p>
     * This can be used to determine if any of the events have changed.
     *
     * @param user the user
     * @param from the from date, inclusive
     * @param to   the to date, exclusive
     * @return the modification hash, or {@code -1} if the schedule and day are not cached
     */
    public long getUserModHash(User user, Date from, Date to) {
        return userCache.getModHash(user, from, to);
    }

    /**
     * Returns references to the active schedules in a roster area.
     *
     * @param area the roster area
     * @return the schedule references
     */
    public List<Reference> getSchedules(Reference area) {
        List<Reference> result = new ArrayList<>();
        ArchetypeQuery query = new ArchetypeQuery((IMObjectReference) area);
        query.add(new ObjectRefSelectConstraint("schedule"));
        query.add(eq("active", true));
        query.add(join("schedules").add(join("target", "schedule").add(eq("active", true))));
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(query);
        while (iterator.hasNext()) {
            result.add(iterator.next().getReference("schedule.reference"));
        }
        return result;
    }

    /**
     * Returns roster events for a user at a practice location, between the specified dates.
     *
     * @param user     the user
     * @param location the practice location. If {@code null}, returns events for all locations
     * @param from     the starting date
     * @param to       the ending date
     * @return the events
     */
    public List<UserEvent> getUserEvents(User user, Party location, Date from, Date to) {
        List<UserEvent> result = new ArrayList<>();
        ArchetypeQuery query = new ArchetypeQuery(ScheduleArchetypes.ROSTER_EVENT, false, false);
        query.getArchetypeConstraint().setAlias("act");
        query.add(new ObjectRefSelectConstraint("act"));
        query.add(new ObjectRefSelectConstraint("schedule.entity"));
        query.add(new NodeSelectConstraint("startTime"));
        query.add(new NodeSelectConstraint("endTime"));

        JoinConstraint userpartic = createJoin("user");
        userpartic.add(eq("entity", user));
        query.add(userpartic);

        if (location != null) {
            JoinConstraint locationpartic = createJoin("location");
            locationpartic.add(eq("entity", location));
            query.add(locationpartic);
        }

        query.add(join("schedule"));
        query.add(and(lt("startTime", to), gt("endTime", from)));
        IArchetypeService service = getService();
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
        while (iterator.hasNext()) {
            PropertySet set = iterator.next();
            result.add(new UserEvent(set.getReference("act.reference"), set.getReference("schedule.entity.reference"),
                                     set.getDate("act.startTime"), set.getDate("act.endTime")));
        }
        return result;
    }

    /**
     * Returns events that overlap those supplied, for a particular user.
     *
     * @param events the events to check
     * @param user   the user
     * @param limit  the maximum no. of events to return
     * @return the overlapping events, or {@code null} if no events overlap
     */
    public List<Times> getOverlappingEvents(List<Times> events, User user, int limit) {
        List<Times> result = new ArrayList<>();
        ObjectSetQueryIterator iterator = createOverlappingEventIterator(events, "user", user.getObjectReference(),
                                                                         limit);
        while (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            result.add(createTimes(set));
        }
        return !result.isEmpty() ? result : null;
    }

    /**
     * Returns the user cache.
     *
     * @return the user cache
     */
    public EhCacheable getUserCache() {
        return userCache;
    }

    /**
     * Adds an event to the cache.
     *
     * @param event the event to add
     */
    @Override
    protected void addEvent(Act event) {
        super.addEvent(event);
        userCache.addEvent(event);
    }

    /**
     * Removes an event from the cache.
     *
     * @param event the event to remove
     */
    @Override
    protected void removeEvent(Act event) {
        super.removeEvent(event);
        userCache.removeEvent(event);
    }

    private JoinConstraint createJoin(String node) {
        JoinConstraint userpartic = join(node);
        userpartic.add(new ParticipationConstraint(ActShortName, ScheduleArchetypes.ROSTER_EVENT));
        return userpartic;
    }
}
