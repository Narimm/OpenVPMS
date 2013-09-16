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
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.openvpms.archetype.rules.product.ProductArchetypes.FIXED_PRICE;
import static org.openvpms.archetype.rules.product.ProductArchetypes.UNIT_PRICE;
import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.CannotCloseExistingPrice;
import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.CannotUpdateLinkedPrice;
import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.DuplicateFixedPrice;
import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.DuplicateUnitPrice;
import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.FromDateGreaterThanToDate;
import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.NoFromDate;
import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.PriceNotFound;
import static org.openvpms.archetype.rules.product.io.ProductIOException.ErrorCode.UnitPriceOverlap;

/**
 * Determines changes between a {@link Product} and corresponding {@link ProductData}.
 *
 * @author Tim Anderson
 */
public class ProductDataComparer {

    /**
     * The price rules.
     */
    private final ProductPriceRules rules;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs an {@link ProductDataComparer}.
     *
     * @param rules   the price rules
     * @param service the archetype service
     */
    public ProductDataComparer(ProductPriceRules rules, IArchetypeService service) {
        this.rules = rules;
        this.service = service;
    }

    /**
     * Compares are product with its corresponding imported data, returning the changes.
     *
     * @param product the product
     * @param data    the product data
     * @return the changes to apply to the product, or {@code null} if there are no changes
     * @throws ProductIOException if the data is invalid
     */
    public ProductData compare(Product product, ProductData data) {
        List<ProductPrice> fixedPrices = getFixedPrices(product, data);
        List<ProductPrice> unitPrices = getUnitPrices(product, data);

        validate(product, data, fixedPrices, unitPrices);
        ProductData result = null;
        List<PriceData> fixedPriceData = getPriceChanges(product, data.getFixedPrices(), fixedPrices);
        List<PriceData> unitPriceData = getPriceChanges(product, data.getUnitPrices(), unitPrices);
        boolean printedNameChanged;
        IMObjectBean bean = new IMObjectBean(product, service);
        printedNameChanged = !ObjectUtils.equals(data.getPrintedName(), bean.getString("printedName"));
        if (!fixedPriceData.isEmpty() || !unitPriceData.isEmpty() || printedNameChanged) {
            result = new ProductData(data);
            result.setPrintedName(data.getPrintedName());
            result.getFixedPrices().addAll(fixedPriceData);
            result.getUnitPrices().addAll(unitPriceData);
        }

        return result;
    }

    /**
     * Returns fixed prices associated with a product that may be updated by, or overlap those in the product data.
     *
     * @param product the product
     * @param data    the product data
     * @return all prices for the product, or an empty list if the product data contains no fixed prices
     */
    public List<ProductPrice> getFixedPrices(Product product, ProductData data) {
        return getPrices(product, data, true);
    }

    /**
     * Returns unit prices associated with a product that may be updated by, or overlap those in the product data.
     *
     * @param product the product
     * @param data    the product data
     * @return all prices for the product, or an empty list if the product data contains no fixed prices
     */
    public List<ProductPrice> getUnitPrices(Product product, ProductData data) {
        return getPrices(product, data, false);
    }

    /**
     * Returns prices associated with a product that may be updated by, or overlap those in the product data.
     *
     * @param product the product
     * @param data    the product data
     * @param fixed   if {@code true}, return fixed prices otherwise return unit prices
     * @return all prices for the product, or an empty list if the product data contains no prices of the specified type
     */
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

    /**
     * Validates prices.
     *
     * @param product     the product
     * @param data        the product data to valid
     * @param fixedPrices the existing fixed prices
     * @param unitPrices  the existing unit prices
     */
    private void validate(Product product, ProductData data, List<ProductPrice> fixedPrices,
                          List<ProductPrice> unitPrices) {
        checkPrices(product, data.getUnitPrices(), unitPrices);
        checkPrices(product, data.getFixedPrices(), fixedPrices);
    }

