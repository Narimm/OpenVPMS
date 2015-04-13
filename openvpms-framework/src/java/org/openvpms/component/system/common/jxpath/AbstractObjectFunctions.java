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

package org.openvpms.component.system.common.jxpath;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.Functions;
import org.apache.commons.jxpath.functions.MethodFunction;
import org.apache.commons.jxpath.util.MethodLookupUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Set;


/**
 * A {@code Function} implementation that enables methods to be invoked on a pre-configured {@code Object}.
 *
 * @author Tim Anderson
 */
public class AbstractObjectFunctions implements Functions {

    /**
     * The object to invoke methods on.
     */
    private Object object;

    /**
     * The object class.
     */
    private Class objectClass;

    /**
     * The function namespace.
     */
    private String namespace;

    /**
     * Array containing just the object and no arguments, for method invocation
     * taking no arguments.
     */
    private final Object[] zeroArgs = new Object[1];

    /**
     * Empty array for static method invocation.
     */
    private static final Object[] EMPTY_ARRAY = new Object[0];

    /**
     * Constructs an {@link AbstractObjectFunctions}.
     *
     * @param object    the object
     * @param namespace the function namespace
     */
    public AbstractObjectFunctions(Object object, String namespace) {
        this(namespace);
        setObject(object);
    }

    /**
     * Constructs an {@link AbstractObjectFunctions}.
     * <p/>
     * Derived classes must invoke {@link #setObject} after construction.
     *
     * @param namespace the function namespace
     */
    protected AbstractObjectFunctions(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Returns a set of one namespace - the one specified in the constructor.
     *
     * @return a singleton
     */
    public Set getUsedNamespaces() {
        return Collections.singleton(namespace);
    }

    /**
     * Returns a Function, if any, for the specified namespace,
     * name and parameter types.
     *
     * @param namespace if it is not the namespace specified in the constructor,
     *                  the method returns null
     * @param name      is a function name.
     * @return a MethodFunction, or null if there is no such function.
     */
    public Function getFunction(String namespace, String name,
                                Object[] parameters) {
        if (!namespace.equals(this.namespace)) {
            return null;
        }

        if (parameters == null) {
            parameters = EMPTY_ARRAY;
        }

        Method method = MethodLookupUtils.lookupStaticMethod(objectClass,
                                                             name, parameters);
        if (method != null && Modifier.isStatic(method.getModifiers())) {
            // need to check static due to bug in MethodLookupUtils
            return new MethodFunction(method);
        }

        Object[] params = getParameters(parameters);
        method = MethodLookupUtils.lookupMethod(objectClass, name, params);
        if (method != null) {
            return new MethodFunction(method) {
                @Override
                public Object invoke(ExpressionContext context,
                                     Object[] parameters) {
                    return super.invoke(context, getParameters(parameters));
                }
            };
        }

        return null;
    }

    /**
     * Registers the object to invoke methods on.
     *
     * @param object the object
     */
    protected void setObject(Object object) {
        this.object = object;
        this.objectClass = object.getClass();
        zeroArgs[0] = object;
    }

    /**
     * Helper to modify method parameters to support invocation on the {@link #object}
     *
     * @param parameters the parameters. May be {@code null}
     * @return the modified parameters
     */
    private Object[] getParameters(Object[] parameters) {
        Object[] result;
        if (parameters == null || parameters.length == 0) {
            result = zeroArgs;
        } else {
            result = new Object[parameters.length + 1];
            result[0] = object;
            System.arraycopy(parameters, 0, result, 1, parameters.length);
        }
        return result;
    }

}
