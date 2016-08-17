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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.service.archetype.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.util.MapPropertySet;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.component.system.common.util.PropertyState;
import org.openvpms.component.system.common.util.Variables;

/**
 * An implementation of {@link Variables} that supports simple variable names,
 * and variable names of the form <tt>variable.node1.node2.nodeN</tt>.
 * <p/>
 * The latter form is used to resolve nodes in {@code IMObject} variables.
 * <p/>
 * This may also be used to declare variables for an {@code JXPathContext}.
 *
 * @author Tim Anderson
 * @see PropertyResolver
 */
public class IMObjectVariables implements Variables, org.apache.commons.jxpath.Variables {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The variables.
     */
    private final PropertySet variables = new MapPropertySet();

    /**
     * The property resolver. This is lazily constructed.
     */
    private PropertyResolver resolver;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMObjectVariables.class);


    /**
     * Constructs an {@link IMObjectVariables}.
     *
     * @param service the archetype service
     * @param lookups the lookup service
     */
    public IMObjectVariables(IArchetypeService service, ILookupService lookups) {
        this.service = service;
        this.lookups = lookups;
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
     * Defines a new variable with the specified value or modifies
     * the value of an existing variable.
     *
     * @param varName variable name
     * @param value   to declare
     */
    @Override
    public void declareVariable(String varName, Object value) {
        add(varName, value);
    }

    /**
     * Removes an existing variable.
     *
     * @param varName is a variable name without the "$" sign
     * @throws UnsupportedOperationException
     */
    @Override
    public void undeclareVariable(String varName) {
        throw new UnsupportedOperationException("undeclareVariable() is not supported by IMObjectVariables");
    }

    /**
     * Returns the value of the specified variable.
     *
     * @param varName variable name
     * @return Object value
     * @throws IllegalArgumentException if there is no such variable.
     */
    @Override
    public Object getVariable(String varName) {
        try {
            return getResolver().getObject(varName);
        } catch (PropertyResolverException exception) {
            throw new IllegalArgumentException("Variable " + varName + " not found");
        }
    }

    /**
     * Returns true if the specified variable is declared.
     *
     * @param varName variable name
     * @return boolean
     */
    @Override
    public boolean isDeclaredVariable(String varName) {
        return exists(varName);
    }

    /**
     * Creates the property resolver.
     *
     * @param variables the variables
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @return a new property resolver
     */
    protected PropertyResolver createResolver(PropertySet variables, IArchetypeService service,
                                              ILookupService lookups) {
        resolver = new PropertySetResolver(variables, service, lookups) {
            @Override
            public Object getObject(String name) {
                return IMObjectVariables.this.getValue(resolve(name));
            }
        };
        return resolver;
    }

    /**
     * Returns the value of a property.
     * <p/>
     * This returns the name of a lookup, rather than its code.
     *
     * @param state the property state
     * @return the property value
     */
    protected Object getValue(PropertyState state) {
        NodeDescriptor descriptor = state.getNode();
        Object value;
        if (descriptor != null && descriptor.isLookup()) {
            value = LookupHelper.getName(service, lookups, descriptor, state.getParent());
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
            resolver = createResolver(variables, service, lookups);
        }
        return resolver;
    }

}