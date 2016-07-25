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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.product.PricingGroup;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.AbstractEntityQuery;
import org.openvpms.web.component.im.query.FilteredResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * Query implementation that queries {@link Product} instances on short name,
 * instance name, active/inactive status, species and location.
 *
 * @author Tim Anderson
 */
public class ProductQuery extends AbstractEntityQuery<Product> {

    /**
     * The species to constrain the query to. May be {@code null}.
     */
    private String species;

    /**
     * Determines if products should be restricted to those available at the location or stock location.
     */
    private boolean useLocationProducts;

    /**
     * The location to constrain the query to. May be {@code null}.
     */
    private Party location;

    /**
     * The stock location to constrain the query to. May be {@code null}.
     */
    private Party stockLocation;

    /**
     * The pricing group.
     */
    private PricingGroup pricingGroup;

    /**
     * Determines if products with {@code templateOnly == true} should be excluded.
     */
    private boolean excludeTemplateOnlyProducts;

    /**
     * Constructs a {@link ProductQuery} that queries products with the specified short names.
     *
     * @param shortNames the short names
     * @param context    the context
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public ProductQuery(String[] shortNames, Context context) {
        super(shortNames, Product.class);

        setLocation(context.getLocation());
    }

    /**
     * Sets the species to constrain the query to.
     *
     * @param species the species classification
     */
    public void setSpecies(String species) {
        this.species = species;
    }

    /**
     * Returns the species.
     *
     * @return the species. May be {@code null}
     */
    public String getSpecies() {
        return species;
    }

    /**
     * Sets the practice location to constraint the query to.
     *
     * @param location the practice location
     */
    public void setLocation(Party location) {
        this.location = location;
        if (location != null) {
            LocationRules rules = ServiceHelper.getBean(LocationRules.class);
            pricingGroup = new PricingGroup(rules.getPricingGroup(location));
        } else {
            pricingGroup = new PricingGroup(null);
        }
    }

    /**
     * Sets the stock location to constrain the query to.
     *
     * @param location the stock location
     */
    public void setStockLocation(Party location) {
        this.stockLocation = location;
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
     * Returns the pricing group.
     *
     * @return the pricing group
     */
    public PricingGroup getPricingGroup() {
        return pricingGroup;
    }

    /**
     * Determines if products with {@code templateOnly == true} should be excluded by the query.
     *
     * @param exclude if {@code true}, exclude template-only products
     */
    public void setExcludeTemplateOnlyProducts(boolean exclude) {
        this.excludeTemplateOnlyProducts = exclude;
    }

    /**
     * Determines if products must be present at the location or stock location to be returned.
     *
     * @param useLocationProducts if {@code true}, products must be present at the location/stock location
     */
    public void setUseLocationProducts(boolean useLocationProducts) {
        this.useLocationProducts = useLocationProducts;
    }

    /**
     * Determines if products must be present at the location or stock location to be returned.
     *
     * @return {@code true} if products must be present at the location/stock location
     */
    public boolean useLocationProducts() {
        return useLocationProducts;
    }

    /**
     * Determines if the query selects a particular object reference.
     *
     * @param reference the object reference to check
     * @return {@code true} if the object reference is selected by the query
     */
    @Override
    public boolean selects(IMObjectReference reference) {
        boolean result;
        if (!excludeTemplateOnlyProducts) {
            result = super.selects(reference);
        } else {
            ProductResultSet set = getResultSet(getDefaultSortConstraint());
            set.setReferenceConstraint(reference);
            ResultSet<Product> filtered = filterTemplateOnlyProducts(set);
            result = filtered.hasNext();
        }
        return result;
    }

    /**
     * Sets the pricing group.
     * <p/>
     * NOTE: this does not update the selector, so should only be invoked prior to its construction.
     *
     * @param group the pricing group
     */
    protected void setPricingGroup(PricingGroup group) {
        pricingGroup = group;
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    protected ResultSet<Product> createResultSet(SortConstraint[] sort) {
        ResultSet<Product> result = getResultSet(sort);
        if (excludeTemplateOnlyProducts) {
            result = filterTemplateOnlyProducts(result);
        }
        return result;
    }

    /**
     * Adds a selector to constrain prices by pricing group.
     *
     * @param container the container to add the component to
     * @param all       if {@code true}, include an option to select 'All'
     */
    protected void addPricingGroupSelector(Component container, boolean all) {
        final PricingGroupSelectField field = new PricingGroupSelectField(pricingGroup, all);
        field.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                PricingGroup group = (field.isAllSelected()) ? PricingGroup.ALL : field.getSelected();
                onPricingGroupChanged(group);
            }
        });

        Label label = LabelFactory.create("product.pricingGroup");
        container.add(label);
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Invoked when the pricing group changes.
     *
     * @param group the selected pricing group
     */
    protected void onPricingGroupChanged(PricingGroup group) {
        pricingGroup = group;
    }

    /**
     * Excludes any products from the result set that have {@code templateOnly == true}.
     * <p/>
     * This needs to be done in memory until OBF-236 is supported.
     *
     * @param set the result set to filter
     * @return the filtered result set
     */
    protected ResultSet<Product> filterTemplateOnlyProducts(final ResultSet<Product> set) {
        return new FilteredResultSet<Product>(set) {
            @Override
            protected void filter(Product object, List<Product> results) {
                IMObjectBean bean = new IMObjectBean(object);
                if (!bean.hasNode("templateOnly") || !bean.getBoolean("templateOnly")) {
                    results.add(object);
                }
            }
        };
    }

    /**
     * Creates a new {@link ProductResultSet}.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new {@link ProductResultSet}
     */
    protected ProductResultSet getResultSet(SortConstraint[] sort) {
        return new ProductResultSet(getArchetypeConstraint(), getValue(), isIdentitySearch(), species,
                                    useLocationProducts, location, stockLocation, sort, getMaxResults());
    }

}
