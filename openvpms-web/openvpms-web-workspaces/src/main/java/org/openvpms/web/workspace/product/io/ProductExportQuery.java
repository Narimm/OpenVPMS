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

package org.openvpms.web.workspace.product.io;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.list.ListCellRenderer;
import org.openvpms.archetype.rules.product.PricingGroup;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.bound.BoundSelectFieldFactory;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.lookup.ArchetypeLookupQuery;
import org.openvpms.web.component.im.lookup.LookupQuery;
import org.openvpms.web.component.im.product.ProductQuery;
import org.openvpms.web.component.im.query.DateRange;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

/**
 * A product query for exporting products.
 *
 * @author Tim Anderson
 */
public class ProductExportQuery extends ProductQuery {

    /**
     * Determines the prices to export.
     */
    public enum Prices {
        CURRENT, ALL, RANGE
    }

    /**
     * The date range component.
     */
    private DateRange range;

    /**
     * The product type.
     */
    private final SimpleProperty productType = new SimpleProperty("productType", null, Entity.class,
                                                                  Messages.get("product.export.productType"));

    /**
     * The product income type code to restrict products to. May be {@code null}
     */
    private String incomeType;

    /**
     * The product group code to restrict products to. May be {@code null}
     */
    private String productGroup;

    /**
     * The prices to export.
     */
    private Prices prices = Prices.CURRENT;

    /**
     * Determines if linked prices should be exported.
     */
    private CheckBox includeLinkedPrices;

    /**
     * The archetype short names to query.
     */
    private static final String[] SHORT_NAMES = new String[]{
            ProductArchetypes.MEDICATION, ProductArchetypes.SERVICE, ProductArchetypes.MERCHANDISE,
            ProductArchetypes.PRICE_TEMPLATE};

    /**
     * The prices.
     */
    private static final Prices[] PRICES = {Prices.CURRENT, Prices.ALL, Prices.RANGE};

    /**
     * The labels used in the price selector. The order corresponds to {@link #PRICES}.
     */
    private final String[] PRICE_LABELS = {Messages.get("product.export.prices.current"),
                                           Messages.get("product.export.prices.all"),
                                           Messages.get("product.export.prices.range")};

    /**
     * Constructs a {@link ProductExportQuery}.
     *
     * @param context the context
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public ProductExportQuery(Context context) {
        super(SHORT_NAMES, context);
        setPricingGroup(PricingGroup.ALL); // don't use the practice location pricing group
    }

    /**
     * Determines the prices to export.
     *
     * @return the prices
     */
    public Prices getPrices() {
        return prices;
    }

    /**
     * Sets the prices to export.
     *
     * @param prices the prices
     */
    public void setPrices(Prices prices) {
        this.prices = prices;
        if (range != null) {
            range.setEnabled(prices == Prices.RANGE);
        }
    }

    /**
     * Returns the price start date.
     * <p/>
     * Only prices active at the start date will be returned. This is only applicable when {@link #getPrices}
     * is {@link Prices#RANGE}.
     *
     * @return the price start date. May be {@code null}
     */
    public Date getFrom() {
        return (range != null) ? range.getFrom() : null;
    }

    /**
     * Returns the price end date.
     * <p/>
     * Only prices active at the end date will be returned. This is only applicable when {@link #getPrices}
     * is {@link Prices#RANGE}.
     *
     * @return the price end date. May be {@code null}
     */
    public Date getTo() {
        return (range != null) ? range.getTo() : null;
    }

    /**
     * Determines if linked prices should be included in the exported data.
     *
     * @return {@code true} if linked prices should be exported, otherwise {@code false}
     */
    public boolean includeLinkedPrices() {
        return includeLinkedPrices.isSelected();
    }

    /**
     * Sets the product type to filter products by.
     *
     * @param productType the product type. May be {@code null}
     */
    public void setProductType(Entity productType) {
        this.productType.setValue(productType);
    }

    /**
     * Returns the product type to filter products by.
     *
     * @return the product type, or {@code null} to select all products
     */
    public Entity getProductType() {
        return (Entity) productType.getValue();
    }

    /**
     * Returns the preferred height of the query when rendered.
     *
     * @return the preferred height, or {@code null} if it has no preferred height
     */
    @Override
    public Extent getHeight() {
        return getHeight(4);
    }

