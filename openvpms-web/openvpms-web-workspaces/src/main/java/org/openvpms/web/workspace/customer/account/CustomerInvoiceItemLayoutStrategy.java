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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.account;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;

import java.util.ArrayList;
import java.util.List;


/**
 * Layout strategy for <em>act.customerAccountInvoiceItem</em> that hides
 * the dispensing, investigation and reminder nodes if they are empty.
 *
 * @author Tim Anderson
 */
public class CustomerInvoiceItemLayoutStrategy extends AbstractLayoutStrategy {

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
     * The alerts node name.
     */
    private static final String ALERTS = "alerts";

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a {@code Component}, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        ActBean bean = new ActBean((Act) object);
        List<String> exclude = new ArrayList<>();
        if (bean.getValues(DISPENSING).isEmpty()) {
            exclude.add(DISPENSING);
        }
        if (bean.getValues(INVESTIGATIONS).isEmpty()) {
            exclude.add(INVESTIGATIONS);
        }
        if (bean.getValues(REMINDERS).isEmpty()) {
            exclude.add(REMINDERS);
        }
        if (bean.getValues(ALERTS).isEmpty()) {
            exclude.add(ALERTS);
        }
        ArchetypeNodes nodes;
        if (!exclude.isEmpty()) {
            nodes = ArchetypeNodes.all().exclude(exclude);
        } else {
            nodes = DEFAULT_NODES;
        }
        setArchetypeNodes(nodes);
        return super.apply(object, properties, parent, context);
    }

}
