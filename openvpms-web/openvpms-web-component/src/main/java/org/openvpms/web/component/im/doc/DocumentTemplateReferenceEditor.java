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

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.property.Property;

/**
 * Editor for <em>entity.documentTemplate</em> references.
 * <p>
 * Allows the template type to be restricted.
 *
 * @author Tim Anderson
 */
public class DocumentTemplateReferenceEditor extends AbstractIMObjectReferenceEditor<Entity> {

    /**
     * The types to filter on.
     */
    private String[] types = {};

    /**
     * Constructs a {@link DocumentTemplateReferenceEditor}.
     *
     * @param property the reference property
     * @param parent   the parent object. May be {@code null}
     * @param context  the layout context
     */
    public DocumentTemplateReferenceEditor(Property property, IMObject parent, LayoutContext context) {
        super(property, parent, context);
    }

    /**
     * Constructs a {@link DocumentTemplateReferenceEditor}.
     *
     * @param property    the reference property
     * @param parent      the parent object. May be {@code null}
     * @param context     the layout context
     * @param allowCreate determines if objects may be created
     */
    public DocumentTemplateReferenceEditor(Property property, IMObject parent, LayoutContext context,
                                           boolean allowCreate) {
        super(property, parent, context, allowCreate);
    }

    /**
     * Sets the document types to filter on.
     *
     * @param types the types to filter on. If empty, queries all types
     */
    public void setTypes(String... types) {
        this.types = types;
    }

    /**
     * Creates a query to select objects.
     *
     * @param name the name to filter on. May be {@code null}
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    @Override
    protected Query<Entity> createQuery(String name) {
        Query<Entity> query = super.createQuery(name);
        if (types.length > 0 && query instanceof DocumentTemplateQuery) {
            ((DocumentTemplateQuery) query).setTypes(types);
        }
        return query;
    }
}
