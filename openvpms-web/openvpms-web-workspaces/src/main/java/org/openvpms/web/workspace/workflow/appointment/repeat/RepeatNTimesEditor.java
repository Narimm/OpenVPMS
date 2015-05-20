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
import org.openvpms.web.component.bound.SpinBox;
import org.openvpms.web.component.property.NumericPropertyTransformer;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

/**
 * A {@link RepeatUntilEditor} that limits an expression to repeat a specified number of times.
 *
 * @author Tim Anderson
 */
public class RepeatNTimesEditor extends AbstractRepeatUntilEditor {

    /**
     * The maximum no. of repeats. This limits appointment series to 365 appointments, corresponding to a years
     * worth of daily appointments.
     */
    private static final int MAX_REPEATS = 364;

    /**
     * Constructs an {@link RepeatNTimesEditor}.
     */
    public RepeatNTimesEditor() {
        this(null);
    }

    /**
     * Constructs a {@link RepeatNTimesEditor}.
     *
     * @param times the no. of times to repeat
     */
    public RepeatNTimesEditor(int times) {
        this(null);
        getProperty().setValue(times);
    }

    /**
     * Constructs an {@link RepeatNTimesEditor}.
     *
     * @param condition the condition. May be {@code null}
     */
    public RepeatNTimesEditor(RepeatNTimesCondition condition) {
        super(new SimpleProperty("times", Integer.class));
        SimpleProperty times = (SimpleProperty) getProperty();
        times.setRequired(true);
        times.setTransformer(new NumericPropertyTransformer(times, true));
        if (condition != null) {
            times.setValue(condition.getTimes());
        }
    }

    /**
     * Returns the condition.
     *
     * @return the condition, or {@code null} if the condition is invalid
     */
    @Override
    public RepeatCondition getCondition() {
        Property times = getProperty();
        return times.isValid() ? new RepeatNTimesCondition(times.getInt()) : null;
    }

    /**
     * Returns the editor component.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        SpinBox field = new SpinBox(getProperty(), 1, MAX_REPEATS);
        getFocusGroup().add(field);
        return RowFactory.create(Styles.CELL_SPACING, field,
                                 LabelFactory.create("workflow.scheduling.appointment.times"));
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean result = super.doValidation(validator);
        if (result && getProperty().getInt() > MAX_REPEATS) {
            validator.add(this, new ValidatorError(Messages.format("workflow.scheduling.appointment.maxrepeats",
                                                                   MAX_REPEATS)));
            result = false;
        }
        return result;
    }
}
