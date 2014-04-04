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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer;

import org.openvpms.archetype.rules.finance.discount.DiscountRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.act.ActItemEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.FixedPriceEditor;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;

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
     * Constructs a {@link PriceActItemEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent act. May be {@code null}
     * @param context the layout context
     */
    public PriceActItemEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        Property fixedPrice = getProperty("fixedPrice");

        Product product = getProduct();
        fixedEditor = new FixedPriceEditor(fixedPrice, getPricingLocation(), context);
        fixedEditor.setProduct(product);
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
        if (!TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
            fixedEditor.setProduct(product);
        } else {
            fixedEditor.setProduct(null);
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
     * Returns the fixed price.
     *
     * @return the fixed price
     */
    protected BigDecimal getFixedPrice() {
        BigDecimal value = (BigDecimal) getProperty("fixedPrice").getValue();
        return (value != null) ? value : BigDecimal.ZERO;
    }

    /**
     * Calculates the discount amount, updating the 'discount' node.
     */
    protected void updateDiscount() {
        try {
            BigDecimal amount = calculateDiscount();
            // If discount amount calculates to zero don't update any
            // existing value as may have been manually modified.
            if (amount.compareTo(BigDecimal.ZERO) != 0) {
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
     * @return the discount, or {@code BigDecimal.ZERO} if the discount can't be calculated
     */
    protected BigDecimal calculateDiscount() {
        BigDecimal amount = BigDecimal.ZERO;
        Party customer = getCustomer();
        Party patient = getPatient();
        Product product = getProduct();

        if (customer != null && product != null
            && !TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
            BigDecimal fixedPrice = getFixedPrice();
            BigDecimal unitPrice = getUnitPrice();
            BigDecimal quantity = getQuantity();
            DiscountRules rules = new DiscountRules();
            Date startTime = getStartTime();
            if (startTime == null) {
                Act parent = (Act) getParent();
                startTime = parent.getActivityStartTime();
            }
            ProductPrice fixedProductPrice = getFixedProductPrice(product);
            ProductPrice unitProductPrice = getUnitProductPrice(product);
            amount = rules.calculateDiscount(startTime, customer, patient, product, fixedPrice,
                                             unitPrice, quantity, getMaxDiscount(fixedProductPrice),
                                             getMaxDiscount(unitProductPrice));
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
