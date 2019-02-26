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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.property;

import org.openvpms.web.component.edit.AlertListener;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.echo.focus.FocusGroup;

/**
 * Abstract implementation of {@link Editor}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractEditor extends AbstractModifiable implements Editor {

    /**
     * The listeners.
     */
    private ModifiableListeners listeners;

    /**
     * The error listener.
     */
    private ErrorListener errorListener;

    /**
     * The alert listener.
     */
    private AlertListener alertListener;

    /**
     * Determines if the editor has been modified.
     */
    private boolean modified;

    /**
     * The focus group.
     */
    private FocusGroup focusGroup;

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
     */
    @Override
    public boolean isModified() {
        return modified;
    }

    /**
     * Clears the modified status of the object.
     */
    @Override
    public void clearModified() {
        modified = false;
    }

    /**
     * Adds a listener to be notified when this changes.
     * <p/>
     * Listeners will be notified in the order they were registered.
     * <p/>
     * Duplicate additions are ignored.
     *
     * @param listener the listener to add
     */
    @Override
    public void addModifiableListener(ModifiableListener listener) {
        if (listeners == null) {
            listeners = new ModifiableListeners();
        }
        listeners.addListener(listener);
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    @Override
    public void addModifiableListener(ModifiableListener listener, int index) {
        if (listeners == null) {
            listeners = new ModifiableListeners();
        }
        listeners.addListener(listener, index);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeModifiableListener(ModifiableListener listener) {
        if (listeners != null) {
            listeners.removeListener(listener);
        }
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group, or {@code null} if the editor hasn't been rendered
     */
    @Override
    public FocusGroup getFocusGroup() {
        if (focusGroup == null) {
            focusGroup = new FocusGroup(getClass().getSimpleName());
        }
        return focusGroup;
    }

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener to register. May be {@code null}
     */
    @Override
    public void setErrorListener(ErrorListener listener) {
        this.errorListener = listener;
    }

    /**
     * Returns the listener to be notified of errors.
     *
     * @return the listener. May be {@code null}
     */
    @Override
    public ErrorListener getErrorListener() {
        return errorListener;
    }

    /**
     * Registers a listener to be notified of alerts.
     *
     * @param listener the listener. May be {@code null}
     */
    @Override
    public void setAlertListener(AlertListener listener) {
        this.alertListener = listener;
    }

    /**
     * Returns the listener to be notified of alerts.
     *
     * @return the listener. May be {@code null}
     */
    @Override
    public AlertListener getAlertListener() {
        return alertListener;
    }

    /**
     * Disposes of the editor.
     * <br/>
     * Once disposed, the behaviour of invoking any method is undefined.
     */
    @Override
    public void dispose() {
        if (listeners != null) {
            listeners.removeAll();
            listeners = null;
        }
        focusGroup = null;
        errorListener = null;
        alertListener = null;
    }

    /**
     * Flags the editor as modified.
     */
    protected void setModified() {
        modified = true;
    }

    /**
     * Notify listeners that this has changed.
     */
    protected void notifyListeners() {
        if (listeners != null) {
            listeners.notifyListeners(this);
        }
    }
}
