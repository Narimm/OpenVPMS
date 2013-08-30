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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityRelationshipEditor;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.system.ServiceHelper;


/**
 * Editor for <em>entityRelationship.productReminder</em> relationships.
 * <p/>
 * Defaults the interactive node value to that of the associated <em>entity.reminderType</em>.
 *
 * @author Tim Anderson
 */
public class ProductReminderRelationshipEditor extends EntityRelationshipEditor {

    /**
     * Constructs a <tt>ProductReminderRelationshipEditor</tt>.
     *
     * @param relationship the relationship
     * @param parent       the parent object
     * @param context      the layout context
     */
    public ProductReminderRelationshipEditor(EntityRelationship relationship, IMObject parent, LayoutContext context) {
        super(relationship, parent, context);

        getTarget().addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onReminderTypeChanged();
            }
        });
    }

    /**
     * Invoked when the reminder type changes.
     * <p/>
     * Updates the <em>period</em>, <em>periodUom</em> and <em>interactive</em> nodes from the reminder type.
     */
    private void onReminderTypeChanged() {
        IMObjectReference reference = (IMObjectReference) getTarget().getValue();
        Entity reminderType = (Entity) getObject(reference);
        if (reminderType != null) {
            ReminderType type = new ReminderType(reminderType, ServiceHelper.getArchetypeService());
            getProperty("period").setValue(type.getDefaultInterval());
            getProperty("periodUom").setValue(type.getDefaultUnits().toString());
            getProperty("interactive").setValue(type.isInteractive());
        }
    }

}
