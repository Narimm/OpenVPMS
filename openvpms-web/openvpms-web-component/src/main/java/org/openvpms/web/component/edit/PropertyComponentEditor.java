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
 *  $Id: PropertyComponentEditor.java 1627 2006-12-12 03:25:07Z tanderson $
 */

package org.openvpms.web.component.edit;

import nextapp.echo2.app.Component;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.component.property.Property;


/**
 * Simple property editor, associated with a component.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-12-12 03:25:07Z $
 */
public class PropertyComponentEditor extends AbstractPropertyEditor {

    /**
     * The edit component.
     */
    private final Component component;

    /**
     * The focus group.
     */
    private final FocusGroup focusGroup;


    /**
     * Construct a new <code>PropertyComponentEditor</code>.
     *
     * @param property  the property being edited
     * @param component the edit component
     */
    public PropertyComponentEditor(Property property, Component component) {
        super(property);
        this.component = component;
        focusGroup = new FocusGroup(property.getDisplayName());
        focusGroup.add(component);
    }

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

}
