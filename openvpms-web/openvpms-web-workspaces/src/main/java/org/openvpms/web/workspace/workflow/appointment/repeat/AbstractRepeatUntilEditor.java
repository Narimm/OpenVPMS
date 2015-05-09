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
import org.openvpms.web.echo.focus.FocusGroup;

/**
 * Abstract implementation of the {@link RepeatUntilEditor} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractRepeatUntilEditor extends AbstractModifiable implements RepeatUntilEditor {

    /**
     * The focus group.
     */
    private final FocusGroup group = new FocusGroup(getClass().getSimpleName());

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
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    @Override
    public void addModifiableListener(ModifiableListener listener, int index) {
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeModifiableListener(ModifiableListener listener) {
    }

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener to register. May be {@code null}
     */
    @Override
    public void setErrorListener(ErrorListener listener) {
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
     * Returns the focus group.
     *
     * @return the focus group
     */
    @Override
    public FocusGroup getFocusGroup() {
        return group;
    }
}
