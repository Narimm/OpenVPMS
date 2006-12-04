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
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
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
 * Queries <em>act.customerAppointments</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AppointmentQuery {

    /**
     * The act reference.
     */
    public static final String ACT_REFERENCE = "act.objectReference";

    /**
     * The act start time.
     */
    public static final String ACT_START_TIME = "act.startTime";

    /**
     * The act end time.
     */
    public static final String ACT_END_TIME = "act.endTime";

    /**
     * The act status.
     */
    public static final String ACT_STATUS = "act.status";

    /**
     * The act reason.
     */
    public static final String ACT_REASON = "act.reason";

    /**
     * The act description.
     */
    public static final String ACT_DESCRIPTION = "act.description";

    /**
     * The customer reference.
     */
    public static final String CUSTOMER_REFERENCE = "customer.objectReference";

    /**
     * The customer name.
     */
    public static final String CUSTOMER_NAME = "customer.name";

    /**
     * The patient reference.
     */
    public static final String PATIENT_REFERENCE = "patient.objectReference";

    /**
     * The patient name.
     */
    public static final String PATIENT_NAME = "patient.name";

    /**
     * The appointment reference.
     */
    public static final String APPOINTMENT_REFERENCE
            = "appointmentType.objectReference";

    /**
     * The appointment name.
     */
    public static final String APPOINTMENT_NAME = "appointmentType.name";

    /**
     * The clinician reference.
     */
    public static final String CLINICIAN_REFERENCE
            = "clinician.objectReference";

    /**
     * The clinician name.
     */
    public static final String CLINICIAN_NAME = "clinician.name";

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
     * The clinician. May be <code>null</code>
     */
    private Party clinician;

    /**
     * Constructs a new <code>AppointmentQuery</code>.
     */
    public AppointmentQuery() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <code>AppointmentQuery</code>.
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
     * @param clinician the clinician. May be <code>null</code>
     */
    public void setClinician(Party clinician) {
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
     * Executes the query.
     *
     * @return the query results
     * @throws ArchetypeServiceException if the query fails
     */
    public IPage<ObjectSet> query() {
        if (schedule == null || from == null || to == null) {
            // return an empty set. Need these values to perform a query
            return new Page<ObjectSet>();
        }
        IArchetypeQuery query = createQuery();
        IPage<ObjectSet> page = service.getObjects(query);
        List<ObjectSet> result = new ArrayList<ObjectSet>();
        IMObjectReference currentAct = null;
        ObjectSet current = null;
        for (ObjectSet set : page.getResults()) {
            IMObjectReference actRef = (IMObjectReference) set.get(
                    ACT_REFERENCE);
            if (currentAct == null || !currentAct.equals(actRef)) {
                if (current != null) {
                    result.add(current);
                }
                currentAct = actRef;
                current = new ObjectSet();
                current.add(ACT_REFERENCE, actRef);
                current.add(ACT_START_TIME, set.get(ACT_START_TIME));
                current.add(ACT_END_TIME, set.get(ACT_END_TIME));
                current.add(ACT_STATUS, set.get(ACT_STATUS));
                current.add(ACT_REASON, set.get(ACT_REASON));
                current.add(ACT_DESCRIPTION, set.get(ACT_DESCRIPTION));
            }
            IMObjectReference entityRef
                    = (IMObjectReference) set.get("entity.objectReference");
            String entityName = (String) set.get("entity.name");
            if (TypeHelper.isA(entityRef, "party.customer*")) {
                current.add(CUSTOMER_REFERENCE, entityRef);
                current.add(CUSTOMER_NAME, entityName);
            } else if (TypeHelper.isA(entityRef, "party.patient*")) {
                current.add(PATIENT_REFERENCE, entityRef);
                current.add(PATIENT_NAME, entityName);
            } else if (TypeHelper.isA(entityRef, "entity.appointmentType")) {
                current.add(APPOINTMENT_REFERENCE, entityRef);
                current.add(APPOINTMENT_NAME, entityName);
            } else if (TypeHelper.isA(entityRef, "security.user")) {
                current.add(CLINICIAN_REFERENCE, entityRef);
                current.add(CLINICIAN_NAME, entityName);
            }
        }
        if (current != null) {
            result.add(current);
        }
        return new Page<ObjectSet>(result, 0, result.size(), result.size());
    }

    /**
     * Creates a new query.
     *
     * @return the query
     */
    private IArchetypeQuery createQuery() {
        Collection<String> names = Arrays.asList("act.objectReference",
                                                 "act.startTime", "act.endTime",
                                                 "act.status", "act.reason",
                                                 "act.description",
                                                 "entity.objectReference",
                                                 "entity.name");
        NamedQuery query;
        if (clinician != null) {
            query = new NamedQuery("act.customerAppointment-clinician", names);
            query.setParameter("clinicianId", clinician.getLinkId());
        } else {
            query = new NamedQuery("act.customerAppointment", names);
        }

        query.setParameter("scheduleId", schedule.getLinkId());
        query.setParameter("from", from);
        query.setParameter("to", to);
        query.setMaxResults(IArchetypeQuery.ALL_RESULTS);
        return query;
    }


}
