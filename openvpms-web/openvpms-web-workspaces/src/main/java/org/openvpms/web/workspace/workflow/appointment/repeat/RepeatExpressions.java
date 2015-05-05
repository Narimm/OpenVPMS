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

import org.openvpms.archetype.rules.util.DateUnits;

import java.util.Date;

/**
 * Factory for {@link RepeatExpression} instances.
 *
 * @author Tim Anderson
 */
public class RepeatExpressions {

    /**
     * Helper to create a daily repeat expression.
     *
     * @return a new expression
     */
    public static RepeatExpression daily() {
        return new CalendarRepeatExpression(1, DateUnits.DAYS);
    }

    /**
     * Helper to create a weekly repeat expression.
     *
     * @return a new expression
     */
    public static RepeatExpression weekly() {
        return new CalendarRepeatExpression(1, DateUnits.WEEKS);
    }

    /**
     * Helper to create a monthly repeat expression.
     *
     * @return a new expression
     */
    public static RepeatExpression monthly() {
        return new CalendarRepeatExpression(1, DateUnits.MONTHS);
    }

    /**
     * Helper to create a yearly repeat expression.
     *
     * @return a new expression
     */
    public static RepeatExpression yearly() {
        return new CalendarRepeatExpression(1, DateUnits.YEARS);
    }

    /**
     * Helper to create a weekdays repeat expression.
     *
     * @param startTime the expression start time
     * @return a new expression
     */
    public static RepeatExpression weekdays(Date startTime) {
        return CronRepeatExpression.weekdays(startTime);
    }
}
