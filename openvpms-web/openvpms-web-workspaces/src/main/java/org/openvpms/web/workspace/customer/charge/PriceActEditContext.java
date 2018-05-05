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

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.finance.discount.DiscountRules;
import org.openvpms.archetype.rules.finance.tax.CustomerTaxRules;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.stock.StockRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.CachingReadOnlyArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.CustomerPricingContext;
import org.openvpms.web.component.im.product.PricingContext;
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
     * The practice location.
     */
    private final Party location;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The tax rules.
     */
    private final CustomerTaxRules taxRules;

    /**
     * The location rules.
     */
    private final LocationRules locationRules;

    /**
     * The discount rules.
     */
    private final DiscountRules discountRules;

    /**
     * Stock rules.
     */
    private final StockRules stockRules;

    /**
     * The dose manager. May be {@code null}
     */
    private DoseManager doseManager;


    private final boolean disableDiscounts;

    /**
     * Determines if products must be present at the location in order to select them.
     */
    private final boolean useLocationProducts;

    /**
     * Determines if minimum quantity restrictions are in place.
     */
    private final boolean useMinimumQuantities;

    /**
     * Determines if minimum quantities can be overriden by the user.
     */
    private final boolean overrideMinimumQuantity;

    /**
     * The product pricer.
     */
    private final PricingContext pricingContext;

    /**
     * Constructs a {@link PriceActEditContext}.
     *
     * @param customer the customer
     * @param location the practice location. May be {@code null}
     * @param context  the layout context. The context must supply a practice
     */
    public PriceActEditContext(Party customer, Party location, LayoutContext context) {
        this.practice = context.getContext().getPractice();
        if (practice == null) {
            throw new IllegalStateException("Context is missing the practice");
        }
        this.location = location;
        disableDiscounts = getDisableDiscounts(location);
        useLocationProducts = ProductHelper.useLocationProducts(practice);
        IMObjectBean bean = new IMObjectBean(practice);
        useMinimumQuantities = bean.getBoolean("minimumQuantities", false);
        if (useMinimumQuantities) {
            User user = context.getContext().getUser();
            String userType = bean.getString("minimumQuantitiesOverride");
            overrideMinimumQuantity = userType != null && ServiceHelper.getBean(UserRules.class).isA(user, userType);
        } else {
            overrideMinimumQuantity = false;
        }
        service = new CachingReadOnlyArchetypeService(context.getCache(), ServiceHelper.getArchetypeService());
        ProductPriceRules priceRules = new ProductPriceRules(service);
        taxRules = new CustomerTaxRules(practice, service);
        discountRules = new DiscountRules(service);
        stockRules = new StockRules(service);
        Currency currency = ServiceHelper.getBean(PracticeRules.class).getCurrency(practice);
        locationRules = ServiceHelper.getBean(LocationRules.class);
        pricingContext = new CustomerPricingContext(customer, location, currency, priceRules, locationRules, taxRules);
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
     * Returns the pricing context.
     *
     * @return the pricing context
     */
    public PricingContext getPricingContext() {
        return pricingContext;
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
    public BigDecimal getPrice(Product product, ProductPrice price) {
        return pricingContext.getPrice(product, price);
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
     * Returns the practice location.
     *
     * @return the practice location. May be {@code null}
     */
    public Party getLocation() {
        return location;
    }

    /**
     * Returns the stock location.
     *
     * @return the stock location. May be {@code null}
     */
    public Party getStockLocation() {
        return location != null ? locationRules.getDefaultStockLocation(location) : null;
    }

    /**
     * Returns the stock location associated with a product.
     *
     * @param product the product
     * @return the stock location. May be {@code null}
     */
    public Party getStockLocation(Product product) {
        return (location != null) ? stockRules.getStockLocation(product, location) : null;
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
     * Returns the discount rules.
     *
     * @return rthe discount rules
     */
    public DiscountRules getDiscountRules() {
        return discountRules;
    }

    /**
     * Returns the tax rules.
     *
     * @return the tax rules
     */
    public CustomerTaxRules getTaxRules() {
        return taxRules;
    }

    /**
     * Returns the stock rules.
     *
     * @return the stock rules
     */
    public StockRules getStockRules() {
        return stockRules;
    }

    /**
     * Determines if discounts are disabled at the practice location.
     *
     * @return {@code true} if discounts are disabled at the practice location
     */
    public boolean disableDiscounts() {
        return disableDiscounts;
    }

    /**
     * Determines if products must be present at the location in order to select them.
     *
     * @return {@code true} if if products must be present at the location in order to select them
     */
    public boolean useLocationProducts() {
        return useLocationProducts;
    }

    /**
     * Determines if minimum quantity restrictions are in place.
     *
     * @return {@code true} if minimum quantity restrictions are in place
     */
    public boolean useMinimumQuantities() {
        return useMinimumQuantities;
    }

    /**
     * Determines if the user can override minimum quantities.
     *
     * @return {@code true} if the user can override minimum quantities
     */
    public boolean overrideMinimumQuantities() {
        return overrideMinimumQuantity;
    }

    /**
     * Determines if discounts are disabled at a  practice location.
     *
     * @param location the location. May be {@code null}
     * @return {@code true} if discounts are disabled
     */
    private boolean getDisableDiscounts(Party location) {
        boolean result = false;
        if (location != null) {
            IMObjectBean bean = new IMObjectBean(location);
            result = bean.getBoolean("disableDiscounts");
        }
        return result;
    }

}
