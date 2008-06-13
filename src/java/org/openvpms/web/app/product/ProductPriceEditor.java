/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.product;

import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ErrorHelper;

import java.math.BigDecimal;


/**
 * Editor for <em>productPrice.unitPrice</em> and
 * <em>productPrice.fixedPrice</em> product prices.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ProductPriceEditor extends AbstractIMObjectEditor {

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
     * Flag to indicate that the price has been modified and therefore the
     * markup needs to be recalculated on save. This is a workaround for
     * an echo2 bug. See OVPMS-701
     */
    private boolean recalcMarkup;


    /**
     * Constructs a new <tt>ProductPriceEditor</tt>.
     *
     * @param object        the object to edit
     * @param parent        the parent product. May be <tt>null</tt>
     * @param layoutContext the layout context. May be <tt>null</tt>.
     */
    public ProductPriceEditor(ProductPrice object, Product parent,
                              LayoutContext layoutContext) {
        super(object, parent, layoutContext);

        getProperty("cost").addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updatePrice();
            }
        });

        markupListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                updatePrice();
            }
        };
        getProperty("markup").addModifiableListener(markupListener);

        priceListener = new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                // updateMarkup();
                recalcMarkup = true;
            }
        };
        getProperty("price").addModifiableListener(priceListener);
        rules = new ProductPriceRules();
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    @Override
    protected boolean doSave() {
        if (recalcMarkup) {
            IMObjectBean bean = new IMObjectBean(getObject());
            bean.setValue("markup", calculateMarkup());
            recalcMarkup = false;
        }
        return super.doSave();
    }

    /**
     * Updates the price.
     */
    private void updatePrice() {
        recalcMarkup = false;
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
     * Not currently used due to echo2 bug. See OVPMS-701
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
     * <tt>price = (cost * (1 + markup/100) ) * (1 + tax/100)</tt>
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
     * <tt>markup = ((price / (cost * ( 1 + tax/100))) - 1) * 100</tt>
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
