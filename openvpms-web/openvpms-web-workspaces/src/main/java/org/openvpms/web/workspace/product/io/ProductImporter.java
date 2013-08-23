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

package org.openvpms.web.workspace.product.io;

import org.openvpms.archetype.rules.product.io.PriceData;
import org.openvpms.archetype.rules.product.io.ProductData;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.List;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class ProductImporter {

    private final IArchetypeService service;


    public ProductImporter() {
        service = ServiceHelper.getArchetypeService();
    }

    public void run(List<ProductData> list) {
        for (ProductData data : list) {
            Product product = (Product) service.get(data.getReference());
            if (product != null) {
                for (PriceData price : data.getFixedPrices()) {
                    ProductPrice current = getPrice(price);
                    Date from = price.getFrom();
                    if (from == null) {
                        from = new Date();
                    }
                    if (current != null) {
                        if (current.getToDate() == null) {
                            current.setToDate(from);
                        }
                        ProductPrice newPrice = (ProductPrice) service.create(price.getShortName());
                        newPrice.setFromDate(from);
                    }
                }
                for (PriceData price : data.getUnitPrices()) {

                }
            }
        }
    }

    private ProductPrice getPrice(PriceData price) {
        return null;
    }
}
