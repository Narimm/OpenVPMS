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

package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.math.Currency;
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


/**
 * Editor for <em>productPrice.unitPrice</em> and
 * <em>productPrice.fixedPrice</em> product prices.
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
     * Constructs a {@link ProductPriceEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent product. May be {@code null}
     * @param layoutContext the layout context
     */
    public ProductPriceEditor(ProductPrice object, Product parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);

        costListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updatePrice();
            }
        };
        getProperty("cost").addModifiableListener(costListener);

        markupListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updatePrice();
            }
        };
        getProperty("markup").addModifiableListener(markupListener);

        priceListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updateMarkup();
            }
        };
        getProperty("price").addModifiableListener(priceListener);
        rules = ServiceHelper.getBean(ProductPriceRules.class);
    }

    /**
     * Refreshes the cost, markup and price fields.
     * <p/>
     * This should be invoked if the underlying object changes outside of the editor.
     * <p/>
     * Fields will not recalculate.
     */
    public void refresh() {
        Property cost = getProperty("cost");
        Property markup = getProperty("markup");
        Property price = getProperty("price");
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
     * Updates the price.
     */
    private void updatePrice() {
        try {
            Property property = getProperty("price");
            property.removeModifiableListener(priceListener);
            property.setValue(calculatePrice());
            property.addModifiableListener(priceListener);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Recalculates the markup when the price is updated.
     */
    private void updateMarkup() {
        try {
            Property property = getProperty("markup");
            property.removeModifiableListener(markupListener);
            property.setValue(calculateMarkup());
            property.addModifiableListener(markupListener);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Calculates the price using the following formula:
     * <p/>
     * {@code price = (cost * (1 + markup/100) ) * (1 + tax/100)}
     *
     * @return the price
     */
    private BigDecimal calculatePrice() {
        BigDecimal cost = getValue("cost");
        BigDecimal markup = getValue("markup");
        BigDecimal price = BigDecimal.ZERO;
        Product product = (Product) getParent();
        Context context = getLayoutContext().getContext();
        Party practice = context.getPractice();
        Currency currency = ContextHelper.getPracticeCurrency(context);

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
        BigDecimal cost = getValue("cost");
        BigDecimal price = getValue("price");
        Product product = (Product) getParent();
        Context context = getLayoutContext().getContext();
        Party practice = context.getPractice();
        if (product != null && practice != null) {
            markup = rules.getMarkup(product, cost, price, practice);
        }
        return markup;
    }

    /**
     * Returns the decimal value of a property.
     *
     * @param name the property name
     * @return the property value
     */
    private BigDecimal getValue(String name) {
        BigDecimal value = (BigDecimal) getProperty(name).getValue();
        return (value == null) ? BigDecimal.ZERO : value;
    }

}
