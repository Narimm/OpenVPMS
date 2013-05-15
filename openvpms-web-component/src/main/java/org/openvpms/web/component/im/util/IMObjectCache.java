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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.im.util;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;


/**
 * A cache of {@link IMObject} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public interface IMObjectCache {

    /**
     * Adds an object to the cache.
     *
     * @param object the object to cache
     */
    void add(IMObject object);

    /**
     * Removes an object from the cache, if present.
     *
     * @param object the object to remove
     */
    void remove(IMObject object);

    /**
     * Returns an object given its reference.
     * <p/>
     * If the object isn't cached, it may be retrieved.
     *
     * @param reference the object reference. May be <tt>null</tt>
     * @return the object corresponding to <tt>reference</tt> or <tt>null</tt> if none exists
     */
    IMObject get(IMObjectReference reference);
}