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
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Label;
import org.openvpms.web.echo.button.CheckBox;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;

import java.text.DateFormatSymbols;
import java.util.Calendar;

/**
 * A viewer for repeat expressions produced by {@link RepeatOnDaysEditor}.
 *
 * @author Tim Anderson
 */
class RepeatOnDaysViewer {

    /**
     * The expression.
     */
    private final CronRepeatExpression expression;

    /**
     * Constructs a {@link RepeatOnDaysViewer}.
     *
     * @param expression the expression
     */
    public RepeatOnDaysViewer(CronRepeatExpression expression) {
        this.expression = expression;
    }

    /**
     * Returns a component representing the expression.
     *
     * @return a new component
     */
    public Component getComponent() {
        Grid grid = new Grid(4);
        String[] days = DateFormatSymbols.getInstance().getWeekdays();
        for (int i = Calendar.SUNDAY; i < Calendar.SATURDAY; ++i) {
            boolean selected = expression.getDayOfWeek().isSelected(i);
            CheckBox checkBox = CheckBoxFactory.create(selected);
            checkBox.setText(days[i]);
            grid.add(checkBox);
        }
        Label every = LabelFactory.create("workflow.scheduling.appointment.every");
        return RowFactory.create(Styles.CELL_SPACING, every, grid);
    }
}
