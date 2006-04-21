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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.web.component.dialog.ErrorDialog;


/**
 * Represents a property of an {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectProperty implements Property, CollectionProperty {

    /**
     * The object that the property belongs to.
     */
    private final IMObject _object;

    /**
     * The property descriptor.
     */
    private final NodeDescriptor _descriptor;

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
    protected PropertyHandler _handler;


    /**
     * Construct a new <code>IMObjectProperty</code>.
     *
     * @param object     the object that the property belongs t
     * @param descriptor the property descriptor
     */
    public IMObjectProperty(IMObject object, NodeDescriptor descriptor) {
        _object = object;
        _descriptor = descriptor;
    }

    /**
     * Returns the value of the property.
     *
     * @return the property value
     */
    public Object getValue() {
        return _descriptor.getValue(_object);
    }

    /**
     * Returns the collection.
     *
     * @return the collection
     */
    public Collection getValues() {
        List<IMObject> values = _descriptor.getChildren(_object);
        if (values != null) {
            values = Collections.unmodifiableList(values);
        }
        return values;
    }

    /**
     * Returns the property descriptor.
     *
     * @return the property descriptor
     */
    public NodeDescriptor getDescriptor() {
        return _descriptor;
    }

    /**
     * Set the value of the property.
     *
     * @param value the property value
     */
    public void setValue(Object value) {
        checkReadOnly();
        try {
            value = getHandler().apply(value);
            _descriptor.setValue(_object, value);
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
        checkReadOnly();
        try {
            value = getHandler().apply(value);
            _descriptor.addChildToCollection(_object, value);
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
        checkReadOnly();
        try {
            value = getHandler().apply(value);
            _descriptor.removeChildFromCollection(_object, value);
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
            _valid = getHandler().isValid(getValue());
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
        String title = "Error: " + _descriptor.getDisplayName();
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

    /**
     * Returns the property handler.
     *
     * @return the property handler
     */
    private PropertyHandler getHandler() {
        if (_handler == null) {
            _handler = PropertyHandlerFactory.create(_descriptor);
        }
        return _handler;
    }

    /**
     * Verifies that the property can be modified.
     *
     * @throws UnsupportedOperationException if the property is read-only or
     *                                       derived
     */
    private void checkReadOnly() {
        if (_descriptor.isReadOnly() || _descriptor.isDerived()) {
            throw new UnsupportedOperationException(
                    "Attenpt to modify read-only property: "
                    + getDescriptor().getDisplayName());
        }
    }
}
