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

package org.openvpms.component.system.common.util;

import org.apache.commons.jxpath.util.TypeConverter;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.system.common.jxpath.OpenVPMSTypeConverter;

import java.math.BigDecimal;
import java.util.Date;

import static org.openvpms.component.system.common.util.PropertySetException.ErrorCode.ConversionFailed;


/**
 * Abstract implementation of the {@link PropertySet} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPropertySet implements PropertySet {

    /**
     * Used to convert property values to a particular type.
     */
    private static final TypeConverter CONVERTER = new OpenVPMSTypeConverter();


    /**
     * Determines if a property exists.
     *
     * @param name the property name
     * @return {@code true} if the property exists
     */
    public boolean exists(String name) {
        return getNames().contains(name);
    }

    /**
     * Returns the boolean value of a property.
     *
     * @param name the property name
     * @return the value of the property, or {@code false} if the property is {@code null}
     * @throws OpenVPMSException if the property doesn't exist or conversion fails
     */
    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    /**
     * Returns the boolean value of a property.
     *
     * @param name         the property name
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if the property doesn't exist or conversion fails
     */
    public boolean getBoolean(String name, boolean defaultValue) {
        return (Boolean) get(name, defaultValue, boolean.class);
    }

    /**
     * Returns the integer value of a property.
     *
     * @param name the property name
     * @return the value of the property, or {@code 0} if the property is {@code null}
     * @throws OpenVPMSException if the property doesn't exist or conversion fails
     */
    public int getInt(String name) {
        return getInt(name, 0);
    }

    /**
     * Returns the integer value of a property.
     *
     * @param name         the property name
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it
     *         is {@code null}
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    public int getInt(String name, int defaultValue) {
        return (Integer) get(name, defaultValue, int.class);
    }

    /**
     * Returns the long value of a property.
     *
     * @param name the property name
     * @return the value of the property, or {@code 0} if the property is {@code null}
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    public long getLong(String name) {
        return getLong(name, 0);
    }

    /**
     * Returns the long value of a property.
     *
     * @param name         the property name
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if the property doesn't exist or conversion fails
     */
    public long getLong(String name, long defaultValue) {
        return (Long) get(name, defaultValue, long.class);
    }

    /**
     * Returns the string value of a property.
     *
     * @param name the property name
     * @return the value of the property
     * @throws OpenVPMSException if the property doesn't exist or conversion fails
     */
    public String getString(String name) {
        return getString(name, null);
    }

    /**
     * Returns the string value of a property.
     *
     * @param name         the property name
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if the property doesn't exist or conversion fails
     */
    public String getString(String name, String defaultValue) {
        return (String) get(name, defaultValue, String.class);
    }

    /**
     * Returns the {@code BigDecimal} value of a property.
     *
     * @param name the property name
     * @return the value of the property. May be {@code null}
     * @throws OpenVPMSException if the property doesn't exist or conversion fails
     */
    public BigDecimal getBigDecimal(String name) {
        return getBigDecimal(name, null);
    }

    /**
     * Returns the {@code BigDecimal} value of a property.
     *
     * @param name         the property name
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if the property doesn't exist or conversion fails
     */
    public BigDecimal getBigDecimal(String name, BigDecimal defaultValue) {
        return (BigDecimal) get(name, defaultValue, BigDecimal.class);
    }

    /**
     * Returns the {@code Money} value of a property.
     *
     * @param name the property name
     * @return the value of the property. May be {@code null}
     * @throws OpenVPMSException if the property doesn't exist or conversion fails
     */
    public Money getMoney(String name) {
        return getMoney(name, null);
    }

    /**
     * Returns the {@code BigDecimal} value of a property.
     *
     * @param name         the property name
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if the property doesn't exist or conversion fails
     */
    public Money getMoney(String name, Money defaultValue) {
        return (Money) get(name, defaultValue, Money.class);
    }

    /**
     * Returns the {@code Date} value of a property.
     *
     * @param name the property name
     * @return the value of the property
     * @throws OpenVPMSException if the property doesn't exist or conversion fails
     */
    public Date getDate(String name) {
        return getDate(name, null);
    }

    /**
     * Returns the {@code Date} value of a property.
     *`
     * @param name         the property name
     * @param defaultValue the value to return if the property value is {@code null}
     * @return the value of the property, or {@code defaultValue} if it is {@code null}
     * @throws OpenVPMSException if the property doesn't exist or conversion fails
     */
    public Date getDate(String name, Date defaultValue) {
        return (Date) get(name, defaultValue, Date.class);
    }

    /**
     * Returns the reference value of a property.
     *
     * @param name the property name
     * @return the property value
     * @throws OpenVPMSException if the property doesn't exist or conversion fails
     */
    public IMObjectReference getReference(String name) {
        return (IMObjectReference) get(name);
    }

    /**
     * Resolves the named property.
     *
     * @param name the property name
     * @return the property state
     */
    @Override
    public PropertyState resolve(String name) {
        Object value = get(name);
        return new PropertyState(name, value);
    }

    /**
     * Converts a value to a particular type.
     *
     * @param name         the node name
     * @param defaultValue the value to return if the node value is {@code null}
     * @param type         the type to convert to if {@code defaultValue} is {@code null}
     * @return the value of the node as an instance of {@code type}
     * @throws OpenVPMSException if the property doesn't exist or conversion fails
     */
    protected Object get(String name, Object defaultValue, Class type) {
        Object value = get(name);
        Object result;
        if (value != null) {
            result = convert(name, value, type);
        } else {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Converts a value to the specified type.
     *
     * @param name  the value name, for error reporting
     * @param value the value to convert
     * @param type  the type to convert to
     * @return the converted value
     * @throws PropertySetException if the value cannot be converted
     */
    protected Object convert(String name, Object value, Class type) {
        Object result;
        try {
            result = CONVERTER.convert(value, type);
        } catch (Throwable exception) {
            throw new PropertySetException(ConversionFailed, exception, name, value, type);
        }
        return result;
    }
}