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

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;

import java.util.ArrayList;
import java.util.List;

/**
 * An editor for {@link EntityRelationship}s.
 *
 * @author Tim Anderson
 */
public class EntityRelationshipEditor extends AbstractRelationshipEditor {

    /**
     * Constructs an {@link EntityRelationshipEditor}.
     *
     * @param relationship the relationship
     * @param parent       the parent object
     * @param context      the layout context
     */
    public EntityRelationshipEditor(EntityRelationship relationship, IMObject parent, LayoutContext context) {
        super(relationship, parent, context);
    }

    /**
     * Determines if the editor is empty.
     *
     * @return {@code true} if there are only two editable nodes (source and target), and the non-primary node is empty
     */
    public boolean isEmpty() {
        boolean result = false;
        List<Property> editable = new ArrayList<Property>(getProperties().getEditable());
        editable.remove(getSource());
        editable.remove(getTarget());
        if (editable.isEmpty()) {
            if (getSourceEditor() != null) {
                result = getSource().getValue() == null;
            } else if (getTargetEditor() != null) {
                result = getTarget().getValue() == null;
            }
        }
        return result;
    }
}
