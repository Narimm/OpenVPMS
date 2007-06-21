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

package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Component;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.property.Property;


/**
 * Component state.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ComponentState {

    /**
     * The component.
     */
    private final Component component;

    /**
     * The component's property. May be <code>null</code>
     */
    private final Property property;

    /**
     * The component's focus group. May be <code>null</code>
     */
    private final FocusGroup focusGroup;


    /**
     * Constructs a new <code>ComponentState</code> for a component
     * that may not receive focus and is not associated with a node.
     *
     * @param component the component
     */
    public ComponentState(Component component) {
        this(component, null, null);
    }

    /**
     * Constructs a new <code>ComponentState</code>, not associated with a node.
     *
     * @param component  the component
     * @param focusGroup the component's focus group, or <code>null</code>
     *                   if the component is a simple component or doesn't
     *                   receive focus
     */
    public ComponentState(Component component, FocusGroup focusGroup) {
        this(component, null, focusGroup);
    }

    /**
     * Constructs a new <code>ComponentState</code> not associated with
     * a focus group.
     *
     * @param component the component
     * @param property  the property, or <code>null</code> if the component
     *                  isn't associated with a node
     */
    public ComponentState(Component component, Property property) {
        this(component, property, null);
    }

    /**
     * Constructs a new <code>ComponentState</code>.
     *
     * @param component  the component
     * @param property   the property, or <code>null</code> if the component
     *                   isn't associated with a node
     * @param focusGroup the component's focus group, or <code>null</code>
     *                   if the component is a simple component or doesn't
     *                   receive focus
     */
    public ComponentState(Component component, Property property,
                          FocusGroup focusGroup) {
        this.component = component;
        this.property = property;
        this.focusGroup = focusGroup;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * The property associated with the component.
     *
     * @return the property associated with the component, or <code>null</code>
     *         if the component isn't associated with a node
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Returns the component's focus group.
     *
     * @return the component's focus group, or <code>null</code> if the
     *         component is a simple component or doesn't receive focus
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

}
