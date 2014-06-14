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

package org.openvpms.web.component.edit;

import nextapp.echo2.app.Component;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.echo.focus.FocusGroup;


/**
 * Object editor.
 *
 * @author Tim Anderson
 */
public interface Editor extends Modifiable {

    /**
     * Returns the edit component.
     *
     * @return the edit component
     */
    Component getComponent();

    /**
     * Returns the focus group.
     *
     * @return the focus group, or {@code null} if the editor hasn't been rendered
     */
    FocusGroup getFocusGroup();

    /**
     * Disposes of the editor.
     * <br/>
     * Once disposed, the behaviour of invoking any method is undefined.
     */
    void dispose();
}
