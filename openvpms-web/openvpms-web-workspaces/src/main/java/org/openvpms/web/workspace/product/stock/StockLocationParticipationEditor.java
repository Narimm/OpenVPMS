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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product.stock;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.property.Property;


/**
 * Participation editor for stock locations.
 *
 * @author Tim Anderson
 */
public class StockLocationParticipationEditor extends ParticipationEditor<Party> {

    /**
     * Constructs a {@link StockLocationParticipationEditor}.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context
     */
    public StockLocationParticipationEditor(Participation participation, Act parent, LayoutContext context) {
        super(participation, parent, context);
        if (participation.isNew()) {
            Party location = getLayoutContext().getContext().getStockLocation();
            setEntity(location);
        }
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Party> createEntityEditor(Property property) {
        return new LocationReferenceEditor(property, getLayoutContext());
    }

    /**
     * Editor for the stock location {@link Reference}s.
     * <p/>
     * This restricts the stock location to those locations accessible to the user.
     */
    private class LocationReferenceEditor extends AbstractIMObjectReferenceEditor<Party> {

        LocationReferenceEditor(Property property, LayoutContext context) {
            super(property, getParent(), context);
        }

        /**
         * Creates a query to select stock locations.
         *
         * @param name a name to filter on. May be {@code null}
         * @return a new query
         * @throws ArchetypeQueryException if the short names don't match any archetypes
         */
        @Override
        protected Query<Party> createQuery(String name) {
            Query<Party> query = new UserStockLocationQuery(getContext());
            query.setValue(name);
            return query;
        }
    }

}
