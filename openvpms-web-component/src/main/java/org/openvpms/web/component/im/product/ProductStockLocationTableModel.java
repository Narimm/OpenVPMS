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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.RelationshipDescriptorTableModel;


/**
 * Table model for <em>entityRelationship.productStockLocation</em>
 * relationships.
 *
 * @author Tim Anderson
 */
public class ProductStockLocationTableModel extends RelationshipDescriptorTableModel<IMObjectRelationship> {

    /**
     * Constructs a {@link ProductStockLocationTableModel}.
     * <p/>
     * Enables selection if the context is in edit mode, or {@code null}
     *
     * @param shortNames    the archetype short names
     * @param context       the layout context
     * @param displayTarget if {@code true} display the target node, otherwise display the source node
     */
    public ProductStockLocationTableModel(String[] shortNames, LayoutContext context, boolean displayTarget) {
        super(shortNames, context, displayTarget);
    }
}
