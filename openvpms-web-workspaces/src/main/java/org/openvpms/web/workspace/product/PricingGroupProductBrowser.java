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

package org.openvpms.web.workspace.product;

import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.query.ResultSet;

/**
 * Product browser that refreshes the product table when the pricing group changes.
 *
 * @author Tim Anderson
 */
public class PricingGroupProductBrowser extends IMObjectTableBrowser<Product> {

    /**
     * Constructs a {@link PricingGroupProductBrowser} that queries IMObjects using the specified query.
     *
     * @param query   the query
     * @param context the layout context
     */
    public PricingGroupProductBrowser(final PricingGroupProductQuery query, LayoutContext context) {
        super(query, query.getDefaultSortConstraint(), new ProductTableModel(query, context), context);

        query.setPricingGroupListener(new PricingGroupProductQuery.PricingGroupListener() {
            @Override
            public void onPricingGroupChanged() {
                ProductTableModel model = (ProductTableModel) getTableModel();
                model.setPricingGroup(query.getPricingGroup().getGroup());
            }
        });
    }

    /**
     * Performs the query.
     *
     * @return the query result set
     */
    @Override
    protected ResultSet<Product> doQuery() {
        return super.doQuery();
    }
}
