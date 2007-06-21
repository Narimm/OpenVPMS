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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.customer.account;

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
 * Layout strategy for <em>act.customerAccountInvoiceItem</em> that hides
 * the dispensing node if it is empty.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerInvoiceItemLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Determines if the dispensing node should be hidden.
     */
    private boolean hideDispensing;

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be <code>null</code>
     * @param context    the layout context
     * @return the component containing the rendered <code>object</code>
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties,
                                IMObject parent, LayoutContext context) {
        ActBean bean = new ActBean((Act) object);
        hideDispensing = bean.getValues("dispensing").isEmpty();
        return super.apply(object, properties, parent, context);
    }

    /**
     * Returns a node filter to filter nodes.
     *
     * @param context the context
     * @return a node filter to filter nodes, or <code>null</code> if no
     *         filterering is required
     */
    @Override
    protected NodeFilter getNodeFilter(LayoutContext context) {
        NodeFilter filter;
        if (hideDispensing) {
            filter = getNodeFilter(context, new NamedNodeFilter("dispensing"));
        } else {
            filter = super.getNodeFilter(context);
        }
        return filter;
    }
}
