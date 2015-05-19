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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.repeat;

import nextapp.echo2.app.Component;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.echo.focus.FocusGroup;

/**
 * Editor to limit a repeat expression.
 *
 * @author Tim Anderson
 */
public interface RepeatUntilEditor extends Modifiable {

    /**
     * Returns the condition.
     *
     * @return the condition, or {@code null} if the condition is invalid
     */
    RepeatCondition getCondition();

    /**
     * Returns the editor component.
     *
     * @return the component
     */
    Component getComponent();

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    FocusGroup getFocusGroup();
}
