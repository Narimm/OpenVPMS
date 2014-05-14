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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.im.edit.AbstractEditableIMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.DelegatingCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;

/**
 * Entity relationship collection editor.
 *
 * @author Tim Anderson
 */
public class EntityRelationshipCollectionEditor extends DelegatingCollectionEditor {

    /**
     * Constructs an {@link EntityRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param object   the parent object
     * @param context  the layout context
     */
    public EntityRelationshipCollectionEditor(CollectionProperty property, Entity object, LayoutContext context) {
        String[] shortNames = property.getArchetypeRange();
        int max = property.getMaxCardinality();
        AbstractEditableIMObjectCollectionEditor editor;
        if (max == 1 && shortNames.length == 1) {
            editor = new SingleEntityRelationshipCollectionEditor(property, object, context);
        } else {
            editor = new MultipleEntityRelationshipCollectionEditor(property, object, context);
        }
        setEditor(editor);
    }
}