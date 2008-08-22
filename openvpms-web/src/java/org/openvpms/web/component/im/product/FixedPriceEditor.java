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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.product;

import echopointng.DropDown;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.TextComponentFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;


/**
 * An editor for fixed prices that displays a drop-down of available prices
 * associated with the product.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class FixedPriceEditor extends AbstractPropertyEditor {

    /**
     * The product, used to select the fixed price.
     */
    private Product product;

    /**
     * The date, used to filter active prices.
     */
    private Date date;

    /**
     * The component.
     */
    private DropDown component;

    /**
     * The component focus group.
     */
    private FocusGroup focus;

    /**
     * Price rules, used to select fixed prices associated with the product.
     */
    private ProductPriceRules rules;


    /**
     * Creates a new <tt>FixedPriceEditor</tt>.
     *
     * @param property the fixed price property
     */
    public FixedPriceEditor(Property property) {
        super(property);

        date = new Date();

        TextField field = TextComponentFactory.createNumeric(property, 10);
        component = new DropDown();
        component.setTarget(field);
        component.setPopUpAlwaysOnTop(true);
        component.setFocusOnExpand(true);
        focus = new FocusGroup(property.getDisplayName());
        focus.add(field);
    }

    /**
     * Sets the product, used to select the fixed price.
     *
     * @param product the product. MAy be <tt>null</tt>
     */
    public void setProduct(Product product) {
        this.product = product;
        Component prices = getPrices();
        component.setPopUp(prices);
        component.setFocusComponent(prices);
    }

    /**
     * Sets the date. This determines when a price must be active.
     * Defaults to the current date.
     *
     * @param date the date
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focus;
    }

    /**
     * Invoked when a price is selected.
     *
     * @param price the selected price. May be <tt>null</tt>
     */
    private void onSelected(ProductPrice price) {
        if (price != null) {
            getProperty().setValue(price.getPrice());
        }
        component.setExpanded(false);
    }

    /**
     * Returns a component containing the product prices.
     *
     * @return the price component, or <tt>null</tt> if there are no prices
     */
    private Component getPrices() {
        Component result = null;
        if (product != null) {
            if (rules == null) {
                rules = new ProductPriceRules();
            }
            Set<ProductPrice> prices = rules.getProductPrices(
                    product, ProductArchetypes.FIXED_PRICE, date);
            if (!prices.isEmpty()) {
                result = createPriceTable(prices);
            }
        }
        return result;
    }

    /**
     * Creates a table of prices.
     *
     * @param prices the prices
     * @return a new price table
     */
    private PagedIMTable<ProductPrice> createPriceTable(
            Set<ProductPrice> prices) {
        ResultSet<ProductPrice> set = new IMObjectListResultSet<ProductPrice>(
                new ArrayList<ProductPrice>(prices), 20);
        final PagedIMTable<ProductPrice> table
                = new PagedIMTable<ProductPrice>(new PriceTableModel(), set);
        table.getTable().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onSelected(table.getTable().getSelected());
            }
        });
        return table;
    }

    /**
     * Table model that displays the name and price of an {@link ProductPrice}.
     */
    private static class PriceTableModel
            extends DescriptorTableModel<ProductPrice> {

        /**
         * The nodes to display.
         */
        private static final String[] NODES = new String[]{"name", "price"};

        /**
         * Creates a new <tt>PriceTableModel</tt>.
         */
        public PriceTableModel() {
            super(new String[]{ProductArchetypes.FIXED_PRICE});
        }

        /**
         * Returns a list of node descriptor names to include in the table.
         *
         * @return the list of node descriptor names to include in the table
         */
        @Override
        protected String[] getNodeNames() {
            return NODES;
        }

    }

}
