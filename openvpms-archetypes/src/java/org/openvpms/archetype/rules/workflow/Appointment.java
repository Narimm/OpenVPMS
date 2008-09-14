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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.workflow;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.util.Iterator;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Appointment {

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
     * The schedule reference.
     */
    public static final String SCHEDULE_REFERENCE
            = "schedule.objectReference";

    /**
     * The schedule name.
     */
    public static final String SCHEDULE_NAME = "schedule.name";

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
     * The appointment type reference.
     */
    public static final String APPOINTMENT_TYPE_REFERENCE
            = "appointmentType.objectReference";

    /**
     * The appointment type name.
     */
    public static final String APPOINTMENT_TYPE_NAME = "appointmentType.name";

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
     * The arrival time.
     */
    public static final String ARRIVAL_TIME = "arrivalTime";


    public static ObjectSet createObjectSet(Act appointment,
                                            IArchetypeService service) {
        ActBean bean = new ActBean(appointment, service);
        ObjectSet result = new ObjectSet();
        result.set(ACT_REFERENCE, appointment.getObjectReference());
        result.set(ACT_START_TIME, appointment.getActivityStartTime());
        result.set(ACT_END_TIME, appointment.getActivityEndTime());
        result.set(ACT_STATUS, appointment.getStatus());
        result.set(ACT_REASON, appointment.getReason());
        result.set(ACT_DESCRIPTION, appointment.getDescription());

        IMObjectReference scheduleRef = bean.getNodeParticipantRef("schedule");
        String scheduleName = getName(scheduleRef, service);
        result.set(SCHEDULE_REFERENCE, scheduleRef);
        result.set(SCHEDULE_NAME, scheduleName);

        IMObjectReference customerRef = bean.getNodeParticipantRef("customer");
        String customerName = getName(customerRef, service);
        result.set(CUSTOMER_REFERENCE, customerRef);
        result.set(CUSTOMER_NAME, customerName);

        IMObjectReference patientRef = bean.getNodeParticipantRef("patient");
        String patientName = getName(patientRef, service);
        result.set(PATIENT_REFERENCE, patientRef);
        result.set(PATIENT_NAME, patientName);

        IMObjectReference typeRef
                = bean.getNodeParticipantRef("appointmentType");
        String typeName = getName(typeRef, service);
        result.set(APPOINTMENT_TYPE_REFERENCE, typeRef);
        result.set(APPOINTMENT_TYPE_NAME, typeName);

        IMObjectReference clinicianRef
                = bean.getNodeParticipantRef("clinician");
        String clinicianName = getName(clinicianRef, service);
        result.set(CLINICIAN_REFERENCE, clinicianRef);
        result.set(CLINICIAN_NAME, clinicianName);

        result.set(ARRIVAL_TIME, bean.getDate(ARRIVAL_TIME));
        return result;
    }

    /**
     * Returns the name of an object, given its reference.
     *
     * @param reference the object reference. May be <tt>null</tt>
     * @return the name or <tt>null</tt> if none exists
     */
    private static String getName(IMObjectReference reference,
                                  IArchetypeService service) {
        if (reference != null) {
            ObjectRefConstraint constraint
                    = new ObjectRefConstraint("o", reference);
            ArchetypeQuery query = new ArchetypeQuery(constraint);
            query.add(new NodeSelectConstraint("o.name"));
            query.setMaxResults(1);
            Iterator<ObjectSet> iter = new ObjectSetQueryIterator(service,
                                                                  query);
            if (iter.hasNext()) {
                ObjectSet set = iter.next();
                return set.getString("o.name");
            }
        }
        return null;
    }
}
