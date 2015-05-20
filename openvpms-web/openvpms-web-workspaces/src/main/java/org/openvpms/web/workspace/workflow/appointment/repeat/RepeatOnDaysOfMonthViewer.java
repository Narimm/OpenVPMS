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
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.DayOfMonth;
import static org.openvpms.web.workspace.workflow.appointment.repeat.CronRepeatExpression.Month;

/**
 * A viewer for repeat expressions produced by {@link RepeatOnDaysOfMonthEditor}.
 *
 * @author Tim Anderson
 */
class RepeatOnDaysOfMonthViewer extends CronRepeatExpressionViewer {

    /**
     * Constructs a {@link RepeatOnDaysOfMonthViewer}.
     *
     * @param expression the expression
     */
    public RepeatOnDaysOfMonthViewer(CronRepeatExpression expression) {
        super(expression);
    }

    /**
     * Returns a component representing the expression.
     *
     * @return a new component
     */
    public Component getComponent() {
        CronRepeatExpression expression = getExpression();
        DayOfMonth dayOfMonth = expression.getDayOfMonth();
        Grid grid = new Grid(7);
        grid.setInsets(new Insets(1));
        for (int i = 0; i < 31; ++i) {
            int day = i + 1;
            boolean selected = dayOfMonth.isSelected(day);
            ToggleButton button = new ToggleButton("" + day, selected);
            button.setAlignment(Alignment.ALIGN_RIGHT);
            button.setEnabled(false);
            grid.add(button);
        }
        GridLayoutData layout = new GridLayoutData();
        layout.setColumnSpan(4);
        ToggleButton lastDay = new ToggleButton(Messages.get("workflow.scheduling.appointment.lastday"),
                                                dayOfMonth.hasLast());
        lastDay.setLayoutData(layout);
        lastDay.setAlignment(Alignment.ALIGN_CENTER);
        grid.add(lastDay);

        Label interval = LabelFactory.create();
        Month month = expression.getMonth();
        interval.setText(Messages.format("workflow.scheduling.appointment.everymonth", month.getInterval()));

        return RowFactory.create(Styles.CELL_SPACING, LabelFactory.create("workflow.scheduling.appointment.onthe"),
                                 grid, interval);
    }

}
