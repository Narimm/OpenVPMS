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
import org.openvpms.web.resource.i18n.Messages;

/**
 * A viewer for repeat expressions produced by {@link RepeatOnOrdinalDayEditor}.
 *
 * @author Tim Anderson
 */
class RepeatOnOrdinalDayViewer {

    /**
     * The expression.
     */
    private final CronRepeatExpression expression;

    /**
     * Constructs a {@link RepeatOnOrdinalDayViewer}.
     *
     * @param expression the expression
     */
    public RepeatOnOrdinalDayViewer(CronRepeatExpression expression) {
        this.expression = expression;
    }

    /**
     * Returns a component representing the expression.
     *
     * @return a new component
     */
    public Component getComponent() {
        StringBuilder text = new StringBuilder();
        text.append(Messages.get("workflow.scheduling.appointment.onthe"));
        text.append(" ");
        switch (expression.getDayOfWeek().getOrdindal()) {
            case 1:
                text.append(Messages.get("workflow.scheduling.appointment.first"));
                break;
            case 2:
                text.append(Messages.get("workflow.scheduling.appointment.second"));
                break;
            case 3:
                text.append(Messages.get("workflow.scheduling.appointment.third"));
                break;
            case 4:
                text.append(Messages.get("workflow.scheduling.appointment.fourth"));
                break;
            case 5:
                text.append(Messages.get("workflow.scheduling.appointment.fifth"));
                break;
            case CronRepeatExpression.DayOfWeek.LAST:
                text.append(Messages.get("workflow.scheduling.appointment.last"));
                break;
            default:
                text.append("EXPRESSION ERROR");
        }
        text.append(" ");
        text.append(Messages.get("workflow.scheduling.appointment.every").toLowerCase());
        text.append(expression.getMonth().getInterval());
        text.append(" ");
        text.append(Messages.get("workflow.scheduling.appointment.months"));
        Label result = LabelFactory.create();
        result.setText(text.toString());
        return result;
    }
}
