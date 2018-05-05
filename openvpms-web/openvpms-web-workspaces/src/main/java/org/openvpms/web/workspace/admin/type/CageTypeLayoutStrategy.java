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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.type;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.BoundAbsoluteTimeField;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;


/**
 * Layout strategy for <em>entity.cageType</em> archetypes.
 * <p/>
 * This displays the <em>lateCheckoutTime</em> node as a time field.
 *
 * @author Tim Anderson
 */
public class CageTypeLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a {@code Component}, using a factory to create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        BoundAbsoluteTimeField lateCheckoutTime = new BoundAbsoluteTimeField(properties.get("lateCheckoutTime"),
                                                                             false);
        if (!context.isEdit()) {
            lateCheckoutTime.setEnabled(false);
        }
        addComponent(new ComponentState(lateCheckoutTime, lateCheckoutTime.getProperty()));
        return super.apply(object, properties, parent, context);
    }

}
