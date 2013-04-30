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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.reporting.till.TillQuery;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.property.Property;


/**
 * Editor for till {@link IMObjectReference}s.
 *
 * @author Tim Anderson
 */
class TillReferenceEditor extends AbstractIMObjectReferenceEditor<Party> {

    /**
     * Constructs a {@code TillReferenceEditor}.
     *
     * @param property the till reference property
     * @param parent   the parent object
     * @param context  the layout context
     */
    public TillReferenceEditor(Property property, IMObject parent, LayoutContext context) {
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
        TillQuery query = new TillQuery(getContext().getLocation());
        query.setValue(name);
        return query;
    }

}
