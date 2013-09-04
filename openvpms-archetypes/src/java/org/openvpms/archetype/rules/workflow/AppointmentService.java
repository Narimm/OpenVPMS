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

import net.sf.ehcache.Cache;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Implementation of the {@link ScheduleService} for appointment events.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentService extends AbstractScheduleService {

    /**
     * Reason lookup names, keyed on code.
     */
    private final Map<String, String> reasonNames = Collections.synchronizedMap(new HashMap<String, String>());

    /**
     * Constructs an <tt>AppointmentService</tt>.
     *
     * @param service       the archetype service
     * @param lookupService the lookup service
     * @param cache         the cache
     */
    public AppointmentService(IArchetypeService service, ILookupService lookupService, Cache cache) {
        super(ScheduleArchetypes.APPOINTMENT, service, lookupService, cache);

        Map<String, String> map = LookupHelper.getNames(service, lookupService, ScheduleArchetypes.APPOINTMENT,
                                                        "reason");
        reasonNames.putAll(map);
        service.addListener("lookup.appointmentReason", new AbstractArchetypeServiceListener() {
            @Override
            public void saved(IMObject object) {
                onReasonSaved((Lookup) object);
            }

            @Override
            public void removed(IMObject object) {
                onReasonRemoved((Lookup) object);
            }
        });
    }

    /**
     * Assembles an {@link PropertySet PropertySet} from a source act.
     *
     * @param target the target set
     * @param source the source act
     */
    @Override
    protected void assemble(PropertySet target, ActBean source) {
        super.assemble(target, source);

        String reason = source.getAct().getReason();
        target.set(ScheduleEvent.ACT_REASON, reason);
        target.set(ScheduleEvent.ACT_REASON_NAME, reasonNames.get(reason));

        IMObjectReference scheduleRef = source.getNodeParticipantRef("schedule");
        String scheduleName = getName(scheduleRef);
        target.set(ScheduleEvent.SCHEDULE_REFERENCE, scheduleRef);
        target.set(ScheduleEvent.SCHEDULE_NAME, scheduleName);

        IMObjectReference typeRef
                = source.getNodeParticipantRef("appointmentType");
        String typeName = getName(typeRef);
        target.set(ScheduleEvent.SCHEDULE_TYPE_REFERENCE, typeRef);
        target.set(ScheduleEvent.SCHEDULE_TYPE_NAME, typeName);
        target.set(ScheduleEvent.ARRIVAL_TIME, source.getDate(ScheduleEvent.ARRIVAL_TIME));
    }

    /**
     * Returns the schedule reference from an event.
     *
     * @param event the event
     * @return a reference to the schedule. May be <tt>null</tt>
     */
    protected IMObjectReference getSchedule(Act event) {
        ActBean bean = new ActBean(event, getService());
        return bean.getNodeParticipantRef("schedule");
    }

    /**
     * Creates a new query to query events for the specified schedule and date
     * range.
     *
     * @param schedule the schedule
     * @param from     the start time
     * @param to       the end time
     * @return a new query
     */
    protected ScheduleEventQuery createQuery(Entity schedule, Date from, Date to) {
        return new AppointmentQuery((Party) schedule, from, to, getService());
    }

    /**
     * Invoked when an appointment reason is saved. Updates the name cache and clears the appointment cache.
     *
     * @param reason the reason lookup
     */
    private void onReasonSaved(Lookup reason) {
        boolean exists;
        synchronized (reasonNames) {
            exists = reasonNames.containsKey(reason.getCode());
            reasonNames.put(reason.getCode(), reason.getName());
        }
        if (exists) {
            clearCache();
        }
    }

    /**
     * Invoked when an appointment reason is removed. Updates the name cache.
     * If the name is cached, then the appointment cache will be cleared.
     * <p/>
     * Strictly speaking, no lookup will be removed by the archetype service if it is use.
     *
     * @param reason the reason lookup
     */
    private void onReasonRemoved(Lookup reason) {
        if (reasonNames.remove(reason.getCode()) != null) {
            clearCache();
        }
    }

}
