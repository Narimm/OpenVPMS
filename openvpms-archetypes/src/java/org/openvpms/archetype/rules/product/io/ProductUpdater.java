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
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.openvpms.archetype.rules.product.ProductArchetypes.FIXED_PRICE;
import static org.openvpms.archetype.rules.product.ProductArchetypes.UNIT_PRICE;
import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.CannotUpdateLinkedPrice;
import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.NoFromDate;
import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.PriceNotFound;
import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.UnitPriceOverlap;

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
     * Constructs an {@link ProductUpdater}.
     *
     * @param service the archetype service
     * @param rules   the price rules
     */
    public ProductUpdater(IArchetypeService service, ProductPriceRules rules) {
        this.rules = rules;
        this.service = service;
    }

    /**
     * Updates a product.
     *
     * @param product  the product to update
     * @param data     the product data to update the product with
     * @param practice the practice, used to determine tax rates
     */
    public void update(Product product, ProductData data, Party practice) {
        List<ProductPrice> unitPrices = getPrices(product, data, false);
        checkPrices(product, data.getUnitPrices(), unitPrices);

        List<ProductPrice> fixedPrices = getPrices(product, data, true);
        checkPrices(product, data.getFixedPrices(), fixedPrices);

        addPrices(product, practice, data.getUnitPrices(), unitPrices);
        addPrices(product, practice, data.getFixedPrices(), fixedPrices);

        IMObjectBean bean = new IMObjectBean(product, service);
        if (bean.hasNode("printedName")) {
            bean.setValue("printedName", data.getPrintedName());
        }
    }

    private List<ProductPrice> getPrices(Product product, ProductData data, boolean fixed) {
        List<ProductPrice> result;
        List<PriceData> prices = (fixed) ? data.getFixedPrices() : data.getUnitPrices();
        if (prices.isEmpty()) {
            result = Collections.emptyList();
        } else {
            result = rules.getProductPrices(product, fixed ? FIXED_PRICE : UNIT_PRICE, null, null);
        }
        return result;
    }

    private void addPrices(Product product, Party practice, List<PriceData> prices, List<ProductPrice> existing) {
        Set<PriceData> duplicateFree = new LinkedHashSet<PriceData>(prices);
        List<ProductPrice> newPrices = new ArrayList<ProductPrice>();
        for (PriceData price : duplicateFree) {
            if (price.getId() != -1) {
                updateExistingPrice(product, price, practice, existing);
            } else {
                ProductPrice newPrice = getPrice(product, price, practice);
                if (newPrice != null) {
                    newPrices.add(newPrice);
                }
            }
        }
        for (ProductPrice price : newPrices) {
            product.addProductPrice(price);
        }
    }

    /**
     * Updates an existing price, or creates a new price if required.
     *
     * @param product  the product
     * @param price    the price to add
     * @param practice the practice, used to determine tax rates
     * @return the new price, or {@code null} if a new price wasn't created
     */
    private ProductPrice getPrice(Product product, PriceData price, Party practice) {
        ProductPrice result = null;
        boolean create = false;
        ProductPrice existing = getIntersectMatch(price, product);
        if (existing == null) {
            create = true;
        } else {
            boolean dateMatch = dateEquals(price, existing);
            boolean priceMatch = priceEquals(price, existing);
            if (!dateMatch || !priceMatch) {
                // the price is different to the existing price, so work out if the current price needs to be
                // updated, or a new one created
                if (isLinkedPrice(existing, product)) {
                    // the price is linked from a price template. These cannot be updated
                    create = true;
                    existing = null;
                } else if (dateMatch) {
                    updatePrice(existing, price, product, practice);
                } else {
                    // price overlaps an existing price.
                    if (existing.getToDate() == null) {
                        // the current price is unbounded, so close it off and create a new one
                        create = true;
                    } else if (UNIT_PRICE.equals(price.getShortName())) {
                        // can't have unit prices overlapping
                        throw new ProductIOException(UnitPriceOverlap, product.getName(), product.getId());
                    }
                }
            }
        }
        if (create) {
            Date from = price.getFrom();
            if (existing != null && existing.getToDate() == null) {
                existing.setToDate(DateRules.getDate(from, -1, DateUnits.DAYS));
            }
            ProductPrice newPrice = (ProductPrice) service.create(price.getShortName());
            updatePrice(newPrice, price, product, practice);
            result = newPrice;
        }
        return result;
    }

    private void updateExistingPrice(Product product, PriceData price, Party practice, List<ProductPrice> prices) {
        ProductPrice existing = getPrice(product, price, prices);
        if (isLinkedPrice(existing, product)) {
            if (!priceEquals(price, existing)) {
                throw new ProductIOException(CannotUpdateLinkedPrice, price.getId(), price.getLine(),
                                             product.getName(), product.getId(),
                                             existing.getProduct().getName(), existing.getProduct().getId());
            }
        } else if (!priceEquals(price, existing)) {
            if (UNIT_PRICE.equals(price.getShortName())) {
                checkOverlap(product, price, prices);
            }
            updatePrice(existing, price, product, practice);
        }
    }

    private void checkOverlap(Product product, PriceData price, List<ProductPrice> prices) {
        for (ProductPrice p : prices) {
            if (p.getId() != price.getId()
                && DateRules.intersects(p.getFromDate(), p.getToDate(), price.getFrom(), price.getTo())) {
                throw new ProductIOException(UnitPriceOverlap, product.getName(), product.getId());
            }
        }
    }

    /**
     * Returns the price with an identifier matching that in the data.
     *
     * @param product the product
     * @param data    the price data
     * @param prices  the available prices
     * @return the corresponding price
     * @throws ProductIOException if the price is not found
     */
    private ProductPrice getPrice(Product product, PriceData data, List<ProductPrice> prices) {
        long id = data.getId();
        for (ProductPrice price : prices) {
            if (price.getId() == id) {
                return price;
            }
        }
        throw new ProductIOException(PriceNotFound, id, data.getLine(), product.getName(), product.getId());
    }

    /**
     * Determines if a price is linked from a price template.
     *
     * @param price   the price
     * @param product the product
     * @return {@code true}  if the price is linked from a price template
     */
    private boolean isLinkedPrice(ProductPrice price, Product product) {
        return !ObjectUtils.equals(price.getProduct(), product);
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
    }

    /**
     * Determines if two prices are the same.
     */
    private boolean equals(PriceData data, ProductPrice price) {
        return priceEquals(data, price) && dateEquals(data, price);
    }

    private boolean dateEquals(PriceData data, ProductPrice price) {
        return DateRules.dateEquals(data.getFrom(), price.getFromDate())
               && DateRules.dateEquals(data.getTo(), price.getToDate());
    }

    /**
     * Determines if the price and cost of two prices are the same.
     *
     * @param data  the price data
     * @param price the price to compare with
     * @return {@code true} if the price and cost are the same in both
     */
    private boolean priceEquals(PriceData data, ProductPrice price) {
        IMObjectBean bean = new IMObjectBean(price, service);
        BigDecimal cost = bean.getBigDecimal("cost");
        return price.getPrice().compareTo(data.getPrice()) == 0 && cost.compareTo(data.getCost()) == 0;
    }

    private ProductPrice getIntersectMatch(PriceData price, Product product) {
        List<ProductPrice> productPrices = rules.getProductPrices(product, price.getShortName(), price.getFrom(),
                                                                  price.getTo());
        if (productPrices.isEmpty()) {
            return null;
        } else if (productPrices.size() == 1) {
            return productPrices.iterator().next();
        } else if (UNIT_PRICE.equals(price.getShortName())) {
            throw new ProductIOException(UnitPriceOverlap, product.getName(), product.getId());
        } else {
            // multiple fixed prices. This OK.
            return null;
        }
    }

    private void checkPrices(Product product, List<PriceData> prices, List<ProductPrice> existing) {
        for (PriceData price : prices) {
            if (price.getFrom() == null) {
                throw new ProductIOException(NoFromDate, product.getName(), product.getId(), price.getLine());
            }
            if (price.getId() != -1) {
                boolean found = false;
                for (ProductPrice pp : existing) {
                    if (pp.getId() == price.getId()) {
                        if (pp.getProduct().getId() != product.getId() && !equals(price, pp)) {
                            throw new ProductIOException(CannotUpdateLinkedPrice, price.getId(), price.getLine(),
                                                         product.getName(), product.getId(),
                                                         pp.getProduct().getName(), pp.getProduct().getId());
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new ProductIOException(PriceNotFound, price.getId(), price.getLine(), product.getName(),
                                                 product.getId());
                }
            }
        }
    }

}