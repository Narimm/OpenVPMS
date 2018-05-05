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

package org.openvpms.web.workspace.product.io;

import org.junit.Test;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.query.AbstractQueryTest;
import org.openvpms.web.component.im.query.Query;

/**
 * Tests the {@link ProductExportQuery}.
 *
 * @author Tim Anderson
 */
public class ProductExportQueryTestCase extends AbstractQueryTest<Product> {

    /**
     * Tests querying by product type.
     */
    @Test
    public void testQueryByProductType() {
        Entity type1 = ProductTestHelper.createProductType();
        Entity type2 = ProductTestHelper.createProductType();
        Product product1 = ProductTestHelper.createMedication(type1);
        Product product2 = ProductTestHelper.createMedication(type2);
        Product product3 = TestHelper.createProduct();

        ProductExportQuery query = new ProductExportQuery(new LocalContext());
        query.getComponent();
        query.setProductType(type1);
        checkSelects(true, query, product1);
        checkSelects(false, query, product2);
        checkSelects(false, query, product3);
    }

    /**
     * Creates a new query.
     *
     * @return a new query
     */
    @Override
    protected Query<Product> createQuery() {
        return new ProductExportQuery(new LocalContext());
    }

    /**
     * Creates a new object, selected by the query.
     *
     * @param value a value that can be used to uniquely identify the object
     * @param save  if {@code true} save the object, otherwise don't save it
     * @return the new object
     */
    @Override
    protected Product createObject(String value, boolean save) {
        Product product = TestHelper.createProduct(ProductArchetypes.MEDICATION, "CANINE", false);
        product.setName(value);
        if (save) {
            save(product);
        }
        return product;
    }

    /**
     * Generates a unique value which may be used for querying objects on.
     *
     * @return a unique value
     */
    @Override
    protected String getUniqueValue() {
        return getUniqueValue("ZProduct");
    }
}
