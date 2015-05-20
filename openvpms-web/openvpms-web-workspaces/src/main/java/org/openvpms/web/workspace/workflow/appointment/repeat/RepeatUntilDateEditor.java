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

import echopointng.DateField;
import nextapp.echo2.app.Component;
import org.openvpms.web.component.bound.BoundDateFieldFactory;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;

import java.util.Date;

/**
 * A {@link RepeatUntilEditor} that limits an expression to repeat until a specific date.
 *
 * @author Tim Anderson
 */
public class RepeatUntilDateEditor extends AbstractRepeatUntilEditor {

    /**
     * Constructs a {@link RepeatUntilDateEditor}.
     *
     * @param date the date to repeat until
     */
    public RepeatUntilDateEditor(Date date) {
        super(new SimpleProperty("date", Date.class));
        SimpleProperty property = (SimpleProperty) getProperty();
        property.setRequired(true);
        property.setValue(date);
    }

    /**
     * Constructs a {@link RepeatUntilDateEditor}.
     *
     * @param condition specifies the date to repeat until
     */
    public RepeatUntilDateEditor(RepeatUntilDateCondition condition) {
        this(condition.getDate());
    }

    /**
     * Returns the condition.
     *
     * @return the condition, or {@code null} if the condition is invalid
     */
    @Override
    public RepeatCondition getCondition() {
        return isValid() ? new RepeatUntilDateCondition(getProperty().getDate()) : null;
    }

    /**
     * Returns the editor component.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        DateField field = BoundDateFieldFactory.create(getProperty());
        field.setPopUpAlwaysOnTop(true);
        getFocusGroup().add(field);
        return RowFactory.create(Styles.CELL_SPACING, LabelFactory.create("workflow.scheduling.appointment.until"),
                                 field);
    }

}
