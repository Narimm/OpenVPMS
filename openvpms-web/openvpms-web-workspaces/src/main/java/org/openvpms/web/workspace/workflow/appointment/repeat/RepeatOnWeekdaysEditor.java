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

import java.util.Date;

/**
 * Repeat on weekdays expression editor.
 *
 * @author Tim Anderson
 */
public class RepeatOnWeekdaysEditor extends AbstractRepeatExpressionEditor {

    /**
     * Returns the expression.
     *
     * @return the expression, or {@code null} if the expression is invalid
     */
    @Override
    public RepeatExpression getExpression() {
        Date startTime = getStartTime();
        return (startTime != null) ? Repeats.weekdays(startTime) : null;
    }

    /**
     * Returns the component representing the editor.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        Label result = LabelFactory.create();
        String text = RepeatHelper.toString(RepeatExpression.Type.WEEKDAYS);
        result.setText(text);
        return result;
    }

    /**
     * Determines if the editor can edit the supplied expression.
     *
     * @param expression the expression
     * @return {@code true} if the editor can edit the expression
     */
    public static boolean supports(CronRepeatExpression expression) {
        return expression.getDayOfMonth().isAll() && expression.getMonth().isAll()
               && expression.getDayOfWeek().weekdays();
    }
}
