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

package org.openvpms.web.component.focus;

import echopointng.DateField;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.button.AbstractButton;
import nextapp.echo2.app.text.TextComponent;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.table.KeyTable;

import java.util.List;


/**
 * Focus helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class FocusHelper {


    /**
     * Returns the first component that may have focus set.
     *
     * @param state the component state
     * @return the first component that may have focus set, or
     *         <code>null</code> if none may have focus set
     */
    public static Component getFocusable(ComponentState state) {
        Component result;
        if (state.getFocusGroup() != null) {
            result = getFocusable(state.getFocusGroup());
        } else {
            result = getFocusable(state.getComponent());
        }
        return result;
    }

    /**
     * Returns the first component that may have focus set.
     *
     * @param group the focus group
     * @return the first component that may have focus set, or
     *         <code>null</code> if none may have focus set
     */
    public static Component getFocusable(FocusGroup group) {
        Component result = null;
        for (Object object : group.getComponents()) {
            if (object instanceof Component) {
                result = getFocusable((Component) object);
            } else {
                result = getFocusable((FocusGroup) object);
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }

    /**
     * Returns the first focusable component, selecting invalid properties
     * in preference to other components.
     *
     * @param components the components
     * @return the first focusable component
     */
    public static Component getFocusable(List<ComponentState> components) {
        Component result = null;
        for (ComponentState state : components) {
            Component child = getFocusable(state);
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
     * Returns the first component that may have focus set.
     *
     * @param component the component
     * @return the first child component that may have focus set, or
     *         <code>null</code> if none may have focus set
     */
    public static Component getFocusable(Component component) {
        Component result = null;
        if (isFocusable(component) && component.isEnabled()
                && component.isFocusTraversalParticipant()) {
            if (component instanceof DateField) {
                result = ((DateField) component).getTextField();
            } else {
                result = component;
            }
        } else if (component.getComponentCount() != 0) {
            for (Component child : component.getComponents()) {
                result = getFocusable(child);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Determines if a component is one that may receive focus.
     *
     * @param component the component
     * @return <code>true</code> if the component is a focusable component;
     *         otherwise <code>false</code>
     */
    private static boolean isFocusable(Component component) {
        return (component instanceof TextComponent
                || component instanceof CheckBox
                || component instanceof SelectField
                || component instanceof DateField
                || component instanceof AbstractButton
                || component instanceof KeyTable);
    }

}
