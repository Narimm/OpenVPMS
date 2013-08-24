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

package org.openvpms.archetype.rules.product.io;

import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

/**
 * Product I/O helper methods.
 *
 * @author Tim Anderson
 */
public class ProductIOTestHelper {

    /**
     * Creates and saves a product with the specified name and printed name.
     *
     * @param name        the product name
     * @param printedName the product printed name. May be {@code null}
     * @return a new product
     */
    public static Product createProduct(String name, String printedName) {
        Product result = TestHelper.createProduct();
        IMObjectBean bean = new IMObjectBean(result);
        bean.setValue("name", name);
        bean.setValue("printedName", printedName);
        bean.save();
        return result;
    }
}
