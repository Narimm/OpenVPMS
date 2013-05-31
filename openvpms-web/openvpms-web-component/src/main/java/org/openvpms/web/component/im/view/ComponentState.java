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

package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.apache.commons.lang.StringUtils;
import org.openvpms.web.component.edit.PropertyEditor;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.focus.FocusHelper;


/**
 * Component state.
 *
 * @author Tim Anderson
 */
public class ComponentState {

    /**
     * The component.
     */
    private final Component component;

    /**
     * The component's property. May be {@code null}
     */
    private final Property property;

    /**
     * The component's focus group. May be {@code null}
     */
    private final FocusGroup focusGroup;

    /**
     * Display name for the component. May be {@code null}
     */
    private String displayName;

    /**
     * The label for the component. May be {@code null}
     */
    private Label label;

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
     * @param focusGroup the component's focus group, or {@code null} if the component is a simple component or doesn't
     *                   receive focus
     */
    public ComponentState(Component component, FocusGroup focusGroup) {
        this(component, null, focusGroup);
    }

    /**
     * Constructs a <tt>ComponentState</tt> not associated with a focus group.
     *
     * @param component the component
     * @param property  the property, or {@code null} if the component isn't associated with a node
     */
    public ComponentState(Component component, Property property) {
        this(component, property, null);
    }

    /**
     * Constructs a <tt>ComponentState</tt>.
     *
     * @param component  the component
     * @param property   the property, or {@code null} if the component isn't associated with a node
     * @param focusGroup the component's focus group, or {@code null} if the component is a simple component or doesn't
     *                   receive focus
     */
    public ComponentState(Component component, Property property, FocusGroup focusGroup) {
        this(component, property, focusGroup, null);
    }

    /**
     * Constructs a <tt>ComponentState</tt> from a property editor.
     *
     * @param editor the property editor
     */
    public ComponentState(PropertyEditor editor) {
        this(editor.getComponent(), editor.getProperty(), editor.getFocusGroup(),
             editor.getProperty().getDisplayName());
    }

    /**
     * Constructs a new <tt>ComponentState</tt>.
     *
     * @param component   the component
     * @param property    the property, or {@code null} if the component isn't associated with a node
     * @param focusGroup  the component's focus group, or {@code null} if the component is a simple component or
     *                    doesn't receive focus
     * @param displayName a display name for the component. If not specified, defaults to the property's display name,
     *                    if a property is supplied
     */
    public ComponentState(Component component, Property property, FocusGroup focusGroup, String displayName) {
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
     * @return the property associated with the component, or {@code null}
     *         if the component isn't associated with a node
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Returns the component's focus group.
     *
     * @return the component's focus group, or {@code null} if the
     *         component is a simple component or doesn't receive focus
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Determines if the component has a label.
     *
     * @return {@code true} if the component has a label
     */
    public boolean hasLabel() {
        return label != null || !StringUtils.isEmpty(displayName);
    }

    /**
     * Returns a label for the component.
     */
    public Label getLabel() {
        if (label == null) {
            label = LabelFactory.create();
            label.setText(displayName);
        }
        return label;
    }

    /**
     * Returns a display name for the component.
     *
     * @return a display name for the component. May be {@code null}
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name for the component.
     *
     * @param displayName the display name. May be {@code null}
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the first component that may have focus set.
     *
     * @return the first component that may have focus set, or {@code null} if none may have focus set
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
