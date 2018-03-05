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

package org.openvpms.web.component.im.product;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;

/**
 * A layout strategy for products.
 * <p/>
 * This excludes the locations node, if products aren't being filtered by location.
 *
 * @author Tim Anderson
 */
public class ProductLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Concentration node name.
     */
    private static final String CONCENTRATION = "concentration";

    /**
     * Concentration units node name.
     */
    private static final String CONCENTRATION_UNITS = "concentrationUnits";

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
        boolean useLocationProducts = ProductHelper.useLocationProducts(context.getContext());
        ArchetypeNodes nodes = new ArchetypeNodes();
        if (!useLocationProducts) {
            nodes.exclude("locations");
        }
        if (properties.get(CONCENTRATION) != null && properties.get(CONCENTRATION_UNITS) != null) {
            // display the concentration and concentration units next to each other
            addComponent(createComponentPair(CONCENTRATION, CONCENTRATION_UNITS, object, properties, context));
            nodes.exclude(CONCENTRATION_UNITS);
        }
        setArchetypeNodes(nodes);
        return super.apply(object, properties, parent, context);
    }
}
