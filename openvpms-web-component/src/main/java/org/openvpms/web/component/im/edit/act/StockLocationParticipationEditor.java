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

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.property.Property;

import static org.openvpms.archetype.rules.stock.StockArchetypes.STOCK_XFER_LOCATION_PARTICIPATION;


/**
 * Participation editor for stock locations.
 *
 * @author Tim Anderson
 */
public class StockLocationParticipationEditor extends ParticipationEditor<Party> {

    /**
     * Constructs a {@code StockLocationParticipationEditor}.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context
     */
    public StockLocationParticipationEditor(Participation participation, Act parent, LayoutContext context) {
        super(participation, parent, context);
        if (participation.getEntity() == null && parent.isNew()
            && !TypeHelper.isA(participation, STOCK_XFER_LOCATION_PARTICIPATION)) {
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
     * Editor for stock location {@link IMObjectReference}s.
     */
    private class LocationReferenceEditor
            extends AbstractIMObjectReferenceEditor<Party> {

        public LocationReferenceEditor(Property property, LayoutContext context) {
            super(property, getParent(), context);
        }

        /**
         * Creates a query to select stock locations.
         * <p/>
         * If the participation is not an
         * <em>participation.stockTransferLocation</em>,
         * constrains the stock location to those associated with the current
         * practice location, if any.
         *
         * @param name a name to filter on. May be {@code null}
         * @return a new query
         * @throws ArchetypeQueryException if the short names don't match any
         *                                 archetypes
         */
        @Override
        protected Query<Party> createQuery(String name) {
            Query<Party> query = super.createQuery(name);
            if (!TypeHelper.isA(getObject(),
                                STOCK_XFER_LOCATION_PARTICIPATION)) {
                Context context = getLayoutContext().getContext();
                Party location = context.getLocation();
                if (location != null) {
                    CollectionNodeConstraint node
                            = new CollectionNodeConstraint("locations");
                    node.add(new NodeConstraint("source",
                                                location.getObjectReference()));
                    query.setConstraints(node);
                }
            }
            return query;
        }
    }

}
