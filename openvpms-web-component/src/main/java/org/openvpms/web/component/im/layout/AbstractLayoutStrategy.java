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
 */

package org.openvpms.web.component.im.layout;

import echopointng.TabbedPane;
import echopointng.tabbedpane.TabModel;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.property.DelegatingProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.TabPaneModel;
import org.openvpms.web.component.util.TabbedPaneFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Abstract implementation of the {@link IMObjectLayoutStrategy} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractLayoutStrategy implements IMObjectLayoutStrategy {

    /**
     * The component states, used to determine initial focus.
     */
    private ComponentSet components;

    /**
     * Pre-created component states, keyed on property name.
     */
    private Map<String, ComponentState> states = new HashMap<String, ComponentState>();

    /**
     * The focus group of the current component.
     */
    private FocusGroup focusGroup;

    /**
     * If <tt>true</tt> keep layout state after invoking <tt>apply()</tt>. Use this if the same strategy will be used
     * to layout a component multiple times.
     */
    private boolean keepState;

    /**
     * Sanity checker to detect recursion.
     */
    boolean inApply;


    /**
     * Constructs a <tt>AbstractLayoutStrategy</tt>.
     */
    public AbstractLayoutStrategy() {
        this(false);
    }

    /**
     * Constructs a <tt>AbstractLayoutStrategy</tt>.
     *
     * @param keepState if <tt>true</tt> keep layout state. Use this if the same strategy will be used to layout a
     *                  component multiple times
     */
    public AbstractLayoutStrategy(boolean keepState) {
        this.keepState = keepState;
    }

    /**
     * Pre-registers a component for inclusion in the layout.
     * <p/>
     * The component must be associated with a property.
     *
     * @param state the component state
     * @throws IllegalStateException if the component isn't associated with a property
     */
    public void addComponent(ComponentState state) {
        Property property = state.getProperty();
        if (property == null) {
            throw new IllegalArgumentException("Argument 'state' must be associated with a property");
        }
        states.put(property.getName(), state);
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered <code>object</code>
     */
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        ComponentState state;
        if (inApply) {
            throw new IllegalStateException("Cannot call apply() recursively");
        }
        inApply = true;
        try {
            focusGroup = new FocusGroup(DescriptorHelper.getDisplayName(object));
            components = new ComponentSet(focusGroup);
            Component container = doLayout(object, properties, parent, context);
            focusGroup.setDefault(getDefaultFocus());
            state = new ComponentState(container, focusGroup);
            components = null;
            if (!keepState) {
                focusGroup = null;
            }
        } finally {
            inApply = false;
        }
        return state;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or {@code null} if it hasn't been initialised
     */
    protected FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Lay out out the object.
     *
     * @param object     the object to lay out
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component
     */
    protected Component doLayout(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        Column container = ColumnFactory.create("CellSpacing");
        doLayout(object, properties, parent, container, context);
        return container;
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
    protected void doLayout(IMObject object, PropertySet properties, IMObject parent, Component container,
                            LayoutContext context) {
        ArchetypeDescriptor archetype = context.getArchetypeDescriptor(object);
        List<NodeDescriptor> simple = getSimpleNodes(archetype);
        List<NodeDescriptor> complex = getComplexNodes(archetype);

        NodeFilter filter = getNodeFilter(object, context);
        simple = filter(object, simple, filter);
        complex = filter(object, complex, filter);

        doSimpleLayout(object, parent, simple, properties, container, context);
        doComplexLayout(object, parent, complex, properties, container, context);
    }

    /**
     * Lays out child components in a grid.
     *
     * @param object      the object to lay out
     * @param parent      the parent object. May be {@code null}
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param container   the container to use
     * @param context     the layout context
     */
    protected void doSimpleLayout(IMObject object, IMObject parent, List<NodeDescriptor> descriptors,
                                  PropertySet properties, Component container, LayoutContext context) {
        if (!descriptors.isEmpty()) {
            Grid grid = createGrid(object, descriptors, properties, context);
            container.add(ColumnFactory.create("Inset.Small", grid));
        }
    }

    /**
     * Lays out components in a grid.
     *
     * @param object      the object to lay out
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param context     the layout context
     */
    protected Grid createGrid(IMObject object, List<NodeDescriptor> descriptors,
                              PropertySet properties, LayoutContext context) {
        int columns = getColumns(descriptors);
        return createGrid(object, descriptors, properties, context, columns);
    }

    /**
     * Lays out components in a grid.
     *
     * @param object      the object to lay out
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param context     the layout context
     */
    protected Grid createGrid(IMObject object, List<NodeDescriptor> descriptors,
                              PropertySet properties, LayoutContext context, int columns) {
        ComponentSet set = createComponentSet(object, descriptors, properties, context);
        ComponentGrid grid = new ComponentGrid();
        grid.add(set, columns);
        return createGrid(grid);
    }

    /**
     * Creates a grid.
     *
     * @param grid the component grid
     * @return the corresponding grid
     */
    protected Grid createGrid(ComponentGrid grid) {
        return grid.createGrid(components);
    }

    /**
     * Determines the no. of columns to display.
     *
     * @param descriptors the node descriptors
     * @return the number of columns
     */
    protected int getColumns(List<NodeDescriptor> descriptors) {
        return (descriptors.size() <= 4) ? 1 : 2;
    }

    /**
     * Lays out each child component in a tabbed pane.
     *
     * @param object      the object to lay out
     * @param parent      the parent object. May be {@code null}
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param container   the container to use
     * @param context     the layout context
     */
    protected void doComplexLayout(IMObject object, IMObject parent, List<NodeDescriptor> descriptors,
                                   PropertySet properties, Component container, LayoutContext context) {
        if (!descriptors.isEmpty()) {
            TabModel model = doTabLayout(object, descriptors, properties, container, context, false);
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
     * @see ArchetypeDescriptor#getSimpleNodeDescriptors()
     */
    protected List<NodeDescriptor> getSimpleNodes(ArchetypeDescriptor archetype) {
        return archetype.getSimpleNodeDescriptors();
    }

    /**
     * Returns the 'complex' nodes.
     *
     * @param archetype the archetype
     * @return the complex nodes
     * @see ArchetypeDescriptor#getComplexNodeDescriptors()
     */
    protected List<NodeDescriptor> getComplexNodes(ArchetypeDescriptor archetype) {
        return archetype.getComplexNodeDescriptors();
    }

    /**
     * Returns a node filter to filter nodes. This implementation returns {@link LayoutContext#getDefaultNodeFilter()}.
     *
     * @param object  the object to filter nodes for
     * @param context the context
     * @return a node filter to filter nodes, or {@code null} if no filtering is required
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
    protected ChainedNodeFilter getNodeFilter(LayoutContext context, NodeFilter filter) {
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
    protected List<NodeDescriptor> filter(IMObject object, List<NodeDescriptor> descriptors, NodeFilter filter) {
        return FilterHelper.filter(object, filter, descriptors);
    }

    /**
     * Lays out a component grid.
     *
     * @param grid      the grid
     * @param container the container to add the grid to
     */
    protected void doGridLayout(ComponentGrid grid, Component container) {
        Grid g = grid.createGrid(components);
        container.add(g);
    }

    /**
     * Lays out child components in a tab model.
     *
     * @param object       the parent object
     * @param descriptors  the property descriptors
     * @param properties   the properties
     * @param container    the container
     * @param context      the layout context
     * @param shortcutHint a hint to display short cuts for tabs. If <tt>false</tt> shortcuts will only be displayed if
     *                     there is more than one descriptor. Shortcuts will never be displayed if the layout depth
     *                     is non-zero
     * @return the tab model
     */
    protected TabPaneModel doTabLayout(IMObject object, List<NodeDescriptor> descriptors, PropertySet properties,
                                       Component container, LayoutContext context, boolean shortcutHint) {
        TabPaneModel model;
        boolean shortcuts = false;
        if (context.getLayoutDepth() == 0 && (descriptors.size() > 1 || shortcutHint)) {
            model = createTabModel(container);
            shortcuts = true;
        } else {
            model = createTabModel(null);
        }
        doTabLayout(object, descriptors, properties, model, context, shortcuts);
        return model;
    }

    /**
     * Creates a new tab model.
     *
     * @param container the tab container. May be {@code null}
     * @return a new tab model
     */
    protected TabPaneModel createTabModel(Component container) {
        return new TabPaneModel(container);
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
        for (NodeDescriptor nodeDesc : descriptors) {
            Property property = properties.get(nodeDesc);
            ComponentState child = createComponent(property, object, context);
            addTab(model, property, child, shortcuts);
        }
    }

    /**
     * Adds a tab to a tab model.
     *
     * @param model       the tab  model
     * @param property    property
     * @param component   the component to add
     * @param addShortcut if <tt>true</tt> add a tab shortcut
     */
    protected void addTab(TabPaneModel model, Property property, ComponentState component, boolean addShortcut) {
        setFocusTraversal(component);
        String text = component.getDisplayName();
        if (text == null) {
            text = property.getDisplayName();
        }
        if (addShortcut && model.size() < 10) {
            text = getShortcut(text, model.size() + 1);
        }
        Component inset = ColumnFactory.create("Inset", component.getComponent());
        model.addTab(text, inset);
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
    protected ComponentSet createComponentSet(IMObject object, List<NodeDescriptor> descriptors, PropertySet properties,
                                              LayoutContext context) {
        ComponentSet result = new ComponentSet();
        for (NodeDescriptor descriptor : descriptors) {
            ComponentState component = createComponent(object, descriptor, properties, context);
            result.add(component);
        }
        return result;
    }

    /**
     * Creates a components to render the property associated with the supplied descriptor.
     *
     * @param object     the parent object
     * @param descriptor the property descriptors
     * @param properties the properties
     * @param context    the layout context
     * @return the components
     */
    protected ComponentState createComponent(IMObject object, NodeDescriptor descriptor, PropertySet properties,
                                             LayoutContext context) {
        Property property = properties.get(descriptor);
        ComponentState component = createComponent(property, object, context);
        String displayName = component.getDisplayName();
        if (displayName == null) {
            displayName = descriptor.getDisplayName();
            component.setDisplayName(displayName);
        }
        return component;
    }

    /**
     * Helper to add a node to a container.
     *
     * @param container the container
     * @param name      the node display name. May be {@code null}
     * @param component the component representing the node
     */
    protected void add(Component container, String name, Component component) {
        Label label = LabelFactory.create();
        if (name != null) {
            label.setText(name);
        }
        add(container, label, component);
    }

    /**
     * Helper to add a node to a container.
     *
     * @param container the container
     * @param label     the component label
     * @param component the component representing the node
     */
    protected void add(Component container, Label label, Component component) {
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
    protected void add(Component container, String name, ComponentState component) {
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
     * <p/>
     * If there is a pre-existing component, registered via {@link #addComponent}, this will be returned.
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a component to display <code>property</code>
     */
    protected ComponentState createComponent(Property property, IMObject parent, LayoutContext context) {
        ComponentState result = getComponent(property);
        if (result == null) {
            IMObjectComponentFactory factory = context.getComponentFactory();
            result = factory.create(property, parent);
        }
        return result;
    }

    /**
     * Returns the component associated with the specified property.
     *
     * @param property the property
     * @return the corresponding component, or {@code null} if none is found
     */
    protected ComponentState getComponent(Property property) {
        return states.get(property.getName());
    }

    /**
     * Returns the default focus component.
     * <p/>
     * Delegates to {@link #getDefaultFocus(ComponentSet)}.
     *
     * @return the default focus component, or {@code null} if none is found
     */
    protected Component getDefaultFocus() {
        return getDefaultFocus(components);
    }

    /**
     * Returns the default focus component.
     * <p/>
     * This implementation returns the first focusable component.
     *
     * @param components the components
     * @return the default focus component, or {@code null} if none is found
     */
    protected Component getDefaultFocus(ComponentSet components) {
        return components.getFocusable();
    }

    /**
     * Sets the focus traversal index of a component, if it is a focus traversal participant.
     * NOTE: if a component doesn't specify a focus group, this may register a child component with the focus group
     * rather than the parent.
     *
     * @param state the component state
     */
    protected void setFocusTraversal(ComponentState state) {
        if (components != null) {    // will be null if apply() has completed
            components.setFocusTraversal(state);
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

    /**
     * Creates a read-only version of the supplied property.
     *
     * @param property the property
     * @return a read-only version of the property
     */
    protected Property createReadOnly(Property property) {
        property = new DelegatingProperty(property) {
            @Override
            public boolean isReadOnly() {
                return true;
            }
        };
        return property;
    }
}

