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

import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityRelationshipEditor;
import org.openvpms.web.component.im.view.ComponentState;

/**
 * Editor for <em>entityRelationship.productStockLocation</em> relationships that constrains preferred suppliers
 * to those linked to the product.
 *
 * @author Tim Anderson
 */
public class ProductStockLocationEditor extends EntityRelationshipEditor {

    /**
     * The preferred supplier reference editor.
     */
    private final PreferredSupplierEditor supplier;

    /**
     * The product editor.
     */
    private ProductEditor productEditor;

    /**
     * Constructs a {@link ProductStockLocationEditor}.
     *
     * @param relationship the relationship
     * @param parent       the parent object
     * @param context      the layout context
     */
    public ProductStockLocationEditor(EntityRelationship relationship, IMObject parent, LayoutContext context) {
        super(relationship, parent, context);
        supplier = new PreferredSupplierEditor(this, getProperty("supplier"), getLayoutContext());
        getEditors().add(supplier);
        getArchetypeNodes().simple("supplier");
    }

    /**
     * Registers the product editor.
     *
     * @param editor the editor. May be {@code null}
     */
    public void setProductEditor(ProductEditor editor) {
        productEditor = editor;
    }

    /**
     * Returns the product editor.
     *
     * @return the product editor. May be {@code null}
     */
    public ProductEditor getProductEditor() {
        return productEditor;
    }

    /**
     * Returns the product.
     *
     * @return the product. May be {@code null}
     */
    public Product getProduct() {
        IMObject parent = getParent();
        return (parent instanceof Product) ? (Product) parent : (Product) getObject(getSource().getReference());
    }

    /**
     * Returns the stock location.
     *
     * @return the stock location. May be {@code null}
     */
    public Party getStockLocation() {
        IMObject parent = getParent();
        return (parent instanceof Party) ? (Party) parent : (Party) getObject(getTarget().getReference());
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategy strategy = super.createLayoutStrategy();
        strategy.addComponent(new ComponentState(supplier));
        return strategy;
    }
}
