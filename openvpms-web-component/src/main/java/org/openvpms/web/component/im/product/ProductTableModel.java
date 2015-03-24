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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
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
import org.openvpms.archetype.rules.product.ProductArchetypes;
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
     * The product price rules.
     */
    private ProductPriceRules rules;

    /**
     * The practice location, used to determine service ratios. May be {@code null}
     */
    private Party location;

    /**
     * The pricing location. May be {@code null}
     */
    private Lookup pricingGroup;

    /**
     * The currency, used for rounding.
     */
    private final Currency currency;


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
        super(null);
        rules = ServiceHelper.getBean(ProductPriceRules.class);
        PracticeRules practiceRules = ServiceHelper.getBean(PracticeRules.class);
        currency = practiceRules.getCurrency(context.getContext().getPractice());
        boolean active = (query == null) || query.getActive() == BaseArchetypeConstraint.State.BOTH;
        setTableColumnModel(createTableColumnModel(active));
        if (query != null) {
            pricingGroup = query.getPricingGroup().getGroup();
        } else {
            Party location = context.getContext().getLocation();
            if (location != null) {
                LocationRules locationRules = ServiceHelper.getBean(LocationRules.class);
                pricingGroup = locationRules.getPricingGroup(location);
            }
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
        if (!ObjectUtils.equals(this.pricingGroup, pricingGroup)) {
            this.pricingGroup = pricingGroup;
            fireTableDataChanged();
        }
    }

    /**
     * Sets the practice location.
     * <p/>
     * If non-null, this is used to determine service ratios that are applied to prices.
     * If null, the prices are displayed unchanged.
     *
     * @param location the practice location. May be {@code null}
     */
    public void setLocation(Party location) {
        this.location = location;
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
            return getPrice(ProductArchetypes.FIXED_PRICE, product);
        } else if (index == unitPriceIndex) {
            return getPrice(ProductArchetypes.UNIT_PRICE, product);
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
     * Returns a component for a product price.
     *
     * @param shortName the product price short name
     * @param product   the product
     * @return a component for the product price corresponding to {@code shortName} or {@code null} if none is found
     */
    private Component getPrice(String shortName, Product product) {
        Component result = null;
        ProductPrice price = rules.getProductPrice(product, shortName, new Date(), pricingGroup);
        if (price != null && price.getPrice() != null) {
            BigDecimal value = price.getPrice();
            if (location != null) {
                BigDecimal ratio = rules.getServiceRatio(product, location);
                value = ProductHelper.getPrice(price, ratio, currency);
            }
            result = TableHelper.rightAlign(NumberFormatter.formatCurrency(value));
        }
        return result;
    }
}
