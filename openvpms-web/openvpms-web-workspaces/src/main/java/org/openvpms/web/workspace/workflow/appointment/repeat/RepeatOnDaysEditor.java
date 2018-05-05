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
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.button.ToggleButton;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfMonth;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfWeek;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.Month;

/**
 * An editor for expressions that repeat on Sunday-Saturday.
 *
 * @author Tim Anderson
 */
class RepeatOnDaysEditor extends AbstractRepeatExpressionEditor {

    /**
     * The days to repeat on.
     */
    private ToggleButton[] days = new ToggleButton[7];


    /**
     * Constructs an {@link RepeatOnDaysEditor}.
     *
     * @param startTime the series start time. May be {@code null}
     */
    public RepeatOnDaysEditor(Date startTime) {
        super(startTime);
        String[] weekdays = DateFormatSymbols.getInstance().getShortWeekdays();
        for (int i = 0; i < days.length; ++i) {
            int day = Calendar.SUNDAY + i;
            String name = weekdays[day];
            days[i] = new ToggleButton(name, false);
        }

        if (startTime != null) {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startTime);
            days[calendar.get(Calendar.DAY_OF_WEEK) - 1].setSelected(true);
        }
    }

    /**
     * Constructs an {@link RepeatOnDaysEditor}.
     *
     * @param expression the source expression
     */
    public RepeatOnDaysEditor(CronRepeatExpression expression) {
        this((Date) null);
        DayOfWeek dayOfWeek = expression.getDayOfWeek();
        for (int i = 0; i < days.length; ++i) {
            int day = Calendar.SUNDAY + i;
            if (dayOfWeek.isSelected(day)) {
                days[i].setSelected(true);
            }
        }
    }

    /**
     * Returns the component representing the editor.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        Row row = new Row();
        row.setCellSpacing(new Extent(1));
        FocusGroup group = getFocusGroup();
        for (ToggleButton day : days) {
            row.add(day);
            group.add(day);
        }
        Label every = LabelFactory.create("workflow.scheduling.appointment.every");
        return RowFactory.create(Styles.CELL_SPACING, every, row);
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
        return !dayOfWeek.isOrdinal() && dayOfMonth.isAll() && month.isAll();
    }

    /**
     * Returns the expression.
     *
     * @return the expression, or {@code null} if the expression is invalid
     */
    @Override
    public RepeatExpression getExpression() {
        List<String> list = new ArrayList<String>();
        Date startTime = getStartTime();
        if (startTime != null) {
            for (int i = 0; i < days.length; ++i) {
                boolean selected = days[i].isSelected();
                if (selected) {
                    String day = DayOfWeek.getDay(Calendar.SUNDAY + i);
                    list.add(day);
                }
            }
            if (!list.isEmpty()) {
                DayOfWeek dayOfWeek = DayOfWeek.days(list);
                return new CronRepeatExpression(startTime, dayOfWeek);
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
        for (ToggleButton button : days) {
            if (button.isSelected()) {
                return true;
            }
        }
        validator.add(this, new ValidatorError(Messages.get("workflow.scheduling.appointment.selectdays")));
        return false;
    }
}
