/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.system.common.cache;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.object.Reference;

import java.util.Map;


/**
 * Abstract implementation of the {@link IMObjectCache} interface.
 * <p>
 * Note that this implementation excludes {@link Document} instances which may be too large to cache for long periods.
 *
 * @author Tim Anderson
 */
public abstract class AbstractIMObjectCache implements IMObjectCache {

    /**
     * The cache.
     */
    private final Map<Reference, IMObject> cache;

    /**
     * The archetype service. May be {@code null}
     */
    private final IArchetypeService service;

    /**
     * Constructs an {@link AbstractIMObjectCache}
     *
     * @param cache   the cache
     * @param service the archetype service. If non-null, non-cached objects may retrieved
     */
    protected AbstractIMObjectCache(Map<Reference, IMObject> cache, IArchetypeService service) {
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
     * <p>
     * If the object isn't cached, it will be retrieved from the archetype service and added to the cache if it exists.
     *
     * @param reference the object reference. May be {@code null}
     * @return the object corresponding to {@code reference} or {@code null} if none exists
     */
    public IMObject get(Reference reference) {
        IMObject result = null;
        if (reference != null) {
            result = cache.get(reference);
            if (result == null && service != null) {
                result = service.get(reference);
                if (result != null && !(result instanceof Document)) {
                    cache.put(reference, result);
                }
            }
        }
        return result;
    }

    /**
     * Determines if an object is cached.
     *
     * @param reference the object reference. May be {@code null}
     * @return {@code true} if the object is cached
     */
    @Override
    public boolean exists(Reference reference) {
        return reference != null && cache.containsKey(reference);
    }

    /**
     * Clears the cache.
     */
    @Override
    public void clear() {
        cache.clear();
    }
}
