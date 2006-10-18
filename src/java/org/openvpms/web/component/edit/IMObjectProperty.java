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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.ValidationHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


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
     * Current validation errors.
     */
    private List<ValidationError> _errors;

    /**
     * The listeners.
     */
    private ModifiableListeners _listeners;

    /**
     * The property handler.
     */
    private PropertyTransformer _transformer;

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(IMObjectProperty.class);


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
        List<IMObject> values = null;
        try {
            values = _descriptor.getChildren(_object);
            if (values != null) {
                values = Collections.unmodifiableList(values);
            }
        } catch (OpenVPMSException exception) {
            _log.error(exception, exception);
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
     * @return <code>true</code> if the value was set
     */
    public boolean setValue(Object value) {
        boolean set = false;
        checkReadOnly();
        try {
            value = getTransformer().apply(value);
            _descriptor.setValue(_object, value);
            set = true;
            modified();
        } catch (DescriptorException exception) {
            invalidate(exception);
        } catch (ValidationException exception) {
            invalidate(exception);
        }
        return set;
    }

    /**
     * Add a value.
     *
     * @param value the value to add
     */
    public void add(Object value) {
        checkReadOnly();
        try {
            value = getTransformer().apply(value);
            _descriptor.addChildToCollection(_object, value);
            modified();
        } catch (ValidationException exception) {
            invalidate(exception);
        } catch (DescriptorException exception) {
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
            value = getTransformer().apply(value);
            _descriptor.removeChildFromCollection(_object, value);
            modified();
        } catch (ValidationException exception) {
            invalidate(exception);
        } catch (DescriptorException exception) {
            invalidate(exception);
        }
    }

    /**
     * Returns the minimum cardinality.
     *
     * @return the minimum cardinality
     */
    public int getMinCardinality() {
        return _descriptor.getMinCardinality();
    }

    /**
     * Returns the maximum cardinality.
     *
     * @return the maximum cardinality, or <code>-1</code> if it is unbounded
     */
    public int getMaxCardinality() {
        if (_descriptor.getMaxCardinality() == NodeDescriptor.UNBOUNDED) {
            return -1;
        }
        return _descriptor.getMaxCardinality();
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
     * Notify any listeners that they need to refresh and marks this modified.
     */
    public void refresh() {
        _dirty = true;
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
        Validator validator = new Validator();
        return validator.validate(this);
    }

    /**
     * Validates the object.
     *
     * @param validator thhe validator
     */
    public boolean validate(Validator validator) {
        List<ValidationError> errors = null;
        if (_errors == null) {
            // determine if this is valid
            int minSize = getMinCardinality();
            if (minSize == 1 && getValue() == null) {
                addError("node.error.required", _descriptor.getDisplayName());
            } else if (_descriptor.isCollection()) {
                Collection values = getValues();
                int size = values.size();
                int maxSize = getMaxCardinality();
                if (minSize != -1 && size < minSize) {
                    addError("node.error.minSize", _descriptor.getDisplayName(),
                             minSize);
                } else if (maxSize != -1 && size > maxSize) {
                    addError("node.error.maxSize", _descriptor.getDisplayName(),
                             maxSize);
                } else if (size != 0) {
                    // don't cache any validation errors from collection objects
                    // as these may be corrected without updating the status
                    // of this property
                    IArchetypeService service
                            = ServiceHelper.getArchetypeService();
                    for (Object value : getValues()) {
                        IMObject object = (IMObject) value;
                        errors = ValidationHelper.validate(object, service);
                        if (errors != null) {
                            break;
                        }
                    }
                }
            } else {
                PropertyTransformer transformer = getTransformer();
                try {
                    transformer.apply(getValue());
                } catch (ValidationException exception) {
                    invalidate(exception);
                }
            }
        }
        if (_errors != null) {
            errors = _errors;
        }
        if (errors != null) {
            validator.add(this, errors);
        }
        return (errors != null);
    }

    /**
     * Returns the object that the property belongs to.
     *
     * @return the object
     */
    public IMObject getObject() {
        return _object;
    }

    /**
     * Sets the property transformer.
     *
     * @param transformer the property transformer. May be <code>null</code>
     */
    public void setTransformer(PropertyTransformer transformer) {
        _transformer = transformer;
    }

    /**
     * Returns the property transformer.
     * If none has been set, creates one using
     * {@link PropertyTransformerFactory#create}.
     *
     * @return the property transformer
     */
    public PropertyTransformer getTransformer() {
        if (_transformer == null) {
            _transformer = PropertyTransformerFactory.create(_object,
                                                             _descriptor);
        }
        return _transformer;
    }

    /**
     * Invoked when this is modified. Updates flags, and notifies the
     * listeners.
     */
    private void modified() {
        _errors = null;
        refresh();
    }

    /**
     * Invoked when an update fails. Marks this as invalid.
     *
     * @param exception the reason for the failure
     */
    private void invalidate(DescriptorException exception) {
        if (_errors != null && !_errors.isEmpty()) {
            _errors.clear();
        }
        Throwable cause = ExceptionUtils.getRootCause(exception);
        if (cause != null)
            addError(cause.getMessage());
        else
            addError(exception.getMessage());
    }

    /**
     * Invoked when an update fails. Marks this as invalid.
     *
     * @param exception the reason for the failure
     */
    private void invalidate(ValidationException exception) {
        _errors = exception.getErrors();
        if (_errors.isEmpty()) {
            addError(exception.getMessage());
        }
    }

    /**
     * Adds a validation error.
     *
     * @param message the error message
     */
    private void addError(String message) {
        ValidationError error = new ValidationError(
                _descriptor.getDisplayName(), message);
        if (_errors == null) {
            _errors = new ArrayList<ValidationError>();
        }
        _errors.add(error);
    }

    /**
     * Adds a validation error.
     *
     * @param message the key of the message
     * @param args    an array of arguments to be inserted into the message
     */
    private void addError(String message, Object ... args) {
        message = Messages.get(message, args);
        addError(message);
    }

    /**
     * Verifies that the property can be modified.
     *
     * @throws UnsupportedOperationException if the property is read-only or
     *                                       derived
     */
    private void checkReadOnly() {
        if (_descriptor.isDerived()) {
            throw new UnsupportedOperationException(
                    "Attenpt to modify read-only property: "
                            + getDescriptor().getDisplayName());
        }
    }

}
