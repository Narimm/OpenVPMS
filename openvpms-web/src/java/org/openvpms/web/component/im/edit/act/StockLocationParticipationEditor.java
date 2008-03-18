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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
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


/**
 * Participation editor for stock locations.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class StockLocationParticipationEditor
        extends AbstractParticipationEditor<Product> {

    /**
     * Constructs a new <tt>StockLocationParticipationEditor</tt>.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context. May be <tt>null</tt>
     */
    public StockLocationParticipationEditor(Participation participation,
                                            Act parent, LayoutContext context) {
        super(participation, parent, context);
        if (!TypeHelper.isA(participation, "participation.stockLocation")) {
            throw new IllegalArgumentException(
                    "Invalid participation type:"
                            + participation.getArchetypeId().getShortName());
        }
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Product> createObjectReferenceEditor(
            Property property) {
        return new LocationReferenceEditor(property);
    }

    /**
     * Editor for stock location {@link IMObjectReference}s.
     */
    private class LocationReferenceEditor
            extends AbstractIMObjectReferenceEditor<Product> {

        public LocationReferenceEditor(Property property) {
            super(property, getParent(), getLayoutContext());
        }

        /**
         * Creates a query to select objects.
         *
         * @param name a name to filter on. May be <tt>null</tt>
         * @param name the name to filter on. May be <tt>null</tt>
         * @return a new query
         * @throws ArchetypeQueryException if the short names don't match any
         *                                 archetypes
         */
        @Override
        protected Query<Product> createQuery(String name) {
            Query<Product> query = super.createQuery(name);
            Context context = getLayoutContext().getContext();
            Party location = context.getLocation();
            if (location != null) {
                CollectionNodeConstraint node
                        = new CollectionNodeConstraint("locations");
                node.add(new NodeConstraint("source",
                                            location.getObjectReference()));
                query.setConstraints(node);
            }
            return query;
        }
    }

}
