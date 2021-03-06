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

package org.openvpms.archetype.rules.product.io;

import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.List;

/**
 * Product importer.
 *
 * @author Tim Anderson
 */
public class ProductImporter {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The price updater.
     */
    private final ProductUpdater updater;

    /**
     * Constructs a {@link ProductImporter}.
     *
     * @param rules   the price rules
     * @param service the archetype service
     */
    public ProductImporter(ProductPriceRules rules, IArchetypeService service) {
        this.service = service;
        this.updater = new ProductUpdater(rules, service);
    }

    /**
     * Runs the import.
     *
     * @param products the products to import
     */
    public void run(List<ProductData> products) {
        for (ProductData data : products) {
            Product product = (Product) service.get(data.getReference());
            if (product != null) {
                updater.update(product, data);
                service.save(product);
            }
        }
    }

}
