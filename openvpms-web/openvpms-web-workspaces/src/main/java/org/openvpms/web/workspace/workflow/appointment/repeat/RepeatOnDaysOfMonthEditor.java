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
import org.openvpms.web.echo.button.ToggleButton;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.Date;
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

    private final ToggleButton lastDay;

    /**
     * Constructs an {@link RepeatOnDaysOfMonthEditor}.
     */
    public RepeatOnDaysOfMonthEditor() {
        this(null);
    }

    /**
     * Constructs an {@link RepeatOnDaysOfMonthEditor}.
     *
     * @param expression the source expression. May be {@code null}
     */
    public RepeatOnDaysOfMonthEditor(CronRepeatExpression expression) {
        DayOfMonth dayOfMonth = (expression != null) ? expression.getDayOfMonth() : null;
        for (int i = 0; i < days.length; ++i) {
            int day = i + 1;
            boolean selected = (dayOfMonth != null) && dayOfMonth.isSelected(day);
            final ToggleButton button = new ToggleButton("" + (day), selected);
            button.setAlignment(Alignment.ALIGN_RIGHT);
            days[i] = button;
        }
        boolean last = (dayOfMonth != null) && dayOfMonth.hasLast();
        lastDay = new ToggleButton(Messages.get("workflow.scheduling.appointment.lastday"), last);
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
        Label every = LabelFactory.create("workflow.scheduling.appointment.every");
        return RowFactory.create(Styles.CELL_SPACING, every, grid);
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
        return dayOfWeek.isAll() && !dayOfMonth.isAll() && month.isAll();
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
                DayOfMonth dayOfMonth = new DayOfMonth(list, last);
                return new CronRepeatExpression(startTime, dayOfMonth, Month.ALL, DayOfWeek.NO_VALUE);
            }
        }
        return null;
    }
}
