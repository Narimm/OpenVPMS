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
import nextapp.echo2.app.Label;
import org.openvpms.web.echo.factory.LabelFactory;

/**
 * A viewer for {@link CalendarRepeatExpression}s.
 *
 * @author Tim Anderson
 */
class CalendarRepeatExpressionViewer {

    /**
     * The expression.
     */
    private final CalendarRepeatExpression expression;


    /**
     * Constructs an {@link CalendarRepeatExpressionViewer}.
     *
     * @param expression the expression
     */
    public CalendarRepeatExpressionViewer(CalendarRepeatExpression expression) {
        this.expression = expression;
    }

    /**
     * Returns the expression viewer.
     *
     * @return the component
     */
    public Component getComponent() {
        Label label = LabelFactory.create();
        label.setText(expression.toString());
        return label;
    }
}
