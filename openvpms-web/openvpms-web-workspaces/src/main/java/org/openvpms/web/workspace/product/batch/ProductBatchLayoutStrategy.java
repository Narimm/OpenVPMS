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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.PeriodRelationship;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;

import java.util.List;

/**
 * Layout strategy for <em>entity.productBatch</em>.
 * <p/>
 * Note that this layout is only suitable for viewing batches.
 *
 * @author Tim Anderson
 */
public class ProductBatchLayoutStrategy extends AbstractProductBatchLayoutStrategy {

    /**
     * Lay out out the object in the specified container.
     *
     * @param object     the object to lay out
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doLayout(IMObject object, PropertySet properties, IMObject parent, Component container,
                            LayoutContext context) {
        IMObjectBean bean = new IMObjectBean(object);
        List<PeriodRelationship> values = bean.getValues("product", PeriodRelationship.class);

        Property product = null;
        Property expiryDate = null;
        if (!values.isEmpty()) {
            PropertySet relationship = new PropertySet(values.get(0), context);
            product = relationship.get("target");
            expiryDate = relationship.get("activeEndTime");
        }

        doLayout(object, properties, parent, container, context, product, expiryDate);
    }

}
