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

package org.openvpms.web.component.im.product;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.DelegatingRelationshipStateTableModel;
import org.openvpms.web.component.im.relationship.RelationshipCollectionViewer;
import org.openvpms.web.component.im.relationship.RelationshipState;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Viewer for collections of <em>entityRelationship.productStockLocation</em>.
 * <p/>
 * Hides any inactive relationships if the 'hide inactive' checkbox is
 * selected.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2008-03-19 07:02:38Z $
 */
public class ProductStockLocationCollectionViewer
        extends RelationshipCollectionViewer {

    /**
     * Constructs a new <tt>ProductStockLocationCollectionViewer</tt>.
     *
     * @param property the collection to view
     * @param parent   the parent object
     * @param context  the layout context. May be <tt>null</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public ProductStockLocationCollectionViewer(CollectionProperty property,
                                                Entity parent,
                                                LayoutContext context) {
        super(property, parent, context);
    }

    /**
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMTableModel<RelationshipState> createTableModel(
            LayoutContext context) {
        String[] shortNames = getProperty().getArchetypeRange();
        IMTableModel<IMObjectRelationship> model
                = new ProductStockLocationTableModel(shortNames, context,
                                                     parentIsSource());
        return new DelegatingRelationshipStateTableModel(model, context);
    }
}
