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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.product;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.AndPredicate;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.openvpms.archetype.rules.math.MathRules.ONE_HUNDRED;
import static org.openvpms.archetype.rules.product.ProductArchetypes.FIXED_PRICE;
import static org.openvpms.component.business.service.archetype.functor.IsActiveRelationship.isActive;
import static org.openvpms.component.business.service.archetype.functor.IsActiveRelationship.isActiveNow;
import static org.openvpms.component.business.service.archetype.functor.RefEquals.getTargetEquals;


/**
 * Product price rules.
 *
 * @author Tim Anderson
 */
public class ProductPriceRules {

    /**
     * Default maximum discount.
     */
    public static final BigDecimal DEFAULT_MAX_DISCOUNT = MathRules.ONE_HUNDRED;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    private static final int PARTIAL_MATCH = 1;

    private static final int EXACT_MATCH = 2;

    /**
     * Constructs a {@link ProductPriceRules}.
     *
     * @param service the archetype service
     */
    public ProductPriceRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the first price with the specified short name and pricing group, active at the specified date.
     * <p/>
     * If {@code group} is:
     * <ul>
     * <li>non-null - it matches prices that have that pricing group, or no pricing group</li>
     * <li>null - it matches prices that have no pricing group</li>
     * </ul>
     *
     * @param product   the product
     * @param shortName the price short name
     * @param date      the date
     * @param group     the pricing group. May be {@code null}
     * @return the first matching price, or {@code null} if none is found
     */
    public ProductPrice getProductPrice(Product product, String shortName, Date date, Lookup group) {
        ProductPricePredicate predicate = new ShortNameDatePredicate(shortName, date, new PricingGroup(group));
        return getProductPrice(shortName, product, predicate, date);
    }

    /**
     * Returns the first product price with the specified price, short name, and group, active at the specified date.
     * <p/>
     * If {@code group} is:
     * <ul>
     * <li>non-null - it matches prices that have that pricing group, or no pricing group</li>
     * <li>null - it matches prices that have no pricing group</li>
     * </ul>
     *
     * @param product   the product
     * @param price     the price
     * @param shortName the price short name
     * @param date      the date
     * @param group     the pricing group. May be {@code null}
     * @return the first matching price, or {@code null} if none is found
     */
    public ProductPrice getProductPrice(Product product, BigDecimal price, String shortName, Date date,
                                        Lookup group) {
        PricePredicate predicate = new PricePredicate(price, shortName, date, new PricingGroup(group));
        return getProductPrice(shortName, product, predicate, date);
    }

    /**
     * Returns all prices matching the specified short name.
     * <p/>
     * This will examine linked products if {@code shortName} is <em>productPrice.fixedPrice</em>.
     *
     * @param product   the product
     * @param shortName the price short name
     * @param group     the pricing group. May be {@code null}
     * @return the matching prices, sorted on descending time
     */
    public List<ProductPrice> getProductPrices(Product product, String shortName, PricingGroup group) {
        return getProductPrices(product, shortName, true, group);
    }

    /**
     * Returns all prices matching the specified short name.
     *
     * @param product       the product
     * @param shortName     the price short name
     * @param includeLinked if {@code true} and the requested prices are <em>productPrice.fixedPrice</em>, linked
     *                      products will be searched
     * @param group         the pricing group
     * @return the matching prices, sorted on descending time
     */
    public List<ProductPrice> getProductPrices(Product product, String shortName, boolean includeLinked,
                                               PricingGroup group) {
        List<ProductPrice> result = new ArrayList<>();
        ProductPricePredicate predicate = new ProductPricePredicate(shortName, group);
        List<ProductPrice> prices = findPrices(product, predicate);
        result.addAll(prices);
        if (includeLinked && FIXED_PRICE.equals(shortName)) {
            // see if there is a fixed price in linked products
            result.addAll(findLinkedPrices(product, predicate, IsActiveRelationship.isActiveNow()));
        }
        return sort(result);
    }

    /**
     * Returns all prices matching the specified short name, active at the specified date.
     * <p/>
     * This will examine linked products if {@code shortName} is <em>productPrice.fixedPrice</em>.
     * <p/>
     * If {@code group} is:
     * <ul>
     * <li>non-null - it matches prices that have that pricing group, or no pricing group</li>
     * <li>null - it matches prices that have no pricing group</li>
     * </ul>
     *
     * @param product   the product
     * @param shortName the price short name
     * @param date      the date
     * @param group     the pricing group
     * @return all prices matching the criteria
     */
    public List<ProductPrice> getProductPrices(Product product, String shortName, Date date, PricingGroup group) {
        return getProductPrices(product, shortName, date, true, group);
    }

