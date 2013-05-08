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
 */

package org.openvpms.web.component.im.product;

import echopointng.DropDown;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.TextComponentFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import static org.openvpms.archetype.rules.product.ProductArchetypes.FIXED_PRICE;


/**
 * An editor for fixed prices that displays a drop-down of available prices
 * associated with the product.
 *
 * @author Tim Anderson
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
     * The wrapper component, containing either the text field or the prices
     * dropdown.
     */
    private Component container;

    /**
     * The text field for editing the price.
     */
    private TextField field;

    /**
     * The price drop down component. May be {@code null}
     */
    private DropDown priceDropDown;

    /**
     * The component focus group.
     */
    private FocusGroup focus;

    /**
     * Price rules, used to select fixed prices associated with the product.
     */
    private ProductPriceRules rules;

    /**
     * The selected fixed price.
     */
    private ProductPrice price;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * Creates a new {@code FixedPriceEditor}.
     *
     * @param property the fixed price property
     * @param context  the layout context
     */
    public FixedPriceEditor(Property property, LayoutContext context) {
        super(property);
        this.context = context;

        date = new Date();

        field = TextComponentFactory.createNumeric(property, 10);
        focus = new FocusGroup(property.getDisplayName());
        focus.add(field);
        container = RowFactory.create(field);
    }

    /**
     * Sets the product, used to select the fixed price.
     *
     * @param product the product. MAy be {@code null}
     */
    public void setProduct(Product product) {
        this.product = product;
        updatePrices();
    }

    /**
     * Returns the price.
     *
     * @return the price
     */
    public BigDecimal getPrice() {
        return (BigDecimal) getProperty().getValue();
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
        return container;
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
     * Returns the selected product price.
     *
     * @return the product price, or {@code null} if none is selected
     */
    public ProductPrice getProductPrice() {
        return price;
    }

    /**
     * Sets the product price.
     *
     * @param price the product price. May be {@code null}
     */
    public void setProductPrice(ProductPrice price) {
        this.price = price;
    }

    /**
     * Invoked when a price is selected.
     *
     * @param price the selected price. May be {@code null}
     */
    private void onSelected(ProductPrice price) {
        this.price = price;
        if (price != null) {
            getProperty().setValue(price.getPrice());
        }
        priceDropDown.setExpanded(false);
    }

    /**
     * Updates the product prices.
     * <p/>
     * If there are fixed prices associated with the product, renders a drop
     * down containing the prices, beside the text field.
     */
    private void updatePrices() {
        Component component = field;
        Component table = null;
        if (product != null) {
            if (rules == null) {
                rules = new ProductPriceRules();
            }
            Set<ProductPrice> prices = rules.getProductPrices(
                product, FIXED_PRICE, date);
            if (!prices.isEmpty()) {
                table = createPriceTable(prices);
            }
        }
        if (table != null) {
            priceDropDown = new DropDown();
            priceDropDown.setTarget(field);
            priceDropDown.setPopUpAlwaysOnTop(true);
            priceDropDown.setFocusOnExpand(true);
            priceDropDown.setPopUp(table);
            priceDropDown.setFocusComponent(table);
            component = priceDropDown;
        } else {
            priceDropDown = null;
        }
        container.removeAll();
        container.add(component);
    }

    /**
     * Creates a table of prices.
     *
     * @param prices the prices
     * @return a new price table
     */
    private PagedIMTable<ProductPrice> createPriceTable(Set<ProductPrice> prices) {
        ResultSet<ProductPrice> set = new IMObjectListResultSet<ProductPrice>(
            new ArrayList<ProductPrice>(prices), 20);
        final PagedIMTable<ProductPrice> table = new PagedIMTable<ProductPrice>(new PriceTableModel(context), set);
        table.getTable().addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
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
         * Constructs a {@code PriceTableModel}.
         *
         * @param context the layout context
         */
        public PriceTableModel(LayoutContext context) {
            super(new String[]{FIXED_PRICE}, context);
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
