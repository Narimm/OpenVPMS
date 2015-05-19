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

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.layout.GridLayoutData;
import org.joda.time.DateTime;
import org.openvpms.web.component.bound.SpinBox;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.button.ToggleButton;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfMonth;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfWeek;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.Month;

/**
 * A {@link RepeatExpressionEditor} that supports expressions that repeat on days of the month.
 *
 * @author Tim Anderson
 */
class RepeatOnDaysOfMonthEditor extends AbstractRepeatExpressionEditor {

    /**
     * The days to repeat on.
     */
    private final ToggleButton[] days = new ToggleButton[31];

    /**
     * The last day of the month button.
     */
    private final ToggleButton lastDay;

    /**
     * The month interval.
     */
    private final SimpleProperty interval
            = new SimpleProperty("interval", null, Integer.class,
                                 Messages.get("workflow.scheduling.appointment.interval"));

    /**
     * Constructs an {@link RepeatOnDaysOfMonthEditor}.
     *
     * @param startTime time to start the expression on. May be {@code null}
     */
    public RepeatOnDaysOfMonthEditor(Date startTime) {
        super(startTime);
        interval.setRequired(true);
        for (int i = 0; i < days.length; ++i) {
            int day = i + 1;
            ToggleButton button = new ToggleButton("" + day, false);
            button.setAlignment(Alignment.ALIGN_RIGHT);
            days[i] = button;
        }
        lastDay = new ToggleButton(Messages.get("workflow.scheduling.appointment.lastday"));
        if (startTime != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startTime);
            days[calendar.get(Calendar.DATE) - 1].setSelected(true);
        }
        interval.setValue(1);
    }

    /**
     * Constructs an {@link RepeatOnDaysOfMonthEditor}.
     *
     * @param expression the source expression
     */
    public RepeatOnDaysOfMonthEditor(CronRepeatExpression expression) {
        this((Date) null);
        DayOfMonth dayOfMonth = expression.getDayOfMonth();
        for (int i = 0; i < days.length; ++i) {
            int day = i + 1;
            if (dayOfMonth.isSelected(day)) {
                days[i].setSelected(true);
            }
        }
        if (dayOfMonth.hasLast()) {
            lastDay.setSelected(true);
        }
        interval.setValue(expression.getMonth().getInterval());
    }

    /**
     * Returns the component representing the editor.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        Grid grid = new Grid(7);
        grid.setInsets(new Insets(1));
        FocusGroup group = getFocusGroup();
        for (ToggleButton day : days) {
            grid.add(day);
            group.add(day);
        }
        GridLayoutData layout = new GridLayoutData();
        layout.setColumnSpan(4);
        lastDay.setLayoutData(layout);
        lastDay.setAlignment(Alignment.ALIGN_CENTER);
        grid.add(lastDay);
        Label onthe = LabelFactory.create("workflow.scheduling.appointment.onthe");
        Label every = LabelFactory.create();
        every.setText(Messages.get("workflow.scheduling.appointment.every").toLowerCase());
        SpinBox intervalField = new SpinBox(interval, 1, 12);
        Label months = LabelFactory.create("workflow.scheduling.appointment.months");
        return RowFactory.create(Styles.CELL_SPACING, onthe, grid, every, intervalField, months);
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
        return dayOfWeek.isAll() && !dayOfMonth.isAll() && month.getInterval() != -1;
    }

    /**
     * Returns the expression.
     *
     * @return the expression, or {@code null} if the expression is invalid
     */
    @Override
    public RepeatExpression getExpression() {
        List<Integer> list = new ArrayList<Integer>();
        Date startTime = getStartTime();
        if (startTime != null) {
            for (int i = 0; i < days.length; ++i) {
                boolean selected = days[i].isSelected();
                if (selected) {
                    list.add(i + 1);
                }
            }
            boolean last = lastDay.isSelected();
            if (!list.isEmpty() || last) {
                DayOfMonth dayOfMonth = DayOfMonth.days(list, last);
                DateTime time = new DateTime(startTime);
                return new CronRepeatExpression(startTime, dayOfMonth, Month.every(time.getMonthOfYear(),
                                                                                   interval.getInt()));
            }
        }
        return null;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return validateDays(validator) && interval.validate(validator);
    }

    /**
     * Ensures that one or more days are selected.
     *
     * @param validator the validator
     * @return {@code true} if one or more days are selected
     */
    private boolean validateDays(Validator validator) {
        boolean result = lastDay.isSelected();
        if (!result) {
            for (ToggleButton button : days) {
                if (button.isSelected()) {
                    result = true;
                    break;
                }
            }
        }
        if (!result) {
            validator.add(this, new ValidatorError(Messages.get("workflow.scheduling.appointment.selectdays")));
        }
        return result;
    }
}
