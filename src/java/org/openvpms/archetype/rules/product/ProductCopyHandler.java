/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.product;

import org.openvpms.archetype.rules.util.MappingCopyHandler;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopyHandler;


/**
 * {@link IMObjectCopyHandler} that creates copies of products.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-12-12 04:10:40Z $
 */
public class ProductCopyHandler extends MappingCopyHandler {

    /**
     * The product being copied.
     */
    private final IMObjectReference product;

    /**
     * Creates a <tt>ProductCopyHandler</tt>.
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
     * This implementation changes the order of evaluation so that {@link #copy}
     * is evaluated prior to {@link #reference}.
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
     * This implementation returns <tt>true</tt> if the object is the specified product.
     *
     * @param object the object to check
     * @return <tt>true</tt> if it should be copied
     */
    @Override
    protected boolean copy(IMObject object) {
        return object.getObjectReference().equals(product);
    }
}
