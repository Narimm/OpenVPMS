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
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;

import java.util.List;

/**
 * Layout strategy for <em>entity.productBatch</em>.
 *
 * @author Tim Anderson
 */
public abstract class AbstractProductBatchLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The nodes to display.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes().exclude("product");

    /**
     * @param object     the object to lay out
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param container  the container to use
     * @param context    the layout context
     * @param product    the product property
     * @param expiryDate the expiry date property
     */
    protected void doLayout(IMObject object, PropertySet properties, IMObject parent, Component container,
                            LayoutContext context, Property product, Property expiryDate) {
        ArchetypeDescriptor descriptor = context.getArchetypeDescriptor(object);

        List<Property> simple = NODES.getSimpleNodes(properties, descriptor, object, null);
        List<Property> complex = NODES.getComplexNodes(properties, descriptor, object, null);

        if (product != null && expiryDate != null) {
            ArchetypeNodes.insert(simple, "name", product, expiryDate);
        }
        doSimpleLayout(object, parent, simple, container, context);
        doComplexLayout(object, parent, complex, container, context);
    }

    /**
     * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
     *
     * @return the archetype nodes
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes() {
        return NODES;
    }

}
