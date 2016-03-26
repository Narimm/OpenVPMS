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

package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Editor for <em>productPrice.unitPrice</em> and <em>productPrice.fixedPrice</em> product prices.
 *
 * @author Tim Anderson
 */
public class ProductPriceEditor extends AbstractIMObjectEditor {

    /**
     * The cost property listener.
     */
    private ModifiableListener costListener;

    /**
     * The price property listener.
     */
    private final ModifiableListener priceListener;

    /**
     * The markup property listener.
     */
    private final ModifiableListener markupListener;

    /**
     * Product price calculator.
     */
    private final ProductPriceRules rules;

    /**
     * The cost node.
     */
    private static final String COST = "cost";

    /**
     * The markup node.
     */
    private static final String MARKUP = "markup";

    /**
     * The price node.
     */
    private static final String PRICE = "price";

    /**
     * The max discount node.
     */
    private static final String MAX_DISCOUNT = "maxDiscount";

    /**
     * The practice, used to determine the tax rate.
     */
    private Party practice;

    /**
     * The practice currency.
     */
    private Currency currency;

    /**
     * Constructs a {@link ProductPriceEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent product. May be {@code null}
     * @param layoutContext the layout context
     */
    public ProductPriceEditor(ProductPrice object, Product parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);

        Context context = layoutContext.getContext();
        practice = context.getPractice();
        currency = ContextHelper.getPracticeCurrency(context);

        costListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updatePrice();
            }
        };
        getProperty(COST).addModifiableListener(costListener);

        markupListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updatePrice();
            }
        };
        getProperty(MARKUP).addModifiableListener(markupListener);

        priceListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateMarkup();
            }
        };
        getProperty(PRICE).addModifiableListener(priceListener);
        rules = ServiceHelper.getBean(ProductPriceRules.class);
    }

    /**
     * Returns the cost.
     *
     * @return the cost
     */
    public BigDecimal getCost() {
        return getProperty(COST).getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Sets the cost.
     * <p/>
     * The price will be recalculated using the cost and markup.
     *
     * @param cost the cost
     */
    public void setCost(BigDecimal cost) {
        getProperty(COST).setValue(cost);
    }

    /**
     * Returns the markup.
     *
     * @return the markup
     */
    public BigDecimal getMarkup() {
        return getProperty(MARKUP).getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Sets the markup.
     * <p/>
     * The price will be recalculated using the cost and markup.
     *
     * @param markup the markup
     */
    public void setMarkup(BigDecimal markup) {
        getProperty(MARKUP).setValue(markup);
    }

    /**
     * Returns the price.
     *
     * @return the price
     */
    public BigDecimal getPrice() {
        return getProperty(PRICE).getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Sets the price.
     * <p/>
     * The markup will be recalculated using the cost and price.
     *
     * @param price the price
     */
    public void setPrice(BigDecimal price) {
        getProperty(PRICE).setValue(price);
    }

    /**
     * Returns the maximum discount.
     *
     * @return the maximum discount
     */
    public BigDecimal getMaxDiscount() {
        return getProperty(MAX_DISCOUNT).getBigDecimal(BigDecimal.ZERO);
    }

    /**
     * Sets the date that the price is valid to.
     *
     * @param date the date. May be {@code nul}
     */
    public void setToDate(Date date) {
        getProperty("toDate").setValue(date);
    }

    /**
     * Refreshes the cost, markup and price fields.
     * <p/>
     * This should be invoked if the underlying object changes outside of the editor.
     * <p/>
     * Fields will not recalculate.
     */
    public void refresh() {
        Property cost = getProperty(COST);
        Property markup = getProperty(MARKUP);
        Property price = getProperty(PRICE);
        try {
            cost.removeModifiableListener(costListener);
            markup.removeModifiableListener(markupListener);
            price.removeModifiableListener(priceListener);
            price.refresh();
        } finally {
            cost.addModifiableListener(costListener);
            markup.addModifiableListener(markupListener);
            price.addModifiableListener(priceListener);
        }
    }

    /**
     * Updates the price and maximum discount.
     * <p/>
     * Note that the markup is also recalculated if the currency has a non-default minimum price set.
     * <p/>
     * This doesn't happen by default due to rounding errors.
     */
    private void updatePrice() {
        try {
            Property property = getProperty(PRICE);
            property.removeModifiableListener(priceListener);
            property.setValue(calculatePrice());
            property.addModifiableListener(priceListener);

            if (currencyHasNonDefaultMinimumPrice()) {
                // recalculate the markup as the price may have been rounded. if the minimum price is the same as
                // the default rounding amount, then don't recalculate as any difference is due to rounding error.
                updateMarkup();
            }
            updateMaxDiscount();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Recalculates the markup when the price is updated, and adjusts the maximum discount.
     */
    private void updateMarkup() {
        try {
            Property property = getProperty(MARKUP);
            property.removeModifiableListener(markupListener);
            property.setValue(calculateMarkup());
            property.addModifiableListener(markupListener);
            updateMaxDiscount();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Updates the maximum discount.
     */
    private void updateMaxDiscount() {
        Property property = getProperty(MAX_DISCOUNT);
        BigDecimal maxDiscount = property.getBigDecimal(BigDecimal.ZERO);
        property.setValue(calculateDiscount(maxDiscount));
    }

    /**
     * Calculates the price using the following formula:
     * <p/>
     * {@code price = (cost * (1 + markup/100) ) * (1 + tax/100)}
     *
     * @return the price
     */
    private BigDecimal calculatePrice() {
        BigDecimal cost = getCost();
        BigDecimal markup = getMarkup();
        BigDecimal price = BigDecimal.ZERO;
        Product product = (Product) getParent();

        if (product != null && practice != null && currency != null) {
            price = rules.getPrice(product, cost, markup, practice, currency);
        }
        return price;
    }

    /**
     * Calculates the markup using the following formula:
     * <p/>
     * {@code markup = ((price / (cost * ( 1 + tax/100))) - 1) * 100}
     *
     * @return the markup
     */
    private BigDecimal calculateMarkup() {
        BigDecimal markup = BigDecimal.ZERO;
        BigDecimal cost = getCost();
        BigDecimal price = getPrice();
        Product product = (Product) getParent();
        Context context = getLayoutContext().getContext();
        Party practice = context.getPractice();
        if (product != null && practice != null) {
            markup = rules.getMarkup(product, cost, price, practice);
        }
        return markup;
    }

    /**
     * Calculates the maximum discount.
     * <p/>
     * If the current maximum discount is zero, this is left unchanged, otherwise it is determined from the
     * current markup using {@link ProductPriceRules#calcMaxDiscount(BigDecimal)}.
     *
     * @param maxDiscount the current maximum discount
     * @return the new maximum discount
     */
    private BigDecimal calculateDiscount(BigDecimal maxDiscount) {
        BigDecimal result;
        if (maxDiscount.compareTo(BigDecimal.ZERO) == 0) {
            result = BigDecimal.ZERO;
        } else {
            BigDecimal markup = getMarkup();
            result = rules.calcMaxDiscount(markup);
        }
        return result;
    }

    /**
     * Determines if the currency has a non-default minimum price.
     *
     * @return {@code true} if the currency has a non-default minimum price
     */
    private boolean currencyHasNonDefaultMinimumPrice() {
        if (currency != null) {
            BigDecimal minimumPrice = currency.getMinimumPrice();
            return !MathRules.isZero(minimumPrice)
                   && !MathRules.equals(currency.getDefaultRoundingAmount(), minimumPrice);
        }
        return false;
    }
}
