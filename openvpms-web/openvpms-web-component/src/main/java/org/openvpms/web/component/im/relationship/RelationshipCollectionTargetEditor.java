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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.web.component.im.edit.AbstractEditableIMObjectCollectionEditor;
import org.openvpms.web.component.im.edit.DelegatingCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.List;


/**
 * A editor for collections of {@link IMObjectRelationship}s, where the
 * target of the relationship is the object being edited.
 *
 * @author Tim Anderson
 */
public class RelationshipCollectionTargetEditor extends DelegatingCollectionEditor {

    /**
     * The collection property editor.
     */
    private final RelationshipCollectionTargetPropertyEditor propertyEditor;


    /**
     * Creates a new <tt>RelationshipCollectionTargetEditor</tt>.
     *
     * @param propertyEditor the collection property editor
     * @param object         the parent object
     * @param context        the layout context
     */
    public RelationshipCollectionTargetEditor(
            RelationshipCollectionTargetPropertyEditor propertyEditor,
            IMObject object, LayoutContext context) {
        CollectionProperty property = propertyEditor.getProperty();
        String[] shortNames = property.getArchetypeRange();
        int max = property.getMaxCardinality();
        AbstractEditableIMObjectCollectionEditor editor;
        if (max == 1 && shortNames.length == 1) {
            editor = new SingleRelationshipCollectionTargetEditor(propertyEditor, object, context);
        } else {
            editor = new MultipleRelationshipCollectionTargetEditor(propertyEditor, object, context);
        }
        setEditor(editor);
        this.propertyEditor = propertyEditor;
    }

    /**
     * Returns the target objects in the collection.
     *
     * @return the target objects in the collection
     */
    public List<IMObject> getObjects() {
        return propertyEditor.getObjects();
    }

}
