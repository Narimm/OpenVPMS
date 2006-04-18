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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.layout;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import echopointng.DateField;
import echopointng.TabbedPane;
import echopointng.tabbedpane.DefaultTabModel;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.button.AbstractButton;
import nextapp.echo2.app.text.TextComponent;
import org.apache.commons.lang.StringUtils;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.focus.FocusSet;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;


/**
 * Abstract implementation of the {@link IMObjectLayoutStrategy} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractLayoutStrategy implements IMObjectLayoutStrategy {

    /**
     * Map of properties to their corresponding components, used to set focus.
     */
    private final Map<Property, Component> _components
            = new LinkedHashMap<Property, Component>();

    /**
     * The focus set.
     */
    private FocusSet _focusSet;

    /**
     * Construct a new <code>AbstractLayoutStrategy</code>.
     */
    public AbstractLayoutStrategy() {
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param context    the layout context
     * @return the component containing the rendered <code>object</code>
     */
    public Component apply(IMObject object, PropertySet properties,
                           LayoutContext context) {
        _components.clear();
        _focusSet = new FocusSet(DescriptorHelper.getDisplayName(object));
        context.getFocusTree().add(_focusSet);
        Column column = ColumnFactory.create("CellSpacing");
        doLayout(object, properties, column, context);
        setFocus(column);
        return column;
    }

    /**
     * Lay out out the object in the specified container.
     *
     * @param object     the object to lay out
     * @param properties the object's properties
     * @param container  the container to use
     * @param context    the layout context
     */
    protected void doLayout(IMObject object, PropertySet properties,
                            Component container, LayoutContext context) {
        ArchetypeDescriptor descriptor
                = DescriptorHelper.getArchetypeDescriptor(object);
        List<NodeDescriptor> simple;
        List<NodeDescriptor> complex;

        NodeFilter filter = getNodeFilter(context);
        simple = filter(object, descriptor.getSimpleNodeDescriptors(), filter);
        complex = filter(object, descriptor.getComplexNodeDescriptors(), filter);

        doSimpleLayout(object, simple, properties, container, context);
        doComplexLayout(object, complex, properties, container, context);
    }

    /**
     * Lays out child components in a 2x2 grid.
     *
     * @param object      the parent object
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param container   the container to use
     * @param context     the layout context
     */
    protected void doSimpleLayout(IMObject object,
                                  List<NodeDescriptor> descriptors,
                                  PropertySet properties, Component container,
                                  LayoutContext context) {
        if (!descriptors.isEmpty()) {
            Grid grid = GridFactory.create(4);
            doGridLayout(object, descriptors, properties, grid, context);
            container.add(grid);
        }
    }

    /**
     * Lays out each child component in a tabbed pane.
     *
     * @param object      the parent object
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param container   the container to use
     * @param context     the layout context
     */
    protected void doComplexLayout(IMObject object,
                                   List<NodeDescriptor> descriptors,
                                   PropertySet properties, Component container,
                                   LayoutContext context) {
        if (!descriptors.isEmpty()) {
            DefaultTabModel model = new DefaultTabModel();
            for (NodeDescriptor nodeDesc : descriptors) {
                Property property = properties.get(nodeDesc);
                Component child = createComponent(property, object, context);

                DefaultTabModel.TabButton button
                        = model.new TabButton(nodeDesc.getDisplayName(), null);
                button.setFocusTraversalParticipant(false);
                // @todo - button doesn't respond to keypress, so don't focus
                // on it.

                Component inset = ColumnFactory.create("Inset", child);
                model.insertTab(model.size(), button, inset);
                setTabIndex(child, context);
            }
            TabbedPane pane = new TabbedPane();
            pane.setModel(model);
            pane.setSelectedIndex(0);
            container.add(pane);
        }
    }

    /**
     * Returns a node filter to filter nodes. This implementation return {@link
     * LayoutContext#getDefaultNodeFilter()}.
     *
     * @param context the context
     * @return a node filter to filter nodes, or <code>null</code> if no
     *         filterering is required
     */
    protected NodeFilter getNodeFilter(LayoutContext context) {
        return context.getDefaultNodeFilter();
    }

    /**
     * Helper to create a chained node filter from the default node filter and a
     * custom node filter.
     *
     * @param context the context
     * @param filter  the node filter
     */
    protected ChainedNodeFilter getNodeFilter(LayoutContext context,
                                              NodeFilter filter) {
        return FilterHelper.chain(context.getDefaultNodeFilter(), filter);
    }

    /**
     * Filters a set of node descriptors, using the specfied node filter.
     *
     * @param object      the object
     * @param descriptors the node descriptors to filter
     * @param filter      the filter to use
     * @return the filtered nodes
     */
    protected List<NodeDescriptor> filter(IMObject object,
                                          List<NodeDescriptor> descriptors,
                                          NodeFilter filter) {
        return FilterHelper.filter(object, filter, descriptors);
    }

    /**
     * Lays out child components in 2 columns.
     *
     * @param object      the parent object
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param grid        the grid to use
     * @param context     the layout context
     */
    protected void doGridLayout(IMObject object,
                                List<NodeDescriptor> descriptors,
                                PropertySet properties, Grid grid,
                                LayoutContext context) {
        int size = descriptors.size();
        Component[] components = new Component[size];
        String[] labels = new String[components.length];
        for (int i = 0; i < components.length; ++i) {
            NodeDescriptor descriptor = descriptors.get(i);
            labels[i] = descriptor.getDisplayName();
            Property property = properties.get(descriptor);
            Component component = createComponent(property, object, context);
            setTabIndex(component, context);
            if (component instanceof SelectField) {
                // workaround for render bug in firefox. See OVPMS-239 
                component = RowFactory.create(component);
            }
            components[i] = component;
        }

        int rows = (size / 2) + (size % 2);
        for (int i = 0, j = rows; i < rows; ++i, ++j) {
            add(grid, labels[i], components[i]);
            if (j < size) {
                add(grid, labels[j], components[j]);
            }
        }
    }

    /**
     * Helper to add a node to a container.
     *
     * @param container the container
     * @param name      the node display name
     * @param component the component representing the node
     */
    protected void add(Component container, String name, Component component) {
        Label label = LabelFactory.create();
        label.setText(name);
        container.add(label);
        container.add(component);
    }

    /**
     * Helper to add a node to a container, setting its tab index.
     *
     * @param container the container
     * @param name      the node display name
     * @param component the component representing the node
     * @param context   the layout context
     */
    protected void add(Component container, String name, Component component,
                       LayoutContext context) {
        add(container, name, component);
        setTabIndex(component, context);
    }

    /**
     * Creates a component for a property. This maintains a cache of created
     * components, in order for the focus to be set on an appropriate
     * component.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display <code>property</code>
     */
    protected Component createComponent(Property property, IMObject parent,
                                        LayoutContext context) {
        IMObjectComponentFactory factory = context.getComponentFactory();
        Component component = factory.create(property, parent);
        _components.put(property, component);
        return component;
    }

    /**
     * Sets focus on the first focusable field.
     *
     * @param container the component container
     */
    protected void setFocus(Component container) {
        Component focusable = null;
        for (Map.Entry<Property, Component> entry :
                _components.entrySet()) {
            Component child = entry.getValue();
            if (child instanceof TextComponent || child instanceof CheckBox
                || child instanceof DateField
                || child instanceof AbstractButton) {
                if (child.isEnabled() && child.isFocusTraversalParticipant()) {
                    Property property = entry.getKey();
                    try {
                        Object value = property.getValue();
                        if (value == null
                            || (value instanceof String
                                && StringUtils.isEmpty((String) value))) {
                            // null field. Set focus on it in preference to
                            // others
                            focusable = child;
                            break;
                        } else {
                            if (focusable == null) {
                                focusable = child;
                            }
                        }
                    } catch (DescriptorException ignore) {
                    }
                }
            }
        }
        if (focusable != null) {
            if (focusable instanceof DateField) {
                // @todo - workaround
                focusable = ((DateField) focusable).getTextField();
            }
            ApplicationInstance.getActive().setFocusedComponent(focusable);
        }
    }

    /**
     * Sets the tab index of a component, if it is a focus traversal
     * participant.
     *
     * @param component the component
     * @param context   the layout context
     */
    protected void setTabIndex(Component component, LayoutContext context) {
        if (component.isFocusTraversalParticipant()) {
            _focusSet.add(component);
        }
    }

}
