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

package org.openvpms.web.app.patient;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.Date;
import java.util.Set;


/**
 * Editor for <em>party.patientpet</em> parties.
 * Creates an <em>entityRelationship.patientOwner</em> with the current
 * customer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientEditor extends AbstractIMObjectEditor {

    /**
     * Patient/owner relationship short name.
     */
    private static final String PATIENT_OWNER
            = "entityRelationship.patientOwner";


    /**
     * Construct a new <code>PatientEditor</code>.
     *
     * @param patient the object to edit
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>.
     */
    public PatientEditor(Party patient, IMObject parent,
                         LayoutContext context) {
        super(patient, parent, context);
        if (patient.isNew()) {
            Party customer = context.getContext().getCustomer();
            if (customer != null) {
                if (!hasRelationship(PATIENT_OWNER, patient, customer)) {
                    addRelationship(PATIENT_OWNER, patient, customer);
                    // todo - need a way to indicate that the context customer
                    // has changed
                }
            }
        }
    }

    /**
     * Determines if a relationship of the specified type exists.
     * todo - should be moved to EntityBean
     *
     * @param shortName the relationship short name
     * @param patient   the patient
     * @param customer  the customer
     * @return <code>true</code> if a relationship exists; otherwsie
     *         <code>false</code>
     */
    private boolean hasRelationship(String shortName, Entity patient,
                                    Entity customer) {
        boolean result = false;
        Set<EntityRelationship> relationships
                = patient.getEntityRelationships();
        IMObjectReference source = new IMObjectReference(customer);
        IMObjectReference target = new IMObjectReference(patient);

        for (EntityRelationship relationship : relationships) {
            ArchetypeId id = relationship.getArchetypeId();
            if (id.getShortName().equals(shortName)) {
                if (source.equals(relationship.getSource())
                        && target.equals(relationship.getTarget())) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Add a new relationship.
     * todo - should be moved to EntityBean
     *
     * @param shortName the relationship short name
     * @param patient   the patient
     * @param customer  the customer
     */
    private void addRelationship(String shortName, Party patient,
                                 Party customer) {
        EntityRelationship relationship
                = (EntityRelationship) IMObjectCreator.create(shortName);
        if (relationship != null) {
            relationship.setActiveStartTime(new Date());
            relationship.setSequence(1);
            relationship.setSource(new IMObjectReference(customer));
            relationship.setTarget(new IMObjectReference(patient));

            patient.addEntityRelationship(relationship);
        } else {
            String msg = Messages.get("imobject.create.failed", shortName);
            ErrorHelper.show(msg);
        }
    }
}