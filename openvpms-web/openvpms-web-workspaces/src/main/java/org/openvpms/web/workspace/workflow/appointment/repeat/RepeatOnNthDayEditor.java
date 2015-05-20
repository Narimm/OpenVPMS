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
import nextapp.echo2.app.SelectField;
import org.joda.time.DateTime;
import org.openvpms.web.component.bound.SpinBox;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Date;

import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfMonth;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfWeek;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.Month;

/**
 * A {@link RepeatExpressionEditor} that supports expressions that repeat on the first..fifth/last Sunday-Saturday of
 * the month.
 *
 * @author Tim Anderson
 */
class RepeatOnNthDayEditor extends AbstractRepeatOnNthDayEditor {

    /**
     * Constructs an {@link RepeatOnNthDayEditor}.
     *
     * @param startTime the expression start time. May be {@code null}
     */
    public RepeatOnNthDayEditor(Date startTime) {
        super(startTime);
    }

    /**
     * Constructs an {@link RepeatOnNthDayEditor}.
     *
     * @param expression the source expression
     */
    public RepeatOnNthDayEditor(CronRepeatExpression expression) {
        super(expression);
        Month month = expression.getMonth();
        setInterval(month.getInterval());
    }

    /**
     * Returns the component representing the editor.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        SelectField ordinalField = createOrdinalSelector();
        SelectField dayField = createDaySelector();

        Label the = LabelFactory.create("workflow.scheduling.appointment.onthe");
        Label every = LabelFactory.create();
        every.setText(Messages.get("workflow.scheduling.appointment.every").toLowerCase());
        Label label = LabelFactory.create("workflow.scheduling.appointment.months");
        SpinBox intervalField = createIntervalField();
        FocusGroup focusGroup = getFocusGroup();
        focusGroup.add(ordinalField);
        focusGroup.add(dayField);
        focusGroup.add(intervalField);
        return RowFactory.create(Styles.CELL_SPACING, the, ordinalField, dayField, every, intervalField, label);
    }

    /**
     * Returns the expression.
     *
     * @return the expression, or {@code null} if the expression is invalid
     */
    @Override
    public RepeatExpression getExpression() {
        Date startTime = getStartTime();
        DayOfWeek dayOfWeek = getDayOfWeek();
        if (startTime != null && dayOfWeek != null) {
            DateTime time = new DateTime(startTime);
            return new CronRepeatExpression(startTime, Month.every(time.getMonthOfYear(), getInterval()), dayOfWeek);
        }
        return null;
    }

    /**
     * Determines if the editor can edit the supplied expression.
     *
     * @param expression the expression
     * @return {@code true} if the editor can edit the expression
     */
    public static boolean supports(CronRepeatExpression expression) {
        DayOfWeek dayOfWeek = expression.getDayOfWeek();
        DayOfMonth dayOfMonth = expression.getDayOfMonth();
        Month month = expression.getMonth();
        return dayOfWeek.isOrdinal() && dayOfMonth.isAll() && month.getInterval() != -1;
    }

}
