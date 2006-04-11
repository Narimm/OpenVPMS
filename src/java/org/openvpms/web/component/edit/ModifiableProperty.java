/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.edit;

import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.web.component.dialog.ErrorDialog;


/**
 * A modifiable {@link Property} that validates inputs using an {@link
 * Validator}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ModifiableProperty extends IMObjectProperty {

    /**
     * Determines if the underlying object is dirty.
     */
    private boolean _dirty;

    /**
     * Determines if the object is valid.
     */
    private Boolean _valid;

    /**
     * The listeners.
     */
    private ModifiableListeners _listeners;

    /**
     * The property handler.
     */
    private final PropertyHandler _handler;


    /**
     * Construct a new <code>IMObjectProperty</code>.
     *
     * @param object     the object that the property belongs t
     * @param descriptor the property descriptor
     */
    public ModifiableProperty(IMObject object, NodeDescriptor descriptor) {
        super(object, descriptor);
        _handler = PropertyHandlerFactory.create(descriptor);
    }

    /**
     * Set the value of the property.
     *
     * @param value the property value
     */
    public void setValue(Object value) {
        try {
            value = _handler.apply(value);
            NodeDescriptor descriptor = getDescriptor();
            descriptor.setValue(getObject(), value);
            modified();
        } catch (ValidationException exception) {
            invalidate(exception);
        }
    }

    /**
     * Add a value.
     *
     * @param value the value to add
     */
    public void add(Object value) {
        try {
            value = _handler.apply(value);
            NodeDescriptor descriptor = getDescriptor();
            descriptor.addChildToCollection(getObject(), value);
            modified();
        } catch (ValidationException exception) {
            invalidate(exception);
        }
    }

    /**
     * Remove a value.
     *
     * @param value the value to remove
     */
    public void remove(Object value) {
        try {
            value = _handler.apply(value);
            NodeDescriptor descriptor = getDescriptor();
            descriptor.removeChildFromCollection(getObject(), value);
            modified();
        } catch (ValidationException exception) {
            invalidate(exception);
        }
    }

    /**
     * Determines if the underlying object has been modified.
     *
     * @return <code>true</code> if this has been modified; otherwise
     *         <code>false</code>
     */
    public boolean isModified() {
        return _dirty;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        _dirty = false;
    }

    /**
     * Notify any listeners that they need to refresh.
     */
    public void refresh() {
        if (_listeners != null) {
            _listeners.notifyListeners(this);
        }
    }

    /**
     * Add a listener to be notified when a this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        if (_listeners == null) {
            _listeners = new ModifiableListeners();
        }
        _listeners.addListener(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        if (_listeners != null) {
            _listeners.removeListener(listener);
        }
    }

    /**
     * Determines if the object is valid.
     *
     * @return <code>true</code> if the object is valid; otherwise
     *         <code>false</code>
     */
    public boolean isValid() {
        if (_valid == null) {
            _valid = _handler.isValid(getValue());
        }
        return _valid;
    }

    /**
     * Invoked when this is modified. Updates flags, and notifies the
     * listeners.
     */
    private void modified() {
        _dirty = true;
        _valid = true;
        refresh();
    }

    /**
     * Invoked when an update fails. Marks this as invalid.
     *
     * @param exception the reason for the failure
     */
    private void invalidate(ValidationException exception) {
        _valid = false;
        String title = "Error: " + getDescriptor().getDisplayName();
        String message;
        List<ValidationError> errors = exception.getErrors();
        if (!errors.isEmpty()) {
            ValidationError error = errors.get(0);
            message = error.getErrorMessage();
        } else {
            message = exception.getMessage();
        }
        ErrorDialog.show(title, message);
    }


}
