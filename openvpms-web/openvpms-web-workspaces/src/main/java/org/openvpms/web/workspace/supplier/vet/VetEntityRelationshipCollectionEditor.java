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

package org.openvpms.web.workspace.supplier.vet;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityRelationshipCollectionPropertyEditor;
import org.openvpms.web.component.im.relationship.RelationshipCollectionEditor;
import org.openvpms.web.component.im.relationship.RelationshipCollectionPropertyEditor;
import org.openvpms.web.component.im.relationship.RelationshipState;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.property.CollectionProperty;

/**
 * Editor for <em>entityRelationship.referredFrom</em> and <em>entityRelationship.referredTo</em> relationships.
 *
 * @author Tim Anderson
 */
public class VetEntityRelationshipCollectionEditor extends RelationshipCollectionEditor {

    /**
     * Constructs a {@link VetEntityRelationshipCollectionEditor}.
     *
     * @param property the collection property editor
     * @param object   the object being edited
     * @param context  the layout context
     */
    public VetEntityRelationshipCollectionEditor(CollectionProperty property, Entity object, LayoutContext context) {
        super(new EntityRelationshipCollectionPropertyEditor(property, object), object, context);
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMTableModel<RelationshipState> createTableModel(LayoutContext context) {
        RelationshipCollectionPropertyEditor editor = getCollectionPropertyEditor();
        return new VetRelationshipStateTableModel(context, editor.parentIsSource());
    }

}
