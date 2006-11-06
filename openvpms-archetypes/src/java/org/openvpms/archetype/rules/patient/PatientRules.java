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
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.Set;


/**
 * Patient rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientRules {

    /**
     * Patient owner relationship short name.
     */
    private static final String PATIENT_OWNER
            = "entityRelationship.patientOwner";


    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Construct a new <code>PatientRules/code>.
     *
     * @param service the archetype service
     */
    public PatientRules(IArchetypeService service) {
        this.service = service;
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
}
