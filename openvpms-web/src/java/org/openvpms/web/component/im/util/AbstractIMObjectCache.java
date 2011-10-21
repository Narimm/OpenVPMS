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
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.Map;


/**
 * Abstract implementation of the {@link IMObjectCache} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class AbstractIMObjectCache implements IMObjectCache {

    /**
     * The cache.
     */
    private final Map<IMObjectReference, IMObject> cache;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs an <tt>AbstractIMObjectCache</tt>
     *
     * @param cache   the cache
     * @param service the archetype service
     */
    protected AbstractIMObjectCache(Map<IMObjectReference, IMObject> cache, IArchetypeService service) {
        this.cache = cache;
        this.service = service;
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
     * @param reference the object reference. May be <tt>null</tt>
     * @return the object corresponding to <tt>reference</tt> or <tt>null</tt> if none exists
     */
    public IMObject get(IMObjectReference reference) {
        IMObject result = null;
        if (reference != null) {
            result = cache.get(reference);
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
