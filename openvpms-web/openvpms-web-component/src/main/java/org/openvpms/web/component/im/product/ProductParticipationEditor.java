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

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductSupplier;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.DelegatingProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.ReadOnlyProperty;
import org.openvpms.web.system.ServiceHelper;


/**
 * Participation editor for products.
 * <p/>
 * For customer and patient acts, when the practice has useLocationProducts == true, products are filtered by
 * location and stock location.
 * <p/>
 * For all other acts, no location filtering occurs.
 *
 * @author Tim Anderson
 */
public class ProductParticipationEditor extends ParticipationEditor<Product> {

    /**
     * The patient, used to constrain searches to a particular species. May be {@code null}.
     */
    private Party patient;

    /**
     * The current supplier.
     */
    private Party supplier;

    /**
     * The product supplier relationship.
     */
    private ProductSupplier productSupplier;

    /**
     * The practice location, used to determine price service ratios. May be {@code null}
     */
    private Party location;

    /**
     * The stock location, used to constrain searches to a particular location. May be {@code null}.
     */
    private Party stockLocation;

    /**
     * Determines if products with {@code templateOnly == true} should be excluded.
     */
    private boolean excludeTemplateOnlyProducts;

    /**
     * Determines if products that don't have a location relationship should be excluded.
     */
    private boolean useLocationProducts;

    /**
     * The product short names that may be queried.
     */
    private String[] shortNames;

    /**
     * Container for the component. This allows the product to be made read-only if required.
     */
    private final Column container = new Column();

    /**
     * Determines if the product can be changed by the user.
     */
    private boolean readOnly;

    /**
     * Constructs a {@link ProductParticipationEditor}.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param context       the layout context
     */
    public ProductParticipationEditor(Participation participation, Act parent, LayoutContext context) {
        super(participation, parent, context);
        if (!TypeHelper.isA(participation, ProductArchetypes.PRODUCT_PARTICIPATION,
                            ProductArchetypes.MEDICATION_PARTICIPATION, StockArchetypes.STOCK_PARTICIPATION)) {
            throw new IllegalArgumentException("Invalid participation type:"
                                               + participation.getArchetypeId().getShortName());
        }
        resetShortNames();
        if (TypeHelper.isA(parent, "act.customer*", "act.patient*")) {
            // for customer and patient acts, restrict product selection to those available at the
            // practice location/stock location, when useLocationProducts == true.
            Party practice = context.getContext().getPractice();
            if (practice != null) {
                useLocationProducts = ServiceHelper.getBean(PracticeRules.class).useLocationProducts(practice);
                if (useLocationProducts) {
                    setLocations(context.getContext().getLocation());
                }
            }
        }
    }

    /**
     * Returns the rendered object.
     *
     * @return the rendered object
     */
    @Override
    public Component getComponent() {
        if (container.getComponentCount() == 0) {
            container.add(super.getComponent());
        }
        return container;
    }

    /**
     * Sets the product archetypes that may be queried.
     * <p/>
     * These should be a subset allowed by the underlying archetype
     *
     * @param shortNames the product archetypes
     */
    public void setShortNames(String... shortNames) {
        this.shortNames = shortNames;
    }

    /**
     * Resets the product archetypes that may be queried to the default.
     */
    public void resetShortNames() {
        shortNames = getEntityProperty().getArchetypeRange();
    }

    /**
     * Sets the patient, used to constrain product searches to a set of species.
     *
     * @param patient the patient. May be {@code null}
     */
    public void setPatient(Party patient) {
        this.patient = patient;
    }

    /**
     * Returns the patient .
     *
     * @return the patient. May be {@code null}
     */
    public Party getPatient() {
        return patient;
    }

    /**
     * Sets the product supplier.
     *
     * @param supplier the supplier. May be {@code null}
     */
    public void setSupplier(Party supplier) {
        this.supplier = supplier;
        productSupplier = null;
    }

    /**
     * Returns the product supplier.
     *
     * @return the product supplier. May be {@code null}
     */
    public Party getSupplier() {
        return supplier;
    }

    /**
     * Sets the practice location, and default stock location for the location.
     *
     * @param location the location. May be {@code null}
     */
    public void setLocations(Party location) {
        Party stockLocation = null;
        setLocation(location);
        if (location != null) {
            LocationRules bean = ServiceHelper.getBean(LocationRules.class);
            stockLocation = bean.getDefaultStockLocation(location);
        }
        setStockLocation(stockLocation);
    }

    /**
     * Sets the practice location.
     * <p/>
     * This is used to:
     * <ul>
     * <li>determine price service ratios.</li>
     * <li>exclude service and template products, when {@link #useLocationProducts()} == true</li>
     * </ul>
     *
     * @param location the practice location. May be {@code null}
     */
    public void setLocation(Party location) {
        this.location = location;
    }

    /**
     * Returns the practice location.
     *
     * @return the practice location. May be {@code null}
     */
    public Party getLocation() {
        return location;
    }

    /**
     * Sets the stock location. If set, only those products that have a relationship with the location, or no stock
     * relationships at all will be returned.
     *
     * @param location the stock location. May be {@code null}
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
     * The <em>entityLink.productSupplier</em> relationship associated with the product. Only populated when
     * the user selects the product.
     *
     * @return the product supplier relationship. May be {@code null}
     */
    public ProductSupplier getProductSupplier() {
        return productSupplier;
    }

    /**
     * Sets the product supplier relationship.
     *
     * @param relationship the product supplier relationship. May be {@code null}
     */
    public void setProductSupplier(ProductSupplier relationship) {
        productSupplier = relationship;
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
     * Determines if products with {@code templateOnly == true} should be excluded by the query.
     *
     * @return {@code true} if template-only products should b excluded, otherwise include them
     */
    public boolean getExcludeTemplateOnlyProducts() {
        return excludeTemplateOnlyProducts;
    }

    /**
     * Determines if products must be present at the location in order to select them.
     *
     * @param useLocationProducts if {@code true}, products must be present at the location to select them
     */
    public void setUseLocationProducts(boolean useLocationProducts) {
        this.useLocationProducts = useLocationProducts;
    }

    /**
     * Determines if products must be present at the location in order to select them.
     *
     * @return {@code true} if products must be present at the location to select them
     */
    public boolean useLocationProducts() {
        return useLocationProducts;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategy strategy;
        if (readOnly) {
            LayoutContext context = getLayoutContext();
            IMObjectLayoutStrategyFactory layoutStrategy = context.getLayoutStrategyFactory();
            IMObject object = getObject();
            strategy = layoutStrategy.create(object, getParent());
            Property property = new ReadOnlyProperty(getEntityProperty());
            ComponentState state = context.getComponentFactory().create(property, object);
            strategy.addComponent(state);
        } else {
            strategy = super.createLayoutStrategy();
        }
        return strategy;
    }

    /**
     * Determines if the product should be read-only.
     * <p/>
     * Note that this only affects the user interface presentation.
     *
     * @param readOnly if {@code true}, prevent the user from changing the product
     */
    public void setReadOnly(boolean readOnly) {
        boolean current = this.readOnly;
        this.readOnly = readOnly;
        if (readOnly != current) {
            onLayout();
        }
    }

    /**
     * Change the layout.
     */
    @Override
    protected void onLayout() {
        container.removeAll();
        super.onLayout();
        container.add(super.getComponent());
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Product> createEntityEditor(Property property) {
        DelegatingProperty p = new DelegatingProperty(property) {
            @Override
            public String[] getArchetypeRange() {
                return shortNames;
            }
        };
        return new ProductReferenceEditor(this, p, getLayoutContext());
    }

}
