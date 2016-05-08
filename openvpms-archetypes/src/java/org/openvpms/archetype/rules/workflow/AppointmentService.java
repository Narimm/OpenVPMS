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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.workflow;

import net.sf.ehcache.Ehcache;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
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
import static org.openvpms.component.system.common.query.Constraints.not;
import static org.openvpms.component.system.common.query.Constraints.sort;
import static org.openvpms.component.system.common.query.ParticipationConstraint.Field.ActShortName;


/**
 * Implementation of the {@link ScheduleService} for appointments.
 *
 * @author Tim Anderson
 */
public class AppointmentService extends AbstractScheduleService {

    /**
     * Listener for visit reason changes.
     */
    private final IArchetypeServiceListener listener;

    /**
     * The archetypes to cache. .
     */
    private static final String[] SHORT_NAMES = {ScheduleArchetypes.APPOINTMENT, ScheduleArchetypes.BLOCK};

    /**
     * Constructs an {@link AppointmentService}.
     *
     * @param service       the archetype service
     * @param lookupService the lookup service
     * @param cache         the cache
     */
    public AppointmentService(IArchetypeService service, ILookupService lookupService, Ehcache cache) {
        super(SHORT_NAMES, service, cache, new AppointmentFactory(service, lookupService));

        listener = new AbstractArchetypeServiceListener() {
            @Override
            public void saved(IMObject object) {
                onReasonSaved((Lookup) object);
            }

            @Override
            public void removed(IMObject object) {
                onReasonRemoved((Lookup) object);
            }
        };
        service.addListener(ScheduleArchetypes.VISIT_REASON, listener);
    }

    /**
     * Determines if there are acts that overlap with an appointment.
     *
     * @param appointment the appointment
     * @return the first overlapping appointment times, or {@code null} if none are found
     * @throws OpenVPMSException for any error
     */
    public Times getOverlappingAppointment(Act appointment) {
        Times result = null;
        ActBean bean = new ActBean(appointment, getService());
        IMObjectReference schedule = bean.getNodeParticipantRef("schedule");
        Times times = Times.create(appointment);
        if (schedule != null && times != null) {
            result = getOverlappingAppointment(times, schedule);
        }
        return result;
    }

    /**
     * Determines if there are acts that overlap with an appointment.
     * <p/>
     * This checks the cache first for overlaps before making a more expensive database query.
     * <p/>
     * Note that due to race conditions, it is possible that an appointment may be saved in another thread that
     * won't be seen in the cache. This may result in double booking of appointments for schedules that don't support
     * it.
     * While not ideal, the likelihood is small, and needs to be weighed against:
     * <ul>
     * <li>issuing a database query each time</li>
     * <li>changing the architecture to route all appointment updates directly through this service first
     * to keep the cache in sync.</li>
     * </ul>
     *
     * @param times    the appointment times
     * @param schedule the schedule
     * @return the first overlapping appointment times, or {@code null} if none are found
     * @throws OpenVPMSException for any error
     */
    public Times getOverlappingAppointment(Times times, IMObjectReference schedule) {
        Times result = null;
        Date startTime = times.getStartTime();
        Date endTime = times.getEndTime();
        Date fromDay = DateRules.getDate(startTime);
        Date toDay = DateRules.getDate(endTime);
        boolean incomplete = false;

        // first check to see if the days that the appointment spans are already cached.
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
     * Returns the first appointment that overlaps the specified appointments.
     *
     * @param appointments the appointment times
     * @param schedule     the schedule
     * @return the first appointment, or {@code null} if none exists
     */
    public Times getOverlappingAppointment(List<Times> appointments, Entity schedule) {
        return getOverlappingAppointment(appointments, schedule.getObjectReference());
    }

    /**
     * Returns the first appointment that overlaps the specified appointments.
     *
     * @param appointments the appointment times
     * @param schedule     the schedule
     * @return the first appointment, or {@code null} if none exists
     */
    public Times getOverlappingAppointment(List<Times> appointments, IMObjectReference schedule) {
        Times result;
        if (appointments.isEmpty()) {
            result = null;
        } else if (appointments.size() == 1) {
            result = getOverlappingAppointment(appointments.get(0), schedule);
        } else {
            result = getOverlap(appointments, schedule);
        }
        return result;
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
        getService().removeListener(ScheduleArchetypes.VISIT_REASON, listener);
        super.destroy();
    }

    /**
     * Returns the event factory.
     *
     * @return the event factory
     */
    @Override
    protected AppointmentFactory getEventFactory() {
        return (AppointmentFactory) super.getEventFactory();
    }

    /**
     * Returns the first appointment that overlaps the specified appointments.
     * <p/>
     * This implementation queries the database.
     *
     * @param appointments the appointment times
     * @param schedule     the schedule
     * @return the first appointment, or {@code null} if none exists
     */
    private Times getOverlap(List<Times> appointments, IMObjectReference schedule) {
        Times result = null;
        List<Long> ids = new ArrayList<>();
        for (Times times : appointments) {
            if (times.getId() != -1) {
                ids.add(times.getId());
            }
        }
        ArchetypeQuery query = new ArchetypeQuery(ScheduleArchetypes.APPOINTMENT);
        query.getArchetypeConstraint().setAlias("act");
        query.add(new NodeSelectConstraint("id"));
        query.add(new NodeSelectConstraint("startTime"));
        query.add(new NodeSelectConstraint("endTime"));
        JoinConstraint participation = join("schedule");
        participation.add(eq("entity", schedule));

        // to encourage mysql to use the correct index
        participation.add(new ParticipationConstraint(ActShortName, ScheduleArchetypes.APPOINTMENT));
        query.add(participation);
        if (!ids.isEmpty()) {
            query.add(not(in("id", ids.toArray())));
        }
        OrConstraint or = new OrConstraint();
        for (Times times : appointments) {
            or.add(and(lt("startTime", times.getEndTime()), gt("endTime", times.getStartTime())));
        }
        query.add(or);
        query.add(sort("startTime"));
        query.setMaxResults(1);
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(getService(), query);
        if (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            result = new Times(set.getLong("act.id"), set.getDate("act.startTime"), set.getDate("act.endTime"));
        }
        return result;
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
            IMObjectReference ref = event.getReference(ScheduleEvent.ACT_REFERENCE);
            if (isNew || ref.getId() != times.getId()) {
                Date startTime2 = event.getDate(ScheduleEvent.ACT_START_TIME);
                Date endTime2 = event.getDate(ScheduleEvent.ACT_END_TIME);
                if (times.intersects(startTime2, endTime2)) {
                    result = new Times(ref.getId(), startTime2, endTime2);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Invoked when a visit reason is saved. Updates the name cache and clears the appointment cache.
     *
     * @param reason the reason lookup
     */
    private void onReasonSaved(Lookup reason) {
        boolean updated = getEventFactory().addReason(reason);
        if (updated) {
            clearCache();
        }
    }

    /**
     * Invoked when a visit reason is removed. Updates the name cache.
     * If the name is cached, then the appointment cache will be cleared.
     * <p/>
     * Strictly speaking, no lookup will be removed by the archetype service if it is use.
     *
     * @param reason the reason lookup
     */
    private void onReasonRemoved(Lookup reason) {
        if (getEventFactory().removeReason(reason)) {
            clearCache();
        }
    }

}
