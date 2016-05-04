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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.property.Property;

/**
 * An editor for <em>party.supplier*</em> references.
 *
 * @author Tim Anderson
 */
public class SupplierReferenceEditor extends AbstractIMObjectReferenceEditor<Party> {

    /**
     * Constructs an {@link AbstractIMObjectReferenceEditor}.
     *
     * @param property the reference property
     * @param parent   the parent object. May be {@code null}
     * @param context  the layout context
     */
    public SupplierReferenceEditor(Property property, IMObject parent, LayoutContext context) {
        super(property, parent, context);
    }

    /**
     * Creates a query to select objects.
     *
     * @param name the name to filter on. May be {@code null}
     * @return a new query
     */
    @Override
    protected Query<Party> createQuery(String name) {
        String[] shortNames = getProperty().getArchetypeRange();
        // uses the query handler for party.supplier* by default
        Query<Party> query = QueryFactory.create(shortNames, false, getContext(), Party.class);
        query.setValue(name);
        return query;
    }
}
