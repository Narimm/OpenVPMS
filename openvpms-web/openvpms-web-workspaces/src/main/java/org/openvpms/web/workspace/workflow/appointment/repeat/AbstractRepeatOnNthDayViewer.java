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

import org.openvpms.web.resource.i18n.Messages;

/**
 * A viewer for expressions produced by {@link AbstractRepeatOnNthDayEditor}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractRepeatOnNthDayViewer extends CronRepeatExpressionViewer {

    /**
     * Constructs an {@link AbstractRepeatOnNthDayViewer}.
     *
     * @param expression the expression
     */
    public AbstractRepeatOnNthDayViewer(CronRepeatExpression expression) {
        super(expression);
    }

    /**
     * Helper to return a formatted version of the day-of-week ordinal value.
     *
     * @return the formatted ordinal value
     */
    protected String getOrdinal() {
        String result;
        switch (getExpression().getDayOfWeek().getOrdinal()) {
            case 1:
                result = Messages.get("workflow.scheduling.appointment.first");
                break;
            case 2:
                result = Messages.get("workflow.scheduling.appointment.second");
                break;
            case 3:
                result = Messages.get("workflow.scheduling.appointment.third");
                break;
            case 4:
                result = Messages.get("workflow.scheduling.appointment.fourth");
                break;
            case 5:
                result = Messages.get("workflow.scheduling.appointment.fifth");
                break;
            case CronRepeatExpression.DayOfWeek.LAST:
                result = Messages.get("workflow.scheduling.appointment.last");
                break;
            default:
                result = "EXPRESSION ERROR";
        }
        return result;
    }

}
