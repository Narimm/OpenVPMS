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

import nextapp.echo2.app.Component;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a set of labelled components.
 *
 * @author Tim Anderson
 */
public class ComponentSet {

    /**
     * The components.
     */
    private final List<ComponentState> components = new ArrayList<ComponentState>();

    /**
     * The focus group. May be {@code null}
     */
    private final FocusGroup focusGroup;


    /**
     * Default constructor.
     */
    public ComponentSet() {
        this(null);
    }

    /**
     * Constructs a {@code ComponentSet}.
     *
     * @param focusGroup the focus group. May be {@code null}
     */
    public ComponentSet(FocusGroup focusGroup) {
        this.focusGroup = focusGroup;
    }

    /**
     * Adds a component.
     *
     * @param component the component to add
     */
    public void add(ComponentState component) {
        add(components.size(), component);
    }

    /**
     * Adds a component.
     *
     * @param index     index at which the component is to be inserted.
     * @param component the component to add
     */
    public void add(int index, ComponentState component) {
        components.add(index, component);
    }

    /**
     * Returns the index of the component associated with the named property.
     *
     * @param name the property name
     * @return the index, or <tt>-1</tt> if there is no component
     */
    public int indexOf(String name) {
        for (int i = 0; i < components.size(); ++i) {
            ComponentState state = components.get(i);
            if (state.getProperty() != null && name.equals(state.getProperty().getName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the component associated with the named property.
     *
     * @param name the property name
     * @return the component, or <tt>null</tt> if none is found
     */
    public ComponentState get(String name) {
        int index = indexOf(name);
        return (index != -1) ? components.get(index) : null;
    }

    /**
     * Returns the components.
     *
     * @return the components
     */
    public List<ComponentState> getComponents() {
        return components;
    }

    /**
     * Returns the number of components.
     *
     * @return the number of components
     */
    public int size() {
        return components.size();
    }

    /**
     * Sets the focus traversal index of a component, if it is a focus traversal
     * participant.
     * NOTE: if a component doesn't specify a focus group, this may register a child component with the focus group
     * rather than the parent.
     *
     * @param state the component state
     */
    public void setFocusTraversal(ComponentState state) {
        Component component = state.getComponent();
        if (state.getFocusGroup() != null) {
            components.add(state);
            focusGroup.add(state.getFocusGroup());
        } else {
            Component focusable = FocusHelper.getFocusable(component);
            if (focusable != null) {
                components.add(state);
                focusGroup.add(focusable);
            }
        }
    }

    /**
     * Returns the first focusable component, selecting invalid properties in preference to other components.
     *
     * @return the first focusable component
     */
    public Component getFocusable() {
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
     * Returns the focusable component associated with the property with the specified name.
     *
     * @param name the property name
     * @return the corresponding component, or {@code null} if none is found
     */
    public Component getFocusable(String name) {
        for (ComponentState state : components) {
            Property property = state.getProperty();
            if (property != null && name.equals(property.getName())) {
                return state.getFocusable();
            }
        }
        return null;
    }

}
