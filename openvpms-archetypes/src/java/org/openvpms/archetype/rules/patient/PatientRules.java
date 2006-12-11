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

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.Date;
import java.util.Set;


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
     * Patient location relationship short name.
     */
    private static final String PATIENT_LOCATION
            = "entityRelationship.patientLocation";


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
     * Returns the owner of a patient.
     *
     * @param patient the patient
     * @return the patient's owner, or <code>null</code> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getOwner(Party patient) {
        Party owner = null;
        Set<EntityRelationship> relationships
                = patient.getEntityRelationships();
        for (EntityRelationship relationship : relationships) {
            if (TypeHelper.isA(relationship, PATIENT_OWNER)
                    && relationship.isActive()) {
                IMObjectReference custRef = relationship.getSource();
                if (custRef != null) {
                    owner = (Party) ArchetypeQueryHelper.getByObjectReference(
                            service, custRef);
                    if (owner != null) {
                        break;
                    }
                }
            }
        }
        return owner;
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
}
