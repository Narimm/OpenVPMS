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

import org.openvpms.archetype.rules.finance.tax.TaxRules;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;

import java.math.BigDecimal;
import java.math.RoundingMode;


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
     * Updates the price using the following formula:
     * <p/>
     * <tt>price = (cost * (1 + markup/100) ) * (1 + tax/100)</tt>
     */
    private void updatePrice() {
        recalcMarkup = false;
        Property property = getProperty("price");
        property.removeModifiableListener(priceListener);
        property.setValue(calculatePrice());
        property.addModifiableListener(priceListener);
    }

    /**
     * Recalculates the markup when the price is updated.
     * Not currently used due to echo2 bug. See OVPMS-701
     */
    private void updateMarkup() {
        Property property = getProperty("markup");
        property.removeModifiableListener(markupListener);
        property.setValue(calculateMarkup());
        property.addModifiableListener(markupListener);
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
        BigDecimal price = BigDecimal.ZERO;
        if (cost.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal markup = getValue("markup");
            BigDecimal markupDec = getPercentRate(markup);
            Product product = (Product) getParent();
            BigDecimal taxDec = BigDecimal.ZERO;
            if (product != null) {
                TaxRules rules = new TaxRules();
                taxDec = getPercentRate(rules.getTaxRate(product));
            }
            price = cost.multiply(
                    BigDecimal.ONE.add(markupDec)).multiply(
                    BigDecimal.ONE.add(taxDec));
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
        if (cost.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal price = getValue("price");
            Product product = (Product) getParent();
            BigDecimal taxDec = BigDecimal.ZERO;
            if (product != null) {
                TaxRules rules = new TaxRules();
                taxDec = getPercentRate(rules.getTaxRate(product));
            }
            markup = price.divide(
                    cost.multiply(BigDecimal.ONE.add(taxDec)), 3,
                    RoundingMode.HALF_UP).subtract(
                    BigDecimal.ONE).multiply(new BigDecimal(100));
            if (markup.compareTo(BigDecimal.ZERO) < 0) {
                markup = BigDecimal.ZERO;
            }
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

    /**
     * Returns a percentage / 100.
     *
     * @param percent the percent
     * @return <tt>percent / 100 </tt>
     */
    private BigDecimal getPercentRate(BigDecimal percent) {
        if (percent.compareTo(BigDecimal.ZERO) != 0) {
            return percent.divide(new BigDecimal(100));
        }
        return BigDecimal.ZERO;
    }

}
