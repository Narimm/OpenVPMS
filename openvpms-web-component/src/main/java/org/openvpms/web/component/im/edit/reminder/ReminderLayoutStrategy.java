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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.im.edit.reminder;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;


/**
 * Layout strategy for <em>act.patientReminder</em> acts.
 * <p/>
 * This supresses the product node if the parent act has a product.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class ReminderLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Determines if the product node should be displayed. False if
     * the parent act has a product.
     */
    private boolean showProduct;

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be <tt>null</tt>
     * @param context    the layout context
     * @return the component containing the rendered <code>object</code>
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        if (parent instanceof Act) {
            ActBean bean = new ActBean((Act) parent);
            showProduct = !bean.hasNode("product");
        } else {
            showProduct = true;
        }
        return super.apply(object, properties, parent, context);
    }

    /**
     * Returns a node filter to filter nodes. This implementation filters
     * out the product node if {@link #showProduct} is <tt>false</tt>.
     *
     * @param object  the object to filter nodes for
     * @param context the context
     * @return a node filter to filter nodes, or <tt>null</tt> if no filterering is required
     */
    @Override
    protected NodeFilter getNodeFilter(IMObject object, LayoutContext context) {
        NodeFilter filter;
        if (!showProduct) {
            filter = super.getNodeFilter(context, new NamedNodeFilter("product"));
        } else {
            filter = super.getNodeFilter(object, context);
        }
        return filter;
    }

}
