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

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.system.common.util.AbstractPropertySet;
import org.openvpms.component.system.common.util.MapPropertySet;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.component.system.common.util.PropertyState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * An {@link PropertySet} that supports composite property names using an {@link PropertyResolver}.
 *
 * @author Tim Anderson
 */
public class ResolvingPropertySet extends AbstractPropertySet {

    /**
     * The underlying properties.
     */
    private PropertySet properties;

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The property resolver.
     */
    private PropertyResolver resolver;

    /**
     * Constructs a {@link ResolvingPropertySet}.
     *
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public ResolvingPropertySet(IArchetypeService service, ILookupService lookups) {
        this(new HashMap<String, Object>(), service, lookups);
    }

    /**
     * Constructs a {@link ResolvingPropertySet}.
     *
     * @param properties the underlying properties
     * @param service    the archetype service
     * @param lookups    the lookup service
     */
    public ResolvingPropertySet(Map<String, Object> properties, IArchetypeService service, ILookupService lookups) {
        this.properties = new MapPropertySet(properties);
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Determines if a property exists.
     *
     * @param name the property name
     * @return <tt>true</tt> if the property exists
     */
    @Override
    public boolean exists(String name) {
        boolean exists = properties.exists(name);
        if (!exists) {
            try {
                getResolver().resolve(name);
                exists = true;
            } catch (PropertyResolverException ignore) {
                // do nothing
            }
        }
        return exists;
    }

    /**
     * Returns the field names.
     *
     * @return the field names
     */
    public Set<String> getNames() {
        return properties.getNames();
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
        return resolve(name).getValue();
    }

    /**
     * Sets the value of a property.
     *
     * @param name  the property name
     * @param value the property value
     * @throws OpenVPMSException if the property cannot be set
     */
    @Override
    public void set(String name, Object value) {
        properties.set(name, value);
    }

    /**
     * Resolves the named property.
     *
     * @param name the property name
     * @return the property state
     */
    @Override
    public PropertyState resolve(String name) {
        return getResolver().resolve(name);
    }

    /**
     * Returns the resolver.
     *
     * @return the resolver
     */
    protected PropertyResolver getResolver() {
        if (resolver == null) {
            resolver = createResolver(properties, service);
        }
        return resolver;
    }

    /**
     * Creates a new {@link PropertyResolver}.
     *
     * @param properties the properties
     * @param service    the archetype service
     * @return a new resolver
     */
    protected PropertyResolver createResolver(PropertySet properties, IArchetypeService service) {
        return new PropertySetResolver(properties, service, lookups);
    }
}
