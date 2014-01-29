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

package org.openvpms.archetype.rules.product.io;

import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.List;

/**
 * Updates a product with imported prices.
 *
 * @author Tim Anderson
 */
class ProductUpdater {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The price rules.
     */
    private final ProductPriceRules rules;

    /**
     * The product data comparer, used to determine changes between the existing product and the imported product
     * data.
     */
    private final ProductDataComparator comparer;

    /**
     * Constructs an {@link ProductUpdater}.
     *
     * @param rules   the price rules
     * @param service the archetype service
     */
    public ProductUpdater(ProductPriceRules rules, IArchetypeService service) {
        this.rules = rules;
        this.service = service;
        comparer = new ProductDataComparator(rules, service);
    }

    /**
     * Updates a product.
     *
     * @param product  the product to update
     * @param data     the product data to update the product with
     * @param practice the practice, used to determine tax rates
     */
    public void update(Product product, ProductData data, Party practice) {
        ProductData changes = comparer.compare(product, data);
        if (changes != null) {
            List<ProductPrice> unitPrices = comparer.getUnitPrices(product, changes);
            List<ProductPrice> fixedPrices = comparer.getFixedPrices(product, changes);

            updateProduct(product, changes.getUnitPrices(), unitPrices, practice);
            updateProduct(product, changes.getFixedPrices(), fixedPrices, practice);

            IMObjectBean bean = new IMObjectBean(product, service);
            if (bean.hasNode("printedName")) {
                bean.setValue("printedName", data.getPrintedName());
            }
        }
    }

    /**
     * Updates a product's prices.
     *
     * @param product  the product to update
     * @param prices   the prices to update with
     * @param existing the prices to update
     * @param practice the practice, used to determine tax rates
     */
    private void updateProduct(Product product, List<PriceData> prices, List<ProductPrice> existing, Party practice) {
        for (PriceData price : prices) {
            if (price.getId() != -1) {
                // existing price
                ProductPrice match = ProductImportHelper.getPrice(price, existing);
                updatePrice(match, price, product, practice);
            } else {
                ProductPrice newPrice = (ProductPrice) service.create(price.getShortName());
                updatePrice(newPrice, price, product, practice);
                product.addProductPrice(newPrice);
            }
        }
    }

    /**
     * Updates a price.
     *
     * @param price    the price to update
     * @param data     the data to update the price with
     * @param product  the parent product
     * @param practice the practice, used to determine tax rates
     */
    private void updatePrice(ProductPrice price, PriceData data, Product product, Party practice) {
        BigDecimal markup = rules.getMarkup(product, data.getCost(), data.getPrice(), practice);
        IMObjectBean bean = new IMObjectBean(price, service);
        price.setPrice(data.getPrice());
        price.setFromDate(data.getFrom());
        price.setToDate(data.getTo());
        bean.setValue("cost", data.getCost());
        bean.setValue("markup", markup);
        if (bean.hasNode("default")) {
            bean.setValue("default", data.isDefault());
        }
    }

}
