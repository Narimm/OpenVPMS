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

import org.apache.commons.collections.map.ReferenceMap;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.system.ServiceHelper;


/**
 * Default implementation of the {@link IMObjectCache} interface.
 * <p/>
 * This implementation allows objects to be reclaimed by the garbage collector if they are not referenced by any other
 * object.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class DefaultIMObjectCache implements IMObjectCache {

    /**
     * The underlying cache.
     */
    private ReferenceMap cache;

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Constructs a <tt>DefaultIMObjectCache</tt>.
     */
    public DefaultIMObjectCache() {
        service = ServiceHelper.getArchetypeService();
        cache = new ReferenceMap(ReferenceMap.HARD, ReferenceMap.SOFT);
    }


    /**
     * Adds an object to the cache.
     *
     * @param object the object to cache
     */
    public void add(IMObject object) {
        cache.put(object.getObjectReference(), object);
    }

    /**
     * Removes an object from the cache, if present.
     *
     * @param object the object to remove
     */
    public void remove(IMObject object) {
        cache.remove(object.getObjectReference());
    }

    /**
     * Returns an object given its reference.
     * <p/>
     * If the object isn't cached, it will be retrieved from the archetype service and added to the cache if it exists.
     *
     * @param reference the object reference
     * @return the object corresponding to <tt>reference</tt> or <tt>null</tt> if none exists
     */
    public IMObject get(IMObjectReference reference) {
        IMObject result = null;
        if (reference != null) {
            result = (IMObject) cache.get(reference);
            if (result == null) {
                result = service.get(reference);
                if (result != null) {
                    cache.put(reference, result);
                }
            }
        }
        return result;
    }
}
