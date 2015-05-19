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
 * An editor for expressions that repeat on Sunday-Saturday.
 *
 * @author Tim Anderson
 */
class RepeatOnDateEditor extends AbstractRepeatExpressionEditor {


    /**
     * The day to repeat on.
     */
    private SimpleProperty day = new SimpleProperty("day", Integer.class);

    /**
     * The month to repeat on.
     */
    private SimpleProperty month = new SimpleProperty("month", Integer.class);

    /**
     * The interval.
     */
    private SimpleProperty interval = new SimpleProperty("interval", Integer.class);

    /**
     * Constructs an {@link RepeatOnDateEditor}.
     *
     * @param startTime the series start time. May be {@code null}
     */
    public RepeatOnDateEditor(Date startTime) {
        super(startTime);
        day.setRequired(true);
        interval.setRequired(true);
        month.setRequired(true);
        if (startTime != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startTime);
            day.setValue(calendar.get(Calendar.DATE));
            month.setValue(calendar.get(Calendar.MONTH) + 1); // cron months go from 1..12
        }
        interval.setValue(1);
    }

    /**
     * Constructs an {@link RepeatOnDateEditor}.
     *
     * @param expression the source expression
     */
    public RepeatOnDateEditor(CronRepeatExpression expression) {
        this((Date) null);
        DayOfMonth dayOfMonth = expression.getDayOfMonth();
        int selectedDay = dayOfMonth.day();
        if (selectedDay != -1) {
            day.setValue(selectedDay);
        }
        int selectedMonth = expression.getMonth().month();
        if (selectedMonth != -1) {
            month.setValue(selectedMonth);
        }
        Year year = expression.getYear();
        if (year.getInterval() != -1) {
            interval.setValue(year.getInterval());
        }
    }

    /**
     * Returns the component representing the editor.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        FocusGroup group = getFocusGroup();
        SpinBox dayField = new SpinBox(day, 1, 31);
        SelectField monthField = new MonthSelectField(month);
        SpinBox intervalField = new SpinBox(interval, 1, 10);

        group.add(dayField);
        group.add(monthField);
        group.add(intervalField);
        Label every = LabelFactory.create();
        every.setText(Messages.get("workflow.scheduling.appointment.every").toLowerCase());
        Label years = LabelFactory.create();
        years.setText(Messages.get("workflow.scheduling.appointment.years"));

        return RowFactory.create(Styles.CELL_SPACING, LabelFactory.create("workflow.scheduling.appointment.onthe"),
                                 dayField, monthField, every, intervalField, years);
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
        return dayOfWeek.isAll() && dayOfMonth.singleDay() && month.singleMonth() && year.getInterval() != -1;
    }

    /**
     * Returns the expression.
     *
     * @return the expression, or {@code null} if the expression is invalid
     */
    @Override
    public RepeatExpression getExpression() {
        Date startTime = getStartTime();
        if (startTime != null) {
            DayOfMonth dayOfMonth = DayOfMonth.day(day.getInt());
            DateTime time = new DateTime(startTime);
            return new CronRepeatExpression(startTime, dayOfMonth, Month.month(month.getInt()),
                                            Year.every(time.getYear(), interval.getInt()));
        }
        return null;
    }

}
