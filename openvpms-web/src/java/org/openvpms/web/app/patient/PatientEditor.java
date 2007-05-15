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

import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Editor for <em>party.patientpet</em> parties.
 * Creates an <em>entityRelationship.patientOwner</em> with the current
 * customer, if the parent object isn't an entity relationship.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientEditor extends AbstractIMObjectEditor {

    /**
     * Constructs a new <tt>PatientEditor</tt>.
     *
     * @param patient the object to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>.
     */
    public PatientEditor(Party patient, IMObject parent,
                         LayoutContext context) {
        super(patient, parent, context);
        if (patient.isNew() && !(parent instanceof EntityRelationship)) {
            Party customer = getLayoutContext().getContext().getCustomer();
            if (customer != null) {
                PatientRules rules = new PatientRules();
                if (!rules.isOwner(customer, patient)) {
                    rules.addPatientOwnerRelationship(customer, patient);
                }
            }
        }
    }


}