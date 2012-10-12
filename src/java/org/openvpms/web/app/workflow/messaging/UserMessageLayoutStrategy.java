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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.workflow.messaging;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.GridLayoutData;
import nextapp.echo2.app.text.TextComponent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.app.sms.BoundCountedTextArea;
import org.openvpms.web.component.bound.BoundTextArea;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.ComponentSet;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;

import java.util.List;

import static org.openvpms.web.component.im.filter.NamedNodeFilter.exclude;
import static org.openvpms.web.component.im.filter.NamedNodeFilter.include;


/**
 * Layout strategy for <em>act.userMessage</em>.
 *
 * @author Tim Anderson
 */
public class UserMessageLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Default extent - 100%.
     */
    private static final Extent FULL_WIDTH = new Extent(100, Extent.PERCENT);

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
        ComponentState message = createMessage(properties, context);

        if (description.getComponent() instanceof TextComponent) {
            ((TextComponent) description.getComponent()).setWidth(FULL_WIDTH);
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
        List<NodeDescriptor> simple = getSimpleNodes(archetype);
        List<NodeDescriptor> complex = getComplexNodes(archetype);

        NodeFilter filter = getNodeFilter(object, context);
        simple = filter(object, simple, filter);
        complex = filter(object, complex, filter);

        List<NodeDescriptor> from = filter(object, simple, include("from"));
        List<NodeDescriptor> header = filter(object, simple, include("to", "description"));
        List<NodeDescriptor> customer = filter(object, simple, include("customer", "patient"));
        List<NodeDescriptor> fields = filter(object, simple, exclude("from", "to", "description", "startTime",
                                                                     "customer", "patient", "message", "status"));

        if (!context.isEdit()) {
            // hide empty customer and patient nodes in view layout
            ActBean bean = new ActBean((Act) object);
            if (bean.getNodeParticipantRef("customer") == null) {
                customer = filter(object, customer, exclude("customer"));
            }
            if (bean.getNodeParticipantRef("patient") == null) {
                customer = filter(object, customer, exclude("patient"));
            }
        }
        List<NodeDescriptor> message = filter(object, simple, include("message"));

        ComponentGrid componentGrid = new ComponentGrid();
        ComponentSet fromSet = createComponentSet(object, from, properties, context);
        ComponentSet headerSet = createComponentSet(object, header, properties, context);
        ComponentSet customerSet = createComponentSet(object, customer, properties, context);
        ComponentSet fieldSet = createComponentSet(object, fields, properties, context);
        ComponentSet messageSet = createComponentSet(object, message, properties, context);
        componentGrid.add(fromSet);
        if (!context.isEdit()) {
            ComponentState date = createDate((Act) object);
            componentGrid.set(0, 1, date);
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
        grid.setWidth(FULL_WIDTH);

        Component child = ColumnFactory.create("Inset.Large", grid);
        doComplexLayout(object, parent, complex, properties, child, context);

        container.add(child);
    }

    /**
     * Creates a component to display the act date.
     *
     * @param act the act
     * @return a component to display the date
     */
    private ComponentState createDate(Act act) {
        Label label = LabelFactory.create();
        label.setText(MessageTableModel.formatStartTime(act));
        ComponentState date = new ComponentState(label);
        GridLayoutData layout = new GridLayoutData();
        layout.setAlignment(Alignment.ALIGN_RIGHT);
        label.setLayoutData(layout);
        return date;
    }

    /**
     * Creates a component to display the message.
     *
     * @param properties the properties
     * @param context    the layout context
     * @return a component to display the message
     */
    private ComponentState createMessage(PropertySet properties, LayoutContext context) {
        Property message = properties.get("message");
        TextComponent textArea;
        if (context.isEdit()) {
            textArea = new BoundCountedTextArea(message);
        } else {
            textArea = new BoundTextArea(message);
            textArea.setEnabled(false);
        }
        textArea.setStyleName("UserMessage.message");
        return new ComponentState(textArea, message);
    }

}
