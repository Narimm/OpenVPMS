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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.common;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;


/**
 * Hibernate helper methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class HibernateHelper {

    /**
     * Helper to deproxy an object if required.
     *
     * @param object the potentially proxied object
     * @return the deproxied object, or <tt>object</tt> if it wasn't proxied
     */
    public static Object deproxy(Object object) {
        if (object instanceof HibernateProxy) {
            HibernateProxy proxy = ((HibernateProxy) object);
            LazyInitializer init = proxy.getHibernateLazyInitializer();
            object = init.getImplementation();
        }
        return object;
    }

    /**
     * Determines if a (possibly) persistent object is uninitialised.
     *
     * @param object the object
     * @return <tt>true</tt> if the object is uninitialised,
     *         otherwise <tt>false</tt>
     */
    public static boolean isUnintialised(Object object) {
        if (object instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy) object;
            LazyInitializer init = proxy.getHibernateLazyInitializer();
            return init.isUninitialized();
        }
        return false;
    }
}
