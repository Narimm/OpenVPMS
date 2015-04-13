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

package org.openvpms.archetype.rules.product;

import org.openvpms.archetype.rules.util.MappingCopyHandler;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;


/**
 * An {@link IMObjectCopyHandler} that creates copies of products.
 *
 * @author Tim Anderson
 */
public class ProductCopyHandler extends MappingCopyHandler {

    /**
     * The product being copied.
     */
    private final IMObjectReference product;

    /**
     * Creates a {@link ProductCopyHandler}.
     *
     * @param product the product being copied
     */
    public ProductCopyHandler(Product product) {
        this.product = product.getObjectReference();
        setCopy(Product.class);
        setReference(Entity.class); // reference all other entities
    }

    /**
     * Determines how an object should be handled.
     * <p/>
     * This implementation changes the order of evaluation so that {@link #copy} is evaluated prior to
     * {@link #reference}.
     *
     * @param object the object
     * @return the type of behaviour to apply to the object
     */
    @Override
    protected Treatment getTreatment(IMObject object) {
        if (copy(object)) {
            return Treatment.COPY;
        } else if (reference(object)) {
            return Treatment.REFERENCE;
        } else if (exclude(object)) {
            return Treatment.EXCLUDE;
        }
        return getDefaultTreatment();
    }

    /**
     * Determines if an object should be copied.
     * <p/>
     * This implementation returns {@code true} if the object is the specified product.
     *
     * @param object the object to check
     * @return {@code true} if it should be copied
     */
    @Override
    protected boolean copy(IMObject object) {
        return object.getObjectReference().equals(product);
    }

    /**
     * Determines how a node should be copied.
     * <p/>
     * This implementation excludes the quantity node from <em>entityRelationship.productStockLocation</em> when
     * copying products.
     *
     * @param source     the source archetype
     * @param sourceNode the source node
     * @param target     the target archetype
     * @return a node to copy sourceNode to, or {@code null} if the node shouldn't be copied
     */
    @Override
    public NodeDescriptor getNode(ArchetypeDescriptor source, NodeDescriptor sourceNode, ArchetypeDescriptor target) {
        boolean isStockLocation = TypeHelper.isA(source, "entityRelationship.productStockLocation");
        if (isStockLocation && "quantity".equals(sourceNode.getName())) {
            return null;
        }
        return super.getNode(source, sourceNode, target);
    }

}
