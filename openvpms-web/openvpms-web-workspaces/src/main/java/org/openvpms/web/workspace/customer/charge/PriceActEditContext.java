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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.finance.discount.DiscountRules;
import org.openvpms.archetype.rules.finance.tax.CustomerTaxRules;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.CachingReadOnlyArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.ProductHelper;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.DoseManager;
import org.openvpms.web.workspace.customer.PriceActItemEditor;

import java.math.BigDecimal;


/**
 * Edit context for {@link PriceActItemEditor}s, to enable them to share state.
 *
 * @author Tim Anderson
 */
public class PriceActEditContext {

    /**
     * The practice.
     */
    private final Party practice;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The product price rules.
     */
    private final ProductPriceRules priceRules;

    /**
     * The tax rules.
     */
    private final CustomerTaxRules taxRules;

    /**
     * The discount rules.
     */
    private final DiscountRules discountRules;

    /**
     * The practice currency.
     */
    private final Currency currency;

    /**
     * The dose manager. May be {@code null}
     */
    private DoseManager doseManager;

    /**
     * Constructs a {@link PriceActEditContext}.
     *
     * @param context the layout context. The context supply a practice
     */
    public PriceActEditContext(LayoutContext context) {
        this.practice = context.getContext().getPractice();
        if (practice == null) {
            throw new IllegalStateException("Context is missing the practice");
        }
        service = new CachingReadOnlyArchetypeService(context.getCache(), ServiceHelper.getArchetypeService());
        ILookupService lookups = ServiceHelper.getLookupService();
        priceRules = new ProductPriceRules(service);
        taxRules = new CustomerTaxRules(practice, service, lookups);
        discountRules = new DiscountRules(service, lookups);
        currency = ServiceHelper.getBean(PracticeRules.class).getCurrency(practice);
    }

    /**
     * Sets the dose manager.
     *
     * @param doseManager the dose manager. May be {@code null}
     */
    public void setDoseManager(DoseManager doseManager) {
        this.doseManager = doseManager;
    }

    /**
     * Returns the price of a product.
     * <p/>
     * This:
     * <ul>
     * <li>applies any service ratio to the price</li>
     * <li>subtracts any tax exclusions the customer may have</li>
     * </ul>
     *
     * @param price the price
     * @return the price, minus any tax exclusions
     */
    public BigDecimal getPrice(Product product, ProductPrice price, BigDecimal serviceRatio, Party customer) {
        BigDecimal amount = ProductHelper.getPrice(price, serviceRatio, currency);
        return taxRules.getTaxExAmount(amount, product, customer);
    }

    /**
     * Returns the dose of a product for a patient, based on the patient's weight.
     *
     * @param product the product
     * @param patient the patient
     * @return the dose, or {@code 0} if no dose exists for the patient weight or the {@link DoseManager} hasn't been
     * registered
     */
    public BigDecimal getDose(Product product, Party patient) {
        return doseManager != null ? doseManager.getDose(product, patient) : BigDecimal.ZERO;
    }

    /**
     * Returns a caching read-only archetype service, used to improve performance accessing common reference data.
     *
     * @return a caching archetype service
     */
    public IArchetypeService getCachingArchetypeService() {
        return service;
    }

    /**
     * Returns the practice.
     *
     * @return the practice
     */
    public Party getPractice() {
        return practice;
    }

    /**
     * Returns the practice currency.
     *
     * @return the practice currency
     */
    public Currency getCurrency() {
        return currency;
    }

    /**
     * Returns the discount rules.
     *
     * @return rthe discount rules
     */
    public DiscountRules getDiscountRules() {
        return discountRules;
    }

    /**
     * Returns the product price rules.
     *
     * @return the product price rules
     */
    public ProductPriceRules getPriceRules() {
        return priceRules;
    }

    /**
     * Returns the tax rules.
     *
     * @return the tax rules
     */
    public CustomerTaxRules getTaxRules() {
        return taxRules;
    }
}
