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

package org.openvpms.archetype.rules.stock;

import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.functor.RefEquals;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.List;


/**
 * Base class for stock test cases.
 *
 * @author Tim Anderson
 */
public abstract class AbstractStockTest extends ArchetypeServiceTest {

    /**
     * Helper to create a stock location.
     *
     * @return a new stock location
     */
    protected Party createStockLocation() {
        return ProductTestHelper.createStockLocation();
    }

    /**
     * Returns the stock for at a location for a product.
     *
     * @param location the location
     * @param product  the product
     * @return the stock count
     */
    protected BigDecimal getStock(Party location, Product product) {
        product = get(product);
        EntityBean prodBean = new EntityBean(product);
        List<IMObjectRelationship> values = prodBean.getValues("stockLocations", RefEquals.getTargetEquals(location),
                                                               IMObjectRelationship.class);
        if (!values.isEmpty()) {
            IMObjectBean relBean = new IMObjectBean(values.get(0));
            return relBean.getBigDecimal("quantity");
        }
        return BigDecimal.ZERO;
    }


}
