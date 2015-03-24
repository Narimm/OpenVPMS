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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer;

import org.openvpms.archetype.rules.finance.discount.DiscountRules;
import org.openvpms.archetype.rules.finance.tax.CustomerTaxRules;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.act.ActItemEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.FixedPriceEditor;
import org.openvpms.web.component.im.product.ProductHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.Date;


/**
 * An editor for {@link Act}s that have fixed and unit prices.
 *
 * @author Tim Anderson
 */
public abstract class PriceActItemEditor extends ActItemEditor {

    /**
     * Fixed price node editor.
     */
    private FixedPriceEditor fixedEditor;

    /**
     * The unit price.
     */
    private ProductPrice unitProductPrice;

    /**
     * The practice.
     */
    private final Party practice;

    /**
     * The practice currency, used for rounding.
     */
    private final Currency currency;

    /**
     * If {@code true}, disable discounts.
     */
    private boolean disableDiscounts;

    /**
     * The service ratio.
     */
    private BigDecimal serviceRatio;

    /**
     * The product price rules.
     */
    private final ProductPriceRules priceRules;

    /**
     * The discount rules.
     */
    private final DiscountRules discountRules;

    /**
     * Customer tax rules.
     */
    private final CustomerTaxRules taxRules;

    /**
     * Constructs a {@link PriceActItemEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent act. May be {@code null}
     * @param context the layout context
     */
    public PriceActItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);

        practice = context.getContext().getPractice();
        priceRules = ServiceHelper.getBean(ProductPriceRules.class);
        taxRules = new CustomerTaxRules(practice, ServiceHelper.getArchetypeService(),
                                        ServiceHelper.getLookupService());
        discountRules = ServiceHelper.getBean(DiscountRules.class);
        currency = ServiceHelper.getBean(PracticeRules.class).getCurrency(practice);

        Product product = getProduct();
        Party location = getLocation();
        serviceRatio = getServiceRatio(product, location);

        Property fixedPrice = getProperty("fixedPrice");

        fixedEditor = new FixedPriceEditor(fixedPrice, getPricingGroup(), currency);
        fixedEditor.setProduct(product, serviceRatio);
    }

    /**
     * Returns the fixed price.
     *
     * @return the fixed price
     */
    public BigDecimal getFixedPrice() {
        return getProperty("fixedPrice").getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Returns the service ratio.
     *
     * @return the service ratio
     */
    public BigDecimal getServiceRatio() {
        return serviceRatio;
    }

    /**
     * Save any edits.
     * <p/>
     * This implementation saves the current object before children, to ensure deletion of child acts
     * don't result in StaleObjectStateException exceptions.
     * <p/>
     * This implementation will throw an exception if the product is an <em>product.template</em>.
     * Ideally, the act would be flagged invalid if this is the case, but template expansion only works for valid
     * acts. TODO
     *
     * @return {@code true} if the save was successful
     * @throws IllegalStateException if the product is a template
     */
    @Override
    protected boolean doSave() {
        if (TypeHelper.isA(getProductRef(), ProductArchetypes.TEMPLATE)) {
            Product product = getProduct();
            String name = product != null ? product.getName() : null;
            throw new IllegalStateException("Cannot save with product template: " + name);
        }
        boolean saved = saveObject();
        if (saved) {
            saved = saveChildren();
        }
        return saved;
    }

    /**
     * Invoked when the product is changed.
     *
     * @param product the product. May be {@code null}
     */
    @Override
    protected void productModified(Product product) {
        serviceRatio = getServiceRatio(product, getLocation());
        if (!TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
            fixedEditor.setProduct(product, serviceRatio);
        } else {
            fixedEditor.setProduct(null, BigDecimal.ONE);
        }
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return createLayoutStrategy(fixedEditor);
    }

    /**
     * Creates the layout strategy.
     *
     * @param fixedPrice the fixed price editor
     * @return a new layout strategy
     */
    protected IMObjectLayoutStrategy createLayoutStrategy(FixedPriceEditor fixedPrice) {
        return new PriceItemLayoutStrategy(fixedPrice);
    }

    /**
     * Returns the fixed cost.
     *
     * @return the fixed cost
     */
    protected BigDecimal getFixedCost() {
        return getProperty("fixedCost").getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Returns the unit cost.
     *
     * @return the unit cost
     */
    protected BigDecimal getUnitCost() {
        return getProperty("unitCost").getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Determines if discounting should be disabled.
     *
     * @param disable if {@code true} disable discounts
     */
    protected void setDisableDiscounts(boolean disable) {
        disableDiscounts = disable;
    }

    /**
     * Determines if discounting has been disabled.
     *
     * @return {@code true} if discounts are disabled
     */
    protected boolean getDisableDiscounts() {
        return disableDiscounts;
    }

    /**
     * Returns the maximum discount allowed on the fixed price.
     *
     * @return the maximum discount
     */
    protected BigDecimal getFixedPriceMaxDiscount() {
        Product product = getProduct();
        BigDecimal result;
        if (product != null) {
            ProductPrice price = getFixedProductPrice(product);
            result = getMaxDiscount(price);
        } else {
            result = BigDecimal.ZERO;
        }
        return result;
    }

    /**
     * Returns the maximum discount allowed on the unit price.
     *
     * @return the maximum discount
     */
    protected BigDecimal getUnitPriceMaxDiscount() {
        Product product = getProduct();
        BigDecimal result;
        if (product != null) {
            ProductPrice price = getUnitProductPrice(product);
            result = getMaxDiscount(price);
        } else {
            result = BigDecimal.ZERO;
        }
        return result;
    }

    /**
     * Calculates the discount amount, updating the 'discount' node.
     * <p/>
     * If discounts are disabled, any existing discount will be set to {@code 0}.
     */
    protected void updateDiscount() {
        try {
            BigDecimal amount = calculateDiscount();
            // If discount amount calculates to zero don't update any
            // existing value as may have been manually modified.
            if (disableDiscounts || amount.compareTo(BigDecimal.ZERO) != 0) {
                Property discount = getProperty("discount");
                discount.setValue(amount);
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Calculates the discount.
     *
     * @return the discount or {@code BigDecimal.ZERO} if the discount can't be calculated or discounts are disabled
     */
    protected BigDecimal calculateDiscount() {
        BigDecimal unitPrice = getUnitPrice();
        BigDecimal quantity = getQuantity();
        return calculateDiscount(unitPrice, quantity);
    }

    /**
     * Calculates the discount.
     *
     * @param unitPrice the unit price
     * @param quantity  the quantity
     * @return the discount or {@code BigDecimal.ZERO} if the discount can't be calculated or discounts are disabled
     */
    protected BigDecimal calculateDiscount(BigDecimal unitPrice, BigDecimal quantity) {
        BigDecimal amount = BigDecimal.ZERO;
        if (!disableDiscounts) {
            Party customer = getCustomer();
            Party patient = getPatient();
            Product product = getProduct();

            if (customer != null && product != null && !TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
                BigDecimal fixedCost = getFixedCost();
                BigDecimal unitCost = getUnitCost();
                BigDecimal fixedPrice = getFixedPrice();
                BigDecimal fixedPriceMaxDiscount = getFixedPriceMaxDiscount();
                BigDecimal unitPriceMaxDiscount = getUnitPriceMaxDiscount();
                Date startTime = getStartTime();
                if (startTime == null) {
                    Act parent = (Act) getParent();
                    startTime = parent.getActivityStartTime();
                }
                amount = discountRules.calculateDiscount(startTime, practice, customer, patient, product,
                                                         fixedCost, unitCost, fixedPrice, unitPrice, quantity,
                                                         fixedPriceMaxDiscount, unitPriceMaxDiscount);
            }
        }
        return amount;
    }

    /**
     * Returns the default fixed product price for the specified product.
     *
     * @param product the product
     * @return the product price, or {@code null} if none is found
     */
    protected ProductPrice getDefaultFixedProductPrice(Product product) {
        return getProductPrice(ProductArchetypes.FIXED_PRICE, product);
    }

    /**
     * Returns the fixed product price for the specified product.
     *
     * @param product the product
     * @return the product price, or {@code null} if none is found
     */
    protected ProductPrice getFixedProductPrice(Product product) {
        ProductPrice result = fixedEditor.getProductPrice();
        result = getProductPrice(product, ProductArchetypes.FIXED_PRICE, result, getFixedPrice());
        fixedEditor.setProductPrice(result);
        return result;
    }

    /**
     * Returns the default unit product price for the specified product.
     *
     * @param product the product
     * @return the product price, or {@code null} if none is found
     */
    protected ProductPrice getDefaultUnitProductPrice(Product product) {
        return getProductPrice(ProductArchetypes.UNIT_PRICE, product);
    }

    /**
     * Returns the unit product price for the specified product.
     *
     * @param product the product
     * @return the product price, or {@code null} if none is found
     */
    protected ProductPrice getUnitProductPrice(Product product) {
        unitProductPrice = getProductPrice(product, ProductArchetypes.UNIT_PRICE, unitProductPrice, getUnitPrice());
        return unitProductPrice;
    }

    /**
     * Determines the service ratio for a product at a practice location.
     *
     * @param product  the product. May be {@code null}
     * @param location the practice location. May be {@code null}
     * @return the service ratio
     */
    protected BigDecimal getServiceRatio(Product product, Party location) {
        BigDecimal result = BigDecimal.ONE;
        if (product != null && location != null) {
            result = priceRules.getServiceRatio(product, location);
        }
        return result;
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
    protected BigDecimal getPrice(Product product, ProductPrice price) {
        BigDecimal amount = ProductHelper.getPrice(price, getServiceRatio(), currency);
        return taxRules.getTaxExAmount(amount, product, getCustomer());
    }

    /**
     * Calculate the amount of tax for the act using tax type information for the product, product type, organisation
     * and customer associated with the act.
     * The tax amount will be calculated and stored in the tax node for the act.
     *
     * @param customer the customer
     * @return the amount of tax for the act
     */
    protected BigDecimal calculateTax(Party customer) {
        FinancialAct act = (FinancialAct) getObject();
        return taxRules.calculateTax(act, customer);
    }

    /**
     * Helper to return a product price for a product.
     *
     * @param product   the product
     * @param shortName the product price archetype short name
     * @param current   the current product price. May be {@code null}
     * @param price     the current price
     * @return {@code current} if it matches the specified product and price;
     *         or the first matching product price associated with the product,
     *         or {@code null} if none is found
     */
    private ProductPrice getProductPrice(Product product, String shortName, ProductPrice current, BigDecimal price) {
        ProductPrice result = null;
        if (current != null && current.getProduct().equals(product)) {
            BigDecimal defaultValue = current.getPrice();
            if (price.compareTo(defaultValue) == 0) {
                result = current;
            }
        }
        if (result == null) {
            if (price.compareTo(BigDecimal.ZERO) == 0) {
                result = getProductPrice(shortName, product);
            } else {
                result = getProductPrice(shortName, price, product);
            }
        }
        return result;
    }

    /**
     * Determines if discounts are disabled for a practice location.
     *
     * @param location the practice location. May be {@code null}
     * @return {@code true} if discounts are disabled
     */
    protected boolean getDisableDiscounts(Party location) {
        boolean result = false;
        if (location != null) {
            IMObjectBean bean = new IMObjectBean(location);
            result = bean.getBoolean("disableDiscounts");
        }
        return result;
    }

    /**
     * Layout strategy that includes the fixed price editor.
     */
    protected class PriceItemLayoutStrategy extends LayoutStrategy {

        public PriceItemLayoutStrategy(FixedPriceEditor editor) {
            addComponent(new ComponentState(fixedEditor.getComponent(), fixedEditor.getProperty(),
                                            fixedEditor.getFocusGroup()));
            // need to register the editor
            getEditors().add(editor);
        }
    }

}
