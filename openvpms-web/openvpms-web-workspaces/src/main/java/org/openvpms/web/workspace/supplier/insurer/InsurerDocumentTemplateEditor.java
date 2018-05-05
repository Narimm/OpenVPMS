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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier.insurer;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.doc.DocumentTemplateQuery;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.relationship.EntityLinkEditor;
import org.openvpms.web.component.property.Property;

/**
 * An editor for <em>entityLink.insurerDocumetnTemplate</em> relationships.
 * <p>
 * This limits document templates to INSURANCE_CLAIM instances.
 *
 * @author Tim Anderson
 */
public class InsurerDocumentTemplateEditor extends EntityLinkEditor {

    /**
     * Constructs an {@link InsurerDocumentTemplateEditor}.
     *
     * @param relationship the link
     * @param parent       the parent object
     * @param context      the layout context
     */
    public InsurerDocumentTemplateEditor(EntityLink relationship, IMObject parent, LayoutContext context) {
        super(relationship, parent, context);
    }

    /**
     * Creates a new editor for the relationship target.
     *
     * @param property the target property
     * @param context  the layout context
     * @return a new reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Entity> createTargetEditor(Property property, LayoutContext context) {
        return new AbstractIMObjectReferenceEditor<Entity>(property, getObject(), context) {
            @Override
            protected Query<Entity> createQuery(String name) {
                DocumentTemplateQuery query = new DocumentTemplateQuery();
                query.setValue(name);
                query.setTypes("INSURANCE_CLAIM");
                return query;
            }
        };
    }
}
