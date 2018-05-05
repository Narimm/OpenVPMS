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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.PricingGroup;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.BaseIMObjectTableModel;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.format.NumberFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Table model for <em>product.*</em> objects. Displays the fixed and unit prices if available.
 *
 * @author Tim Anderson
 */
public class ProductTableModel extends BaseIMObjectTableModel<Product> {

    /**
     * The fixed price model index.
     */
    private int fixedPriceIndex;

    /**
     * The unit price model index.
     */
    private int unitPriceIndex;

    /**
     * The pricing context.
     */
    private final ProductPricingContext pricingContext;

    /**
     * Determines if the active column should be displayed.
     */
    private boolean showActive;

    /**
     * Constructs a {@link ProductTableModel}.
     *
     * @param context the layout context
     */
    public ProductTableModel(LayoutContext context) {
        this(null, context);
    }

    /**
     * Constructs a {@link ProductTableModel}.
     *
     * @param query   the query. May be {@code null}
     * @param context the layout context
     */
    public ProductTableModel(ProductQuery query, LayoutContext context) {
        this(query, context.getContext().getLocation(), context);
    }

    /**
     * Constructs a {@link ProductTableModel}.
     *
     * @param query    the query. May be {@code null}
     * @param location the practice location, used to determine service ratios. May be {@code null}
     * @param context  the layout context
     */
    public ProductTableModel(ProductQuery query, Party location, LayoutContext context) {
        super(null);
        ProductPriceRules rules = ServiceHelper.getBean(ProductPriceRules.class);
        PracticeRules practiceRules = ServiceHelper.getBean(PracticeRules.class);
        Party practice = context.getContext().getPractice();
        Currency currency = practiceRules.getCurrency(practice);
        if (query != null) {
            pricingContext = new ProductPricingContext(currency, query.getPricingGroup(), practice, location, rules);
        } else {
            LocationRules locationRules = ServiceHelper.getBean(LocationRules.class);
            pricingContext = new ProductPricingContext(currency, practice, location, rules, locationRules);
        }
        showActive = (query == null) || query.getActive() == BaseArchetypeConstraint.State.BOTH;
        setTableColumnModel(createTableColumnModel(showActive));
    }

    /**
     * Determines if the active column should be displayed.
     *
     * @param show if {@code true} show the active column
     */
    public void setShowActive(boolean show) {
        if (show != showActive) {
            showActive = show;
            setTableColumnModel(createTableColumnModel(showActive));
        }
    }

    /**
     * Sets the pricing group.
     * <p/>
     * This determines the fixed and unit prices displayed.
     *
     * @param pricingGroup the pricing group. May be {@code null}
     */
    public void setPricingGroup(Lookup pricingGroup) {
        if (!ObjectUtils.equals(pricingContext.getPricingGroup().getGroup(), pricingGroup)) {
            pricingContext.setPricingGroup(new PricingGroup(pricingGroup));
            fireTableDataChanged();
        }
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param product the object
     * @param column  the column
     * @param row     the row
     * @return the value at the given coordinate
     */
    @Override
    protected Object getValue(Product product, TableColumn column, int row) {
        int index = column.getModelIndex();
        if (index == fixedPriceIndex) {
            return getFixedPrice(product);
        } else if (index == unitPriceIndex) {
            return getUnitPrice(product);
        }
        return super.getValue(product, column, row);
    }

    /**
     * Creates a new column model.
     *
     * @return a new column model
     */
    protected TableColumnModel createTableColumnModel(boolean active) {
        DefaultTableColumnModel model = new DefaultTableColumnModel();
        model.addColumn(createTableColumn(ID_INDEX, ID));
        model.addColumn(createTableColumn(NAME_INDEX, NAME));
        model.addColumn(createTableColumn(DESCRIPTION_INDEX, DESCRIPTION));
        model.addColumn(createTableColumn(ARCHETYPE_INDEX, ARCHETYPE));
        fixedPriceIndex = getNextModelIndex(model);
        unitPriceIndex = fixedPriceIndex + 1;
        TableColumn fixedPrice = createTableColumn(fixedPriceIndex, "producttablemodel.fixedPrice");
        TableColumn unitPrice = createTableColumn(unitPriceIndex, "producttablemodel.unitPrice");
        model.addColumn(fixedPrice);
        model.addColumn(unitPrice);
        if (active) {
            model.addColumn(createTableColumn(ACTIVE_INDEX, ACTIVE));
        }
        return model;
    }

    /**
     * Returns a component representing the default fixed price for a product.
     *
     * @param product the product
     * @return the default fixed price component, or {@code null} if the product doesn't have a fixed price
     */
    private Component getFixedPrice(Product product) {
        ProductPrice price = pricingContext.getFixedPrice(product, new Date());
        return getPrice(product, price);
    }

    /**
     * Returns a component representing the unit price for a product.
     *
     * @param product the product
     * @return the unit price component, or {@code null} if the product doesn't have a unit price
     */
    private Component getUnitPrice(Product product) {
        ProductPrice price = pricingContext.getUnitPrice(product, new Date());
        return getPrice(product, price);
    }

    /**
     * Returns a component for a product price.
     *
     * @param product the product
     * @return a component for the product price corresponding to {@code shortName} or {@code null} if none is found
     */
    private Component getPrice(Product product, ProductPrice price) {
        Component result = null;
        if (price != null) {
            BigDecimal value = pricingContext.getPrice(product, price);
            result = TableHelper.rightAlign(NumberFormatter.formatCurrency(value));
        }
        return result;
    }
}
