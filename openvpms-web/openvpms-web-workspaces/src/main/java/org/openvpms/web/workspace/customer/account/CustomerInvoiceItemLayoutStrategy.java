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

package org.openvpms.web.workspace.customer.account;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;

import java.util.ArrayList;
import java.util.List;


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
     * Determines if the investigations node should be hidden.
     */
    private boolean hideInvestigations;

    /**
     * Determines if the reminders node should be hidden.
     */
    private boolean hideReminders;

    /**
     * The dispensing node name.
     */
    private static final String DISPENSING = "dispensing";

    /**
     * The investigations node name.
     */
    private static final String INVESTIGATIONS = "investigations";

    /**
     * The reminders node name.
     */
    private static final String REMINDERS = "reminders";

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <tt>Component</tt>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be <tt>null</tt>
     * @param context    the layout context
     * @return the component containing the rendered <tt>object</tt>
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties,
                                IMObject parent, LayoutContext context) {
        ActBean bean = new ActBean((Act) object);
        hideDispensing = bean.getValues(DISPENSING).isEmpty();
        hideInvestigations = bean.getValues(INVESTIGATIONS).isEmpty();
        hideReminders = bean.getValues(REMINDERS).isEmpty();
        return super.apply(object, properties, parent, context);
    }

    /**
     * Returns a node filter to filter nodes.
     *
     * @param object  the object
     * @param context the context
     * @return a node filter to filter nodes, or <tt>null</tt> if no filterering is required
     */
    @Override
    protected NodeFilter getNodeFilter(IMObject object, LayoutContext context) {
        NodeFilter filter;
        if (hideDispensing || hideInvestigations || hideReminders) {
            List<String> nodes = new ArrayList<String>();
            if (hideDispensing) {
                nodes.add(DISPENSING);
            }
            if (hideInvestigations) {
                nodes.add(INVESTIGATIONS);
            }
            if (hideReminders) {
                nodes.add(REMINDERS);
            }
            filter = getNodeFilter(context, new NamedNodeFilter(nodes));
        } else {
            filter = super.getNodeFilter(object, context);
        }
        return filter;
    }
}
