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

package org.openvpms.archetype.rules.patient;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;

import java.util.Date;


/**
 * Patient rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Patient owner relationship short name.
     */
    private static final String PATIENT_OWNER
            = "entityRelationship.patientOwner";


    /**
     * Constructs a new <code>PatientRules</code>.
     *
     * @throws ArchetypeServiceException if the archetype service is not
     *                                   configured
     */
    public PatientRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Construct a new <code>PatientRules/code>.
     *
     * @param service the archetype service
     */
    public PatientRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Adds a patient-owner relationship between the supplied customer and
     * patient.
     *
     * @param customer the customer
     * @param patient  the patient
     * @throws ArchetypeServiceException for any archetype service error
     */
    public void addPatientOwnerRelationship(Party customer, Party patient) {
        EntityRelationship relationship
                = (EntityRelationship) service.create(PATIENT_OWNER);
        relationship.setActiveStartTime(new Date());
        relationship.setSequence(1);
        relationship.setSource(new IMObjectReference(customer));
        relationship.setTarget(new IMObjectReference(patient));
        customer.addEntityRelationship(relationship);
        patient.addEntityRelationship(relationship);
    }

    /**
     * Returns the owner of a patient associated with an act.
     *
     * @param act the act
     * @return the patient's owner, or <code>null</code> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getOwner(Act act) {
        ActBean bean = new ActBean(act, service);
        Party patient = (Party) bean.getParticipant("participation.patient");
        Date startTime = act.getActivityStartTime();
        if (patient != null && startTime != null) {
            EntityBean patientBean = new EntityBean(patient, service);
            return (Party) patientBean.getSourceEntity(PATIENT_OWNER, startTime);
        }
        return null;
    }

    /**
     * Returns the owner of a patient.
     *
     * @param patient the patient
     * @return the patient's owner, or <code>null</code> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getOwner(Party patient) {
        EntityBean bean = new EntityBean(patient, service);
        return (Party) bean.getSourceEntity(PATIENT_OWNER);
    }

    /**
     * Determines if a patient has a customer as its owner.
     *
     * @param customer the customer
     * @param patient  the patient
     * @return <code>true</code> if the customer is the owner of the patient
     * @throws ArchetypeServiceException for any archetype service error
     */
    public boolean isOwner(Party customer, Party patient) {
        Party owner = getOwner(patient);
        return (owner != null && owner.equals(customer));
    }

    /**
     * Returns the referral vet for a patient.
     * This is the associated party from the first matching
     * <em>entityRelationship.referredFrom</em> or
     * <em>entityrRelationship.referredTo</em> overlapping the specified time.
     *
     * @param patient the patient
     * @param time    the time
     * @return the referral vet, or <code>null</code> if none is founds
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getReferralVet(Party patient, Date time) {
        EntityBean bean = new EntityBean(patient);
        return (Party) bean.getNodeTargetEntity("referrals", time);
    }
}