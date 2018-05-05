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

import org.openvpms.component.exception.OpenVPMSException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.openvpms.component.system.common.util.PropertySetException.ErrorCode.PropertyNotFound;

/**
 * A {@link PropertySet} backed by a map.
 *
 * @author Tim Anderson
 */
public class MapPropertySet extends AbstractPropertySet {

    /**
     * The properties.
     */
    private final Map<String, Object> properties;


    /**
     * Constructs a {@link MapPropertySet}.
     */
    public MapPropertySet() {
        properties = new HashMap<String, Object>();
    }

    /**
     * Constructs a {@link MapPropertySet}.
     * <p/>
     * This performs a shallow copy of the properties.
     *
     * @param properties the properties
     */
    public MapPropertySet(Map<String, Object> properties) {
        this(properties, true);
    }

    /**
     * Constructs a {@link MapPropertySet}.
     *
     * @param properties the properties
     * @param copy       if {@code true}, performs a shallow copy of the properties, otherwise updates them directly
     */
    public MapPropertySet(Map<String, Object> properties, boolean copy) {
        this.properties = (copy) ? new HashMap<String, Object>(properties) : properties;
    }

    /**
     * Returns the property names.
     *
     * @return the property names
     */
    @Override
    public Set<String> getNames() {
        return properties.keySet();
    }

    /**
     * Sets the value of a property.
     *
     * @param name  the property name
     * @param value the property value
     */
    @Override
    public void set(String name, Object value) {
        properties.put(name, value);
    }

    /**
     * Returns the value of a property.
     *
     * @param name the property name
     * @return the value of the property
     * @throws OpenVPMSException if the property doesn't exist
     */
    @Override
    public Object get(String name) {
        if (properties.containsKey(name)) {
            return properties.get(name);
        }
        throw new PropertySetException(PropertyNotFound, name);
    }

    /**
     * Returns the properties.
     *
     * @return the properties
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

}
