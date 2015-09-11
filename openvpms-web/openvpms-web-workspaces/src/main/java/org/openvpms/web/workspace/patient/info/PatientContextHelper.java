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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.info;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.system.ServiceHelper;

/**
 * Helper to create {@link PatientContext} instances.
 *
 * @author Tim Anderson
 */
public class PatientContextHelper {

    /**
     * Returns the patient context for an appointment.
     *
     * @param appointment the appointment
     * @param context     the context
     * @return the patient context, or {@code null} if the patient can't be found, or has no current visit
     */
    public static PatientContext getAppointmentContext(Act appointment, Context context) {
        PatientContext result = null;
        ActBean bean = new ActBean(appointment);
        Party patient = (Party) bean.getNodeParticipant("patient");
        Party location = context.getLocation();
        if (patient != null && location != null) {
            Party customer = (Party) bean.getNodeParticipant("customer");
            PatientContextFactory factory = ServiceHelper.getBean(PatientContextFactory.class);
            result = factory.createContext(patient, customer, context.getLocation());
        }
        return result;
    }

    /**
     * Returns the patient context for a patient associated with an act.
     * <p>
     * Note that this uses the current owner and visit for the patient.
     *
     * @param act     the patient act
     * @param context the context
     * @return the patient context, or {@code null} if the patient can't be found, or has no current visit
     */
    public static PatientContext getPatientContext(Act act, Context context) {
        PatientContext result = null;
        ActBean bean = new ActBean(act);
        Party patient = (Party) bean.getNodeParticipant("patient");
        Party location = context.getLocation();
        if (patient != null && location != null) {
            PatientContextFactory factory = ServiceHelper.getBean(PatientContextFactory.class);
            result = factory.createContext(patient, location);
        }
        return result;
    }

    /**
     * Returns the patient context for a patient.
     *
     * @param patient the patient. May be {@code null}
     * @param context the context
     * @return the patient context, or {@code null} if the patient doesn't exist or has no current visit
     */
    public static PatientContext getPatientContext(Party patient, Context context) {
        PatientContext result = null;
        Party location = context.getLocation();
        if (patient != null && location != null) {
            PatientContextFactory factory = ServiceHelper.getBean(PatientContextFactory.class);
            result = factory.createContext(patient, location);
        }
        return result;
    }

}
