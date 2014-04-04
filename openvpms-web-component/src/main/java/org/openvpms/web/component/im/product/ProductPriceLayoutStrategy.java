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

package org.openvpms.web.component.im.product;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * Layout strategy for {@link ProductPrice} instances.
 * <p/>
 * This suppresses the pricingLocations node if it is not required.
 *
 * @author Tim Anderson
 */
public class ProductPriceLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
     *
     * @param object  the object to display
     * @param context the layout context
     * @return the archetype nodes
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes(IMObject object, LayoutContext context) {
        ArchetypeNodes nodes;
        if (PricingLocationHelper.hasPricingLocations((ProductPrice) object)) {
            nodes = super.getArchetypeNodes(object, context);
        } else if (!context.isEdit() || !PricingLocationHelper.pricingLocationsConfigured()) {
            nodes = new ArchetypeNodes().exclude("pricingLocations");
        } else {
            nodes = super.getArchetypeNodes(object, context);
        }
        return nodes;
    }
}
