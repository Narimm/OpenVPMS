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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import echopointng.DropDown;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.edit.AbstractPropertyEditor;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.AbstractIMObjectTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.format.NumberFormatter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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
     * The wrapper component, containing either the text field or the prices dropdown.
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
     * The selected fixed price.
     */
    private ProductPrice price;

    /**
     * The pricing context.
     */
    private final PricingContext pricingContext;

    /**
     * Constructs a {@link FixedPriceEditor}.
     *
     * @param property       the fixed price property
     * @param pricingContext the pricing context
     */
    public FixedPriceEditor(Property property, PricingContext pricingContext) {
        super(property);
        this.pricingContext = pricingContext;

        date = new Date();

        field = BoundTextComponentFactory.createNumeric(property, 10);
        if (property.isReadOnly()) {
            // can select prices from the dropdown, but not edit the price directly
            field.setEnabled(false);
        }
        focus = new FocusGroup(property.getDisplayName());
        focus.add(field);
        if (!property.isReadOnly()) {
            focus.add(field);
        }
        container = RowFactory.create(field);
    }

    /**
     * Sets the product, used to select the fixed price.
     *
     * @param product the product. May be {@code null}
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
            getProperty().setValue(getPrice(price));
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
            List<ProductPrice> prices = pricingContext.getFixedPrices(product, date);
            if (!prices.isEmpty()) {
                table = createPriceTable(prices).getComponent();
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
    private PagedIMTable<ProductPrice> createPriceTable(List<ProductPrice> prices) {
        ResultSet<ProductPrice> set = new IMObjectListResultSet<>(new ArrayList<>(prices), 20);
        final PagedIMTable<ProductPrice> table = new PagedIMTable<>(new PriceTableModel(), set);
        table.getTable().addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onSelected(table.getTable().getSelected());
            }
        });
        return table;
    }

    /**
     * Returns the price for a product price, multiplied by the service ratio if there is one.
     *
     * @param price the price
     * @return the
     */
    private BigDecimal getPrice(ProductPrice price) {
        return pricingContext.getPrice(product, price);
    }

    /**
     * Table model that displays the name and price of an {@link ProductPrice}.
     */
    private class PriceTableModel extends AbstractIMObjectTableModel<ProductPrice> {

        /**
         * Name column index.
         */
        private static final int NAME_INDEX = 0;

        /**
         * Price column index.
         */
        private static final int PRICE_INDEX = 1;

        /**
         * Constructs a {@code PriceTableModel}.
         */
        public PriceTableModel() {
            DefaultTableColumnModel model = new DefaultTableColumnModel();
            model.addColumn(createTableColumn(NAME_INDEX, NAME));
            TableColumn price = new TableColumn(PRICE_INDEX);
            price.setHeaderValue(DescriptorHelper.getDisplayName(ProductArchetypes.FIXED_PRICE, "price"));
            model.addColumn(price);
            setTableColumnModel(model);
        }

        /**
         * Returns the sort criteria.
         *
         * @param column    the primary sort column
         * @param ascending if {@code true} sort in ascending order; otherwise sort in {@code descending} order
         * @return the sort criteria, or {@code null} if the column isn't sortable
         */
        @Override
        public SortConstraint[] getSortConstraints(int column, boolean ascending) {
            SortConstraint[] result = null;
            switch (column) {
                case NAME_INDEX:
                    result = new NodeSortConstraint[]{new NodeSortConstraint("name", ascending)};
                    break;
                case PRICE_INDEX:
                    result = new NodeSortConstraint[]{new NodeSortConstraint("price", ascending)};
                    break;
            }
            return result;
        }

        /**
         * Returns the value found at the given coordinate within the table.
         *
         * @param object the object
         * @param column the column
         * @param row    the row
         * @return the value at the given coordinate.
         */
        @Override
        protected Object getValue(ProductPrice object, TableColumn column, int row) {
            Object result = null;
            switch (column.getModelIndex()) {
                case NAME_INDEX:
                    result = object.getName();
                    break;
                case PRICE_INDEX:
                    result = TableHelper.rightAlign(NumberFormatter.formatCurrency(getPrice(object)));
                    break;
            }
            return result;
        }
    }

}
