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

import java.util.HashMap;
import java.util.Map;


/**
 * Abstract implementation of the {@link ObjectLoader} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractObjectLoader implements ObjectLoader {

    /**
     * The default loader, if none is specified.
     */
    private static final ObjectLoader defaultLoader
            = new ReflectingObjectLoader();

    /**
     * Loaders, keyed on class name.
     */
    private Map<String, ObjectLoader> loaders
            = new HashMap<String, ObjectLoader>();


    /**
     * Sets the loader for a particular class name.
     *
     * @param className the class name
     * @param loader    the loader for the class
     */
    public void setLoader(String className, ObjectLoader loader) {
        loaders.put(className, loader);
    }

    /**
     * Sets the loader for a particular class.
     *
     * @param clazz  the class
     * @param loader the loader for the class
     */
    public void setLoader(Class clazz, ObjectLoader loader) {
        setLoader(clazz.getName(), loader);
    }

    /**
     * Returns the loader for an object.
     *
     * @param object the object
     * @return the loader for <tt>object</tt>
     */
    public ObjectLoader getLoader(Object object) {
        ObjectLoader loader = loaders.get(object.getClass().getName());
        if (loader == null) {
            loader = defaultLoader;
        }
        return loader;
    }
}
