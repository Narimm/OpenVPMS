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
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractEntityQuery;
import org.openvpms.web.component.im.query.DateRange;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.echo.text.TextField;

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
     * The manufacturer filter.
     */
    private TextField manufacturer;

    /**
     * Constructs a {@link ProductBatchQuery}.
     *
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public ProductBatchQuery() {
        this(new String[]{ProductArchetypes.PRODUCT_BATCH});
    }

    /**
     * Constructs a {@link ProductBatchQuery}.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public ProductBatchQuery(String[] shortNames) {
        super(shortNames);
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
     * Sets the manufacturer name.
     *
     * @param manufacturer the manufacturer name. May be {@code null}
     */
    public void setManufacturer(String manufacturer) {
        getManufacturerField().setText(manufacturer);
    }

    /**
     * Returns the manufacturer name.
     *
     * @return the manufacturer name. May be {@code null}
     */
    public String getManufacturer() {
        return getManufacturerField().getText();
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
     * Lays out the component in a container, and sets focus on the search field.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        addShortNameSelector(container);
        addSearchField(container);
        if (product == null) {
            addProductName(container);
        }
        addExpiryDate(container);
        addManufacturer(container);
        addActive(container);
        FocusHelper.setFocus(getSearchField());
        getExpiryDate().setFrom(DateRules.getToday());
        getExpiryDate().setTo(null);
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
     * Returns the manufacturer field.
     *
     * @return the manufacturer field
     */
    protected TextField getManufacturerField() {
        if (manufacturer == null) {
            manufacturer = TextComponentFactory.create();
            manufacturer.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onQuery();
                }
            });
        }
        return manufacturer;
    }

    /**
     * Returns the expiry date range.
     *
     * @return the expiry date range
     */
    protected DateRange getExpiryDate() {
        if (expiryDate == null) {
            expiryDate = new DateRange(getFocusGroup(), false) {
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
     * Adds the manufacturer field to a container.
     *
     * @param container the container
     */
    protected void addManufacturer(Component container) {
        Label label = LabelFactory.create("product.batch.manufacturer");
        container.add(label);
        TextField field = getManufacturerField();
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Adds a date range to the container.
     *
     * @param container the container
     */
    protected void addExpiryDate(Component container) {
        container.add(getExpiryDate().getComponent());
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
        String manufacturer = getWildcardedText(getManufacturerField());
        Date from = getExpiryDate().getFrom();
        Date to = getExpiryDate().getTo();
        return new ProductBatchResultSet(getArchetypeConstraint(), getValue(), product, productName, from, to,
                                         manufacturer, sort, getMaxResults());
    }

}