    /**
     * Returns all prices matching the specified short name, active at the specified date.
     * <p/>
     * If {@code group} is:
     * <ul>
     * <li>non-null - it matches prices that have that pricing group, or no pricing group</li>
     * <li>null - it matches prices that have no pricing group</li>
     * </ul>
     *
     * @param product       the product
     * @param shortName     the price short name
     * @param date          the date
     * @param includeLinked if {@code true} and the requested prices are <em>productPrice.fixedPrice</em>, linked
     *                      products will be searched
     * @param group         the pricing group
     * @return all prices matching the criteria
     */
    public List<ProductPrice> getProductPrices(Product product, String shortName, Date date, boolean includeLinked,
                                               PricingGroup group) {
        List<ProductPrice> result = new ArrayList<>();
        ShortNameDatePredicate predicate = new ShortNameDatePredicate(shortName, date, group);
        List<ProductPrice> prices = findPrices(product, predicate);
        result.addAll(prices);
        if (includeLinked && FIXED_PRICE.equals(shortName)) {
            // see if there is a fixed price in linked products
            result.addAll(findLinkedPrices(product, predicate, isActive(date)));
        }
        return sort(result);
    }

    /**
     * Returns all prices matching the specified short name, active between a date range.
     * <p/>
     * This will examine linked products if {@code shortName} is <em>productPrice.fixedPrice</em>.
     * <p/>
     * If {@code group} is:
     * <ul>
     * <li>non-null - it matches prices that have that pricing group, or no pricing group</li>
     * <li>null - it matches prices that have no pricing group</li>
     * </ul>
     *
     * @param product   the product
     * @param shortName the price short name
     * @param from      the start of the date range. May be {@code null}
     * @param to        the end of the date range. May be {@code null}
     * @param group     the pricing group
     * @return the matching prices, sorted on descending time
     */
    public List<ProductPrice> getProductPrices(Product product, String shortName, Date from, Date to,
                                               PricingGroup group) {
        return getProductPrices(product, shortName, from, to, true, group);
    }

    /**
     * Returns all prices matching the specified short name and pricing group active between a date range.
     * <p/>
     * If {@code group} is:
     * <ul>
     * <li>non-null - it matches prices that have that pricing group, or no pricing group</li>
     * <li>null - it matches prices that have no pricing group</li>
     * </ul>
     *
     * @param product       the product
     * @param shortName     the price short name
     * @param from          the start of the date range. May be {@code null}
     * @param to            the end of the date range. May be {@code null}
     * @param includeLinked if {@code true} and the requested prices are <em>productPrice.fixedPrice</em>, linked
     *                      products will be searched
     * @param group         the pricing group
     * @return the matching prices, sorted on descending time
     */
    public List<ProductPrice> getProductPrices(Product product, String shortName, Date from, Date to,
                                               boolean includeLinked, PricingGroup group) {
        List<ProductPrice> result = new ArrayList<>();
        ShortNameDateRangePredicate predicate = new ShortNameDateRangePredicate(shortName, from, to, group);
        List<ProductPrice> prices = findPrices(product, predicate);
        result.addAll(prices);
        if (includeLinked && FIXED_PRICE.equals(shortName)) {
            // see if there is a fixed price in linked products
            result.addAll(findLinkedPrices(product, predicate, isActive(from, to)));
        }
        return sort(result);
    }

    /**
     * Calculates a tax-exclusive price, given the cost and markup.
     * <p/>
     * The formula is:
     * {@code taxExPrice = cost * (1 + markup/100)}
     *
     * @param cost   the cost
     * @param markup the markup
     * @return the tax-exclusive price
     */
    public BigDecimal getTaxExPrice(BigDecimal cost, BigDecimal markup) {
        BigDecimal price = ZERO;
        if (cost.compareTo(ZERO) != 0) {
            BigDecimal markupDec = getRate(markup);
            price = cost.multiply(ONE.add(markupDec));
        }
        return price;
    }

