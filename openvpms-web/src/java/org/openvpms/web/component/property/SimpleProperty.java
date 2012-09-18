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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.property;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.util.TextHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Simple implementation of the {@link Property} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SimpleProperty extends AbstractProperty {

    /**
     * The property name.
     */
    private final String name;

    /**
     * The property type.
     */
    private final Class type;

    /**
     * The property display name.
     */
    private String displayName;

    /**
     * The property description. May be <tt>null</tt>
     */
    private String description;

    /**
     * The property value. May be <tt>null</tt>
     */
    private Object value;

    /**
     * The minimum length.
     */
    private int minLength;

    /**
     * The maximum length.
     */
    private int maxLength = 255;

    /**
     * The archetype short names that the property supports
     */
    private String[] shortNames = EMPTY;

    /**
     * Determines if the property is read-only.
     */
    private boolean readOnly;

    /**
     * Determines if the property is hidden.
     */
    private boolean hidden;

    /**
     * Determines if the property is required.
     */
    private boolean required;


    private List<ValidatorError> validationErrors;

    /**
     * Empty string array.
     */
    private static final String[] EMPTY = new String[0];


    /**
     * Creates a new <tt>SimpleProperty</tt>.
     *
     * @param name the property name
     * @param type the property type
     */
    public SimpleProperty(String name, Class type) {
        this(name, null, type);
    }

    /**
     * Creates a new <tt>SimpleProperty</tt>.
     *
     * @param name  the property name
     * @param value the property value. May be <tt>null</tt>
     * @param type  the property type
     */
    public SimpleProperty(String name, Object value, Class type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    /**
     * Returns the property name.
     *
     * @return the property name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the property display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        if (displayName == null) {
            displayName = TextHelper.unCamelCase(name);
        }
        return displayName;
    }

    /**
     * Sets the property display name.
     *
     * @param displayName the display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the property description.
     *
     * @return the description. May be <tt>null</tt>
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the property description.
     *
     * @param description the description. May be <tt>null</tt>
     */
    public void setDescription(String description) {
        this.description = description;
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
            if (!ObjectUtils.equals(this.value, value)) {
                value = getTransformer().apply(value);
                this.value = value;
                set = true;
                modified();
            } else if (validationErrors != null) {
                // a previous set triggered an error, and didn't update the value. If a new update occurs
                // but has the same value, need to clear any errors
                modified();
            }
        } catch (OpenVPMSException exception) {
            invalidate(exception);
        }
        return set;
    }

    /**
     * Returns the value of the property.
     *
     * @return the property value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the minimum length of the property.
     *
     * @return the minimum length
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum length of the property.
     *
     * @param length the minimum length
     */
    public void setMinLength(int length) {
        minLength = length;
    }

    /**
     * Returns the maximum length of the property.
     *
     * @return the maximum length, or {@code -1} if it is unbounded
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the maximum length of the property.
     *
     * @param length the maximum length. Use {@code -1} to indicate unbounded length
     */
    public void setMaxLength(int length) {
        maxLength = length;
    }

    /**
     * Returns the property type.
     *
     * @return the property type
     */
    public Class getType() {
        return type;
    }

    /**
     * Determines if the property is a boolean.
     *
     * @return <tt>true</tt> if it is a boolean
     */
    public boolean isBoolean() {
        return Boolean.class == type || boolean.class == type;
    }

    /**
     * Determines if the property is a string.
     *
     * @return <tt>true</tt> if it is a string
     */
    public boolean isString() {
        return String.class == type;
    }

    /**
     * Determines if the property is numeric.
     *
     * @return <tt>true</tt> if it is numeric
     */
    public boolean isNumeric() {
        return Number.class.isAssignableFrom(type)
               || byte.class == type
               || short.class == type
               || int.class == type
               || long.class == type
               || float.class == type
               || double.class == type;
    }

    /**
     * Determines if the property is a date.
     *
     * @return <tt>true</tt> if it is a date
     */
    public boolean isDate() {
        return Date.class == type;
    }

    /**
     * Determines if the property is a money property.
     *
     * @return <tt>true</tt> it is a money property
     */
    public boolean isMoney() {
        return Money.class == type;
    }

    /**
     * Determines if the property is an object reference.
     *
     * @return <tt>true</tt> if it is an object reference
     */
    public boolean isObjectReference() {
        return IMObjectReference.class.isAssignableFrom(type);
    }

    /**
     * Determines if the property is a lookup.
     *
     * @return <tt>true</tt> if it is a lookup
     */
    public boolean isLookup() {
        return false;
    }

    /**
     * Determines if the property is a collection.
     *
     * @return <tt>true</tt> if it is a collection
     */
    public boolean isCollection() {
        return false;
    }

    /**
     * Returns the archetype short names that this property may support.
     *
     * @return the archetype short names
     */
    public String[] getArchetypeRange() {
        return shortNames;
    }

    /**
     * Sets the archetype short names that this property may support.
     * <p/>
     * Wildcards are expanded.
     *
     * @param shortNames the archetype short names
     */
    public void setArchetypeRange(String[] shortNames) {
        this.shortNames = DescriptorHelper.getShortNames(shortNames);
    }

    /**
     * Determines if the property value is derived from an expression.
     *
     * @return <tt>true</tt> if the value is derived, otherwise <tt>false</tt>
     */
    public boolean isDerived() {
        return false;
    }

    /**
     * Determines if the property is read-only.
     *
     * @return <tt>true</tt> if the property is read-only
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets if the property is read-only.
     *
     * @param readOnly <tt>true</tt> if the property is read-only
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Determines if the property is hidden.
     *
     * @return <tt>true</tt> if the property is hidden; otherwise <tt>false</tt>
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Sets if the property is hidden.
     *
     * @param hidden <tt>true</tt> if the property is hidden
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Determines if the property is required.
     *
     * @return <tt>true</tt> if the property is required; otherwise
     *         <tt>false</tt>
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Determines if the property is required.
     *
     * @param required if <tt>true</tt> the property is required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Returns the property descriptor.
     *
     * @return <tt>null</tt>
     */
    public NodeDescriptor getDescriptor() {
        return null;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the object and its descendants are valid otherwise <tt>false</tt>
     */
    protected boolean doValidation(Validator validator) {
        List<ValidatorError> errors = null;
        if (validationErrors == null) {
            PropertyTransformer transformer = getTransformer();
            try {
                transformer.apply(getValue());
            } catch (OpenVPMSException exception) {
                invalidate(exception);
            }
        }
        if (validationErrors != null) {
            errors = validationErrors;
        } else if (isRequired() && getValue() == null) {
            validationErrors = new ArrayList<ValidatorError>();
            validationErrors.add(new ValidatorError(this, Messages.get("property.error.required", getDisplayName())));
            errors = validationErrors;
        }
        if (errors != null) {
            validator.add(this, errors);
        }
        return (errors == null);
    }

    private void invalidate(OpenVPMSException exception) {
        if (validationErrors != null) {
            validationErrors.clear();
        } else {
            validationErrors = new ArrayList<ValidatorError>();
        }
        Throwable cause = ExceptionUtils.getRootCause(exception);
        if (cause == null) {
            cause = exception;
        }
        validationErrors.add(new ValidatorError(this, cause.getMessage()));
        resetValid();
    }

    /**
     * Invoked when this is modified. Updates flags, and notifies the
     * listeners.
     */
    private void modified() {
        validationErrors = null;
        refresh();
    }

}
