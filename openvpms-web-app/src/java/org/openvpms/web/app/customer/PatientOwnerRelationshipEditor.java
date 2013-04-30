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

package org.openvpms.web.app.customer;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.PatientReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityRelationshipEditor;
import org.openvpms.web.component.property.Property;


/**
 * Editor for <em>entityRelationship.patientOwner</em> relationships
 * that enables all patients to be selected.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientOwnerRelationshipEditor extends EntityRelationshipEditor {

    /**
     * Construct a new <code>PatientOwnerRelationshipEditor</code>.
     *
     * @param relationship the relationship
     * @param parent       the parent object
     * @param context      the layout context
     */
    public PatientOwnerRelationshipEditor(EntityRelationship relationship,
                                          IMObject parent,
                                          LayoutContext context) {
        super(relationship, parent, context);
    }

    /**
     * Creates a new reference editor.
     *
     * @param property the reference property
     * @param context  the layout context
     * @return a new reference editor
     */
    @Override
    @SuppressWarnings("unchecked")
    protected IMObjectReferenceEditor<Entity> createReferenceEditor(
        Property property, LayoutContext context) {
        IMObjectReferenceEditor editor
            = super.createReferenceEditor(property, context);
        if (editor instanceof PatientReferenceEditor) {
            ((PatientReferenceEditor) editor).setAllPatients(true);
        }
        return editor;
    }

}
