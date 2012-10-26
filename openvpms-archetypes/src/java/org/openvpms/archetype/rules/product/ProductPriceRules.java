/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.product;

import org.apache.commons.collections.Predicate;
import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Product price rules.
 *
 * @author Tim Anderson
 */
public class ProductPriceRules {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;


    /**
     * Creates a new <tt>ProductPriceRules</tt>.
     */
    public ProductPriceRules() {
        this(ArchetypeServiceHelper.getArchetypeService(),
             LookupServiceHelper.getLookupService());
    }

    /**
     * Constructs a <tt>ProductPriceRules</tt>.
     *
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public ProductPriceRules(IArchetypeService service, ILookupService lookups) {
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Returns the first price with the specified short name, active at the
     * specified date.
     *
     * @param product   the product
     * @param shortName the price short name
     * @param date      the date
     * @return the first matching price, or <tt>null</tt> if none is found
     */
    public ProductPrice getProductPrice(Product product, String shortName,
                                        Date date) {
        Predicate predicate = new ShortNameDatePredicate(shortName, date);
        return getProductPrice(shortName, product, predicate, date);
    }

    /**
     * Returns the first product price with the specified price, and short name,
     * active at the specified date.
     *
     * @param product   the product
     * @param price     the price
     * @param shortName the price short name
     * @param date      the date
     * @return the first matching price, or <tt>null</tt> if none is found
     */
    public ProductPrice getProductPrice(Product product, BigDecimal price,
                                        String shortName, Date date) {
        Predicate predicate = new PricePredicate(price, shortName, date);
        return getProductPrice(shortName, product, predicate, date);
    }

    /**
     * Returns all prices matching the specified short name, active at the * specified date.
     *
     * @param product   the product
     * @param shortName the price short name
     * @param date      the date
     * @return the first matching price, or <tt>null</tt> if none is found
     */
    public Set<ProductPrice> getProductPrices(Product product, String shortName, Date date) {
        Set<ProductPrice> result = new HashSet<ProductPrice>();
        ShortNameDatePredicate predicate = new ShortNameDatePredicate(shortName, date);
        List<ProductPrice> prices = findPrices(product, predicate);
        result.addAll(prices);
        if (ProductArchetypes.FIXED_PRICE.equals(shortName)) {
            // see if there is a fixed price in linked products
            EntityBean bean = new EntityBean(product, service);
            if (bean.hasNode("linked")) {
                for (Entity linked : bean.getNodeTargetEntities("linked", date)) {
                    prices = findPrices((Product) linked, predicate);
                    result.addAll(prices);
                }
            }
        }
        return result;
    }

