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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.hl7;

import nextapp.echo2.app.Component;

/**
 * Tab pane component.
 *
 * @author Tim Anderson
 */
public interface TabComponent {

    /**
     * Invoked when the tab is displayed.
     */
    void show();

    /**
     * Returns the tab component.
     *
     * @return the tab component
     */
    Component getComponent();

    /**
     * Returns the button component.
     *
     * @return the button component, or {@code null} if this tab doesn't provide any buttons
     */
    Component getButtons();

}
