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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.system.common.util;

import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;


/**
 * A <em>PropertySet</em> is a set of name-value pairs.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface PropertySet {

    /**
     * Returns the property names.
     *
     * @return the property names
     */
    Set<String> getNames();

    /**
     * Determines if a property exists.
     *
     * @param name the property name
     * @return <tt>true</tt> if the property exists
     */
    boolean exists(String name);

    /**
     * Returns the boolean value of a property.
     *
     * @param name the property name
     * @return the value of the property, or <tt>false</tt> if the property is
     *         null
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    boolean getBoolean(String name);

    /**
     * Returns the boolean value of a property.
     *
     * @param name         the property name
     * @param defaultValue the value to return if the property value is null
     * @return the value of the property, or <tt>defaultValue</tt> if it
     *         is null
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    boolean getBoolean(String name, boolean defaultValue);

    /**
     * Returns the integer value of a property.
     *
     * @param name the property name
     * @return the value of the property, or <tt>0</tt> if the property is null
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    int getInt(String name);

    /**
     * Returns the integer value of a property.
     *
     * @param name         the property name
     * @param defaultValue the value to return if the property value is null
     * @return the value of the property, or <tt>defaultValue</tt> if it
     *         is null
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    int getInt(String name, int defaultValue);

    /**
     * Returns the long value of a property.
     *
     * @param name the property name
     * @return the value of the property, or <tt>0</tt> if the property is null
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    long getLong(String name);

    /**
     * Returns the long value of a property.
     *
     * @param name         the property name
     * @param defaultValue the value to return if the property value is null
     * @return the value of the property, or <tt>defaultValue</tt> if it
     *         is null
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    long getLong(String name, long defaultValue);

    /**
     * Returns the string value of a property.
     *
     * @param name the property name
     * @return the value of the property.
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    String getString(String name);

    /**
     * Returns the string value of a property.
     *
     * @param name         the property name
     * @param defaultValue the value to return if the property value is null
     * @return the value of the property, or <tt>defaultValue</tt> if it
     *         is null
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    String getString(String name, String defaultValue);

    /**
     * Returns the <tt>BigDecimal</tt> value of a property.
     *
     * @param name the property name
     * @return the value of the property. May be <tt>null</tt>
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    BigDecimal getBigDecimal(String name);

    /**
     * Returns the <tt>BigDecimal</tt> value of a property.
     *
     * @param name         the property name
     * @param defaultValue the value to return if the property value is null
     * @return the value of the property, or <tt>defaultValue</tt> if it
     *         is null
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    BigDecimal getBigDecimal(String name, BigDecimal defaultValue);

    /**
     * Returns the <tt>Money</tt> value of a property.
     *
     * @param name the property name
     * @return the value of the property. May be <tt>null</tt>
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    Money getMoney(String name);

    /**
     * Returns the <tt>BigDecimal</tt> value of a property.
     *
     * @param name         the property name
     * @param defaultValue the value to return if the property value is null
     * @return the value of the property, or <tt>defaultValue</tt> if it
     *         is null
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    Money getMoney(String name, Money defaultValue);

    /**
     * Returns the <tt>Date</tt> value of a property.
     *
     * @param name the property name
     * @return the value of the property
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    Date getDate(String name);

    /**
     * Returns the <tt>Date</tt> value of a property.
     *
     * @param name         the property name
     * @param defaultValue the value to return if the property value is null
     * @return the value of the property, or <tt>defaultValue</tt> if it
     *         is null
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    Date getDate(String name, Date defaultValue);

    /**
     * Returns the reference value of a property.
     *
     * @param name the property name
     * @return the property value
     * @throws OpenVPMSException if the property doesn't exist or conversion
     *                           fails
     */
    IMObjectReference getReference(String name);

    /**
     * Returns the value of a property.
     *
     * @param name the property name
     * @return the value of the property
     * @throws OpenVPMSException if the property doesn't exist
     */
    Object get(String name);

    /**
     * Sets the value of a property.
     *
     * @param name the propery name
     * @param value the property value
     * @throws OpenVPMSException if the property cannot be set
     */
    void set(String name, Object value);

}
