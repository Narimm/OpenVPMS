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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SingleIMObjectCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;

/**
 * An editor for collections of {@code LookupRelationship} instances with 0..1 or 1..1 cardinality.
 *
 * @author Tim Anderson
 */
public class SingleLookupRelationshipCollectionEditor extends SingleIMObjectCollectionEditor {

    /**
     * Constructs a {@link SingleLookupRelationshipCollectionEditor}.
     *
     * @param editor  the collection property
     * @param object  the parent object
     * @param context the layout context
     */
    public SingleLookupRelationshipCollectionEditor(CollectionProperty editor, Lookup object, LayoutContext context) {
        super(editor, object, context);
    }

    /**
     * Returns the relationship editor.
     *
     * @return the relationship editor, or {@code null} if there is no current relationship editor
     */
    protected LookupRelationshipEditor getRelationshipEditor() {
        IMObjectEditor editor = getCurrentEditor();
        return (editor instanceof LookupRelationshipEditor) ? (LookupRelationshipEditor) editor : null;
    }

    /**
     * Determines if the object being edited is empty.
     *
     * @return {@code true} if the object is empty
     */
    @Override
    protected boolean isEmpty() {
        LookupRelationshipEditor editor = getRelationshipEditor();
        return editor != null && getCollection().getMinCardinality() == 0 && editor.isEmpty();
    }
}
