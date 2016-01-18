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

package org.openvpms.archetype.rules.finance.tax;

import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;


/**
 * Tax Rules.
 *
 * @author Tim Anderson
 */
public class TaxRules {

    /**
     * The practice tax rates classifications.
     */
    private Collection<Lookup> practiceTaxRates;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link TaxRules}.
     *
     * @param practice the practice, for default tax classifications
     * @param service  the archetype service
     */
    public TaxRules(Party practice, IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(practice, service);
        practiceTaxRates = Collections.unmodifiableList(bean.getValues("taxes", Lookup.class));
        this.service = service;
    }

    /**
     * Returns the tax rate of a product, expressed as a percentage.
     *
     * @param product the product
     * @return the tax rate
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal getTaxRate(Product product) {
        Collection<Lookup> rates = getProductTaxRates(product);
        return getTaxRate(rates);
    }

    /**
     * Calculates the tax for an amount using the tax rates associated with
     * a product.
     *
     * @param amount    the amount
     * @param product   the product
     * @param inclusive if {@code true} the amount is tax inclusive, otherwise it is tax exclusive
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal calculateTax(BigDecimal amount, Product product, boolean inclusive) {
        return calculateTax(amount, getProductTaxRates(product), inclusive);
    }

    /**
     * Calculates the tax for an amount, given a list of tax rate
     * classifications.
     *
     * @param amount    the amount
     * @param taxRates  the tax rate classifications
     * @param inclusive if {@code true} the amount is tax inclusive, otherwise it is tax exclusive
     * @return the tax on the amount
     * @throws ArchetypeServiceException for any archetype service error
     */
    public BigDecimal calculateTax(BigDecimal amount, Collection<Lookup> taxRates, boolean inclusive) {
        BigDecimal rate = getTaxRate(taxRates);
        BigDecimal tax = amount.multiply(rate);
        BigDecimal divisor = BigDecimal.valueOf(100);
        if (inclusive) {
            divisor = divisor.add(rate);
        }
        tax = MathRules.divide(tax, divisor, 3);
        return tax;
    }

    /**
     * Returns a list of taxes for a product.
     * <p/>
     * If the product has no taxType classifications, it returns any taxType classifications for the
     * entity.productType associated with the product.
     * <p/>
     * If there are no taxType classifications associated with the product type, returns any taxType classifications
     * associated with the practice.
     *
     * @param product the product
     * @return a list of taxes for the product
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Collection<Lookup> getProductTaxRates(Product product) {
        EntityBean bean = new EntityBean(product, service);
        Collection<Lookup> taxes = new HashSet<>();
        taxes.addAll(bean.getValues("taxes", Lookup.class));
        if (taxes.isEmpty()) {
            Entity productType = bean.getNodeTargetEntity("type");
            if (productType != null) {
                taxes.addAll(getProductTypeTaxRates(productType));
            }
        }
        if (taxes.isEmpty()) {
            taxes = getPracticeTaxRates();
        }
        return taxes;
    }

    /**
     * Returns the tax rate, expressed as a percentage.
     *
     * @param taxRates the tax rate classifications
     * @return the tax rate
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected BigDecimal getTaxRate(Collection<Lookup> taxRates) {
        BigDecimal result = BigDecimal.ZERO;
        for (IMObject taxRate : taxRates) {
            IMObjectBean taxBean = new IMObjectBean(taxRate, service);
            BigDecimal rate = taxBean.getBigDecimal("rate", BigDecimal.ZERO);
            result = result.add(rate);
        }
        return result;
    }

    /**
     * Returns a list of taxes for the practice.
     *
     * @return a list of taxes for the practice
     */
    protected Collection<Lookup> getPracticeTaxRates() {
        return practiceTaxRates;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Returns any tax rates associated with an <em>entity.productType</em>.
     *
     * @param productType the product type
     * @return a list of tax rates associated with the product type
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Collection<Lookup> getProductTypeTaxRates(Entity productType) {
        IMObjectBean bean = new IMObjectBean(productType, service);
        return bean.getValues("taxes", Lookup.class);
    }

}
