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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.NodeResolver;
import org.openvpms.component.business.service.archetype.helper.PropertyResolver;
import org.openvpms.component.business.service.archetype.helper.PropertyResolverException;
import org.openvpms.component.business.service.archetype.helper.PropertySetResolver;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.util.AbstractPropertySet;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.component.system.common.util.PropertySetException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.openvpms.component.system.common.util.PropertySetException.ErrorCode.PropertyNotFound;


/**
 * An implementation of {@link Variables} that supports simple variable names, and variable names of the form
 * <tt>variable.node1.node2.nodeN</tt>.
 * <p/>
 * The latter form is used to resolve nodes in {@code IMObject} variables.
 *
 * @author Tim Anderson
 * @see PropertyResolver
 */
public class IMObjectVariables implements Variables {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookupService;

    /**
     * The variables.
     */
    private final Properties variables = new Properties();

    /**
     * The property resolver. This is lazily constructed
     */
    private PropertyResolver resolver;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMObjectVariables.class);


    /**
     * Constructs an {@code IMObjectVariables}.
     *
     * @param service the archetype service
     */
    public IMObjectVariables(IArchetypeService service, ILookupService lookupService) {
        this.service = service;
        this.lookupService = lookupService;
    }

    /**
     * Adds a variable.
     *
     * @param name  the variable name
     * @param value the variable value
     */
    public void add(String name, Object value) {
        variables.set(name, value);
    }

    /**
     * Returns a variable value.
     *
     * @param name the variable name
     * @return the variable value
     */
    public Object get(String name) {
        Object result = null;
        try {
            result = getResolver().getObject(name);
        } catch (PropertyResolverException exception) {
            log.debug("Variable not found: " + name, exception);
        }
        return result;
    }

    /**
     * Determines if a variable exists.
     *
     * @param name the variable name
     * @return {@code true} if the variable exists
     */
    public boolean exists(String name) {
        boolean result = false;
        try {
            getResolver().getObject(name);
            result = true;
        } catch (PropertyResolverException exception) {
            log.debug("Variable not found: " + name, exception);
        }
        return result;
    }

    /**
     * Creates the property resolver.
     *
     * @param variables the variables
     * @param service   the archetype service
     * @return a new property resolver
     */
    protected PropertyResolver createResolver(PropertySet variables, IArchetypeService service) {
        resolver = new PropertySetResolver(variables, service) {
            @Override
            protected Object resolve(IMObject object, String name) {
                return IMObjectVariables.this.resolve(object, name);
            }
        };
        return resolver;
    }

    /**
     * Resolves the value corresponding to a composite node name.
     * <p/>
     * This returns the name of a lookup, rather than its code.
     *
     * @param object the root object
     * @param name   the composite node name
     * @return the resolved object
     * @throws PropertyResolverException if the name is invalid
     */
    protected Object resolve(IMObject object, String name) {
        NodeResolver resolver = new NodeResolver(object, service);
        NodeResolver.State state = resolver.resolve(name);
        NodeDescriptor descriptor = state.getLeafNode();
        Object value;
        if (descriptor != null && descriptor.isLookup()) {
            value = LookupHelper.getName(service, lookupService, descriptor, state.getParent());
        } else {
            value = state.getValue();
        }
        return value;
    }

    /**
     * Returns the property resolver, creating it if required.
     *
     * @return the property resolver
     */
    protected PropertyResolver getResolver() {
        if (resolver == null) {
            resolver = createResolver(variables, service);
        }
        return resolver;
    }

    /**
     * An implementation of {@link PropertySet} using a map.
     */
    private static class Properties extends AbstractPropertySet {

        /**
         * The properties.
         */
        private Map<String, Object> properties = new HashMap<String, Object>();

        /**
         * Returns the property names.
         *
         * @return the property names
         */
        public Set<String> getNames() {
            return properties.keySet();
        }

        /**
         * Returns the value of a property.
         *
         * @param name the property name
         * @return the value of the property
         * @throws OpenVPMSException if the property doesn't exist
         */
        public Object get(String name) {
            if (properties.containsKey(name)) {
                return properties.get(name);
            }
            throw new PropertySetException(PropertyNotFound, name);
        }

        /**
         * Sets the value of a property.
         *
         * @param name  the property name
         * @param value the property value
         */
        public void set(String name, Object value) {
            properties.put(name, value);
        }
    }

}
