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
import org.openvpms.web.component.focus.FocusHelper;
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
     * The component's property. May be <tt>null</tt>
     */
    private final Property property;

    /**
     * The component's focus group. May be <tt>null</tt>
     */
    private final FocusGroup focusGroup;

    /**
     * Display name for the component. May be <tt>null</tt>
     */
    private String displayName;


    /**
     * Constructs a new <tt>ComponentState</tt> for a component
     * that may not receive focus and is not associated with a node.
     *
     * @param component the component
     */
    public ComponentState(Component component) {
        this(component, null, null);
    }

    /**
     * Constructs a new <tt>ComponentState</tt>, not associated with a node.
     *
     * @param component  the component
     * @param focusGroup the component's focus group, or <tt>null</tt>
     *                   if the component is a simple component or doesn't
     *                   receive focus
     */
    public ComponentState(Component component, FocusGroup focusGroup) {
        this(component, null, focusGroup);
    }

    /**
     * Constructs a new <tt>ComponentState</tt> not associated with
     * a focus group.
     *
     * @param component the component
     * @param property  the property, or <tt>null</tt> if the component
     *                  isn't associated with a node
     */
    public ComponentState(Component component, Property property) {
        this(component, property, null);
    }

    /**
     * Constructs a new <tt>ComponentState</tt>.
     *
     * @param component  the component
     * @param property   the property, or <tt>null</tt> if the component
     *                   isn't associated with a node
     * @param focusGroup the component's focus group, or <tt>null</tt>
     *                   if the component is a simple component or doesn't
     *                   receive focus
     */
    public ComponentState(Component component, Property property,
                          FocusGroup focusGroup) {
        this(component, property, focusGroup, null);
    }

    /**
     * Constructs a new <tt>ComponentState</tt>.
     *
     * @param component   the component
     * @param property    the property, or <tt>null</tt> if the component
     *                    isn't associated with a node
     * @param focusGroup  the component's focus group, or <tt>null</tt>
     *                    if the component is a simple component or doesn't
     *                    receive focus
     * @param displayName a display name for the component. If not specified,
     *                    defaults to the property's display name, if a
     *                    property is supplied
     */
    public ComponentState(Component component, Property property,
                          FocusGroup focusGroup, String displayName) {
        this.component = component;
        this.property = property;
        this.focusGroup = focusGroup;
        if (displayName != null) {
            this.displayName = displayName;
        } else if (property != null) {
            this.displayName = property.getDisplayName();
        } else {
            this.displayName = null;
        }
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
     * @return the property associated with the component, or <tt>null</tt>
     *         if the component isn't associated with a node
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Returns the component's focus group.
     *
     * @return the component's focus group, or <tt>null</tt> if the
     *         component is a simple component or doesn't receive focus
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Returns a display name for the component.
     *
     * @return a display name for the component. May be <tt>null</tt>
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name for the component.
     *
     * @param displayName the display name. May be <tt>null</tt>
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the first component that may have focus set.
     *
     * @return the first component that may have focus set, or
     *         <tt>null</tt> if none may have focus set
     */
    public Component getFocusable() {
        Component result;
        if (focusGroup != null) {
            result = focusGroup.getFocusable();
        } else {
            result = FocusHelper.getFocusable(component);
        }
        return result;
    }

}
