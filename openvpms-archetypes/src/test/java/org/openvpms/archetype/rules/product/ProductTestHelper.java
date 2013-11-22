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

package org.openvpms.archetype.rules.product;

import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

/**
 * Product test helper methods.
 *
 * @author Tim Anderson
 */
public class ProductTestHelper {

    /**
     * Creates a stock location.
     *
     * @return a new stock location
     */
    public static Party createStockLocation() {
        Party result = (Party) TestHelper.create(StockArchetypes.STOCK_LOCATION);
        result.setName("STOCK-LOCATION-" + result.hashCode());
        TestHelper.save(result);
        return result;
    }

    /**
     * Adds a demographic update to a product, and saves it.
     *
     * @param product    the product
     * @param node       the node name. May be {@code null}
     * @param expression the expression
     */
    public static void addDemographicUpdate(Product product, String node, String expression) {
        Lookup lookup = (Lookup) TestHelper.create("lookup.demographicUpdate");
        lookup.setCode("XDEMOGRAPHICUPDATE_" + System.currentTimeMillis());
        IMObjectBean bean = new IMObjectBean(lookup);
        bean.setValue("nodeName", node);
        bean.setValue("expression", expression);
        bean.save();
        product.addClassification(lookup);
        TestHelper.save(product);
    }
}
