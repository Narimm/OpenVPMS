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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.edit;

import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.ErrorListener;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;


/**
 * Abstract implementation of the {@link PropertyEditor} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPropertyEditor extends AbstractModifiable implements PropertyEditor {

    /**
     * The property being edited.
     */
    private final Property property;

    /**
     * Listener to reset the validity status.
     */
    private final ModifiableListener listener;


    /**
     * Constructs an {@link AbstractPropertyEditor}.
     *
     * @param property the property being edited
     */
    public AbstractPropertyEditor(Property property) {
        this.property = property;
        listener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                AbstractPropertyEditor.this.modified();
            }
        };
        property.addModifiableListener(listener);
    }

    /**
     * Returns the property being edited.
     *
     * @return the property being edited
     */
    public Property getProperty() {
        return property;
    }

    /**
     * Determines if the object has been modified.
     *
     * @return {@code true} if the object has been modified
     */
    public boolean isModified() {
        return getProperty().isModified();
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        getProperty().clearModified();
    }

    /**
     * Add a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        getProperty().addModifiableListener(listener);
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    public void addModifiableListener(ModifiableListener listener, int index) {
        getProperty().addModifiableListener(listener, index);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        getProperty().removeModifiableListener(listener);
    }

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener to register. May be {@code null}
     */
    @Override
    public void setErrorListener(ErrorListener listener) {
        getProperty().setErrorListener(listener);
    }

    /**
     * Returns the listener to be notified of errors.
     *
     * @return the listener. May be {@code null}
     */
    @Override
    public ErrorListener getErrorListener() {
        return getProperty().getErrorListener();
    }

    /**
     * Registers a listener to be notified of alerts.
     *
     * @param listener the listener. May be {@code null}
     */
    @Override
    public void setAlertListener(AlertListener listener) {
        // no-op
    }

    /**
     * Returns the listener to be notified of alerts.
     *
     * @return the listener. May be {@code null}
     */
    @Override
    public AlertListener getAlertListener() {
        return null;
    }

    /**
     * Disposes of the editor.
     * <br/>
     * Once disposed, the behaviour of invoking any method is undefined.
     */
    public void dispose() {
        getProperty().removeModifiableListener(listener);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    protected boolean doValidation(Validator validator) {
        return getProperty().validate(validator);
    }

    /**
     * Resets the cached validity state of the object.
     *
     * @param descendants if {@code true} reset the validity state of any descendants as well.
     */
    @Override
    protected void resetValid(boolean descendants) {
        super.resetValid(descendants);
        getProperty().resetValid();
    }

    /**
     * Invoked when the property is modified.
     * <p/>
     * This calls {@link #resetValid()}.
     */
    protected void modified() {
        resetValid();
    }
}
