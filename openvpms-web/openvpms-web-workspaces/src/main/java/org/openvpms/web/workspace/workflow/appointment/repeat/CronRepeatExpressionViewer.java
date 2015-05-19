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

import java.text.DateFormatSymbols;

/**
 * Viewer for {@link CronRepeatExpression}s.
 *
 * @author Tim Anderson
 */
public abstract class CronRepeatExpressionViewer implements RepeatExpressionViewer {

    /**
     * The expression.
     */
    private final CronRepeatExpression expression;

    /**
     * Constructs a {@link CronRepeatExpressionViewer}.
     *
     * @param expression the expression
     */
    public CronRepeatExpressionViewer(CronRepeatExpression expression) {
        this.expression = expression;
    }

    /**
     * Returns the expression.
     *
     * @return the expression
     */
    @Override
    public CronRepeatExpression getExpression() {
        return expression;
    }

    /**
     * Helper to return a formatted day string.
     *
     * @return the day
     */
    protected String getDay() {
        String[] days = DateFormatSymbols.getInstance().getWeekdays();
        int day = expression.getDayOfWeek().day();
        return day >= 0 && day < days.length ? days[day] : "EXPRESSION ERROR";
    }

    /**
     * Helper to return a formatted month string.
     *
     * @return the month
     */
    protected String getMonth() {
        String[] months = DateFormatSymbols.getInstance().getMonths();
        int month = getExpression().getMonth().month() - 1;
        return month >= 0 && month < months.length ? months[month] : "EXPRESSION ERROR";
    }

}

