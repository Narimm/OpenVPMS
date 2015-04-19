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

package org.openvpms.web.workspace.product.stock;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.lookup.ArchetypeLookupQuery;
import org.openvpms.web.component.im.lookup.LookupQuery;
import org.openvpms.web.component.im.query.AbstractEntityQuery;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class StockExportObjectSetQuery extends AbstractEntityQuery<ObjectSet> {

    /**
     * The stock location.
     */
    private Party stockLocation;

    /**
     * The product type to restrict products to. May be {@code null}
     */
    private Entity productType;

    /**
     * /**
     * The product income type code to restrict products to. May be {@code null}
     */
    private String incomeType;

    /**
     * The product group code to restrict products to. May be {@code null}
     */
    private String productGroup;

    /**
     * If selected, set the new quantity to zero if the on-hand quantity is negative.
     */
    private CheckBox zeroNegativeQuantities;


    /**
     * The archetype short names to query.
     */
    private static final String[] SHORT_NAMES = new String[]{ProductArchetypes.MEDICATION,
                                                             ProductArchetypes.MERCHANDISE};


    /**
     * Constructs a {@link StockExportObjectSetQuery}.
     *
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public StockExportObjectSetQuery() {
        this(null);
    }

    /**
     * Constructs a {@link StockExportObjectSetQuery}.
     *
     * @param stockLocation the stock location. May be {@code null}
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public StockExportObjectSetQuery(Party stockLocation) {
        super(SHORT_NAMES, ObjectSet.class);
        setStockLocation(stockLocation);
        zeroNegativeQuantities = CheckBoxFactory.create("product.stock.export.zeroNegativeQuantities", true);
    }

    /**
     * Sets the stock location.
     *
     * @param stockLocation the stock location. May be {@code null}
     */
    public void setStockLocation(Party stockLocation) {
        this.stockLocation = stockLocation;
    }

    /**
     * Returns the stock location.
     *
     * @return the stock location. May be {@code null}
     */
    public Party getStockLocation() {
        return stockLocation;
    }

    /**
     * Sets the product type.
     *
     * @param type the product type. May be {@code null}
     */
    public void setProductType(Entity type) {
        this.productType = type;
    }

    /**
     * Sets the product group.
     *
     * @param productGroup the product group classification code. May be {@code null}
     */
    public void setProductGroup(String productGroup) {
        this.productGroup = productGroup;
    }

    /**
     * Sets the income type.
     *
     * @param incomeType the income type classification code. May be {@code null}
     */
    public void setIncomeType(String incomeType) {
        this.incomeType = incomeType;
    }

    /**
     * Determines if negative quantities should be set to zero.
     *
     * @return {@code true} if negative quantities should be set to zero
     */
    public boolean getZeroNegativeQuantities() {
        return zeroNegativeQuantities.isSelected();
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return the query result set. May be {@code null}
     */
    @Override
    public ResultSet<ObjectSet> query(SortConstraint[] sort) {
        if (stockLocation != null) {
            return super.query(sort);
        }
        return null;
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
        addStockLocationSelector(container);
        addIncomeTypeSelector(container);
        addProductGroupSelector(container);
        container.add(zeroNegativeQuantities);
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        return new StockExportResultSet(getArchetypeConstraint(), getValue(), isIdentitySearch(), stockLocation,
                                        productType, incomeType, productGroup, sort, getMaxResults());
    }

    /**
     * Adds a selector to restrict products by stock location.
     *
     * @param container the container to add the component to
     */
    private void addStockLocationSelector(Component container) {
        final SelectField field = createObjectSelector(StockArchetypes.STOCK_LOCATION, false, stockLocation);
        if (stockLocation == null) {
            setStockLocation((Party) field.getSelectedItem());
        }
        field.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                setStockLocation((Party) field.getSelectedItem());
            }
        });

        Label label = LabelFactory.create("product.stockLocation");
        container.add(label);
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Adds a selector to restrict products by product type.
     *
     * @param container the container to add the component to
     */
    private void addProductTypeSelector(Component container) {
        final SelectField field = createObjectSelector(ProductArchetypes.PRODUCT_TYPE, true, productType);
        if (productType == null) {
            setProductType((Entity) field.getSelectedItem());
        }
        field.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                setProductType((Entity) field.getSelectedItem());
            }
        });

        Label label = LabelFactory.create("product.export.productType");
        container.add(label);
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Adds a selector to constrain the products by income type.
     *
     * @param container the container to add the component to
     */
    private void addIncomeTypeSelector(Component container) {
        final SelectField field = createLookupSelector("lookup.productIncomeType", incomeType);
        if (incomeType == null) {
            setIncomeType((String) field.getSelectedItem());
        }
        field.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                setProductGroup((String) field.getSelectedItem());
            }
        });

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
        final SelectField field = createLookupSelector("lookup.productGroup", productGroup);
        if (productGroup == null) {
            setProductGroup((String) field.getSelectedItem());
        }
        field.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                setProductGroup((String) field.getSelectedItem());
            }
        });
        Label label = LabelFactory.create("product.export.productGroup");
        container.add(label);
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Helper to create a {@code SelectField} from queried objects.
     *
     * @param shortName    the archetype to query
     * @param all          if {@code true}, add a localised "All"
     * @param defaultValue the default selection. May be {@code null}
     * @return a new select field
     */
    private SelectField createObjectSelector(String shortName, boolean all, IMObject defaultValue) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, true)
                .add(Constraints.sort("name"))
                .setMaxResults(ArchetypeQuery.ALL_RESULTS);
        final IMObjectListModel model = new IMObjectListModel(QueryHelper.query(query), all, false);
        SelectField field = SelectFieldFactory.create(model);
        field.setCellRenderer(IMObjectListCellRenderer.NAME);
        if (defaultValue != null) {
            field.setSelectedItem(defaultValue);
        }
        return field;
    }

    /**
     * Helper to create a {@code SelectField} for lookups.
     *
     * @param shortName    the lookup archetype short name
     * @param defaultValue the default selection. May be {@code null}
     * @return a new select field
     */
    private SelectField createLookupSelector(String shortName, String defaultValue) {
        LookupQuery query = new ArchetypeLookupQuery(shortName);
        SelectField field = SelectFieldFactory.create(new LookupListModel(query, true), defaultValue);
        field.setCellRenderer(LookupListCellRenderer.INSTANCE);
        return field;
    }


}
