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

package org.openvpms.archetype.function.product;

import org.apache.commons.jxpath.Function;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.jxpath.AbstractObjectFunctions;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.ObjectRefSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

import java.math.BigDecimal;

/**
 * Product reporting functions.
 *
 * @author Tim Anderson
 */
public class ProductFunctions extends AbstractObjectFunctions {

    /**
     * The price rules.
     */
    private final ProductPriceRules priceRules;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link ProductFunctions}.
     *
     * @param priceRules      the product price rules
     * @param practiceService the practice service
     * @param service         the archetype service
     */
    public ProductFunctions(ProductPriceRules priceRules, PracticeService practiceService, IArchetypeService service) {
        super("product");
        setObject(this);
        this.priceRules = priceRules;
        this.practiceService = practiceService;
        this.service = service;
    }

    /**
     * Returns a price for a product.
     * <p/>
     * This will either be tax inclusive or tax exclusive, depending on the practice preference.
     *
     * @param productId  the product identifier
     * @param taxExPrice the tax-exclusive price
     * @return the product price
     */
    public BigDecimal priceById(long productId, BigDecimal taxExPrice) {
        BigDecimal result = taxExPrice != null ? taxExPrice : BigDecimal.ZERO;
        if (!MathRules.isZero(result)) {
            Product product = getProduct(productId);
            if (product != null) {
                result = price(product, taxExPrice);
            }
        }
        return result;
    }

    /**
     * Returns a price for a product.
     * <p/>
     * This will either be tax inclusive or tax exclusive, depending on the practice preference.
     *
     * @param productId    the product identifier
     * @param taxExPrice   the tax-exclusive price
     * @param taxInclusive if {@code true}, the returned price will include any taxes for the product, otherwise it will
     *                     be rounded according to the practice currency convention
     * @return the product price
     */
    public BigDecimal priceById(long productId, BigDecimal taxExPrice, boolean taxInclusive) {
        BigDecimal result = taxExPrice != null ? taxExPrice : BigDecimal.ZERO;
        if (!MathRules.isZero(result)) {
            if (taxInclusive) {
                Product product = getProduct(productId);
                if (product != null) {
                    result = price(product, taxExPrice, true);
                } else {
                    result = round(taxExPrice);
                }
            } else {
                result = round(taxExPrice);
            }
        }
        return result;
    }

    /**
     * Returns a price for a product.
     * <p/>
     * This will either be tax inclusive or tax exclusive, depending on the practice preference.
     *
     * @param product    the product
     * @param taxExPrice the tax-exclusive price
     * @return the product price
     */
    public BigDecimal price(Product product, BigDecimal taxExPrice) {
        BigDecimal result = taxExPrice != null ? taxExPrice : BigDecimal.ZERO;
        if (!MathRules.isZero(result) && product != null) {
            Party practice = practiceService.getPractice();
            if (practice != null) {
                boolean taxInclusive = isTaxInclusive(practice);
                result = getPrice(product, taxExPrice, taxInclusive, practice);
            }
        }
        return result;
    }

    /**
     * Returns a price for a product.
     *
     * @param product      the product
     * @param taxExPrice   the tax-exclusive price
     * @param taxInclusive if {@code true}, the returned price will include any taxes for the product, otherwise it will
     *                     be rounded according to the practice currency convention
     * @return the product price
     */
    public BigDecimal price(Product product, BigDecimal taxExPrice, boolean taxInclusive) {
        BigDecimal result = taxExPrice != null ? taxExPrice : BigDecimal.ZERO;
        if (!MathRules.isZero(result) && product != null) {
            Party practice = practiceService.getPractice();
            if (practice != null) {
                result = getPrice(product, taxExPrice, taxInclusive, practice);
            }
        }
        return result;
    }

    /**
     * Returns a Function, if any, for the specified namespace, name and parameter types.
     * <p/>
     * This version changes price -> priceById and taxRate -> taxRateById, if the first parameter is numeric.
     * <br/>
     * This is required as JXPath thinks the methods are ambiguous if they have the same name.
     *
     * @param namespace  if it is not the namespace specified in the constructor, the method returns null
     * @param name       is a function name.
     * @param parameters the function parameterss
     * @return a MethodFunction, or null if there is no such function.
     */
    @Override
    public Function getFunction(String namespace, String name, Object[] parameters) {
        if ("price".equals(name) && parameters.length >= 1 && parameters[0] instanceof Number) {
            name = "priceById";
        } else if ("taxRate".equals(name) && parameters.length == 1 && parameters[0] instanceof Number) {
            name = "taxRateById";
        }
        return super.getFunction(namespace, name, parameters);
    }

    /**
     * Returns the tax rate for a product.
     *
     * @param productId the product identifier
     * @return the tax rate for the product
     */
    public BigDecimal taxRateById(long productId) {
        Product product = getProduct(productId);
        return (product != null) ? taxRate(product) : BigDecimal.ZERO;
    }

    /**
     * Returns the tax rate for a product.
     *
     * @param product the product
     * @return the tax rate for the product
     */
    public BigDecimal taxRate(Product product) {
        BigDecimal result = BigDecimal.ZERO;
        Party practice = practiceService.getPractice();
        if (product != null && practice != null) {
            result = priceRules.getTaxRate(product, practice);
        }
        return result;
    }

    /**
     * Rounds a price according to the practice currency rounding convention.
     *
     * @param price the price to round
     * @return the rounded price
     */
    public BigDecimal round(BigDecimal price) {
        BigDecimal result = (price != null) ? price : BigDecimal.ZERO;
        Currency currency = practiceService.getCurrency();
        if (currency != null) {
            result = currency.round(price);
        }
        return result;
    }

    /**
     * Deterrmines if prices are displayed tax inclusive.
     *
     * @param practice the practice
     * @return {@code true} if prices are displayed tax inclusive
     */
    protected boolean isTaxInclusive(Party practice) {
        IMObjectBean bean = new IMObjectBean(practice, service);
        return bean.getBoolean("showPricesTaxInclusive", true);
    }

    /**
     * Returns a price for a product.
     * <p/>
     * This will either be tax inclusive or tax exclusive, depending on the practice preference.
     *
     * @param product      the product
     * @param taxExPrice   the tax-exclusive price
     * @param taxInclusive if {@code true}, the returned price will include any taxes for the product, otherwise it will
     *                     be rounded according to the practice currency convention
     * @param practice     the practice
     * @return the product price
     */
    private BigDecimal getPrice(Product product, BigDecimal taxExPrice, boolean taxInclusive, Party practice) {
        BigDecimal result = taxExPrice;
        Currency currency = practiceService.getCurrency();
        if (currency != null) {
            if (taxInclusive) {
                result = priceRules.getTaxIncPrice(taxExPrice, product, practice, currency);
            } else {
                result = currency.round(taxExPrice);
            }
        }
        return result;
    }

    /**
     * Returns a product given its identifier.
     *
     * @param productId the product identifier
     * @return the corresponding product, or {@code null} if none is found
     */
    private Product getProduct(long productId) {
        // use a 2 stage select to get the product, so that caches can be utilised
        ArchetypeQuery query = new ArchetypeQuery("product.*", false);
        query.getArchetypeConstraint().setAlias("product");
        query.add(Constraints.eq("id", productId));
        query.add(new ObjectRefSelectConstraint("product"));
        query.setMaxResults(1);
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
        if (iterator.hasNext()) {
            IMObjectReference reference = iterator.next().getReference("product.reference");
            return (Product) service.get(reference);
        }
        return null;
    }
}
