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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.business.service.cache.EhcacheManager;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.model.act.Act;
import org.openvpms.component.model.user.User;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.ParticipationConstraint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openvpms.component.system.common.query.Constraints.and;
import static org.openvpms.component.system.common.query.Constraints.eq;
import static org.openvpms.component.system.common.query.Constraints.gt;
import static org.openvpms.component.system.common.query.Constraints.join;
import static org.openvpms.component.system.common.query.Constraints.lt;
import static org.openvpms.component.system.common.query.Constraints.ne;
import static org.openvpms.component.system.common.query.ParticipationConstraint.Field.ActShortName;


/**
 * Implementation of the {@link ScheduleService} for appointments.
 * <p>
 * This uses the supplied {@link EhcacheManager} to create a cache named "appointmentCache".
 *
 * @author Tim Anderson
 */
public class AppointmentService extends AbstractCalendarService {

    /**
     * Listener for visit reason changes.
     */
    private final IArchetypeServiceListener listener;

    /**
     * The archetypes to cache. .
     */
    private static final String[] SHORT_NAMES = {ScheduleArchetypes.APPOINTMENT, ScheduleArchetypes.CALENDAR_BLOCK};

    /**
     * Constructs an {@link AppointmentService}.
     *
     * @param service       the archetype service
     * @param lookupService the lookup service
     * @param cacheManager  the cache manager
     */
    public AppointmentService(IArchetypeService service, ILookupService lookupService,
                              EhcacheManager cacheManager) {
        super(SHORT_NAMES, service, cacheManager, "appointmentCache", new AppointmentFactory(service, lookupService));

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
     * Returns the non-cancelled appointments for a clinician in the specified date range.
     *
     * @param clinician the clinician
     * @param from      the start time, inclusive
     * @param to        the end time, inclusive
     * @return the appointment times
     */
    public List<Times> getAppointmentsForClinician(User clinician, Date from, Date to) {
        return getAppointmentsForClinician(clinician, from, to, null);
    }

    /**
     * Returns the non-cancelled appointments for a clinician in the specified date range.
     *
     * @param clinician the clinician
     * @param from      the start time, inclusive
     * @param to        the end time, inclusive
     * @param excluding an appointment to exclude from the results. May be {@code null}
     * @return the appointment times
     */
    public List<Times> getAppointmentsForClinician(User clinician, Date from, Date to, Act excluding) {
        List<Times> result = new ArrayList<>();
        ArchetypeQuery query = new ArchetypeQuery(ScheduleArchetypes.APPOINTMENT, false, false);
        query.getArchetypeConstraint().setAlias("act");
        query.add(new ObjectRefSelectConstraint("act"));
        query.add(new NodeSelectConstraint("startTime"));
        query.add(new NodeSelectConstraint("endTime"));
        JoinConstraint participation = join("clinician");
        participation.add(eq("entity", clinician));
        participation.add(new ParticipationConstraint(ActShortName, ScheduleArchetypes.APPOINTMENT));
        query.add(participation);
        query.add(and(lt("startTime", to), gt("endTime", from)));
        query.add(ne("status", AppointmentStatus.CANCELLED));
        if (excluding != null) {
            query.add(ne("id", excluding.getId()));
        }
        IArchetypeService service = getService();
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
        while (iterator.hasNext()) {
            result.add(createTimes(iterator.next()));
        }
        return result;
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     *
     * @throws Exception in case of shutdown errors.
     */
    @Override
    public void destroy() throws Exception {
        try {
            getService().removeListener(ScheduleArchetypes.VISIT_REASON, listener);
        } finally {
            super.destroy();
        }
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
     * <p>
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
