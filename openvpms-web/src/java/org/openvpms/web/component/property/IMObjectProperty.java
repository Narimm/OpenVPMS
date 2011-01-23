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
 *  $Id:IMObjectProperty.java 2147 2007-06-21 04:16:11Z tanderson $
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
 *  $Id:IMObjectProperty.java 2147 2007-06-21 04:16:11Z tanderson $
 */

package org.openvpms.web.component.property;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationError;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.util.ObjectHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Represents a property of an {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate:2007-06-21 04:16:11Z $
 */
public class IMObjectProperty extends AbstractProperty
        implements CollectionProperty {

    /**
     * The object that the property belongs to.
     */
    private final IMObject object;

    /**
     * The property descriptor.
     */
    private final NodeDescriptor descriptor;

    /**
     * Current validation errors.
     */
    private List<ValidatorError> validationErrors;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMObjectProperty.class);


    /**
     * Construct a new <tt>IMObjectProperty</tt>.
     *
     * @param object     the object that the property belongs to
     * @param descriptor the property descriptor
     */
    public IMObjectProperty(IMObject object, NodeDescriptor descriptor) {
        this.object = object;
        this.descriptor = descriptor;
    }

    /**
     * Returns the parent object.
     *
     * @return the parent object
     */
    public IMObject getParent() {
        return object;
    }

    /**
     * Returns the property descriptor.
     *
     * @return the property descriptor
     */
    public NodeDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Returns the value of the property.
     *
     * @return the property value
     */
    public Object getValue() {
        return descriptor.getValue(object);
    }

    /**
     * Sets the value of the property.
     * The value will only be set if it is valid, and different to the existing
     * value. If the value is set, any listeners will be notified.
     *
     * @param value the property value
     * @return <tt>true</tt> if the value was set, <tt>false</tt> if it
     *         cannot be set due to error, or is the same as the existing value
     */
    public boolean setValue(Object value) {
        boolean set = false;
        checkModifiable();
        try {
            value = getTransformer().apply(value);
            if (!ObjectHelper.equals(getValue(), value)) {
                descriptor.setValue(object, value);
                set = true;
                modified();
            } else if (validationErrors != null) {
                // a previous set triggered an error, and didn't update the value. If a new update occurs
                // but has the same value, need to clear any errors
                modified();
            }
        } catch (DescriptorException exception) {
            invalidate(exception);
        } catch (ValidationException exception) {
            invalidate(exception);
        } catch (PropertyException exception) {
            invalidate(exception);
        }
        return set;
    }

    /**
     * Returns the property name.
     *
     * @return the property name
     */
    public String getName() {
        return descriptor.getName();
    }

    /**
     * Returns the property display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return descriptor.getDisplayName();
    }

    /**
     * Returns the property description.
     *
     * @return the description. May be <tt>null</tt>
     */
    public String getDescription() {
        return null;
    }

    /**
     * Returns the minimum length of the property.
     *
     * @return the minimum length
     */
    public int getMinLength() {
        return descriptor.getMinLength();
    }

    /**
     * Returns the maximum length of the property.
     *
     * @return the maximum length
     */
    public int getMaxLength() {
        return descriptor.getMaxLength();
    }

    /**
     * Returns the property type.
     *
     * @return the property type
     */
    public Class getType() {
        return descriptor.getClazz();
    }

    /**
     * Determines if the property is a boolean.
     *
     * @return <tt>true</tt> if it is a boolean
     */
    public boolean isBoolean() {
        return descriptor.isBoolean();
    }

    /**
     * Determines if the property is a string.
     *
     * @return <tt>true</tt> if it is a string
     */
    public boolean isString() {
        return descriptor.isString();
    }

    /**
     * Determines if the property is numeric.
     *
     * @return <tt>true</tt> if it is numeric
     */
    public boolean isNumeric() {
        return descriptor.isNumeric();
    }

    /**
     * Determines if the property is a date.
     *
     * @return <tt>true</tt> if it is a date
     */
    public boolean isDate() {
        return descriptor.isDate();
    }

    /**
     * Determines if the property is a money property.
     *
     * @return <tt>true</tt> it is a money property
     */
    public boolean isMoney() {
        return descriptor.isMoney();
    }

    /**
     * Determines if the property is an object reference.
     *
     * @return <tt>true</tt> if it is an object reference
     */
    public boolean isObjectReference() {
        return descriptor.isObjectReference();
    }

    /**
     * Determines if the property is a lookup.
     *
     * @return <tt>true</tt> if it is a lookup
     */
    public boolean isLookup() {
        return descriptor.isLookup();
    }

    /**
     * Determines if the property is a collection.
     *
     * @return <tt>true</tt> if it is a collection
     */
    public boolean isCollection() {
        return descriptor.isCollection();
    }

    /**
     * Returns the archetype short names that this collection may support.
     *
     * @return the archetype short names
     * @throws ArchetypeServiceException for any error
     */
    public String[] getArchetypeRange() {
        return DescriptorHelper.getShortNames(descriptor);
    }

    /**
     * Determines if the property value is derived from an expression.
     *
     * @return <tt>true</tt> if the value is derived, otherwise <tt>false</tt>
     */
    public boolean isDerived() {
        return descriptor.isDerived();
    }

    /**
     * Determines if the property is read-only.
     *
     * @return <tt>true</tt> if the property is read-only
     */
    public boolean isReadOnly() {
        return descriptor.isReadOnly();
    }

    /**
     * Determines if the property is hidden.
     *
     * @return <tt>true</tt> if the property is hidden; otherwise <tt>false</tt>
     */
    public boolean isHidden() {
        return descriptor.isHidden();
    }

    /**
     * Determines if the property is required.
     *
     * @return <tt>true</tt> if the property is required; otherwise
     *         <tt>false</tt>
     */
    public boolean isRequired() {
        return descriptor.isRequired();
    }

    /**
     * Returns the collection.
     *
     * @return the collection
     */
    public Collection getValues() {
        List<IMObject> values = null;
        try {
            values = descriptor.getChildren(object);
            if (values != null) {
                values = Collections.unmodifiableList(values);
            }
        } catch (OpenVPMSException exception) {
            log.error(exception, exception);
        }
        return values;
    }

    /**
     * Returns the no. of elements in the collection
     *
     * @return the no. of elements in the collection
     */
    public int size() {
        List<IMObject> values = descriptor.getChildren(object);
        return values != null ? values.size() : 0;
    }

    /**
     * Add a value.
     *
     * @param value the value to add
     */
    public void add(Object value) {
        checkModifiable();
        try {
            value = getTransformer().apply(value);
            descriptor.addChildToCollection(object, value);
            modified();
        } catch (ValidationException exception) {
            invalidate(exception);
        } catch (DescriptorException exception) {
            invalidate(exception);
        } catch (PropertyException exception) {
            invalidate(exception);
        }
    }

    /**
     * Remove a value.
     *
     * @param value the value to remove
     */
    public void remove(Object value) {
        checkModifiable();
        try {
            int size = size();
            value = getTransformer().apply(value);
            descriptor.removeChildFromCollection(object, value);
            if (size != size()) {
                modified();
            }
        } catch (ValidationException exception) {
            invalidate(exception);
        } catch (DescriptorException exception) {
            invalidate(exception);
        } catch (PropertyException exception) {
            invalidate(exception);
        }
    }

    /**
     * Returns the minimum cardinality.
     *
     * @return the minimum cardinality
     */
    public int getMinCardinality() {
        return descriptor.getMinCardinality();
    }

    /**
     * Returns the maximum cardinality.
     *
     * @return the maximum cardinality, or <tt>-1</tt> if it is unbounded
     */
    public int getMaxCardinality() {
        if (descriptor.getMaxCardinality() == NodeDescriptor.UNBOUNDED) {
            return -1;
        }
        return descriptor.getMaxCardinality();
    }

    /**
     * Determines the relationship of the elements of the collection to the
     * object.
     *
     * @return <tt>true</tt> if the objects are children of the parent object,
     *         or <tt>false</tt> if they are its peer
     */
    public boolean isParentChild() {
        return descriptor.isParentChild();
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendents are valid
     *         otherwise <tt>false</tt>
     */
    public boolean validate(Validator validator) {
        List<ValidatorError> errors = null;
        if (validationErrors == null) {
            // determine if this is valid
            int minSize = getMinCardinality();
            if (minSize == 1 && getValue() == null) {
                addError("property.error.required",
                         descriptor.getDisplayName());
            } else if (descriptor.isCollection()) {
                Collection values = getValues();
                int size = values.size();
                int maxSize = getMaxCardinality();
                if (minSize != -1 && size < minSize) {
                    addError("property.error.minSize",
                             descriptor.getDisplayName(), minSize);
                } else if (maxSize != -1 && size > maxSize) {
                    addError("property.error.maxSize",
                             descriptor.getDisplayName(), maxSize);
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
                } catch (PropertyException exception) {
                    invalidate(exception);
                }
            }
        }
        if (validationErrors != null) {
            errors = validationErrors;
        }
        if (errors != null) {
            validator.add(this, errors);
        }
        return (errors == null);
    }

    /**
     * Returns the object that the property belongs to.
     *
     * @return the object
     */
    public IMObject getObject() {
        return object;
    }

    /**
     * Invoked when this is modified. Updates flags, and notifies the
     * listeners.
     */
    private void modified() {
        validationErrors = null;
        refresh();
    }

    /**
     * Invoked when an update fails. Marks this as invalid.
     *
     * @param exception the reason for the failure
     */
    private void invalidate(DescriptorException exception) {
        resetErrors();
        Throwable cause = ExceptionUtils.getRootCause(exception);
        if (cause != null) {
            addError(cause.getMessage());
        } else {
            addError(exception.getMessage());
        }
    }

    /**
     * Invoked when an update fails. Marks this as invalid.
     *
     * @param exception the reason for the failure
     */
    private void invalidate(ValidationException exception) {
        resetErrors();
        if (!exception.getErrors().isEmpty()) {
            for (ValidationError error : exception.getErrors()) {
                addError(new ValidatorError(error));
            }
        } else {
            addError(exception.getMessage());
        }
    }

    /**
     * Invoked when an update fails. Marks this as invalid.
     *
     * @param exception the reason for the failure
     */
    private void invalidate(PropertyException exception) {
        resetErrors();
        Throwable cause = ExceptionUtils.getRootCause(exception);
        if (cause != null) {
            addError(cause.getMessage());
        } else {
            addError(exception.getMessage());
        }
    }

    /**
     * Removes any validation errors.
     */
    private void resetErrors() {
        if (validationErrors != null && !validationErrors.isEmpty()) {
            validationErrors.clear();
        }
    }

    /**
     * Adds a validation error.
     *
     * @param message the error message
     */
    private void addError(String message) {
        addError(new ValidatorError(object.getArchetypeId().getShortName(),
                                    descriptor.getName(), message));
    }

    /**
     * Adds a validation error.
     *
     * @param error the validation error
     */
    private void addError(ValidatorError error) {
        if (validationErrors == null) {
            validationErrors = new ArrayList<ValidatorError>();
        }
        validationErrors.add(error);
    }

    /**
     * Adds a validation error.
     *
     * @param message the key of the message
     * @param args    an array of arguments to be inserted into the message
     */
    private void addError(String message, Object... args) {
        message = Messages.get(message, args);
        addError(message);
    }

}