    /**
     * Calculates a tax exclusive price given the tax-inclusive price, and tax rates derived from the product and
     * practice.
     *
     * @param taxIncPrice the tax-inclusive price
     * @param product     the product
     * @param practice    the practice
     * @return the tax-exclusive price
     */
    public BigDecimal getTaxExPrice(BigDecimal taxIncPrice, Product product, Party practice) {
        BigDecimal price = taxIncPrice;
        BigDecimal taxRate = getTaxRate(product, practice);
        if (taxRate.compareTo(BigDecimal.ZERO) != 0) {
            price = taxIncPrice.divide(ONE.add(getRate(taxRate)), 3, RoundingMode.HALF_UP);
        }
        return price;
    }

    /**
     * Calculates a tax-inclusive product price using the following formula:
     * <p/>
     * {@code price = taxExPrice * (1 + taxRate/100)}
     * <p/>
     * The price is rounded according to currency conventions.
     *
     * @param taxExPrice the tax-exclusive price
     * @param product    the product
     * @param practice   the practice
     * @param currency   the currency, used for rounding
     * @return the tax-exclusive price
     */
    public BigDecimal getTaxIncPrice(BigDecimal taxExPrice, Product product, Party practice, Currency currency) {
        BigDecimal taxRate = getTaxRate(product, practice);
        return getTaxIncPrice(taxExPrice, taxRate, currency);
    }

    /**
     * Calculates a tax-inclusive product price using the following formula:
     * <p/>
     * {@code price = taxExPrice * (1 + taxRate/100)}
     * <p/>
     * The price is rounded according to currency conventions.
     *
     * @param taxExPrice the tax-exclusive price
     * @param currency   the currency, used for rounding
     * @return the tax-exclusive price
     */
    public BigDecimal getTaxIncPrice(BigDecimal taxExPrice, BigDecimal taxRate, Currency currency) {
        BigDecimal price = taxExPrice;
        if (taxRate.compareTo(BigDecimal.ZERO) != 0) {
            price = taxExPrice.multiply(ONE.add(getRate(taxRate)));
        }
        price = currency.roundPrice(price);
        return price;
    }

    /**
     * Calculates a tax-exclusive price markup, given the cost and tax-exclusive price.
     * <p/>
     * The formula is:
     * {@code markup = (price/cost - 1) * 100}
     *
     * @param cost  the cost
     * @param price the tax-exclusive price
     * @return the tax-exclusive price markup
     */
    public BigDecimal getMarkup(BigDecimal cost, BigDecimal price) {
        BigDecimal markup = ZERO;
        if (cost.compareTo(ZERO) != 0) {
            markup = price.divide(cost, 3, RoundingMode.HALF_UP).subtract(ONE).multiply(ONE_HUNDRED);
            if (markup.compareTo(ZERO) < 0) {
                markup = ZERO;
            }
        }
        return markup;
    }

    /**
     * Calculates the maximum discount that can be applied for a given markup.
     * <p/>
     * Uses the equation:
     * {@code (markup / (100 + markup)) * 100}
     *
     * @param markup the markup expressed as a percentage
     * @return the discount as a percentage rounded down
     */
    public BigDecimal getMaxDiscount(BigDecimal markup) {
        BigDecimal discount = DEFAULT_MAX_DISCOUNT;
        if (markup.compareTo(BigDecimal.ZERO) > 0) {
            discount = markup.divide(ONE_HUNDRED.add(markup), 3, RoundingMode.HALF_DOWN).multiply(ONE_HUNDRED);
        }
        return discount;
    }

    /**
     * Updates the cost node of any <em>productPrice.unitPrice</em> associated with a product active at the current
     * time, and recalculates its price.
     * <p/>
     * Returns a list of unit prices whose cost and price have changed.
     *
     * @param product  the product
     * @param cost     the new cost
     * @param currency the currency, for rounding conventions
     * @return the list of any updated prices
     */
    public List<ProductPrice> updateUnitPrices(Product product, BigDecimal cost, Currency currency) {
        return updateUnitPrices(product, cost, false, currency);
    }

