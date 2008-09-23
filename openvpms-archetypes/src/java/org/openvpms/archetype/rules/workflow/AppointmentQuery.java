/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.workflow;

import org.openvpms.component.business.dao.im.Page;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValue;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.IArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NamedQuery;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;


/**
 * Queries <em>act.customerAppointments</em>, returning a limited set of
 * data for display purposes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class AppointmentQuery {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The schedule.
     */
    private Party schedule;

    /**
     * The 'from' start time.
     */
    private Date from;

    /**
     * The 'to' start time.
     */
    private Date to;

    /**
     * The clinician. May be <tt>null</tt>
     */
    private User clinician;

    /**
     * The statuses to query.
     */
    private WorkflowStatus.StatusRange range = WorkflowStatus.StatusRange.ALL;


    /**
     * Constructs a new <tt>AppointmentQuery</tt>.
     */
    public AppointmentQuery() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <tt>AppointmentQuery</tt>.
     *
     * @param service the archetype service
     */
    public AppointmentQuery(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Sets the schedule.
     *
     * @param schedule the schedule
     */
    public void setSchedule(Party schedule) {
        this.schedule = schedule;
    }

    /**
     * Sets the clinician.
     *
     * @param clinician the clinician. May be <tt>null</tt> to indicate all
     *                  clinicians
     */
    public void setClinician(User clinician) {
        this.clinician = clinician;
    }

    /**
     * Sets the start time date range.
     *
     * @param from the 'from' start time
     * @param to   the 'to' start time
     */
    public void setDateRange(Date from, Date to) {
        this.from = from;
        this.to = to;
    }

    /**
     * Sets the act status range.
     *
     * @param range the status range
     */
    public void setStatusRange(WorkflowStatus.StatusRange range) {
        this.range = range;
    }

    /**
     * Executes the query.
     * Returns an empty page if any of the schedule, from or to dates are null.
     *
     * @return the query results.
     * @throws ArchetypeServiceException if the query fails
     */
    public IPage<ObjectSet> query() {
        if (schedule == null || from == null || to == null) {
            // return an empty set. Need these values to perform a query
            return new Page<ObjectSet>(new ArrayList<ObjectSet>(), 0, 0, -1);
        }
        IArchetypeQuery query = createQuery();
        IPage<ObjectSet> page = service.getObjects(query);
        List<ObjectSet> result = new ArrayList<ObjectSet>();
        IMObjectReference currentAct = null;
        ObjectSet current = null;
        for (ObjectSet set : page.getResults()) {
            IMObjectReference actRef = getAct(set);
            if (currentAct == null || !currentAct.equals(actRef)) {
                if (current != null) {
                    result.add(current);
                }
                currentAct = actRef;
                current = new ObjectSet();
                current.set(Appointment.ACT_REFERENCE, actRef);
                current.set(Appointment.ACT_START_TIME, set.get(Appointment.ACT_START_TIME));
                current.set(Appointment.ACT_END_TIME, set.get(Appointment.ACT_END_TIME));
                current.set(Appointment.ACT_STATUS, set.get(Appointment.ACT_STATUS));
                current.set(Appointment.ACT_REASON, set.get(Appointment.ACT_REASON));
                current.set(Appointment.ACT_DESCRIPTION, set.get(Appointment.ACT_DESCRIPTION));
                current.set(Appointment.CUSTOMER_REFERENCE, null);
                current.set(Appointment.CUSTOMER_NAME, null);
                current.set(Appointment.PATIENT_REFERENCE, null);
                current.set(Appointment.PATIENT_NAME, null);
                current.set(Appointment.APPOINTMENT_TYPE_REFERENCE, null);
                current.set(Appointment.APPOINTMENT_TYPE_NAME, null);
                current.set(Appointment.CLINICIAN_REFERENCE, null);
                current.set(Appointment.CLINICIAN_NAME, null);
                current.set(Appointment.ARRIVAL_TIME, null);
            }
            IMObjectReference entityRef = getEntity(set);
            String entityName = set.getString("entity.name");
            if (TypeHelper.isA(entityRef, "party.customer*")) {
                current.set(Appointment.CUSTOMER_REFERENCE, entityRef);
                current.set(Appointment.CUSTOMER_NAME, entityName);
            } else if (TypeHelper.isA(entityRef, "party.patient*")) {
                current.set(Appointment.PATIENT_REFERENCE, entityRef);
                current.set(Appointment.PATIENT_NAME, entityName);
            } else if (TypeHelper.isA(entityRef, "entity.appointmentType")) {
                current.set(Appointment.APPOINTMENT_TYPE_REFERENCE, entityRef);
                current.set(Appointment.APPOINTMENT_TYPE_NAME, entityName);
            } else if (TypeHelper.isA(entityRef, "security.user")) {
                current.set(Appointment.CLINICIAN_REFERENCE, entityRef);
                current.set(Appointment.CLINICIAN_NAME, entityName);
            }
            String key = set.getString("act.details_Keys");
            TypedValue value = (TypedValue) set.get("act.details_Values");
            if (key != null) {
                Object object = (value != null) ? value.getObject() : null;
                current.set(key, object);
            }
        }
        if (current != null) {
            result.add(current);
        }
        return new Page<ObjectSet>(result, 0, result.size(), result.size());
    }

    /**
     * Helper to return the act reference from a set.
     *
     * @param set the set
     * @return the ct
     */
    private IMObjectReference getAct(ObjectSet set) {
        ArchetypeId archetypeId = (ArchetypeId) set.get("act.archetypeId");
        long id = set.getLong("act.id");
        String linkId = set.getString("act.linkId");
        return new IMObjectReference(archetypeId, id, linkId);
    }

    /**
     * Helper to return the entity reference from a set.
     *
     * @param set the set
     * @return the entity
     */
    private IMObjectReference getEntity(ObjectSet set) {
        ArchetypeId archetypeId = (ArchetypeId) set.get("entity.archetypeId");
        long id = set.getLong("entity.id");
        String linkId = set.getString("entity.linkId");
        return new IMObjectReference(archetypeId, id, linkId);
    }

    /**
     * Creates a new query.
     *
     * @return the query
     */
    private IArchetypeQuery createQuery() {
        Collection<String> names = Arrays.asList("act.archetypeId",
                                                 "act.id", "act.linkId",
                                                 "act.startTime", "act.endTime",
                                                 "act.details_Keys",
                                                 "act.details_Values",
                                                 "act.status", "act.reason",
                                                 "act.description",
                                                 "entity.archetypeId",
                                                 "entity.id", "entity.linkId",
                                                 "entity.name");
        NamedQuery query;
        if (clinician != null) {
            if (range == WorkflowStatus.StatusRange.INCOMPLETE) {
                query = new NamedQuery(
                        "act.customerAppointment-clinician-incomplete", names);
            } else if (range == WorkflowStatus.StatusRange.COMPLETE) {
                query = new NamedQuery(
                        "act.customerAppointment-clinician-complete", names);
            } else {
                query = new NamedQuery(
                        "act.customerAppointment-clinician", names);
            }
            query.setParameter("clinicianId", clinician.getId());
        } else {
            if (range == WorkflowStatus.StatusRange.INCOMPLETE) {
                query = new NamedQuery(
                        "act.customerAppointment-incomplete", names);
            } else if (range == WorkflowStatus.StatusRange.COMPLETE) {
                query = new NamedQuery(
                        "act.customerAppointment-complete", names);
            } else {
                query = new NamedQuery(
                        "act.customerAppointment", names);
            }
        }

        query.setParameter("scheduleId", schedule.getId());
        query.setParameter("from", from);
        query.setParameter("to", to);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        return query;
    }

}
