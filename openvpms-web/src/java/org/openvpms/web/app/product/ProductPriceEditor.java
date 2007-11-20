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
                updateMarkup();
            }
        };
        getProperty("price").addModifiableListener(priceListener);
    }

    /**
     * Updates the price using the following formula:
     * <p/>
     * <tt>price = (cost price * (1 + markup/100) ) * (1 + tax/100)</tt>
     */
    private void updatePrice() {
        BigDecimal cost = getValue("cost");
        if (cost.compareTo(BigDecimal.ZERO) != 0) {
            Property property = getProperty("price");
            property.removeModifiableListener(priceListener);

            BigDecimal markup = getValue("markup");
            BigDecimal markupDec = getPercentRate(markup);
            Product product = (Product) getParent();
            BigDecimal taxDec = BigDecimal.ZERO;
            if (product != null) {
                TaxRules rules = new TaxRules();
                taxDec = getPercentRate(rules.getTaxRate(product));
            }
            BigDecimal price = cost.multiply(
                    BigDecimal.ONE.add(markupDec).multiply(
                            BigDecimal.ONE.add(taxDec)));
            property.setValue(price);
            property.addModifiableListener(priceListener);
        }
    }

    /**
     * Updates the markup using the following formula:
     * <p/>
     * <tt>((price / (cost * ( 1 + tax/100))) - 1) * 100
     */
    private void updateMarkup() {
        BigDecimal cost = getValue("cost");
        if (cost.compareTo(BigDecimal.ZERO) != 0) {
            Property property = getProperty("markup");
            property.removeModifiableListener(markupListener);

            BigDecimal price = getValue("price");
            Product product = (Product) getParent();
            BigDecimal taxDec = BigDecimal.ZERO;
            if (product != null) {
                TaxRules rules = new TaxRules();
                taxDec = getPercentRate(rules.getTaxRate(product));
            }
            BigDecimal markup = price.divide(
                    cost.multiply(BigDecimal.ONE.add(taxDec)), 3,
                    RoundingMode.HALF_UP).subtract(
                    BigDecimal.ONE).multiply(new BigDecimal(100));
            if (markup.compareTo(BigDecimal.ZERO) < 0) {
                markup = BigDecimal.ZERO;
            }
            property.setValue(markup);
            property.addModifiableListener(markupListener);
        }
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
