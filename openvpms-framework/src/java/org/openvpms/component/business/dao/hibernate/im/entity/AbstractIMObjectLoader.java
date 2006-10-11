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

import org.hibernate.HibernateException;
import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Abstract implementation of the {@link IMObjectLoader} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AbstractIMObjectLoader implements IMObjectLoader {

    /**
     * The default loader, if none is specified.
     */
    private static final IMObjectLoader defaultLoader
            = new ReflectingIMObjectLoader();

    /**
     * Loaders, keyed on class type.
     */
    private Map<String, IMObjectLoader> loaders
            = new HashMap<String, IMObjectLoader>();


    /**
     * Sets the loader for a particular IMObject class type.
     *
     * @param type   the IMObject class type
     * @param loader the loader for the class
     */
    public void setLoader(String type, IMObjectLoader loader) {
        loaders.put(type, loader);
    }

    /**
     * Loads an object.
     *
     * @param object the object to load
     * @throws HibernateException for any hibernate error
     */
    public void load(IMObject object) {
        IMObjectLoader loader = loaders.get(object.getClass().getName());
        if (loader == null) {
            loader = defaultLoader;
        }
        loader.load(object);
    }
}
