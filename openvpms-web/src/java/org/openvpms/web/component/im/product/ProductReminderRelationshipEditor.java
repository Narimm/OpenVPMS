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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.im.product;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityRelationshipEditor;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;


/**
 * Editor for <em>entityRelationship.productReminder</em> relationships.
 * <p/>
 * Defaults the interactive node value to that of the associated <em>entity.reminderType</em>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
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

    private void onReminderTypeChanged() {
        IMObjectReference reference = (IMObjectReference) getTarget().getValue();
        Entity reminderType = (Entity) getObject(reference);
        if (reminderType != null) {
            IMObjectBean bean = new IMObjectBean(reminderType);
            getProperty("interactive").setValue(bean.getBoolean("interactive"));
        }
    }


}
