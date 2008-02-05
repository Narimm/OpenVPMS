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
 *  $Id: EntityRelationshipEditor.java 894 2006-05-17 01:10:40Z tanderson $
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;


/**
 * An editor for {@link EntityRelationship}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-17 01:10:40Z $
 */
public class EntityRelationshipEditor extends AbstractRelationshipEditor {

    /**
     * Construct a new <tt>EntityRelationshipEditor</tt>.
     *
     * @param relationship the relationship
     * @param parent       the parent object
     * @param context      the layout context
     */
    public EntityRelationshipEditor(EntityRelationship relationship,
                                    IMObject parent, LayoutContext context) {
        super(relationship, parent, context);
        Property endTime = getProperty("activeEndTime");
        if (endTime != null) {
            endTime.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    onEndTimeModified();
                }
            });
        }
    }

    /**
     * Updates the state of the active flag based on the <em>activeEndTime</em>
     * property. If the <em>activeEndTime</em> is non-null, sets the active
     * flag <tt>false</tt>, otherwise sets it <tt>true</tt>.
     */
    private void onEndTimeModified() {
        EntityRelationship relationship = (EntityRelationship) getObject();
        if (relationship.getActiveEndTime() == null) {
            relationship.setActive(true);
        } else {
            relationship.setActive(false);
        }
    }

}
