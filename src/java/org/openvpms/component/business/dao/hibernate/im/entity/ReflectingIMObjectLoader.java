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
import org.openvpms.component.business.domain.im.common.IMObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * An {@link IMObjectLoader} that uses reflection to load collections.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReflectingIMObjectLoader implements IMObjectLoader {

    /**
     * The logger.
     */
    private static final Log log
            = LogFactory.getLog(ReflectingIMObjectLoader.class);

    /**
     * Loads an object.
     *
     * @param object the object to load
     * @throws HibernateException for any hibernate error
     */
    public void load(IMObject object) {
        load(object, new HashSet<IMObject>());
    }

    /**
     * Recursively loads an object.
     *
     * @param object the object to load
     * @param loaded the set of objects already loaded
     * @throws HibernateException for any hibernate error
     */
    protected void load(IMObject object, Set<IMObject> loaded) {
        Hibernate.initialize(object);
        loaded.add(object);
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            if (isCollectionGetter(method)) {
                Collection collection = null;
                try {
                    collection = (Collection) method.invoke(object);
                } catch (Exception exception) {
                    log.warn(exception, exception);
                }
                if (collection != null) {
                    for (Object elt : collection) {
                        if (elt instanceof IMObject) {
                            IMObject child = (IMObject) elt;
                            if (!loaded.contains(child)) {
                                load(child, loaded);
                            }
                        }
                    }
                }
            }
        }
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
