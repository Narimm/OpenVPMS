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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.layout;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.BoundColorSelect;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.util.ColourHelper;


/**
 * Layout strategy that uses a {@link BoundColorSelect} for any "colour" node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ColourNodeLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Creates a component for a property.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display <tt>property</tt>
     */
    @Override
    protected ComponentState createComponent(Property property,
                                             IMObject parent,
                                             LayoutContext context) {
        ComponentState state;
        if ("colour".equals(property.getName())) {
            if (context.isEdit()) {
                Component component = new BoundColorSelect(property);
                state = new ComponentState(component, property);
            } else {
                state = super.createComponent(property, parent, context);
                String value = (String) property.getValue();
                if (value != null) {
                    Color color = ColourHelper.getColor(value);
                    state.getComponent().setBackground(color);
                }
            }
        } else {
            state = super.createComponent(property, parent, context);
        }
        return state;
    }

}
