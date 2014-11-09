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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.info;

import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.hl7.patient.PatientContext;
import org.openvpms.hl7.patient.PatientContextFactory;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;

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
        if (patient != null) {
            Party customer = (Party) bean.getNodeParticipant("customer");
            User clinician = (User) bean.getNodeParticipant("clinician");
            MedicalRecordRules rules = ServiceHelper.getBean(MedicalRecordRules.class);
            Act event = rules.getEvent(patient, new Date());
            if (event != null) {
                result = getPatientContext(patient, customer, event, context, clinician);
            }
        }
        return result;
    }

    /**
     * Returns the patient context for a patient act.
     *
     * @param act     the patient act
     * @param context the context
     * @return the patient context, or {@code null} if the patient can't be found, or has no current visit
     */
    public static PatientContext getPatientContext(Act act, Context context) {
        ActBean bean = new ActBean(act);
        return getPatientContext((Party) bean.getNodeParticipant("patient"), context);
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
        if (patient != null) {
            PatientRules patientRules = ServiceHelper.getBean(PatientRules.class);
            Party customer = patientRules.getOwner(patient);
            MedicalRecordRules rules = ServiceHelper.getBean(MedicalRecordRules.class);
            Act event = rules.getEvent(patient, new Date());
            if (event != null) {
                ActBean bean = new ActBean(event);
                User clinician = (User) bean.getNodeParticipant("clinician");
                result = getPatientContext(patient, customer, event, context, clinician);
            }
        }
        return result;
    }

    private static PatientContext getPatientContext(Party patient, Party customer, Act event, Context context,
                                                    User clinician) {
        PatientContextFactory factory = ServiceHelper.getBean(PatientContextFactory.class);
        return factory.createContext(patient, customer, event, context.getLocation(), clinician);
    }
}
