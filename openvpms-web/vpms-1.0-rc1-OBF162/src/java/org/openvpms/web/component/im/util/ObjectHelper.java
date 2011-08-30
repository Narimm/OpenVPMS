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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.util;

import org.apache.commons.lang.ObjectUtils;

import java.math.BigDecimal;


/**
 * Object helper methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ObjectHelper {

    /**
     * Compares two objects for equality, where either one or both
     * objects may be <tt>null</tt>.
     * This method will use {@link Comparable#compareTo} in preference
     * of {@link Object#equals} if both instances implement {@link Comparable}
     * and <tt>obj2</tt> is assignable from <tt>obj1</tt>.
     * This is primarily to check equality of {@link BigDecimal}s.
     *
     * @param obj1
     * @param obj2
     * @return <tt>true</tt> if the objects are equal, otherwise <tt>false</tt>
     */
    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 instanceof Comparable && obj2 != null
                && obj1.getClass().isAssignableFrom(obj2.getClass())) {
            Comparable<Object> c1 = (Comparable<Object>) obj1;
            Comparable<Object> c2 = (Comparable<Object>) obj2;
            return (c1.compareTo(c2) == 0);
        }
        return ObjectUtils.equals(obj1, obj2);
    }
}
