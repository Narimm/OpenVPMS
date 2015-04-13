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

package org.openvpms.web.component.im.view.layout;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;

import java.util.List;


/**
 * Participation layout strategy. This displays the "entity" node.
 *
 * @author Tim Anderson
 */
public class ParticipationLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The nodes to display. This excludes all but the entity node, which is treated as a "simple" node.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes(false, false).simple("entity");

    /**
     * Lays out the child components.
     * <p/>
     * If there is only one component, this will be rendered inline without any label.
     * Multiple components will be rendered with labels in a grid.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be {@code null}
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doSimpleLayout(IMObject object, IMObject parent, List<Property> properties,
                                  Component container, LayoutContext context) {
        if (properties.size() == 1) {
            ComponentSet set = createComponentSet(object, properties, context);
            ComponentState state = set.getComponents().get(0);
            container.add(state.getComponent());
            setFocusTraversal(state);
        } else {
            super.doSimpleLayout(object, parent, properties, container, context);
        }
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
}
