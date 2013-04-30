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

package org.openvpms.web.component.util;

import echopointng.DateField;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Component;


/**
 * Component helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ComponentHelper {

    /**
     * Enable/disable a component, changing its foreground to indicate the
     * state.
     *
     * @param component the component to update
     * @param enabled   if <code>true</code> enable the component; otherwise
     *                  disable it
     */
    public static void enable(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (enabled) {
            component.setForeground(Color.BLACK);
        } else {
            component.setForeground(Color.LIGHTGRAY);
        }
    }

    /**
     * Enable/disable a date field.
     *
     * @param field   the field to update
     * @param enabled if <code>true</code> enable the field; otherwise disable
     *                it
     */
    public static void enable(DateField field, boolean enabled) {
        enable(field.getTextField(), enabled);
        field.getTextField().setFocusTraversalParticipant(enabled);
        field.getDateChooser().setEnabled(enabled);
        field.setEnabled(enabled);
        if (!enabled) {
            field.setExpanded(false);
        }
    }
}
