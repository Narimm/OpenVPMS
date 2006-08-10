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
 *  $Id$
 */

package org.openvpms.web.component.im.util;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConstructorUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;


/**
 * Manages the configuration of an archetype handler, loaded by
 * {@link ArchetypeHandlers}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ArchetypeHandler {

    /**
     * The implementation class.
     */
    private final Class _type;

    /**
     * Configuration properties.
     */
    private Map<String, String> _properties;


    /**
     * Creates a new <code>ArchetypeHandler</code>.
     *
     * @param type the handler implementation class
     */
    public ArchetypeHandler(Class type) {
        this(type, null);
    }

    /**
     * Creates a new <code>ArchetypeHandler</code>.
     *
     * @param type       the handler implementation class
     * @param properties configuration properties. May be <code>null</code>
     */
    public ArchetypeHandler(Class type, Map<String, String> properties) {
        _type = type;
        _properties = properties;
    }

    /**
     * Returns the handler implementation class.
     *
     * @return the implementation class
     */
    public Class getType() {
        return _type;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return _type.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     *         argument; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArchetypeHandler) {
            return ((ArchetypeHandler) obj)._type.equals(_type);
        }
        return false;
    }

    /**
     * Creates a new instance of the handler.
     *
     * @return a new instance of the handler
     * @throws NoSuchMethodException     if matching constructor cannot be found
     * @throws IllegalAccessException    thrown on the constructor's invocation
     * @throws InvocationTargetException thrown on the constructor's invocation
     * @throws InstantiationException    thrown on the constructor's invocation
     */
    public Object create() throws NoSuchMethodException,
                                  IllegalAccessException,
                                  InvocationTargetException,
                                  InstantiationException {
        return create(new Object[0]);
    }

    /**
     * Creates a new instance of the handler.
     *
     * @param args the args
     * @return a new instance of the handler
     * @throws NoSuchMethodException     if matching constructor cannot be found
     * @throws IllegalAccessException    thrown on the constructor's invocation
     * @throws InvocationTargetException thrown on the constructor's invocation
     * @throws InstantiationException    thrown on the constructor's invocation
     */
    public Object create(Object[] args) throws NoSuchMethodException,
                                               IllegalAccessException,
                                               InvocationTargetException,
                                               InstantiationException {
        Object handler = ConstructorUtils.invokeConstructor(_type, args);
        return init(handler);
    }

    /**
     * Creates a new instance of the handler.
     *
     * @param args  a list of the arguments
     * @param types the argument types
     * @throws NoSuchMethodException     if matching constructor cannot be found
     * @throws IllegalAccessException    thrown on the constructor's invocation
     * @throws InvocationTargetException thrown on the constructor's invocation
     * @throws InstantiationException    thrown on the constructor's invocation
     */
    public Object create(Object[] args, Class[] types)
            throws IllegalAccessException, NoSuchMethodException,
                   InvocationTargetException, InstantiationException {
        Object handler = ConstructorUtils.invokeConstructor(_type, args, types);
        return init(handler);
    }

    /**
     * Initialises the handler.
     *
     * @param handler the handler
     * @return the handler
     * @throws IllegalAccessException    if the caller does not have
     *                                   access to the property accessor method
     * @throws InvocationTargetException if the property accessor method
     *                                   throws an exception
     */
    private Object init(Object handler) throws IllegalAccessException,
                                               InvocationTargetException {
        if (_properties != null && _properties.size() != 0) {
            BeanUtils.populate(handler, _properties);
        }
        return handler;
    }

}
