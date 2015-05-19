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
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.web.component.bound.SpinBox;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

/**
 * An editor for repeat expressions that repeat every N days/weeks/months/years.
 *
 * @author Tim Anderson
 */
class RepeatEveryEditor extends AbstractRepeatExpressionEditor {

    /**
     * The interval.
     */
    private SimpleProperty interval = new SimpleProperty("interval", null, Integer.class,
                                                         Messages.get("workflow.scheduling.appointment.interval"));

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
        Label every = LabelFactory.create("workflow.scheduling.appointment.every");
        Label label = LabelFactory.create();
        String unitText = RepeatHelper.toString(units);
        label.setText(unitText);
        SpinBox field = new SpinBox(interval, 1, 999);
        getFocusGroup().add(field);
        return RowFactory.create(Styles.CELL_SPACING, every, field, label);
    }

    /**
     * Returns the expression.
     *
     * @return the expression, or {@code null} if the expression is invalid
     */
    @Override
    public RepeatExpression getExpression() {
        return new CalendarRepeatExpression(interval.getInt(), units);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean result = interval.validate(validator);
        if (result) {
            result = interval.getInt() > 0;
            if (!result) {
                String message = Messages.get("workflow.scheduling.appointment.invalidInterval");
                validator.add(interval, new ValidatorError(interval, message));
            }
        }
        return result;
    }

}
