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

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * An {@link ObjectLoader} that uses reflection to load collections.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReflectingObjectLoader implements ObjectLoader {

    /**
     * The logger.
     */
    private static final Log log
            = LogFactory.getLog(ReflectingObjectLoader.class);

    /**
     * Cache of collection methods, keyed on their class.
     */
    private Map<Class, Method[]> collectionMethods
            = new HashMap<Class, Method[]>();


    /**
     * Loads an object.
     *
     * @param object the object to load
     * @throws HibernateException for any hibernate error
     */
    public void load(Object object) {
        load(object, new HashSet<Object>());
    }

    /**
     * Recursively loads an object.
     *
     * @param object the object to load
     * @param loaded the set of objects already loaded
     * @throws HibernateException for any hibernate error
     */
    protected void load(Object object, Set<Object> loaded) {
        Hibernate.initialize(object);
        loaded.add(object);
        Method[] methods = getCollectionMethods(object);
        for (Method method : methods) {
            Collection collection = null;
            try {
                collection = (Collection) method.invoke(object);
            } catch (Exception exception) {
                log.warn(exception, exception);
            }
            if (collection != null) {
                for (Object elt : collection) {
                    if (!loaded.contains(elt)) {
                        load(elt, loaded);
                    }
                }
            }
        }
    }

    /**
     * Returns the collection methods for an object.
     *
     * @param object the object
     * @return the collection methods
     */
    private synchronized Method[] getCollectionMethods(Object object) {
        Class clazz = object.getClass();
        Method[] result = collectionMethods.get(clazz);
        if (result == null) {
            List<Method> list = new ArrayList<Method>();
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (isCollectionGetter(method)) {
                    list.add(method);
                }
            }
            result = list.toArray(new Method[list.size()]);
            collectionMethods.put(clazz, result);
        }
        return result;
    }

    /**
     * Determines if a method is a 'get' method returning a collection.
     *
     * @param method the method to check
     * @return <code>true</code> if the method returns a collection,
     *         otherwise <code>false</code>
     */
    private boolean isCollectionGetter(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Class<?> returnType = method.getReturnType();
        return Modifier.isPublic(method.getModifiers())
                && paramTypes.length == 0
                && Collection.class.isAssignableFrom(returnType)
                && method.getName().startsWith("get");
    }
}
