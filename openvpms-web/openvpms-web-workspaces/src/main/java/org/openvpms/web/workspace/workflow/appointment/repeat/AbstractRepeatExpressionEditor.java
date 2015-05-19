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

import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.ErrorListener;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.echo.focus.FocusGroup;

import java.util.Date;

/**
 * Abstract implementation of the {@link RepeatExpressionEditor} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractRepeatExpressionEditor extends AbstractModifiable implements RepeatExpressionEditor {

    /**
     * The focus group.
     */
    private final FocusGroup group = new FocusGroup(getClass().getName());

    /**
     * The time to start the expression on.
     */
    private Date startTime;

    /**
     * Constructs an {@link AbstractRepeatExpressionEditor}.
     */
    public AbstractRepeatExpressionEditor() {
        this(null);
    }

    /**
     * Constructs an {@link AbstractRepeatExpressionEditor}.
     *
     * @param startTime time to start the expression on. May be {@code null}
     */
    public AbstractRepeatExpressionEditor(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Sets the expression start time.
     *
     * @param startTime the start time
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the time to start the expression.
     *
     * @return the start time. May be {@code null}
     */
    @Override
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
     */
    @Override
    public boolean isModified() {
        return false;
    }

    /**
     * Clears the modified status of the object.
     */
    @Override
    public void clearModified() {
    }

    /**
     * Adds a listener to be notified when this changes.
     * <p/>
     * Listeners will be notified in the order they were registered.
     *
     * @param listener the listener to add
     */
    @Override
    public void addModifiableListener(ModifiableListener listener) {
        // no-op
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    @Override
    public void addModifiableListener(ModifiableListener listener, int index) {
        // no-op
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeModifiableListener(ModifiableListener listener) {
        // no-op
    }

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener to register. May be {@code null}
     */
    @Override
    public void setErrorListener(ErrorListener listener) {
        // no-op
    }

    /**
     * Returns the listener to be notified of errors.
     *
     * @return the listener. May be {@code null}
     */
    @Override
    public ErrorListener getErrorListener() {
        return null;
    }

    /**
     * Returns the component focus group.
     *
     * @return the focus group
     */
    @Override
    public FocusGroup getFocusGroup() {
        return group;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        RepeatExpression expression = getExpression();
        boolean result = expression != null;
        if (!result) {
            validator.add(this, new ValidatorError("The expression is invalid"));
        }
        return result;
    }
}
