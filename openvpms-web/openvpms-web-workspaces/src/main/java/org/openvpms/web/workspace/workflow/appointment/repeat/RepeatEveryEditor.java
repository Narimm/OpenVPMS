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
import org.apache.commons.lang.WordUtils;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextField;

/**
 * An editor for repeat expressions that repeat every N days/weeks/months/years.
 *
 * @author Tim Anderson
 */
class RepeatEveryEditor implements RepeatExpressionEditor {

    /**
     * The interval.
     */
    private SimpleProperty interval = new SimpleProperty("interval", Integer.class);

    /**
     * The date units.
     */
    private final DateUnits units;

    /**
     * Constructs an {@link RepeatEveryEditor}.
     *
     * @param units the date units
     */
    public RepeatEveryEditor(DateUnits units) {
        this.units = units;
    }

    /**
     * Constructs an {@link RepeatEveryEditor}.
     *
     * @param expression the source expression
     */
    public RepeatEveryEditor(CalendarRepeatExpression expression) {
        interval.setValue(expression.getInterval());
        units = expression.getUnits();
    }

    /**
     * Returns the component representing the editor.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        Label every = LabelFactory.create();
        every.setText("Every");
        Label days = LabelFactory.create();
        days.setText(WordUtils.capitalizeFully(units.toString()));
        TextField field = BoundTextComponentFactory.create(interval, 5);
        return RowFactory.create(Styles.CELL_SPACING, every, field, days);
    }

    /**
     * Returns the expression.
     *
     * @return the expression
     */
    @Override
    public RepeatExpression getExpression() {
        return new CalendarRepeatExpression(interval.getInt(), units);
    }

    /**
     * Determines if the editor is valid.
     *
     * @return {@code true} if the editor is valid
     */
    @Override
    public boolean isValid() {
        return interval.isValid() && interval.getInt() > 0;
    }
}
