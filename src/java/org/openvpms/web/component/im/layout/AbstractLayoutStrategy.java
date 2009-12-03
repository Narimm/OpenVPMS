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

import echopointng.TabbedPane;
import echopointng.tabbedpane.TabModel;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.focus.FocusHelper;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.TabPaneModel;
import org.openvpms.web.component.util.TabbedPaneFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract implementation of the {@link IMObjectLayoutStrategy} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractLayoutStrategy implements IMObjectLayoutStrategy {

    /**
     * List of component states, used to determine initial focus.
     */
    private final List<ComponentState> components
            = new ArrayList<ComponentState>();

    /**
     * The focus group of the current component.
     */
    private FocusGroup focusGroup;


    /**
     * Constructs a new <code>AbstractLayoutStrategy</code>.
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
     * @param parent     the parent object. May be <code>null</code>
     * @param context    the layout context
     * @return the component containing the rendered <code>object</code>
     */
    public ComponentState apply(IMObject object, PropertySet properties,
                                IMObject parent, LayoutContext context) {
        focusGroup = new FocusGroup(DescriptorHelper.getDisplayName(object));
        Column column = ColumnFactory.create("CellSpacing");
        doLayout(object, properties, column, context);
        setFocus();
        ComponentState state = new ComponentState(column, focusGroup);
        components.clear();
        focusGroup = null;
        return state;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or <code>null</code> if it hasn't been
     *         initialised
     */
    protected FocusGroup getFocusGroup() {
        return focusGroup;
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
        ArchetypeDescriptor archetype = context.getArchetypeDescriptor(object);
        List<NodeDescriptor> simple = getSimpleNodes(archetype);
        List<NodeDescriptor> complex = getComplexNodes(archetype);

        NodeFilter filter = getNodeFilter(object, context);
        simple = filter(object, simple, filter);
        complex = filter(object, complex, filter);

        doSimpleLayout(object, simple, properties, container, context);
        doComplexLayout(object, complex, properties, container, context);
    }

    /**
     * Lays out child components in a grid.
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
            Grid grid = createGrid(descriptors);
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
            TabModel model = doTabLayout(object, descriptors, properties,
                                         container, context);
            TabbedPane pane = TabbedPaneFactory.create(model);

            pane.setSelectedIndex(0);
            container.add(pane);
        }
    }

    /**
     * Returns the 'simple' nodes.
     *
     * @param archetype the archetype
     * @return the simple nodes
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor#getSimpleNodeDescriptors()
     */
    protected List<NodeDescriptor> getSimpleNodes(
            ArchetypeDescriptor archetype) {
        return archetype.getSimpleNodeDescriptors();
    }

    /**
     * Returns the 'complex' nodes.
     *
     * @param archetype the archetype
     * @return the complex nodes
     * @see org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor#getComplexNodeDescriptors()
     */
    protected List<NodeDescriptor> getComplexNodes(
            ArchetypeDescriptor archetype) {
        return archetype.getComplexNodeDescriptors();
    }

    /**
     * Returns a node filter to filter nodes. This implementation return {@link
     * LayoutContext#getDefaultNodeFilter()}.
     *
     * @param object  the object to filter nodes for
     * @param context the context
     * @return a node filter to filter nodes, or <code>null</code> if no
     *         filterering is required
     */
    protected NodeFilter getNodeFilter(IMObject object, LayoutContext context) {
        return context.getDefaultNodeFilter();
    }

    /**
     * Helper to create a chained node filter from the default node filter and a
     * custom node filter.
     *
     * @param context the context
     * @param filter  the node filter
     * @return a new chained node filter
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
     * Lays out child components in columns.
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
        ComponentSet set = createComponentSet(object, descriptors, properties,
                                              context);
        ComponentState[] states = set.getComponents().toArray(new ComponentState[set.getComponents().size()]);
        Component[] components = new Component[states.length];
        String[] labels = new String[states.length];
        for (int i = 0; i < states.length; ++i) {
            ComponentState state = states[i];
            Component component = state.getComponent();
            components[i] = component;
            labels[i] = set.getLabel(state);
            setFocusTraversal(state);
            if (component instanceof SelectField) {
                // workaround for render bug in firefox. See OVPMS-239
                components[i] = RowFactory.create(component);
            }
        }
        int size = components.length;
        int columns = (grid.getSize() <= 2) ? 1 : 2;
        int rows;
        if (columns == 1) {
            rows = size;
        } else {
            rows = (size / 2) + (size % 2);
        }
        for (int i = 0, j = rows; i < rows; ++i, ++j) {
            add(grid, labels[i], components[i]);
            if (j < size) {
                add(grid, labels[j], components[j]);
            }
        }
    }

    /**
     * Lays out a component grid.
     *
     * @param grid      the grid
     * @param container the container to add the grid to
     */
    protected void doGridLayout(ComponentGrid grid, Component container) {
        Grid g = GridFactory.create(grid.getColumns() * 2);
        for (int row = 0; row < grid.getRows(); ++row) {
            for (int col = 0; col < grid.getColumns(); ++col) {
                ComponentState state = grid.get(row, col);
                if (state != null) {
                    setFocusTraversal(state);
                    Component component = state.getComponent();
                    if (component instanceof SelectField) {
                        // workaround for render bug in firefox. See OVPMS-239
                        component = RowFactory.create(component);
                    }
                    add(g, state.getDisplayName(), component);
                } else {
                    add(g, null, LabelFactory.create());
                }
            }
        }
        container.add(g);
    }

    /**
     * Lays out child components in a tab model.
     *
     * @param object      the parent object
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param container   the container
     * @param context     the layout context
     * @return the tab model
     */
    protected TabPaneModel doTabLayout(IMObject object,
                                       List<NodeDescriptor> descriptors,
                                       PropertySet properties,
                                       Component container,
                                       LayoutContext context) {
        TabPaneModel model;
        boolean shortcuts = false;
        if (context.getLayoutDepth() == 0 && descriptors.size() > 1) {
            model = new TabPaneModel(container);
            shortcuts = true;
        } else {
            model = new TabPaneModel();
        }
        doTabLayout(object, descriptors, properties, model, context, shortcuts);
        return model;
    }

    /**
     * Lays out child components in a tab model.
     *
     * @param object      the parent object
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param model       the tab model
     * @param context     the layout context
     * @param shortcuts   if <tt>true</tt> include short cuts
     */
    protected void doTabLayout(IMObject object, List<NodeDescriptor> descriptors, PropertySet properties,
                               TabPaneModel model, LayoutContext context, boolean shortcuts) {
        int shortcut = 1;
        for (NodeDescriptor nodeDesc : descriptors) {
            Property property = properties.get(nodeDesc);
            ComponentState child = createComponent(property, object, context);
            Component inset = ColumnFactory.create("Inset",
                                                   child.getComponent());
            setFocusTraversal(child);
            String text = child.getDisplayName();
            if (text == null) {
                text = nodeDesc.getDisplayName();
            }

            if (shortcuts && shortcut <= 10) {
                text = getShortcut(text, shortcut);
                ++shortcut;
            }
            model.addTab(text, inset);
        }
    }

    /**
     * Creates a set of components to be rendered from the supplied descriptors.
     *
     * @param object      the parent object
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param context     the layout context
     * @return the components
     */
    protected ComponentSet createComponentSet(IMObject object,
                                              List<NodeDescriptor> descriptors,
                                              PropertySet properties,
                                              LayoutContext context) {
        ComponentSet result = new ComponentSet();
        for (NodeDescriptor descriptor : descriptors) {
            Property property = properties.get(descriptor);
            ComponentState component = createComponent(property, object,
                                                       context);
            String displayName = component.getDisplayName();
            if (displayName == null) {
                displayName = descriptor.getDisplayName();
            }
            result.add(component, displayName);
        }
        return result;
    }

    /**
     * Helper to add a node to a container.
     *
     * @param container the container
     * @param name      the node display name. May be <tt>null</tt>
     * @param component the component representing the node
     */
    protected void add(Component container, String name, Component component) {
        Label label = LabelFactory.create();
        if (name != null) {
            label.setText(name);
        }
        container.add(label);
        container.add(component);
    }

    /**
     * Helper to add a node to a container, setting its tab index.
     *
     * @param container the container
     * @param name      the node display name
     * @param component the component representing the node
     */
    protected void add(Component container, String name,
                       ComponentState component) {
        add(container, name, component.getComponent());
        setFocusTraversal(component);
    }

    /**
     * Helper to add a property to a container, setting its tab index.
     *
     * @param container the container
     * @param component the component representing the property
     */
    protected void add(Component container, ComponentState component) {
        Property property = component.getProperty();
        String name = null;
        if (property != null) {
            name = property.getDisplayName();
        }
        add(container, name, component);
        setFocusTraversal(component);
    }

    /**
     * Creates a component for a property.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display <code>property</code>
     */
    protected ComponentState createComponent(Property property, IMObject parent,
                                             LayoutContext context) {
        IMObjectComponentFactory factory = context.getComponentFactory();
        return factory.create(property, parent);
    }

    /**
     * Creates a grid with the no. of columns determined by the no. of
     * node descriptors.
     *
     * @param descriptors the node descriptors
     * @return a new grid
     */
    protected Grid createGrid(List<NodeDescriptor> descriptors) {
        return (descriptors.size() <= 4) ? GridFactory.create(2)
                                         : GridFactory.create(4);
    }

    /**
     * Sets focus on a component.
     * Delegates to {@link #setFocus(List<ComponentState>)}.
     */
    protected void setFocus() {
        setFocus(components);
    }

    /**
     * Sets focus on a component.
     * This implementation sets focus on the first focusable field.
     *
     * @param components the components
     */
    protected void setFocus(List<ComponentState> components) {
        Component focusable = getFocusable(components);
        FocusHelper.setFocus(focusable);
    }

    /**
     * Returns the first focusable component, selecting invalid properties
     * in preference to other components.
     *
     * @param components the components
     * @return the first focusable component
     */
    protected Component getFocusable(List<ComponentState> components) {
        Component result = null;
        for (ComponentState state : components) {
            Component child = state.getFocusable();
            if (child != null) {
                Property property = state.getProperty();
                if (property != null && !property.isValid()) {
                    result = child;
                    break;
                }
                if (result == null) {
                    result = child;
                }
            }
        }
        return result;
    }

    /**
     * Sets the focus traversal index of a component, if it is a focus traversal
     * participant.
     * NOTE: if a component doesn't specify a focus group,
     * this may register a child component with the focus group rather than the
     * parent.
     *
     * @param state the component state
     */
    protected void setFocusTraversal(ComponentState state) {
        Component component = state.getComponent();
        if (state.getFocusGroup() != null) {
            focusGroup.add(state.getFocusGroup());
            components.add(state);
        } else {
            Component focusable = FocusHelper.getFocusable(component);
            if (focusable != null) {
                focusGroup.add(focusable);
                components.add(state);
            }
        }
    }

    /**
     * Returns a shortcut for a tab.
     * Shortcuts no.s must be from 1..10, and will be displayed as '1..9, 0'.
     *
     * @param name     the tab name
     * @param shortcut the shortcut no.
     * @return the shortcut text
     */
    protected String getShortcut(String name, int shortcut) {
        if (shortcut == 10) {
            shortcut = 0;
        }
        return "&" + shortcut + " " + name;
    }

}

