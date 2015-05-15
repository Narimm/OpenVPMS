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

import java.util.Date;

/**
 * An editor for {@link RepeatExpression}s.
 *
 * @author Tim Anderson
 */
public interface RepeatExpressionEditor extends Modifiable {

    /**
     * Sets the time to start the expression.
     *
     * @param startTime the expression start time. May be {@code null}
     */
    void setStartTime(Date startTime);

    /**
     * Returns the time to start the expression.
     *
     * @return the start time. May be {@code null}
     */
    Date getStartTime();

    /**
     * Returns the expression.
     *
     * @return the expression, or {@code null} if the expression is invalid
     */
    RepeatExpression getExpression();

    /**
     * Returns the component representing the editor.
     *
     * @return the component
     */
    Component getComponent();

    /**
     * Returns the component focus group.
     *
     * @return the focus group
     */
    FocusGroup getFocusGroup();

}
