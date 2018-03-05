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

package org.openvpms.web.component.im.product;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.NumericPropertyTransformer;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


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
     * The tax-inclusive price listener.
     */
    private final ModifiableListener taxIncListener;

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
    private final Party practice;

    /**
     * The practice currency.
     */
    private final Currency currency;

    /**
     * The tax-inclusive price.
     */
    private final Property taxIncPrice = new SimpleProperty("taxIncPrice", BigDecimal.ZERO, BigDecimal.class,
                                                            Messages.get("product.price.taxinc"));

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

        // allow entry of cost and price to 3 decimal places.
        costListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updatePrice();
            }
        };
        Property cost = getProperty(COST);
        cost.setTransformer(new NumericPropertyTransformer(cost, false, 3));
        cost.addModifiableListener(costListener);

        markupListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updatePrice();
            }
        };
        getProperty(MARKUP).addModifiableListener(markupListener);

        priceListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onPriceChanged();
            }
        };
        Property price = getProperty(PRICE);
        price.addModifiableListener(priceListener);
        price.setTransformer(new NumericPropertyTransformer(price, false, 3));
        rules = ServiceHelper.getBean(ProductPriceRules.class);
        taxIncListener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                updatePriceFromTaxInclusivePrice();
            }
        };
        taxIncPrice.addModifiableListener(taxIncListener);
        updateTaxInclusivePrice();
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
     * Returns the tax-exclusive price.
     *
     * @return the tax-exclusive price
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
     * Sets the price, inclusive of tax.
     * <p/>
     * The {@link #getPrice() tax-exclusive} price will be updated.
     *
     * @param price the tax-inclusive price
     */
    public void setTaxInclusivePrice(BigDecimal price) {
        taxIncPrice.setValue(price);
    }

    /**
     * Returns the price, inclusive of tax.
     *
     * @return the tax-inclusive price
     */
    public BigDecimal getTaxInclusivePrice() {
        return taxIncPrice.getBigDecimal(BigDecimal.ZERO);
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
            updateTaxInclusivePrice();
        } finally {
            cost.addModifiableListener(costListener);
            markup.addModifiableListener(markupListener);
            price.addModifiableListener(priceListener);
        }
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy();
    }

    /**
     * Updates the price, maximum discount and tax-inclusive price.
     * <p/>
     * Note that the markup is also recalculated if the currency has a non-default minimum price set.
     * <p/>
     * This doesn't happen by default due to rounding errors.
     */
    private void updatePrice() {
        try {
            Property property = getProperty(PRICE);
            property.removeModifiableListener(priceListener);
            property.setValue(calculateTaxExPrice());
            property.addModifiableListener(priceListener);

            if (currencyHasNonDefaultMinimumPrice()) {
                // recalculate the markup as the price may have been rounded. If the minimum price is the same as
                // the default rounding amount, then don't recalculate as any difference is due to rounding error.
                onPriceChanged();
            }
            updateMaxDiscount();
            updateTaxInclusivePrice();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the price changes.
     * <p/>
     * Recalculates the markup and tax-inclusive price, adjusts the maximum discount.
     */
    private void onPriceChanged() {
        try {
            Property property = getProperty(MARKUP);
            property.removeModifiableListener(markupListener);
            property.setValue(calculateMarkup());
            property.addModifiableListener(markupListener);
            updateMaxDiscount();
            updateTaxInclusivePrice();
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
     * Calculates the tax-exclusive price using the following formula:
     * <p/>
     * {@code taxExPrice = cost * (1 + markup/100)}
     *
     * @return the price
     */
    private BigDecimal calculateTaxExPrice() {
        BigDecimal cost = getCost();
        BigDecimal markup = getMarkup();
        BigDecimal price = BigDecimal.ZERO;
        if (currency != null) {
            price = rules.getTaxExPrice(cost, markup);
        }
        return price;
    }

    /**
     * Calculates the markup using the following formula:
     * <p/>
     * {@code markup = (price/cost - 1) * 100}
     *
     * @return the markup
     */
    private BigDecimal calculateMarkup() {
        BigDecimal cost = getCost();
        BigDecimal price = getPrice();
        return rules.getMarkup(cost, price);
    }

    /**
     * Calculates the maximum discount.
     * <p/>
     * If the current maximum discount is zero, this is left unchanged, otherwise it is determined from the
     * current markup using {@link ProductPriceRules#getMaxDiscount(BigDecimal)}.
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
            result = rules.getMaxDiscount(markup);
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

    /**
     * Calculates the tax-inclusive price from the price.
     */
    private void updateTaxInclusivePrice() {
        Product product = (Product) getParent();
        if (product != null && practice != null && currency != null) {
            BigDecimal price = rules.getTaxIncPrice(getPrice(), product, practice, currency);
            taxIncPrice.removeModifiableListener(taxIncListener);
            taxIncPrice.setValue(price);
            taxIncPrice.addModifiableListener(taxIncListener);
        }
    }

    /**
     * Calculates the tax-exclusive price from the tax-inclusive price, then recalculates the tax-inclusive price
     * to take into account any rounding and minimum currency amounts.
     */
    private void updatePriceFromTaxInclusivePrice() {
        Product product = (Product) getParent();
        if (product != null && practice != null && currency != null) {
            BigDecimal price = rules.getTaxExPrice(taxIncPrice.getBigDecimal(BigDecimal.ZERO), product, practice);
            setPrice(price);
            // now recalculate the tax-inc price from the tax ex, as this reflects what the use will see
            updateTaxInclusivePrice();
        }
    }

    private class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Lays out child components in a grid.
         *
         * @param object     the object to lay out
         * @param parent     the parent object. May be {@code null}
         * @param properties the properties
         * @param container  the container to use
         * @param context    the layout context
         */
        @Override
        protected void doSimpleLayout(IMObject object, IMObject parent, List<Property> properties, Component container,
                                      LayoutContext context) {
            ArchetypeNodes.insert(properties, "price", taxIncPrice);
            super.doSimpleLayout(object, parent, properties, container, context);
        }

    }
}
