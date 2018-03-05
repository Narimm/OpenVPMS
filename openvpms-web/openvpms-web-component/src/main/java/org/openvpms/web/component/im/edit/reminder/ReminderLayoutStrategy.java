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

package org.openvpms.web.component.im.edit.reminder;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;


/**
 * Layout strategy for <em>act.patientReminder</em> acts.
 * <p/>
 * This suppresses the product node if the parent act has a product.
 *
 * @author Tim Anderson
 */
public class ReminderLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a {@code Component}, using a factory to create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be <tt>null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        boolean showProduct;
        if (parent instanceof Act) {
            ActBean bean = new ActBean((Act) parent);
            showProduct = !bean.hasNode("product");
        } else {
            showProduct = true;
        }
        ArchetypeNodes nodes = (showProduct) ? DEFAULT_NODES : new ArchetypeNodes().exclude("product");
        setArchetypeNodes(nodes);
        if (context.isEdit()) {
            int reminderCount = properties.get("reminderCount").getInt();
            if (reminderCount != 0) {
                // don't allow editing the 'Reminder Type' or 'First Due Date' for anything but the original Reminder
                // Count
                addComponent(createComponent(createReadOnly(properties.get("reminderType")), object, context));
                addComponent(createComponent(createReadOnly(properties.get("endTime")), object, context));
            } else {
                // don't allow editing of the 'Next Due Date' for the original Reminder Count, as it is the same
                // as the First Due Date
                addComponent(createComponent(createReadOnly(properties.get("startTime")), object, context));
            }
        }
        return super.apply(object, properties, parent, context);
    }

}
