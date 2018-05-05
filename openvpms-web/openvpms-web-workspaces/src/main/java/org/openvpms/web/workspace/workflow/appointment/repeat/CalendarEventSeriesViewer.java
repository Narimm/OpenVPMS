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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.repeat;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;

/**
 * Calendar event series viewer.
 *
 * @author Tim Anderson
 */
public class CalendarEventSeriesViewer {

    /**
     * The event series.
     */
    private final CalendarEventSeries series;

    /**
     * Constructs an {@link CalendarEventSeriesViewer}.
     *
     * @param series the series to view
     */
    public CalendarEventSeriesViewer(CalendarEventSeries series) {
        this.series = series;
    }

    /**
     * Returns a component to display the series
     *
     * @return a new component
     */
    public Component getComponent() {
        Component result;
        RepeatExpression expression = series.getExpression();
        RepeatCondition condition = series.getCondition();
        if (expression == null) {
            result = LabelFactory.create("workflow.scheduling.appointment.norepeat");
        } else {
            Component expressionComponent;
            Label conditionComponent = LabelFactory.create();
            if (expression instanceof CalendarRepeatExpression) {
                expressionComponent = getExpression((CalendarRepeatExpression) expression);
            } else if (expression instanceof CronRepeatExpression) {
                expressionComponent = getExpression((CronRepeatExpression) expression);
            } else {
                expressionComponent = LabelFactory.create();
            }

            if (condition != null) {
                conditionComponent.setText(condition.toString());
            }
            result = ColumnFactory.create(Styles.CELL_SPACING, expressionComponent, conditionComponent);
        }
        return result;
    }

    private Component getExpression(CronRepeatExpression expression) {
        Component result;
        if (RepeatOnWeekdaysEditor.supports(expression)) {
            Label label = LabelFactory.create();
            label.setText(RepeatHelper.toString(RepeatExpression.Type.WEEKDAYS));
            result = label;
        } else if (RepeatOnDaysEditor.supports(expression)) {
            result = new RepeatOnDaysViewer(expression).getComponent();
        } else if (RepeatOnNthDayEditor.supports(expression)) {
            result = new RepeatOnNthDayViewer(expression).getComponent();
        } else if (RepeatOnDaysOfMonthEditor.supports(expression)) {
            result = new RepeatOnDaysOfMonthViewer(expression).getComponent();
        } else if (RepeatOnDateEditor.supports(expression)) {
            result = new RepeatOnDateViewer(expression).getComponent();
        } else if (RepeatOnNthDayInMonthEditor.supports(expression)) {
            result = new RepeatOnNthDayInMonthViewer(expression).getComponent();
        } else {
            result = LabelFactory.create();
        }
        return result;
    }

    private Component getExpression(CalendarRepeatExpression expression) {
        Component result;
        CalendarRepeatExpressionViewer viewer = new CalendarRepeatExpressionViewer(expression);
        result = viewer.getComponent();
        return result;
    }
}
