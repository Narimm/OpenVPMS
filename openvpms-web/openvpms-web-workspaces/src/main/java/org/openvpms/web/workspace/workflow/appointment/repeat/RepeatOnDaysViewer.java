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
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.web.echo.button.ToggleButton;
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
class RepeatOnDaysViewer extends CronRepeatExpressionViewer {

    /**
     * Constructs a {@link RepeatOnDaysViewer}.
     *
     * @param expression the expression
     */
    public RepeatOnDaysViewer(CronRepeatExpression expression) {
        super(expression);
    }

    /**
     * Returns a component representing the expression.
     *
     * @return a new component
     */
    public Component getComponent() {
        Row row = new Row();
        row.setCellSpacing(new Extent(1));
        CronRepeatExpression expression = getExpression();
        String[] days = DateFormatSymbols.getInstance().getShortWeekdays();
        for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; ++i) {
            boolean selected = expression.getDayOfWeek().isSelected(i);
            ToggleButton button = new ToggleButton(days[i], selected);
            button.setEnabled(false);
            row.add(button);
        }
        Label every = LabelFactory.create("workflow.scheduling.appointment.every");
        return RowFactory.create(Styles.CELL_SPACING, every, row);
    }
}
