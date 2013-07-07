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

package org.openvpms.web.workspace.workflow.messaging;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.text.TextComponent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.style.Styles;

import java.util.List;

import static org.openvpms.web.component.im.layout.ArchetypeNodes.exclude;
import static org.openvpms.web.component.im.layout.ArchetypeNodes.include;


/**
 * Layout strategy for <em>act.systemMessage</em>.
 *
 * @author Tim Anderson
 */
public class SystemMessageLayoutStrategy extends AbstractMessageLayoutStrategy {

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
        ComponentState to = createComponent(properties.get("to"), object, context);
        ComponentState description = createComponent(properties.get("description"), object, context);
        ComponentState message = createMessage(properties, context, !showItem(context, object));

        if (description.getComponent() instanceof TextComponent) {
            ((TextComponent) description.getComponent()).setWidth(Styles.FULL_WIDTH);
        }

        addComponent(to);
        addComponent(description);
        addComponent(message);
        return super.apply(object, properties, parent, context);
    }

    /**
     * Lay out out the object in the specified container.
     *
     * @param object     the object to lay out
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doLayout(IMObject object, PropertySet properties, IMObject parent, Component container,
                            LayoutContext context) {
        ArchetypeDescriptor archetype = context.getArchetypeDescriptor(object);
        ArchetypeNodes nodes = getArchetypeNodes();
        NodeFilter filter = getNodeFilter(object, context);

        List<NodeDescriptor> simple = nodes.getSimpleNodes(archetype, object, filter);
        List<NodeDescriptor> complex = nodes.getComplexNodes(archetype, object, filter);

        List<NodeDescriptor> to = include(simple, "to");
        List<NodeDescriptor> header = include(simple, "description", "reason");
        List<NodeDescriptor> fields = exclude(simple, "to", "description", "reason", "startTime", "message", "status");
        List<NodeDescriptor> message = include(simple, "message");

        ComponentGrid componentGrid = new ComponentGrid();
        ComponentSet toSet = createComponentSet(object, to, properties, context);
        if (!context.isEdit()) {
            ComponentState date = createDate((Act) object);
            toSet.add(date);
        }

        ComponentSet headerSet = createComponentSet(object, header, properties, context);
        ComponentSet fieldSet = createComponentSet(object, fields, properties, context);
        ComponentSet messageSet = createComponentSet(object, message, properties, context);
        componentGrid.add(toSet, 2);
        componentGrid.add(headerSet, 1, 2);
        componentGrid.add(fieldSet, 2);
        componentGrid.add(messageSet, 1, 2);

        if (!showItem(context, object)) {
            complex = exclude(complex, "item");
        }

        Grid grid = createGrid(componentGrid);
        grid.setWidth(Styles.FULL_WIDTH);

        Component child = ColumnFactory.create("Inset.Large", grid);
        doComplexLayout(object, parent, complex, properties, child, context);

        container.add(child);
    }

    /**
     * Creates a component to display the message.
     *
     * @param properties the properties
     * @param context    the layout context
     * @param fullHeight if {@code true} display the message to its maximum
     * @return a component to display the message
     */
    private ComponentState createMessage(PropertySet properties, LayoutContext context, boolean fullHeight) {
        String styleName = (fullHeight) ? "UserMessage.message" : "SystemMessage.message";
        return createMessage(properties, context, styleName);
    }

    /**
     * Determines if the item node should be displayed.
     *
     * @param context the layout context
     * @param object  the object to display
     * @return {@code true} if the node should be displayed, otherwise {@code false}
     */
    private boolean showItem(LayoutContext context, IMObject object) {
        boolean result = context.isEdit();
        if (!result) {
            IMObjectBean bean = new IMObjectBean(object);
            result = !bean.getNodeTargetObjectRefs("item").isEmpty();
        }
        return result;
    }

}
