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

package org.openvpms.web.workspace.product.batch;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityLinkCollectionEditor;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;

/**
 * An editor for <em>entity.productBatch</em> entities.
 * <p/>
 * This has an expiryDate on its product node (an entityLink.batchProduct). Ideally this would be part of
 * entity.productBatch, except that it needs to be used in queries, and placing it on the entityLink is the only
 * way to do this at present.
 * <p/>
 * As a result, some hoops need to be jumped through in order to render the expiryDate at the same level as the
 * product.
 *
 * @author Tim Anderson
 */
public class ProductBatchEditor extends AbstractIMObjectEditor {

    /**
     * The product property.
     */
    private final Property product;

    /**
     * The expiry date property.
     */
    private final Property expiryDate;

    /**
     * Constructs an {@link ProductBatchEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public ProductBatchEditor(Entity object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        CollectionProperty collection = getCollectionProperty("product");
        EntityLinkCollectionEditor productCollectionEditor = new EntityLinkCollectionEditor(collection, object, layoutContext);
        if (collection.size() == 0) {
            productCollectionEditor.add(productCollectionEditor.create());
        }
        getEditors().add(productCollectionEditor);
        IMObjectEditor linkEditor = productCollectionEditor.getEditor((IMObject) collection.getValues().get(0));
        product = linkEditor.getProperty("target");
        expiryDate = linkEditor.getProperty("activeEndTime");
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new AbstractProductBatchLayoutStrategy() {
            @Override
            protected void doLayout(IMObject object, PropertySet properties, IMObject parent, Component container,
                                    LayoutContext context) {
                super.doLayout(object, properties, parent, container, context, product, expiryDate);
            }
        };
    }
}
