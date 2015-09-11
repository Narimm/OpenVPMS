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

package org.openvpms.hl7.patient;

import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.archetype.rules.patient.MedicalRecordRules;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.util.Date;

/**
 * Factory for {@link PatientContext} instances.
 *
 * @author Tim Anderson
 */
public class PatientContextFactory {

    /**
     * The patient rules.
     */
    private final PatientRules patientRules;

    /**
     * The customer rules.
     */
    private final CustomerRules customerRules;

    /**
     * The patient medical record rules.
     */
    private final MedicalRecordRules recordRules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * Constructs a {@link PatientContextFactory}.
     *
     * @param patientRules  the patient rules
     * @param customerRules the customer rules
     * @param recordRules   the patient medical record rules
     * @param service       the archetype service
     * @param lookups       the lookup service
     */
    public PatientContextFactory(PatientRules patientRules, CustomerRules customerRules,
                                 MedicalRecordRules recordRules, IArchetypeService service, ILookupService lookups) {
        this.patientRules = patientRules;
        this.customerRules = customerRules;
        this.recordRules = recordRules;
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Returns the patient context for a patient visit.
     * <p>
     * This will use the visit location if available, falling back to the supplied location if none is present.
     *
     * @param visit    the patient visit
     * @param location the practice location
     * @return the patient context, or {@code null} if the patient can't be found
     */
    public PatientContext createContext(Act visit, Party location) {
        PatientContext result = null;
        ActBean bean = new ActBean(visit, service);
        Party patient = (Party) bean.getNodeParticipant("patient");
        if (patient != null) {
            Party visitLocation = (Party) bean.getNodeParticipant("location");
            if (visitLocation != null) {
                location = visitLocation;
            }
            result = createContext(patient, visit, location);
        }
        return result;
    }

    /**
     * Creates a new context for a patient and practice location.
     * <p>
     * The patient must have a current visit.
     *
     * @param patient  the patient
     * @param location the practice location
     * @return a new {@link PatientContext}, or {@code null} if the patient has no current visit
     */
    public PatientContext createContext(Party patient, Party location) {
        PatientContext result = null;
        Act visit = recordRules.getEvent(patient, new Date());
        if (visit != null) {
            result = createContext(patient, visit, location);
        }
        return result;
    }

    /**
     * Creates a new context for a patient, customer and practice location.
     * <p>
     * The patient must have a current visit.
     *
     * @param patient  the patient
     * @param customer the patient owner. May be {@code null}
     * @param location the practice location
     * @return a new {@link PatientContext}, or {@code null} if the patient has no current visit
     */
    public PatientContext createContext(Party patient, Party customer, Party location) {
        PatientContext result = null;
        Act visit = recordRules.getEvent(patient, new Date());
        if (visit != null) {
            result = createContext(patient, customer, visit, location);
        }
        return result;
    }

    /**
     * Creates a new context.
     * <p>
     * This uses the owner of the patient at the time of visit.
     *
     * @param patient  the patient
     * @param visit    the patient visit
     * @param location the practice location
     * @return a new context
     */
    public PatientContext createContext(Party patient, Act visit, Party location) {
        Party customer = patientRules.getOwner(patient, visit.getActivityStartTime(), false);
        return createContext(patient, customer, visit, location);
    }

    /**
     * Creates a new context.
     * <p>
     * This uses the owner of the patient at the time of visit.
     *
     * @param patient  the patient
     * @param customer the patient owner. May be {@code null}
     * @param visit    the patient visit
     * @param location the practice location
     * @return a new context
     */
    public PatientContext createContext(Party patient, Party customer, Act visit, Party location) {
        ActBean bean = new ActBean(visit, service);
        User clinician = (User) bean.getNodeParticipant("clinician");
        return createContext(patient, customer, visit, location, clinician);
    }

    /**
     * Creates a new context.
     *
     * @param patient   the patient
     * @param customer  the patient owner. May be {@code null}
     * @param visit     the patient visit (an <em>act.patientClinicalEvent</em>
     * @param location  the practice location
     * @param clinician the clinician
     * @return a new {@link PatientContext}
     */
    public PatientContext createContext(Party patient, Party customer, Act visit, Party location, User clinician) {
        return new PatientContext(patient, customer, visit, location, clinician, patientRules, customerRules, service,
                                  lookups);
    }

}
