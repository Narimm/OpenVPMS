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

package org.openvpms.web.workspace.patient.insurance.claim;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.text.TextArea;

/**
 * Layout strategy for claim archetypes.
 *
 * @author Tim Anderson
 */
public abstract class AbstractClaimLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Constructs an {@link AbstractClaimLayoutStrategy}.
     */
    public AbstractClaimLayoutStrategy() {
        super();
    }

    /**
     * Constructs an {@link AbstractClaimLayoutStrategy}.
     *
     * @param nodes the nodes to render
     */
    public AbstractClaimLayoutStrategy(ArchetypeNodes nodes) {
        super(nodes);
    }

    /**
     * Creates a component for the "notes" node.
     *
     * @param object     the parent object
     * @param properties the properties
     * @param context    the layout context
     * @return a new component
     */
    protected ComponentState createNotes(IMObject object, PropertySet properties, LayoutContext context) {
        Property property = properties.get("notes");
        return createTextArea(property, object, context);
    }

    /**
     * Creates a text area for a property, limiting the height to 4 rows.
     *
     * @param property the property
     * @param object   the parent object
     * @param context  the layout context
     * @return a new component
     */
    protected ComponentState createTextArea(Property property, IMObject object, LayoutContext context) {
        ComponentState state = createComponent(property, object, context);
        Component component = state.getComponent();
        if (component instanceof TextArea) {
            TextArea text = (TextArea) component;
            text.setHeight(new Extent(4, Extent.EM));
        }
        return state;
    }

}
