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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.property;


import org.apache.commons.jxpath.util.TypeConverter;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.system.common.jxpath.OpenVPMSTypeConverter;
import org.openvpms.component.system.common.util.PropertySetException;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import static org.openvpms.component.system.common.util.PropertySetException.ErrorCode.ConversionFailed;

/**
 * Abstract implementation of the {@link Property} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractProperty extends AbstractModifiable implements Property {

    /**
     * Determines if the underlying object is dirty.
     */
    private boolean dirty;

    /**
     * The listeners.
     */
    private ModifiableListeners listeners;

    /**
     * The error listener.
     */
    private ErrorListener errorListener;

    /**
     * The property handler.
     */
    private PropertyTransformer transformer;

    /**
     * Used to convert property values to a particular type.
     */
    private static final TypeConverter CONVERTER = new OpenVPMSTypeConverter();


    /**
     * Determines if the underlying object has been modified.
     *
     * @return {@code true} if this has been modified; otherwise {@code false}
     */
    public boolean isModified() {
        return dirty;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        dirty = false;
    }

    /**
     * Returns the boolean value of the property.
     *
     * @return the value of the property, or {@code false} if the property is null
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public boolean getBoolean() {
        return getBoolean(false);
    }

    /**
     * Returns the boolean value of the property.
     *
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public boolean getBoolean(boolean defaultValue) {
        return (Boolean) get(defaultValue, boolean.class);
    }

    /**
     * Returns the integer value of the property.
     *
     * @return the value of the property, or {@code 0} if the property is null
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public int getInt() {
        return getInt(0);
    }

    /**
     * Returns the integer value of the property.
     *
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public int getInt(int defaultValue) {
        return (Integer) get(defaultValue, int.class);
    }

    /**
     * Returns the long value of the property.
     *
     * @return the value of the property, or {@code 0} if the property is null
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public long getLong() {
        return getLong(0);
    }

    /**
     * Returns the long value of the property.
     *
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public long getLong(long defaultValue) {
        return (Long) get(defaultValue, long.class);
    }

    /**
     * Returns the string value of the property.
     *
     * @return the value of the property. May be {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public String getString() {
        return getString(null);
    }

    /**
     * Returns the string value of the property.
     *
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public String getString(String defaultValue) {
        return (String) get(defaultValue, String.class);
    }

    /**
     * Returns the {@code BigDecimal} value of the property.
     *
     * @return the value of the property. May be {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public BigDecimal getBigDecimal() {
        return getBigDecimal(null);
    }

    /**
     * Returns the {@code BigDecimal} value of the property.
     *
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public BigDecimal getBigDecimal(BigDecimal defaultValue) {
        return (BigDecimal) get(defaultValue, BigDecimal.class);
    }

    /**
     * Returns the {@code Money} value of the property.
     *
     * @return the value of the property. May be {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public Money getMoney() {
        return getMoney(null);
    }

    /**
     * Returns the {@code BigDecimal} value of the property.
     *
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public Money getMoney(Money defaultValue) {
        return (Money) get(defaultValue, Money.class);
    }

    /**
     * Returns the {@code Date} value of the property.
     *
     * @return the value of the property. May be {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public Date getDate() {
        return getDate(null);
    }

    /**
     * Returns the {@code Date} value of the property.
     *
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public Date getDate(Date defaultValue) {
        return (Date) get(defaultValue, Date.class);
    }

    /**
     * Returns the reference value of the property.
     *
     * @return the property value. May be {@code null}
     * @throws OpenVPMSException if conversion fails
     */
    @Override
    public IMObjectReference getReference() {
        return (IMObjectReference) get(null, IMObjectReference.class);
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return false;
    }

    /**
     * Returns the property name.
     *
     * @return the property name
     */
    @Override
    public String getName() {
        return null;
    }

    /**
     * Returns the property display name.
     *
     * @return the display name
     */
    @Override
    public String getDisplayName() {
        return null;
    }

    /**
     * Returns the property description.
     *
     * @return the description. May be {@code null}
     */
    @Override
    public String getDescription() {
        return null;
    }

    /**
     * Sets the value of the property.
     * The value will only be set if it is valid, and different to the existing
     * value. If the value is set, any listeners will be notified.
     *
     * @param value the property value
     * @return {@code true} if the value was set, {@code false} if it
     * cannot be set due to error, or is the same as the existing value
     */
    @Override
    public boolean setValue(Object value) {
        return false;
    }

    /**
     * Returns the value of the property.
     *
     * @return the property value. May be {@code null}
     */
    @Override
    public Object getValue() {
        return null;
    }

    /**
     * Returns the minimum length of the property.
     *
     * @return the minimum length
     */
    @Override
    public int getMinLength() {
        return 0;
    }

    /**
     * Returns the maximum length of the property.
     *
     * @return the maximum length, or {@code -1} if it is unbounded
     */
    @Override
    public int getMaxLength() {
        return 0;
    }

    /**
     * Returns the property type.
     *
     * @return the property type
     */
    @Override
    public Class getType() {
        return null;
    }

    /**
     * Determines if the property is a boolean.
     *
     * @return {@code true} if it is a boolean
     */
    @Override
    public boolean isBoolean() {
        return false;
    }

    /**
     * Determines if the property is a string.
     *
     * @return {@code true} if it is a string
     */
    @Override
    public boolean isString() {
        return false;
    }

    /**
     * Determines if the property is numeric.
     *
     * @return {@code true} if it is numeric
     */
    @Override
    public boolean isNumeric() {
        return false;
    }

    /**
     * Determines if the property is a date.
     *
     * @return {@code true} if it is a date
     */
    @Override
    public boolean isDate() {
        return false;
    }

    /**
     * Determines if the property is a money property.
     *
     * @return {@code true} it is a money property
     */
    @Override
    public boolean isMoney() {
        return false;
    }

    /**
     * Determines if the property is an object reference.
     *
     * @return {@code true} if it is an object reference
     */
    @Override
    public boolean isObjectReference() {
        return false;
    }

    /**
     * Determines if the property is a lookup.
     *
     * @return {@code true} if it is a lookup
     */
    @Override
    public boolean isLookup() {
        return false;
    }

    /**
     * Determines if the property is a password.
     *
     * @return {@code true} if it is a password
     */
    @Override
    public boolean isPassword() {
        return false;
    }

    /**
     * Determines if the property is a collection.
     *
     * @return {@code true} if it is a collection
     */
    @Override
    public boolean isCollection() {
        return false;
    }

    /**
     * Returns the archetype short names that this property may support.
     * <p/>
     * Wildcards are expanded.
     *
     * @return the archetype short names
     * @throws ArchetypeServiceException for any error
     */
    @Override
    public String[] getArchetypeRange() {
        return new String[0];
    }

    /**
     * Determines if the property value is derived from an expression.
     *
     * @return {@code true} if the value is derived, otherwise {@code false}
     */
    @Override
    public boolean isDerived() {
        return false;
    }

    /**
     * Determines if the property is read-only.
     *
     * @return {@code true} if the property is read-only
     */
    @Override
    public boolean isReadOnly() {
        return false;
    }

    /**
     * Determines if the property is hidden.
     *
     * @return {@code true} if the property is hidden; otherwise {@code false}
     */
    @Override
    public boolean isHidden() {
        return false;
    }

    /**
     * Determines if the property is required.
     *
     * @return {@code true} if the property is required; otherwise
     * {@code false}
     */
    @Override
    public boolean isRequired() {
        return false;
    }

    /**
     * Determines if the property is empty.
     *
     * @return {@code true} if the value is {@code null}, an empty string or collection; otherwise {@code false}
     */
    @Override
    public boolean isEmpty() {
        boolean result;
        Object value = getValue();
        if (value instanceof String) {
            result = ((String) value).length() == 0;
        } else if (value instanceof Collection) {
            result = ((Collection) value).isEmpty();
        } else {
            result = value == null;
        }
        return result;
    }

    /**
     * Returns the property descriptor.
     *
     * @return the property descriptor, or {@code null} if the property has no descriptor
     */
    @Override
    public NodeDescriptor getDescriptor() {
        return null;
    }

    /**
     * Sets the property transformer.
     *
     * @param transformer the property transformer. May be {@code null}
     */
    public void setTransformer(PropertyTransformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Returns the property transformer.
     * If none has been set, creates one using
     * {@link PropertyTransformerFactory#create}.
     *
     * @return the property transformer
     */
    public PropertyTransformer getTransformer() {
        if (transformer == null) {
            transformer = PropertyTransformerFactory.create(this);
        }
        return transformer;
    }

    /**
     * Notify any listeners that they need to refresh and marks this modified.
     */
    public void refresh() {
        dirty = true;
        resetValid();
        if (listeners != null) {
            listeners.notifyListeners(this);
        }
    }

    /**
     * Add a listener to be notified when a this changes.
     *
     * @param listener the listener to add
     */
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
    public void addModifiableListener(ModifiableListener listener, int index) {
        if (listeners == null) {
            listeners = new ModifiableListeners();
        }
        listeners.addListener(listener, index);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        if (listeners != null) {
            listeners.removeListener(listener);
        }
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
     * Returns a hash code value for the object, based on the property name.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Property) {
            Property p = (Property) obj;
            return getName().equals(p.getName());
        }
        return false;
    }

    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p/>
     * The <code>toString</code> method for class <code>Object</code>
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `<code>@</code>', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Verifies that the property can be modified.
     *
     * @throws UnsupportedOperationException if the property is derived
     */
    protected void checkModifiable() {
        if (isDerived()) {
            throw new UnsupportedOperationException("Attempt to modify derived property: " + getDisplayName());
        }
    }

    /**
     * Converts a value to a particular type.
     *
     * @param defaultValue the value to return if the node value is null
     * @param type         the type to convert to if {@code defaultValue} is null
     * @return the value of the node as an instance of {@code type}
     * @throws OpenVPMSException if conversion fails
     */
    protected Object get(Object defaultValue, Class type) {
        Object value = getValue();
        Object result;
        if (value != null) {
            try {
                result = CONVERTER.convert(value, type);
            } catch (Throwable exception) {
                throw new PropertySetException(ConversionFailed, exception, getName(), value, type);
            }
        } else {
            result = defaultValue;
        }
        return result;
    }

}
