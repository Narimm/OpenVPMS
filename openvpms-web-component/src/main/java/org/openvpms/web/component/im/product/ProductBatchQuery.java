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
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.GridLayoutData;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.AbstractEntityQuery;
import org.openvpms.web.component.im.query.DateRange;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.select.AbstractIMObjectSelectorListener;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

/**
 * Query for <em>entity.productBatch</em>.
 *
 * @author Tim Anderson
 */
public class ProductBatchQuery extends AbstractEntityQuery<Entity> {

    /**
     * The product filter.
     */
    private Product product;

    /**
     * The product name filter.
     */
    private TextField productName;

    /**
     * The expiry date filter.
     */
    private DateRange expiryDate;

    /**
     * The stock location selector.
     */
    private IMObjectSelector<Party> stockLocation;

    /**
     * The manufacturer selector.
     */
    private IMObjectSelector<Party> manufacturer;

    /**
     * The short names to query.
     */
    private static final String[] SHORT_NAMES = new String[]{ProductArchetypes.PRODUCT_BATCH};

    /**
     * Constructs a {@link ProductBatchQuery}.
     *
     * @param context the layout context
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public ProductBatchQuery(LayoutContext context) {
        super(SHORT_NAMES);
        stockLocation = new IMObjectSelector<Party>(Messages.get("product.stockLocation"),
                                                    new DefaultLayoutContext(context, context.getHelpContext()),
                                                    StockArchetypes.STOCK_LOCATION);
        stockLocation.setObject(context.getContext().getStockLocation());
        manufacturer = new IMObjectSelector<Party>(Messages.get("product.batch.manufacturer"),
                                                   new DefaultLayoutContext(context, context.getHelpContext()),
                                                   SupplierArchetypes.MANUFACTURER);

        AbstractIMObjectSelectorListener<Party> listener = new AbstractIMObjectSelectorListener<Party>() {
            public void selected(Party object) {
                onQuery();
            }
        };
        stockLocation.setListener(listener);
        manufacturer.setListener(listener);
    }

    /**
     * Sets the product filter.
     *
     * @param product the product. May be {@code null}
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * Sets the product name.
     *
     * @param product the product name. May be {@code null}
     */
    public void setProductName(String product) {
        getProductField().setText(product);
    }

    /**
     * Returns the product name.
     *
     * @return the product name. May be {@code null}
     */
    public String setProductName() {
        return getProductField().getText();
    }

    /**
     * Sets the date that products must expire on or after.
     *
     * @param date the expiry date
     */
    public void setExpireAfter(Date date) {
        getExpiryDate().setFrom(date);
    }

    /**
     * Creates a container component to lay out the query component in.
     *
     * @return a new container
     */
    @Override
    protected Component createContainer() {
        return GridFactory.create(6);
    }

    /**
     * Lays out the component in a container, and sets focus on the search field.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        addSearchField(container);
        addExpiryDate(container);
        if (product == null) {
            addProductName(container);
        }
        addStockLocation(container);
        addActive(container);
        addManufacturer(container);
        getExpiryDate().setFrom(DateRules.getToday());
        getExpiryDate().setTo(null);
        FocusHelper.setFocus(getSearchField());
    }

    /**
     * Adds the search field to a container.
     *
     * @param container the container
     */
    @Override
    protected void addSearchField(Component container) {
        Label label = LabelFactory.create("product.batch");
        container.add(label);
        TextField field = getSearchField();
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Returns the product field.
     *
     * @return the product field
     */
    protected TextField getProductField() {
        if (productName == null) {
            productName = TextComponentFactory.create();
            productName.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    onQuery();
                }
            });
        }
        return productName;
    }

    /**
     * Returns the expiry date range.
     *
     * @return the expiry date range
     */
    protected DateRange getExpiryDate() {
        if (expiryDate == null) {
            expiryDate = new DateRange(false) {
                @Override
                protected ComponentState createFromDate(Property from) {
                    ComponentState fromDate = super.createFromDate(from);
                    fromDate.setLabel(LabelFactory.create("product.batch.expiringAfter"));
                    return fromDate;
                }

                @Override
                protected ComponentState createToDate(Property to) {
                    ComponentState toDate = super.createToDate(to);
                    toDate.setLabel(LabelFactory.create("product.batch.expiringBefore"));
                    return toDate;
                }
            };
        }
        return expiryDate;
    }

    /**
     * Adds the product field to a container.
     *
     * @param container the container
     */
    protected void addProductName(Component container) {
        Label label = LabelFactory.create("product.batch.product");
        container.add(label);
        TextField field = getProductField();
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Adds the stock location field to a container.
     *
     * @param container the container
     */
    protected void addStockLocation(Component container) {
        addSelector(stockLocation, container);
    }

    /**
     * Adds the manufacturer field to a container.
     *
     * @param container the container
     */
    protected void addManufacturer(Component container) {
        addSelector(manufacturer, container);
    }

    /**
     * Adds a date range to the container.
     *
     * @param container the container
     */
    protected void addExpiryDate(Component container) {
        DateRange dateRange = getExpiryDate();
        dateRange.setContainer(container);
        getFocusGroup().add(dateRange.getFocusGroup());
    }


    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<Entity> createResultSet(SortConstraint[] sort) {
        String productName = getWildcardedText(getProductField());
        Date from = getExpiryDate().getFrom();
        Date to = getExpiryDate().getTo();
        IMObjectReference location = stockLocation.getObject() != null
                                     ? stockLocation.getObject().getObjectReference() : null;
        return new ProductBatchResultSet(getArchetypeConstraint(), getValue(), product, productName,
                                         from, to, location, null, manufacturer.getObject(), sort,
                                         getMaxResults());
    }

    /**
     * Adds a selector field.
     * <p/>
     * This spans 4 columns to reduce the overall width of the display.
     *
     * @param selector  the selector
     * @param container the container
     */
    private void addSelector(IMObjectSelector<Party> selector, Component container) {
        Label label = LabelFactory.create();
        label.setText(selector.getType());
        container.add(label);
        Component component = selector.getComponent();
        container.add(component);
        GridLayoutData layoutData = new GridLayoutData();
        layoutData.setColumnSpan(3);
        component.setLayoutData(layoutData);

        selector.getSelect().setFocusTraversalParticipant(false);
        getFocusGroup().add(selector.getFocusGroup());
    }

}
