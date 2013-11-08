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

import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.util.PropertySet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A factory for events used by the {@link ScheduleService}.
 * <p/>
 * Events are converted from acts to {@link PropertySet} instances to:
 * <ul>
 * <li>lower memory requirements</li>
 * <li>cache customer, patient, clinician and schedule names</li>
 * </ul>
 *
 * @author Tim Anderson
 * @see ScheduleEvent
 */
abstract class ScheduleEventFactory {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Cache of status lookup names, keyed on code.
     */
    private final Map<String, String> statusNames;


    /**
     * Constructs a {@link ScheduleEventFactory}.
     *
     * @param eventShortName the event archetype short name
     * @param service        the archetype service
     * @param lookupService  the lookup service
     */
    public ScheduleEventFactory(String eventShortName, IArchetypeService service, ILookupService lookupService) {
        this.service = service;
        statusNames = LookupHelper.getNames(service, lookupService, eventShortName, "status");
    }

    /**
     * Returns all events for a schedule on the given day.
     *
     * @param schedule the schedule
     * @param day      the day
     * @return all events on the specified day for the schedule
     */
    public List<PropertySet> getEvents(Entity schedule, Date day) {
        ScheduleEventQuery query = createQuery(schedule, day);
        IPage<ObjectSet> page = query.query();
        return new ArrayList<PropertySet>(page.getResults());
    }

    /**
     * Creates an event from an event act.
     *
     * @param act the act
     * @return a new event
     */
    public PropertySet createEvent(Act act) {
        ObjectSet set = new ObjectSet();
        ActBean bean = new ActBean(act, service);
        assemble(set, bean);
        return set;
    }

    /**
     * Assembles a {@link PropertySet PropertySet} from a source act.
     *
     * @param target the target set
     * @param source the source act
     */
    protected void assemble(PropertySet target, ActBean source) {
        Act event = source.getAct();
        String status = event.getStatus();
        target.set(ScheduleEvent.ACT_VERSION, event.getVersion());
        target.set(ScheduleEvent.ACT_REFERENCE, event.getObjectReference());
        target.set(ScheduleEvent.ACT_START_TIME, event.getActivityStartTime());
        target.set(ScheduleEvent.ACT_END_TIME, event.getActivityEndTime());
        target.set(ScheduleEvent.ACT_STATUS, status);
        target.set(ScheduleEvent.ACT_STATUS_NAME, statusNames.get(status));
        target.set(ScheduleEvent.ACT_DESCRIPTION, event.getDescription());

        Participation customer = source.getParticipation(CustomerArchetypes.CUSTOMER_PARTICIPATION);
        IMObjectReference customerRef = (customer != null) ? customer.getEntity() : null;

        String customerName = getName(customerRef);
        target.set(ScheduleEvent.CUSTOMER_REFERENCE, customerRef);
        target.set(ScheduleEvent.CUSTOMER_NAME, customerName);
        target.set(ScheduleEvent.CUSTOMER_PARTICIPATION_VERSION, (customer != null) ? customer.getVersion() : -1);

        Participation patient = source.getParticipation(PatientArchetypes.PATIENT_PARTICIPATION);
        IMObjectReference patientRef = (patient != null) ? patient.getEntity() : null;
        String patientName = getName(patientRef);
        target.set(ScheduleEvent.PATIENT_REFERENCE, patientRef);
        target.set(ScheduleEvent.PATIENT_NAME, patientName);
        target.set(ScheduleEvent.PATIENT_PARTICIPATION_VERSION, patient != null ? patient.getVersion() : -1);

        Participation clinician = source.getParticipation(UserArchetypes.CLINICIAN_PARTICIPATION);
        IMObjectReference clinicianRef = (clinician != null) ? clinician.getEntity() : null;
        String clinicianName = getName(clinicianRef);
        target.set(ScheduleEvent.CLINICIAN_REFERENCE, clinicianRef);
        target.set(ScheduleEvent.CLINICIAN_NAME, clinicianName);
        target.set(ScheduleEvent.CLINICIAN_PARTICIPATION_VERSION, (clinician != null) ? clinician.getVersion() : -1);
    }

    /**
     * Creates a query to query events for a particular schedule and day.
     *
     * @param schedule the schedule
     * @param day      the day
     * @return a new query
     */
    protected abstract ScheduleEventQuery createQuery(Entity schedule, Date day);

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Returns the name of an object, given its reference.
     *
     * @param reference the object reference. May be {@code null}
     * @return the name or {@code null} if none exists
     */
    protected String getName(IMObjectReference reference) {
        if (reference != null) {
            ObjectRefConstraint constraint = new ObjectRefConstraint("o", reference);
            ArchetypeQuery query = new ArchetypeQuery(constraint);
            query.add(new NodeSelectConstraint("o.name"));
            query.setMaxResults(1);
            Iterator<ObjectSet> iter = new ObjectSetQueryIterator(service, query);
            if (iter.hasNext()) {
                ObjectSet set = iter.next();
                return set.getString("o.name");
            }
        }
        return null;
    }


    /**
     * Returns the end of the day for the specified date-time.
     *
     * @param datetime the date-time
     * @return one millisecond to midnight for the specified date
     */
    protected Date getEnd(Date datetime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(datetime);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }


}
