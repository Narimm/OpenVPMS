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

import org.openvpms.web.component.im.view.ComponentState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents a set of labelled components.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ComponentSet {

    /**
     * The components.
     */
    private final List<ComponentState> components
            = new ArrayList<ComponentState>();

    /**
     * The labels, keyed on component.
     */
    private final Map<ComponentState, String> labels
            = new HashMap<ComponentState, String>();


    /**
     * Adds a component.
     *
     * @param component the component to add
     */
    public void add(ComponentState component) {
        add(component, component.getDisplayName());
    }

    /**
     * Adds a component.
     *
     * @param component the component
     * @param label     a label for the component
     */
    public void add(ComponentState component, String label) {
        add(components.size(), component, label);
    }

    /**
     * Adds a component.
     *
     * @param index     index at which the component is to be inserted.
     * @param component the component to add
     */
    public void add(int index, ComponentState component) {
        add(index, component, component.getDisplayName());
    }

    /**
     * Adds a component at the specified index.
     *
     * @param index     index at which the component is to be inserted.
     * @param component the component
     * @param label     a label for the component
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   (index &lt; 0 || index &gt; size()).
     */
    public void add(int index, ComponentState component, String label) {
        components.add(index, component);
        labels.put(component, label);
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
     * Returns the label for a component.
     *
     * @param component the component
     * @return the component's label, or <code>null</code> if the component is
     *         not found
     */
    public String getLabel(ComponentState component) {
        return labels.get(component);
    }

}