    /**
     * Updates the cost node of any <em>productPrice.unitPrice</em> associated with a product active at the current
     * time, and recalculates its price.
     * <p/>
     * Returns a list of unit prices whose cost and price have changed.
     *
     * @param product            the product
     * @param cost               the new cost
     * @param ignoreCostDecrease if {@code true}, don't update any unit price if the new cost price would be less than
     *                           the existing cost price
     * @param currency           the currency, for rounding conventions
     * @return the list of any updated prices
     */
    public List<ProductPrice> updateUnitPrices(Product product, BigDecimal cost, final boolean ignoreCostDecrease,
                                               Currency currency) {
        List<ProductPrice> result = null;
        IMObjectBean bean = new IMObjectBean(product, service);
        final Date now = new Date();
        final BigDecimal roundedCost = currency.round(cost);
        Predicate predicate = object -> {
            ProductPrice price = (ProductPrice) object;
            boolean result1 = price.isActive() && TypeHelper.isA(price, ProductArchetypes.UNIT_PRICE)
                              && DateRules.between(now, price.getFromDate(), price.getToDate());
            if (result1 && ignoreCostDecrease) {
                IMObjectBean bean1 = new IMObjectBean(price, service);
                BigDecimal currentCost = bean1.getBigDecimal("cost", BigDecimal.ZERO);
                result1 = currentCost.compareTo(roundedCost) < 0;
            }
            return result1;
        };
        List<ProductPrice> prices = bean.getValues("prices", predicate, ProductPrice.class);
        if (!prices.isEmpty()) {
            for (ProductPrice price : prices) {
                if (updateUnitPrice(price, roundedCost)) {
                    if (result == null) {
                        result = new ArrayList<>();
                    }
                    result.add(price);
                }
            }
        }
        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Returns the maximum discount for a product price, expressed as a percentage.
     *
     * @param price the price
     * @return the maximum discount for the product price, or {@code 100} if there is no maximum discount associated
     * with the price.
     */
    public BigDecimal getMaxDiscount(ProductPrice price) {
        IMObjectBean bean = new IMObjectBean(price, service);
        BigDecimal result = bean.getBigDecimal("maxDiscount");
        return (result == null) ? DEFAULT_MAX_DISCOUNT : result;
    }

    /**
     * Returns the pricing groups for a price.
     *
     * @param price the price
     * @return the pricing groups
     */
    public List<Lookup> getPricingGroups(ProductPrice price) {
        IMObjectBean bean = new IMObjectBean(price, service);
        return bean.getValues("pricingGroups", Lookup.class);
    }

    /**
     * Returns the cost price for a product price.
     *
     * @param price the cost price
     * @return the cost price
     */

    public BigDecimal getCostPrice(ProductPrice price) {
        IMObjectBean bean = new IMObjectBean(price, service);
        return bean.getBigDecimal("cost", BigDecimal.ZERO);
    }

    /**
     * Returns the service ratio for a product.
     * <p/>
     * This is a factor that is applied to a product's prices when the product is charged at a particular practice
     * location. It is determined by the <em>entityLink.locationProductType</em> relationship between the location and
     * the product's type.
     *
     * @param product  the product
     * @param location the practice location
     * @return the service ratio. If there is no relationship, returns {@code 1.0}
     */
    public BigDecimal getServiceRatio(Product product, Party location) {
        BigDecimal ratio = BigDecimal.ONE;
        IMObjectBean bean = new IMObjectBean(product, service);
        IMObjectReference productType = bean.getNodeTargetObjectRef("type");
        if (productType != null) {
            IMObjectBean locationBean = new IMObjectBean(location, service);
            Predicate predicate = AndPredicate.getInstance(isActiveNow(), getTargetEquals(productType));
            EntityLink link = (EntityLink) locationBean.getValue("serviceRatios", predicate);
            if (link != null) {
                IMObjectBean linkBean = new IMObjectBean(link, service);
                ratio = linkBean.getBigDecimal("ratio", BigDecimal.ONE);
            }
        }
        return ratio;
    }

    /**
     * Sorts prices on descending time.
     * <p/>
     * NOTE: this modifies the input list.
     *
     * @param prices the prices to sort
     * @return the prices
     */
    @SuppressWarnings("unchecked")
    public List<ProductPrice> sort(List<ProductPrice> prices) {
        Collections.sort(prices, ComparatorUtils.reversedComparator(ProductPriceComparator.INSTANCE));
        return prices;
    }

    /**
     * Returns the tax rate of a product.
     *
     * @param product  the product
     * @param practice the <em>party.organisationPractice</em> used to determine product taxes
     * @return the product tax rate
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getTaxRate(Product product, Party practice) {
        TaxRules rules = new TaxRules(practice, service);
        return rules.getTaxRate(product);
    }

    /**
     * Updates an <em>productPrice.unitPrice</em> if required.
     *
     * @param price the price
     * @param cost  the cost price
     * @return {@code true} if the price was updated
     */
    private boolean updateUnitPrice(ProductPrice price, BigDecimal cost) {
        IMObjectBean priceBean = new IMObjectBean(price, service);
        BigDecimal old = priceBean.getBigDecimal("cost", ZERO);
        if (!MathRules.equals(old, cost)) {
            priceBean.setValue("cost", cost);
            BigDecimal markup = priceBean.getBigDecimal("markup", ZERO);
            BigDecimal newPrice = getTaxExPrice(cost, markup);
            price.setPrice(newPrice);
            return true;
        }
        return false;
    }

    /**
     * Returns a percentage / 100.
     * <p/>
     * This is expressed to 4 decimal places to support tax rates like "8.25%".
     *
     * @param percent the percent
     * @return {@code percent / 100 }
     */
    private BigDecimal getRate(BigDecimal percent) {
        if (percent.compareTo(ZERO) != 0) {
            return MathRules.divide(percent, ONE_HUNDRED, 4);
        }
        return ZERO;
    }

    /**
     * Returns a product price matching the specified predicate.
     *
     * @param shortName the price short name
     * @param product   the product
     * @param predicate the predicate
     * @param date      the date
     * @return a price matching the predicate, or {@code null} if none is found
     */
    private ProductPrice getProductPrice(String shortName, Product product, ProductPricePredicate predicate,
                                         Date date) {
        boolean useDefault = FIXED_PRICE.equals(shortName);
        ProductPrice result = findPrice(product, predicate, useDefault);
        if (useDefault && (result == null || !isDefault(result))) {
            // see if there is a fixed price in linked products
            EntityBean bean = new EntityBean(product, service);
            if (bean.hasNode("linked")) {
                List<Entity> products = bean.getNodeTargetEntities("linked", date);
                for (Entity linked : products) {
                    ProductPrice price = findPrice((Product) linked, predicate, true);
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
     * @param useDefault if {@code true}, select prices that have a {@code true} default node
     * @return the price matching the criteria, or {@code null} if none is found
     */
    private ProductPrice findPrice(Product product, ProductPricePredicate predicate, boolean useDefault) {
        ProductPrice result = null;
        ProductPrice fallback = null;
        int fallbackMatch = 0;
        for (org.openvpms.component.model.product.ProductPrice p : product.getProductPrices()) {
            ProductPrice price = (ProductPrice) p;
            int match = predicate.matches(price);
            if (match > 0) {
                if (useDefault) {
                    if (isDefault(price) && match == EXACT_MATCH) {
                        result = price;
                        break;
                    }
                } else if (match == EXACT_MATCH) {
                    result = price;
                    break;
                }
                if (match > fallbackMatch || (match == fallbackMatch && useDefault && isDefault(price))) {
                    fallback = price;
                    fallbackMatch = match;
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
     * @return the prices matching the predicate
     */
    private List<ProductPrice> findPrices(Product product, ProductPricePredicate predicate) {
        List<ProductPrice> result = null;
        for (org.openvpms.component.model.product.ProductPrice p : product.getProductPrices()) {
            ProductPrice price = (ProductPrice) p;
            if (predicate.evaluate(price)) {
                if (result == null) {
                    result = new ArrayList<>();
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
     * Adds any prices from linked products active at time determined by the {@code active} predicate.
     *
     * @param product the product
     * @param price   the predicate to select prices
     * @param active  the predicate to select active linked products
     */
    private List<ProductPrice> findLinkedPrices(Product product, ProductPricePredicate price, Predicate active) {
        List<ProductPrice> result = null;
        List<ProductPrice> prices;
        EntityBean bean = new EntityBean(product, service);
        if (bean.hasNode("linked")) {
            for (Entity linked : bean.getNodeTargetEntities("linked", active)) {
                prices = findPrices((Product) linked, price);
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.addAll(prices);
            }
        }
        return (result != null) ? result : Collections.<ProductPrice>emptyList();
    }

    /**
     * Determines if a fixed price is the default.
     *
     * @param price the price
     * @return {@code true} if it is the default, otherwise {@code false}
     */
    private boolean isDefault(ProductPrice price) {
        IMObjectBean bean = new IMObjectBean(price, service);
        return bean.getBoolean("default");
    }

    private class ProductPricePredicate implements Predicate {

        /**
         * The price short name.
         */
        private final String shortName;

        /**
         * The pricing group.
         */
        private final PricingGroup group;


        public ProductPricePredicate(String shortName, PricingGroup group) {
            this.shortName = shortName;
            this.group = group;
        }

        public boolean evaluate(Object object) {
            ProductPrice price = (ProductPrice) object;
            return matches(price) > 0;
        }

        /**
         * Determines if the predicate matches the price
         *
         * @param price the price
         * @return {@code 0} if it doesn't match, {@link #PARTIAL_MATCH} if it is a partial match on group,
         * {@link #EXACT_MATCH} if it is an exact match on group
         */
        public int matches(ProductPrice price) {
            int result = 0;
            if (TypeHelper.isA(price, shortName) && price.isActive()) {
                if (group.isAll()) {
                    result = EXACT_MATCH;
                } else {
                    IMObjectBean bean = new IMObjectBean(price, service);
                    List<Lookup> groups = bean.getValues("pricingGroups", Lookup.class);
                    Lookup lookup = group.getGroup();
                    if ((lookup == null && groups.isEmpty()) || (lookup != null && groups.contains(lookup))) {
                        result = EXACT_MATCH;
                    } else if (lookup != null && groups.isEmpty() && group.useFallback()) {
                        result = PARTIAL_MATCH;
                    }
                }
            }
            return result;
        }
    }

    /**
     * Predicate to determine if a price matches a short name and date.
     */
    private class ShortNameDatePredicate extends ProductPricePredicate {

        /**
         * The date.
         */
        private final Date date;

        public ShortNameDatePredicate(String shortName, Date date, PricingGroup group) {
            super(shortName, group);
            this.date = date;
        }

        @Override
        public int matches(ProductPrice price) {
            int result = super.matches(price);
            if (result > 0) {
                Date from = price.getFromDate();
                Date to = price.getToDate();
                if (!DateRules.between(date, from, to)) {
                    result = 0;
                }
            }
            return result;
        }
    }

    /**
     * Predicate to determine if a price matches a short name and date range.
     */
    private class ShortNameDateRangePredicate extends ProductPricePredicate {

        /**
         * The from date.
         */
        private final Date from;

        /**
         * The to date.
         */
        private final Date to;

        public ShortNameDateRangePredicate(String shortName, Date from, Date to, PricingGroup group) {
            super(shortName, group);
            this.from = from;
            this.to = to;
        }

        @Override
        public int matches(ProductPrice price) {
            int result = super.matches(price);
            if (result > 0) {
                if (!DateRules.intersects(from, to, price.getFromDate(), price.getToDate())) {
                    result = 0;
                }
            }
            return result;
        }
    }

    /**
     * Predicate to determine if a product price matches a price, short name
     * and date.
     */
    private class PricePredicate extends ShortNameDatePredicate {

        /**
         * The price.
         */
        private final BigDecimal price;

        public PricePredicate(BigDecimal price, String shortName, Date date, PricingGroup group) {
            super(shortName, date, group);
            this.price = price;
        }

        @Override
        public int matches(ProductPrice other) {
            if (other.getPrice().compareTo(price) == 0) {
                return super.matches(other);
            }
            return 0;
        }
    }

    private static class ProductPriceComparator implements Comparator<ProductPrice> {

        /**
         * The singleton instance.
         */
        public static Comparator<ProductPrice> INSTANCE = new ProductPriceComparator();

        @Override
        public int compare(ProductPrice o1, ProductPrice o2) {
            int result;
            if (ObjectUtils.equals(o1.getToDate(), o2.getToDate())) {
                result = 0;
            } else if (o1.getToDate() == null) {
                result = 1;
            } else if (o2.getToDate() == null) {
                result = -1;
            } else {
                result = DateRules.compareTo(o1.getToDate(), o2.getToDate());
            }
            if (result == 0) {
                if (!ObjectUtils.equals(o1.getFromDate(), o2.getFromDate())) {
                    if (o1.getFromDate() == null) {
                        result = 1;
                    } else if (o2.getFromDate() == null) {
                        result = -1;
                    } else {
                        result = DateRules.compareDates(o1.getFromDate(), o2.getFromDate());
                    }
                }
            }
            return result;
        }
    }
}
