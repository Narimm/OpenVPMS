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
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfMonth;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfWeek;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.Month;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.Year;

/**
 * A {@link RepeatExpressionEditor} that supports expressions that repeat on the first..fifth/last Sunday-Saturday of
 * a particular month every N years.
 *
 * @author Tim Anderson
 */
class RepeatOnNthDayInMonthEditor extends AbstractRepeatOnNthDayEditor {

    /**
     * The month to repeat on.
     */
    private SimpleProperty month = new SimpleProperty("month", Integer.class);

    /**
     * Constructs an {@link RepeatOnNthDayInMonthEditor}.
     *
     * @param startTime the expression start time. May be {@code null}
     */
    public RepeatOnNthDayInMonthEditor(Date startTime) {
        super(startTime);
        if (startTime != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startTime);
            month.setValue(calendar.get(Calendar.MONTH) + 1); // cron months go from 1..12
            setInterval(1);
        }
    }

    /**
     * Constructs an {@link RepeatOnNthDayInMonthEditor}.
     *
     * @param expression the source expression
     */
    public RepeatOnNthDayInMonthEditor(CronRepeatExpression expression) {
        super(expression);
        month.setValue(expression.getMonth().month());
        setInterval(expression.getYear().getInterval());
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
        SelectField monthField = new MonthSelectField(month);

        Label every = LabelFactory.create();
        every.setText(Messages.get("workflow.scheduling.appointment.every").toLowerCase());
        SpinBox intervalField = createIntervalField();
        FocusGroup focusGroup = getFocusGroup();
        focusGroup.add(ordinalField);
        focusGroup.add(dayField);
        focusGroup.add(monthField);
        focusGroup.add(intervalField);
        return RowFactory.create(Styles.CELL_SPACING, LabelFactory.create("workflow.scheduling.appointment.onthe"),
                                 ordinalField, dayField, LabelFactory.create("workflow.scheduling.appointment.of"),
                                 monthField, every, intervalField,
                                 LabelFactory.create("workflow.scheduling.appointment.years"));
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
            return new CronRepeatExpression(startTime, Month.month(month.getInt()), dayOfWeek,
                                            Year.every(time.getYear(), getInterval()));
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
        Year year = expression.getYear();
        return dayOfWeek.isOrdinal() && dayOfMonth.isAll() && month.singleMonth() && year.getInterval() != -1;
    }

}
