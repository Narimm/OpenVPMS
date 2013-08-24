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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Date;
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
     * The price rules.
     */
    private final ProductPriceRules rules;

    /**
     * Constructs a {@link ProductImporter}.
     *
     * @param service the archeype service
     * @param rules   the price rules
     */
    public ProductImporter(IArchetypeService service, ProductPriceRules rules) {
        this.service = service;
        this.rules = rules;
    }

    /**
     * Runs the import.
     *
     * @param products the products to import
     * @param practice the practice, used to determine tax rates
     */
    public void run(List<ProductData> products, Party practice) {
        for (ProductData data : products) {
            Product product = (Product) service.get(data.getReference());
            if (product != null) {
                for (PriceData price : data.getFixedPrices()) {
                    addPrice(product, price, practice);
                }
                for (PriceData price : data.getUnitPrices()) {
                    addPrice(product, price, practice);
                }
                service.save(product);
            }
        }
    }

    /**
     * Adds a price to a product.
     *
     * @param product  the product
     * @param price    the price to add
     * @param practice the practice, used to determine tax rates
     */
    private void addPrice(Product product, PriceData price, Party practice) {
        boolean create = false;
        ProductPrice current = getDateMatch(price, product);
        if (current != null) {
            // a price exists with the same start and end dates. Update it if required
            if (!equals(price, current)) {
                if (current.getToDate() == null) {
                    // the current price is unbounded, so close it off and create a new one
                    create = true;
                } else {
                    updatePrice(current, price, product, practice);
                }
            }
        } else if (price.getFrom() == null) {
            // if there is a current price, it will be closed off if required
            current = getLatest(price, product);
            create = true;
        } else {
            current = getIntersectMatch(price, product);
            // create a new price
            create = true;
        }
        if (create) {
            Date from = price.getFrom();
            if (current != null) {
                if (from == null) {
                    if (current.getToDate() == null) {
                        from = DateRules.getToday();
                        current.setToDate(DateRules.getDate(from, -1, DateUnits.DAYS));
                    } else {
                        from = DateRules.getDate(current.getToDate(), 1, DateUnits.DAYS);
                    }
                } else {
                    if (current.getToDate() == null || DateRules.compareDates(current.getToDate(), from) >= 0) {
                        current.setToDate(DateRules.getDate(from, -1, DateUnits.DAYS));
                    }
                }
            }
            ProductPrice newPrice = (ProductPrice) service.create(price.getShortName());
            newPrice.setFromDate(from);
            newPrice.setToDate(price.getTo());
            updatePrice(newPrice, price, product, practice);
            product.addProductPrice(newPrice);
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
        bean.setValue("cost", data.getCost());
        bean.setValue("markup", markup);
    }

    /**
     * Determines if the price and cost of two prices are the same.
     *
     * @param data  the price data
     * @param price the price to compare with
     * @return {@code true} if the price and cost are the same in both
     */
    private boolean equals(PriceData data, ProductPrice price) {
        IMObjectBean bean = new IMObjectBean(price, service);
        BigDecimal cost = bean.getBigDecimal("cost");
        return price.getPrice().compareTo(data.getPrice()) == 0 && cost.compareTo(data.getCost()) == 0;
    }

    /**
     * Tries to find a price that has the same dates as the supplied price data.
     *
     * @param data    the price data to match
     * @param product the product
     * @return the corresponding price, or {@code null} if none is found
     */
    private ProductPrice getDateMatch(PriceData data, Product product) {
        List<ProductPrice> prices = rules.getProductPrices(product, data.getShortName(), data.getFrom(), data.getTo());
        Date from = DateRules.getDate(data.getFrom());
        Date to = DateRules.getDate(data.getTo());
        for (ProductPrice price : prices) {
            Date priceFrom = DateRules.getDate(price.getFromDate());
            Date priceTo = DateRules.getDate(price.getToDate());
            if (ObjectUtils.equals(from, priceFrom) && ObjectUtils.equals(to, priceTo)) {
                return price;
            }
        }
        return null;
    }

    /**
     * Returns the latest price.
     *
     * @param data    the price data to match
     * @param product the product
     * @return the corresponding price, or {@code null} if none is found
     */
    private ProductPrice getLatest(PriceData data, Product product) {
        List<ProductPrice> prices = rules.getProductPrices(product, data.getShortName());
        return !prices.isEmpty() ? prices.get(0) : null;
    }

    private ProductPrice getIntersectMatch(PriceData price, Product product) {
        List<ProductPrice> productPrices = rules.getProductPrices(product, price.getShortName(), price.getFrom(),
                                                                  price.getTo());
        if (productPrices.isEmpty()) {
            return null;
        } else if (productPrices.size() == 1) {
            return productPrices.iterator().next();
        } else {
            if (ProductArchetypes.FIXED_PRICE.equals(price.getShortName())) {
                throw new ProductIOException(ProductIOException.ErrorCode.FixedPriceOverlap, product, product.getId());
            } else {
                throw new ProductIOException(ProductIOException.ErrorCode.UnitPriceOverlap, product, product.getId());
            }
        }
    }
}
