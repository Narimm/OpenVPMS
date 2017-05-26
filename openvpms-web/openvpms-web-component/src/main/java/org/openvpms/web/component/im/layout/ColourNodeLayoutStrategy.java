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

package org.openvpms.web.component.im.layout;

import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.bound.BoundColorSelect;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.colour.ColourHelper;


/**
 * Layout strategy that uses a {@link BoundColorSelect} for any "colour" node.
 *
 * @author Tim Anderson
 */
public class ColourNodeLayoutStrategy extends AbstractLayoutStrategy {

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
        addColour(object, properties, context);
        return super.apply(object, properties, parent, context);
    }

    /**
     * Pre-registers a component to display the colour node.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param context    the layout context
     */
    protected void addColour(IMObject object, PropertySet properties, LayoutContext context) {
        Property property = properties.get("colour");
        if (property != null) {
            ComponentState state;
            if (context.isEdit()) {
                Component component = new BoundColorSelect(property);
                state = new ComponentState(component, property);
            } else {
                state = createComponent(property, object, context);
                String value = (String) property.getValue();
                if (value != null) {
                    Color color = ColourHelper.getColor(value);
                    state.getComponent().setBackground(color);
                }
            }
            addComponent(state);
        }
    }

}