    /**
     * Verifies that the input prices are valid, and don't update linked prices.
     *
     * @param product  the product
     * @param prices   the input prices
     * @param existing the existing prices
     */
    private void checkPrices(Product product, List<PriceData> prices, List<ProductPrice> existing) {
        for (PriceData price : prices) {
            if (price.getFrom() == null) {
                throw new ProductIOException(NoFromDate, price.getLine());
            }
            if (price.getId() != -1) {
                boolean found = false;
                for (ProductPrice pp : existing) {
                    if (pp.getId() == price.getId()) {
                        if (pp.getProduct().getId() != product.getId() && !equals(price, pp)) {
                            throw new ProductIOException(CannotUpdateLinkedPrice, price.getLine(), price.getId(),
                                                         product.getName(), product.getId(), pp.getProduct().getName(),
                                                         pp.getProduct().getId());
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new ProductIOException(PriceNotFound, price.getLine(), price.getId());
                }
            }
        }
    }

    /**
     * Returns the new and updated prices.
     *
     * @param product  the product
     * @param prices   the input prices
     * @param existing the existing prices
     * @return the new and updated prices
     */
    private List<PriceData> getPriceChanges(Product product, List<PriceData> prices, List<ProductPrice> existing) {
        List<PriceData> result = new ArrayList<PriceData>();
        List<PriceData> duplicateFree = removeDuplicates(prices, existing);
        Date from = null;
        boolean sameFromDate = true;

        // update existing prices first
        for (PriceData price : duplicateFree) {
            if (price.getId() != -1) {
                PriceData data = getUpdatedPriceData(product, price, existing);
                if (data != null) {
                    result.add(data);
                }
            } else {
                // determine if new prices have the same from date. If so, the from date is used to close existing
                // prices.
                if (from == null) {
                    from = price.getFrom();
                } else if (price.getFrom() != null && !DateRules.dateEquals(from, price.getFrom())) {
                    sameFromDate = false;
                }
            }
        }

        List<PriceData> merged = merge(result, existing); // merged the changed prices with the other prices

        // process new prices
        for (PriceData price : duplicateFree) {
            if (price.getId() == -1) {
                if (updateExistingPrice(price, merged, result, product, sameFromDate)) {
                    result.add(price);
                }
            }
        }
        return result;
    }

    /**
     * Returns price data with duplicates removed.
     *
     * @param prices  the prices to filter
     * @param current the current product prices
     * @return the price data with duplicates removed
     * @throws ProductIOException if an input price has the same id as another, but the details are different, or
     *                            multiple unit prices are specified that are duplicates
     */
    private List<PriceData> removeDuplicates(List<PriceData> prices, List<ProductPrice> current) {
        List<PriceData> result = new ArrayList<PriceData>();
        List<PriceData> unique;
        if (prices.size() <= 1) {
            unique = prices;
        } else {
            unique = new ArrayList<PriceData>();
            Map<Long, PriceData> dataById = new HashMap<Long, PriceData>();
            Map<DateRange, PriceData> dataByDate = new LinkedHashMap<DateRange, PriceData>();
            for (PriceData data : prices) {
                if (data.getId() != -1) {
                    PriceData existing = dataById.get(data.getId());
                    if (existing != null) {
                        if (!data.equals(existing)) {
                            if (FIXED_PRICE.equals(data.getShortName())) {
                                throw new ProductIOException(DuplicateFixedPrice, data.getLine());
                            } else {
                                throw new ProductIOException(DuplicateUnitPrice, data.getLine());
                            }
                        }
                    } else {
                        dataById.put(data.getId(), data);
                        DateRange key = new DateRange(data);
                        if (dataByDate.get(key) != null) {
                            if (UNIT_PRICE.equals(data.getShortName())) {
                                throw new ProductIOException(DuplicateUnitPrice, data.getLine());
                            }
                        } else {
                            dataByDate.put(key, data);
                        }
                    }
                    unique.add(data);
                }
            }
            for (PriceData data : prices) {
                if (data.getId() == -1) {
                    PriceData existing = dataByDate.get(new DateRange(data));
                    if (existing != null) {
                        if (!priceEquals(data, existing)) {
                            if (UNIT_PRICE.equals(data.getShortName())) {
                                throw new ProductIOException(DuplicateUnitPrice, data.getLine());
                            } else {
                                unique.add(data);
                            }
                        }
                    } else {
                        dataByDate.put(new DateRange(data), data);
                        unique.add(data);
                    }
                }
            }
        }

        for (PriceData data : unique) {
            boolean found = false;
            for (ProductPrice price : current) {
                if (equals(data, price)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.add(data);
            }
        }
        return result;
    }

    /**
     * Returns price data if it updates an existing price.
     *
     * @param product the product that the price belongs to
     * @param price   the price data to
     * @param prices  the existing prices
     * @throws ProductIOException if the price is a linked price or the price is a unit price and overlaps an existing
     *                            price
     */
    private PriceData getUpdatedPriceData(Product product, PriceData price, List<ProductPrice> prices) {
        PriceData result = null;
        ProductPrice existing = ProductImportHelper.getPrice(price, prices);
        if (isLinkedPrice(existing, product)) {
            if (!priceEquals(price, existing)) {
                throw new ProductIOException(CannotUpdateLinkedPrice, price.getLine());
            }
        } else if (!priceEquals(price, existing)) {
            if (UNIT_PRICE.equals(price.getShortName())) {
                checkOverlap(price, prices);
            }
            result = price;
        }
        return result;
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
     * Determines if a price is linked from a price template.
     *
     * @param price   the price
     * @param product the product
     * @return {@code true}  if the price is linked from a price template
     */
    private boolean isLinkedPrice(PriceData price, Product product) {
        for (ProductPrice productPrice : product.getProductPrices()) {
            if (productPrice.getId() == price.getId()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a unit price overlaps another price.
     *
     * @param price  the price to check
     * @param prices the prices to check against
     * @throws ProductIOException if the price overlaps
     */
    private void checkOverlap(PriceData price, List<ProductPrice> prices) {
        for (ProductPrice p : prices) {
            if (p.getId() != price.getId()
                && DateRules.intersects(p.getFromDate(), p.getToDate(), price.getFrom(), price.getTo())) {
                throw new ProductIOException(UnitPriceOverlap, price.getLine());
            }
        }
    }

    /**
     * Updates an existing price based on new price data, if required.
     *
     * @param newPrice the new price data
     * @param prices   the existing prices
     * @param changed  the prices that have been changed
     * @param product  the product
     * @param sameFrom indicates if all new prices have the same from date
     * @return {@code true} if a new price needs to be created for the new price data
     */
    private boolean updateExistingPrice(PriceData newPrice, List<PriceData> prices, List<PriceData> changed,
                                        Product product, boolean sameFrom) {
        boolean create;
        PriceData existing = getIntersectMatch(newPrice, prices);
        if (existing == null) {
            create = true;
        } else {
            boolean dateMatch = dateEquals(newPrice, existing);
            boolean priceMatch = priceEquals(newPrice, existing);
            if (dateMatch && priceMatch) {
                // the new price matches an existing price, but doesn't have an identifier. Ignore it.
                create = false;
            } else {
                // the price is different to the existing price
                if (isLinkedPrice(existing, product)) {
                    // the price is linked from a price template. These cannot be updated.
                    existing = null;
                } else if (UNIT_PRICE.equals(newPrice.getShortName())) {
                    if (dateMatch || existing.getTo() != null) {
                        // can't have unit prices overlapping
                        throw new ProductIOException(UnitPriceOverlap, newPrice.getLine());
                    }
                }
                create = true;
            }
        }
        if (create) {
            Date from = newPrice.getFrom();
            if (existing != null && existing.getTo() == null) {
                if (!sameFrom) {
                    throw new ProductIOException(CannotCloseExistingPrice, newPrice.getLine());
                }
                Date date = DateRules.getDate(from, -1, DateUnits.DAYS);
                if (existing.getTo() != null && DateRules.compareDates(existing.getTo(), date) > 0) {
                    throw new ProductIOException(FromDateGreaterThanToDate, newPrice.getLine());
                }
                existing.setTo(date);
                if (!changed.contains(existing)) {
                    changed.add(existing);
                }
            }
        }
        return create;
    }

    /**
     * Returns price data that is the merging of the updated prices and the existing prices.
     *
     * @param updated  the updated price data
     * @param existing the existing prices
     * @return the merged price data
     */
    private List<PriceData> merge(List<PriceData> updated, List<ProductPrice> existing) {
        List<PriceData> result = new ArrayList<PriceData>();
        for (ProductPrice productPrice : existing) {
            PriceData found = null;
            for (PriceData data : updated) {
                if (data.getId() == productPrice.getId()) {
                    found = data;
                    break;
                }
            }
            if (found != null) {
                result.add(found);
            } else {
                result.add(new PriceData(productPrice, service));
            }
        }
        return result;
    }

    /**
     * Determines if two prices are the same.
     *
     * @param data  the price data
     * @param price the price to compare with
     * @return {@code true} if they are the same
     */
    private boolean equals(PriceData data, ProductPrice price) {
        return priceEquals(data, price) && dateEquals(data, price);
    }

    /**
     * Determines if two prices match on date.
     * <p/>
     * Any time component is ignored.
     *
     * @param data  the price data
     * @param price the price to compare with
     * @return {@code true} if they match on from and to date
     */
    private boolean dateEquals(PriceData data, ProductPrice price) {
        return DateRules.dateEquals(data.getFrom(), price.getFromDate())
               && DateRules.dateEquals(data.getTo(), price.getToDate());
    }

    /**
     * Determines if two prices match on date.
     * <p/>
     * Any time component is ignored.
     *
     * @param data1 the price data
     * @param data2 the price to compare with
     * @return {@code true} if they match on from and to date
     */
    private boolean dateEquals(PriceData data1, PriceData data2) {
        return DateRules.dateEquals(data1.getFrom(), data2.getFrom())
               && DateRules.dateEquals(data1.getTo(), data2.getTo());
    }

    /**
     * Determines if the price and cost of two prices are the same.
     *
     * @param data1 the price data
     * @param data2 the price to compare with
     * @return {@code true} if the price and cost are the same in both. For fixed prices, also compares if default is
     *         the same
     */
    private boolean priceEquals(PriceData data1, PriceData data2) {
        return data2.getPrice().compareTo(data1.getPrice()) == 0 && data2.getCost().compareTo(data1.getCost()) == 0
               && (!FIXED_PRICE.equals(data1.getShortName()) || data1.isDefault() == data2.isDefault());
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
        return price.getPrice().compareTo(data.getPrice()) == 0 && cost.compareTo(data.getCost()) == 0
               && (!FIXED_PRICE.equals(data.getShortName())
                   || data.isDefault() == ProductImportHelper.isDefault(bean));
    }


    /**
     * Returns the price whose dates intersect those of the input price.
     * If there are multiple matches and:
     * <ul>
     * <li>the price is a fixed price, {@code null} will be returned.
     * Multiple fixed prices won't be updated</li>.
     * <li>the price is a unit price, an exception will be raised</li>
     * </ul>
     *
     * @param price  the input price
     * @param prices the prices
     * @return the corresponding price. May be {@code null}
     * @throws ProductIOException if multiple unit prices match
     */
    private PriceData getIntersectMatch(PriceData price, List<PriceData> prices) {
        Date from = price.getFrom();
        Date to = price.getTo();
        List<PriceData> matches = new ArrayList<PriceData>();
        for (PriceData other : prices) {
            if (other.getId() != price.getId() && DateRules.intersects(from, to, other.getFrom(), price.getTo())) {
                matches.add(other);
            }
        }
        if (matches.isEmpty()) {
            return null;
        } else if (matches.size() == 1) {
            return matches.get(0);
        } else if (UNIT_PRICE.equals(price.getShortName())) {
            throw new ProductIOException(UnitPriceOverlap, price.getLine());
        } else {
            // multiple fixed prices. This OK.
            return null;
        }
    }

    private static final class DateRange {
        private Date from;
        private Date to;

        private DateRange(PriceData data) {
            this.from = DateRules.getDate(data.getFrom());
            this.to = DateRules.getDate(data.getTo());

        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param obj the reference object with which to compare.
         * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof DateRange) {
                DateRange other = (DateRange) obj;
                return DateRules.compareTo(from, other.from) == 0 && DateRules.compareTo(to, other.to) == 0;
            }
            return false;
        }

        /**
         * Returns a hash code value for the object. This method is
         * supported for the benefit of hash tables such as those provided by
         * {@link HashMap}.
         *
         * @return a hash code value for this object.
         */
        @Override
        public int hashCode() {
            int fromHash = (from != null) ? from.hashCode() : 0;
            int toHash = (to != null) ? to.hashCode() : 0;
            return fromHash ^ toHash;
        }

    }

}
