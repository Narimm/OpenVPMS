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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.style.Styles;

import java.util.List;

import static org.openvpms.web.component.im.layout.ArchetypeNodes.exclude;
import static org.openvpms.web.component.im.layout.ArchetypeNodes.include;


/**
 * Layout strategy for <em>act.userMessage</em>.
 *
 * @author Tim Anderson
 */
public class UserMessageLayoutStrategy extends AbstractMessageLayoutStrategy {

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
        ComponentState from = createComponent(properties.get("from"), object, context);
        ComponentState to = createComponent(properties.get("to"), object, context);
        ComponentState description = createComponent(properties.get("description"), object, context);
        ComponentState message = createMessage(properties, context, "UserMessage.message");

        if (description.getComponent() instanceof TextComponent) {
            ((TextComponent) description.getComponent()).setWidth(Styles.FULL_WIDTH);
        }

        addComponent(from);
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

        List<Property> simple = nodes.getSimpleNodes(properties, archetype, object, filter);
        List<Property> complex = nodes.getComplexNodes(properties, archetype, object, filter);

        List<Property> from = include(simple, "from");
        List<Property> header = include(simple, "to", "description");
        List<Property> customer = include(simple, "customer", "patient");
        List<Property> fields = exclude(simple, "from", "to", "description", "startTime", "customer", "patient",
                                        "message", "status");

        if (!context.isEdit()) {
            // hide empty customer and patient nodes in view layout
            ActBean bean = new ActBean((Act) object);
            if (bean.getNodeParticipantRef("customer") == null) {
                customer = exclude(customer, "customer");
            }
            if (bean.getNodeParticipantRef("patient") == null) {
                customer = exclude(customer, "patient");
            }
        }
        List<Property> message = include(simple, "message");

        ComponentGrid componentGrid = new ComponentGrid();
        ComponentSet fromSet = createComponentSet(object, from, context);
        ComponentSet headerSet = createComponentSet(object, header, context);
        ComponentSet customerSet = createComponentSet(object, customer, context);
        ComponentSet fieldSet = createComponentSet(object, fields, context);
        ComponentSet messageSet = createComponentSet(object, message, context);
        componentGrid.add(fromSet);
        if (!context.isEdit()) {
            ComponentState date = createDate((Act) object);
            componentGrid.set(0, 2, date);
        }
        componentGrid.add(headerSet, 1, 2);
        if (customerSet.size() != 0) {
            // in view mode, display customer and patient on separate rows as it looks odd on wide-screens.
            // In the dialog (width-constrained) it looks better on one row.
            int columns = (context.isEdit()) ? customerSet.size() : 1;
            componentGrid.add(customerSet, columns);
        }
        componentGrid.add(fieldSet, 2);
        componentGrid.add(messageSet, 1, 2);
        Grid grid = createGrid(componentGrid);
        grid.setWidth(Styles.FULL_WIDTH);

        Component child = ColumnFactory.create(Styles.LARGE_INSET, grid);
        doComplexLayout(object, parent, complex, child, context);

        container.add(child);
    }

}
