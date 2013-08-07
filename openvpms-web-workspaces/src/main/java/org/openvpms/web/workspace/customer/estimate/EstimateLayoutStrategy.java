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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.estimate;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.TextArea;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.style.Styles;

import java.util.List;

/**
 * Layout strategy for <em>act.customerEstimation</em> acts.
 *
 * @author Tim Anderson
 */
public class EstimateLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The archetype nodes. This excludes the lowTotal, highTotal and notes nodes, as they are handled explicitly.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes().exclude("lowTotal", "highTotal", "notes");


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
        Property lowTotal = properties.get("lowTotal");
        Property highTotal = properties.get("highTotal");
        addComponent(createComponent(lowTotal, object, context));
        addComponent(createComponent(highTotal, object, context));
        addComponent(createNotes(object, properties, context));
        return super.apply(object, properties, parent, context);
    }

    /**
     * Lays out child components in a grid.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be {@code null}
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doSimpleLayout(IMObject object, IMObject parent, List<Property> properties, Component container,
                                  LayoutContext context) {
        ComponentGrid grid = createGrid(object, properties, context);
        grid.add(getComponent("notes"), 2);
        grid.add(getComponent("lowTotal"), getComponent("highTotal"));
        Component component = createGrid(grid);
        container.add(ColumnFactory.create(Styles.INSET, component));
    }

    /**
     * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
     *
     * @return the archetype nodes
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes() {
        return NODES;
    }

    /**
     * Creates a component for the "notes" node.
     *
     * @param object     the parent object
     * @param properties the properties
     * @param context    the layout context
     * @return a new component
     */
    private ComponentState createNotes(IMObject object, PropertySet properties, LayoutContext context) {
        ComponentState notes = createComponent(properties.get("notes"), object, context);
        Component component = notes.getComponent();
        if (component instanceof TextArea) {
            TextArea text = (TextArea) component;
            text.setHeight(new Extent(5, Extent.EM));
            text.setWidth(Styles.FULL_WIDTH);
        }
        return notes;
    }

}