    /**
     * Creates a container component to lay out the query component in.
     * This implementation returns a new grid.
     *
     * @return a new container
     * @see #doLayout(Component)
     */
    @Override
    protected Component createContainer() {
        return GridFactory.create(8);
    }

    /**
     * Lays out the component in a container, and sets focus on the instance name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
        addProductTypeSelector(container);
        addSpeciesSelector(container);
        addIncomeTypeSelector(container);
        addProductGroupSelector(container);
        addPriceSelector(container);
        addDateRange(container);
        addLinkedPrices(container);
        addPricingGroupSelector(container, true);
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<Product> createResultSet(SortConstraint[] sort) {
        return new ProductExportResultSet(getArchetypeConstraint(), getValue(), isIdentitySearch(), getSpecies(),
                                          getProductType(), getStockLocation(), incomeType, productGroup, sort, getMaxResults());
    }

    /**
     * Adds a selector to restrict products by product type.
     *
     * @param container the container to add the component to
     */
    private void addProductTypeSelector(Component container) {
        ArchetypeQuery query = new ArchetypeQuery(ProductArchetypes.PRODUCT_TYPE, true)
                .add(Constraints.sort("name"))
                .setMaxResults(ArchetypeQuery.ALL_RESULTS);
        final IMObjectListModel model = new IMObjectListModel(QueryHelper.query(query), true, false);
        final SelectField field = BoundSelectFieldFactory.create(productType, model);
        field.setCellRenderer(IMObjectListCellRenderer.NAME);

        Label label = LabelFactory.create();
        label.setText(productType.getDisplayName());
        container.add(label);
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Adds a selector to constrain the products by the species they are for.
     *
     * @param container the container to add the component to
     */
    private void addSpeciesSelector(Component container) {
        LookupQuery query = new ArchetypeLookupQuery("lookup.species");
        final SelectField field = SelectFieldFactory.create(new LookupListModel(query, true));
        field.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                setSpecies((String) field.getSelectedItem());
            }
        });
        field.setCellRenderer(LookupListCellRenderer.INSTANCE);

        Label species = LabelFactory.create("product.export.species");
        container.add(species);
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Adds a selector to constrain the products by income type.
     *
     * @param container the container to add the component to
     */
    private void addIncomeTypeSelector(Component container) {
        LookupQuery query = new ArchetypeLookupQuery("lookup.productIncomeType");
        final SelectField field = SelectFieldFactory.create(new LookupListModel(query, true));
        field.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                incomeType = (String) field.getSelectedItem();
            }
        });
        field.setCellRenderer(LookupListCellRenderer.INSTANCE);

        Label label = LabelFactory.create("product.export.incomeType");
        container.add(label);
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Adds a selector to constrain the products by product group.
     *
     * @param container the container to add the component to
     */
    private void addProductGroupSelector(Component container) {
        LookupQuery query = new ArchetypeLookupQuery("lookup.productGroup");
        final SelectField field = SelectFieldFactory.create(new LookupListModel(query, true));
        field.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                productGroup = (String) field.getSelectedItem();
            }
        });
        field.setCellRenderer(LookupListCellRenderer.INSTANCE);

        Label label = LabelFactory.create("product.export.productGroup");
        container.add(label);
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Adds a selector to determine which prices are exported.
     *
     * @param container the container to add the component to
     */
    private void addPriceSelector(Component container) {
        final SelectField field = SelectFieldFactory.create(PRICES);
        field.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                setPrices((Prices) field.getSelectedItem());
            }
        });
        field.setCellRenderer(new ListCellRenderer() {
            @Override
            public Object getListCellRendererComponent(Component list, Object value, int index) {
                return PRICE_LABELS[index];
            }
        });
        container.add(LabelFactory.create("product.export.prices"));
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Adds the date range component, used when the price selector is "RANGE",
     *
     * @param container the container to add the range to
     */
    private void addDateRange(final Component container) {
        range = new DateRange(false);
        range.setContainer(container);
        range.setEnabled(prices == Prices.RANGE);
        getFocusGroup().add(range.getFocusGroup());
    }

    /**
     * Adds the 'include linked prices' checkbox.
     *
     * @param container the container to add to
     */
    private void addLinkedPrices(Component container) {
        includeLinkedPrices = CheckBoxFactory.create();
        includeLinkedPrices.setSelected(false);
        container.add(LabelFactory.create("product.export.includeLinkedPrices"));
        container.add(includeLinkedPrices);
        getFocusGroup().add(includeLinkedPrices);
    }

}