    /**
     * Calculates a product price using the following formula:
     * <p/>
     * <tt>price = (cost * (1 + markup/100) ) * (1 + tax/100)</tt>
     *
     * @param product  the product
     * @param cost     the product cost
     * @param markup   the markup percentage
     * @param practice the <em>party.organisationPractice</em> used to determine product taxes
     * @param currency the currency, for rounding conventions
     * @return the price
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getPrice(Product product, BigDecimal cost,
                               BigDecimal markup, Party practice,
                               Currency currency) {
        BigDecimal price = BigDecimal.ZERO;
        if (cost.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal markupDec = getRate(markup);
            BigDecimal taxRate = getTaxRate(product, practice);
            price = cost.multiply(
                    BigDecimal.ONE.add(markupDec)).multiply(
                    BigDecimal.ONE.add(taxRate));
            price = currency.round(price);
        }
        return price;
    }

    /**
     * Calculates a product markup using the following formula:
     * <p/>
     * <tt>markup = ((price / (cost * ( 1 + tax/100))) - 1) * 100</tt>
     *
     * @param product  the product
     * @param cost     the product cost
     * @param price    the price
     * @param practice the <em>party.organisationPractice</em> used to determine
     *                 product taxes
     * @return the markup
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getMarkup(Product product, BigDecimal cost,
                                BigDecimal price, Party practice) {
        BigDecimal markup = BigDecimal.ZERO;
        if (cost.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal taxRate = BigDecimal.ZERO;
            if (product != null) {
                taxRate = getTaxRate(product, practice);
            }
            markup = price.divide(
                    cost.multiply(BigDecimal.ONE.add(taxRate)), 3,
                    RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(MathRules.ONE_HUNDRED);
            if (markup.compareTo(BigDecimal.ZERO) < 0) {
                markup = BigDecimal.ZERO;
            }
        }
        return markup;
    }

    /**
     * Updates the cost node of any <em>productPrice.unitPrice</em>
     * associated with a product, and recalculates its price.
     * <p/>
     * Returns a list of unit prices whose cost and price have changed.
     *
     * @param product  the product
     * @param cost     the new cost
     * @param practice the <em>party.organisationPractice</em> used to determine product taxes
     * @param currency the currency, for rounding conventions
     * @return the list of any updated prices
     */
    public List<ProductPrice> updateUnitPrices(Product product,
                                               BigDecimal cost,
                                               Party practice,
                                               Currency currency) {
        List<ProductPrice> result = null;
        IMObjectBean bean = new IMObjectBean(product, service);
        List<ProductPrice> prices
                = bean.getValues("prices", ProductPrice.class);
        if (!prices.isEmpty()) {
            cost = currency.round(cost);
            for (ProductPrice price : prices) {
                if (TypeHelper.isA(price, ProductArchetypes.UNIT_PRICE)) {
                    if (updateUnitPrice(price, product, cost, practice,
                                        currency)) {
                        if (result == null) {
                            result = new ArrayList<ProductPrice>();
                        }
                        result.add(price);
                    }
                }
            }
        }
        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Updates an <em>productPrice.unitPrice</em> if required.
     *
     * @param price    the price
     * @param product  the associated product
     * @param cost     the cost price
     * @param practice the <em>party.organisationPractice</em> used to determine product taxes
     * @param currency the currency, for rounding conventions
     * @return <tt>true</tt> if the price was updated
     */
    private boolean updateUnitPrice(ProductPrice price, Product product,
                                    BigDecimal cost, Party practice,
                                    Currency currency) {
        IMObjectBean priceBean = new IMObjectBean(price, service);
        BigDecimal old = priceBean.getBigDecimal("cost", BigDecimal.ZERO);
        if (!MathRules.equals(old, cost)) {
            priceBean.setValue("cost", cost);
            BigDecimal markup = priceBean.getBigDecimal("markup", BigDecimal.ZERO);
            BigDecimal newPrice = getPrice(product, cost, markup, practice, currency);
            price.setPrice(newPrice);
            return true;
        }
        return false;
    }

    /**
     * Returns the tax rate of a product.
     *
     * @param product  the product
     * @param practice the <em>party.organisationPractice</em> used to determine product taxes
     * @return the product tax rate
     * @throws ArchetypeServiceException for any archetype service error
     */
    private BigDecimal getTaxRate(Product product, Party practice) {
        TaxRules rules = new TaxRules(practice, service, lookups);
        return getRate(rules.getTaxRate(product));
    }

    /**
     * Returns a percentage / 100.
     *
     * @param percent the percent
     * @return <tt>percent / 100 </tt>
     */
    private BigDecimal getRate(BigDecimal percent) {
        if (percent.compareTo(BigDecimal.ZERO) != 0) {
            return MathRules.divide(percent, MathRules.ONE_HUNDRED, 3);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Returns a product price matching the specified predicate.
     *
     * @param shortName the price short name
     * @param product   the product
     * @param predicate the predicate
     * @param date      the date
     * @return a price matching the predicate, or <tt>null</tt> if none is found
     */
    private ProductPrice getProductPrice(String shortName, Product product, Predicate predicate, Date date) {
        boolean useDefault = ProductArchetypes.FIXED_PRICE.equals(shortName);
        ProductPrice result = findPrice(product, predicate, useDefault);
        if (useDefault && (result == null || !isDefault(result))) {
            // see if there is a fixed price in linked products
            EntityBean bean = new EntityBean(product, service);
            if (bean.hasNode("linked")) {
                List<Entity> products = bean.getNodeTargetEntities("linked", date);
                for (Entity linked : products) {
                    ProductPrice price = findPrice((Product) linked, predicate, useDefault);
                    if (price != null) {
                        if (isDefault(price)) {
                            result = price;
                            break;
                        } else if (result == null) {
                            result = price;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Finds a product price matching the predicate.
     *
     * @param product    the product
     * @param predicate  the predicate to evaluate
     * @param useDefault if <tt>true</tt>, select prices that have a
     *                   <tt>true</tt> default node
     * @return the price matching the criteria, or <tt>null</tt> if none is
     *         found
     */
    private ProductPrice findPrice(Product product, Predicate predicate,
                                   boolean useDefault) {
        ProductPrice result = null;
        ProductPrice fallback = null;
        for (ProductPrice price : product.getProductPrices()) {
            if (predicate.evaluate(price)) {
                if (useDefault) {
                    if (isDefault(price)) {
                        result = price;
                        break;
                    } else {
                        fallback = price;
                    }
                } else {
                    result = price;
                }
            }
        }
        return (result != null) ? result : fallback;
    }

    /**
     * Finds product prices matching the specified predicate.
     *
     * @param product   the product
     * @param predicate the predicate
     * @return the pricees matching the predicate
     */
    private List<ProductPrice> findPrices(Product product,
                                          Predicate predicate) {
        List<ProductPrice> result = null;
        for (ProductPrice price : product.getProductPrices()) {
            if (predicate.evaluate(price)) {
                if (result == null) {
                    result = new ArrayList<ProductPrice>();
                }
                result.add(price);
            }
        }
        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Determines if a fixed price is the default.
     *
     * @param price the price
     * @return <tt>true</tt> if it is the default, otherwise <tt>false</tt>
     */
    private boolean isDefault(ProductPrice price) {
        IMObjectBean bean = new IMObjectBean(price, service);
        return bean.getBoolean("default");
    }

    /**
     * Predicate to determine if a price matches a short name and date.
     */
    private static class ShortNameDatePredicate implements Predicate {

        /**
         * The price short name.
         */
        private final String shortName;

        /**
         * The date.
         */
        private final Date date;

        public ShortNameDatePredicate(String shortName, Date date) {
            this.shortName = shortName;
            this.date = date;
        }

        public boolean evaluate(Object object) {
            ProductPrice price = (ProductPrice) object;
            if (TypeHelper.isA(price, shortName) && price.isActive()) {
                Date from = price.getFromDate();
                Date to = price.getToDate();
                if (DateRules.betweenDates(date, from, to)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Predicate to determine if a product price matches a price, short name
     * and date.
     */
    private static class PricePredicate extends ShortNameDatePredicate {

        /**
         * The price.
         */
        private final BigDecimal price;

        public PricePredicate(BigDecimal price, String shortName, Date date) {
            super(shortName, date);
            this.price = price;
        }

        public boolean evaluate(Object object) {
            ProductPrice other = (ProductPrice) object;
            return other.getPrice().compareTo(price) == 0 && super.evaluate(other);
        }
    }

}
