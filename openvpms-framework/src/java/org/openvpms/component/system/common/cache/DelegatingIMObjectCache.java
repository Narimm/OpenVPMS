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
import org.openvpms.component.model.object.Reference;

/**
 * Implementation of {@link IMObjectCache} that delegating to another.
 *
 * @author Tim Anderson
 */
public class DelegatingIMObjectCache implements IMObjectCache {

    /**
     * The underlying cache.
     */
    private final IMObjectCache cache;

    /**
     * Constructs a {@link DelegatingIMObjectCache}.
     *
     * @param cache the cache to delegate to
     */
    public DelegatingIMObjectCache(IMObjectCache cache) {
        this.cache = cache;
    }

    /**
     * Adds an object to the cache.
     *
     * @param object the object to cache
     */
    @Override
    public void add(IMObject object) {
        cache.add(object);
    }

    /**
     * Removes an object from the cache, if present.
     *
     * @param object the object to remove
     */
    @Override
    public void remove(IMObject object) {
        cache.remove(object);
    }

    /**
     * Returns an object given its reference.
     * <p/>
     * If the object isn't cached, it may be retrieved.
     *
     * @param reference the object reference. May be {@code null}
     * @return the object corresponding to {@code reference} or {@code null} if none exists
     */
    @Override
    public IMObject get(Reference reference) {
        return cache.get(reference);
    }

    /**
     * Determines if an object is cached.
     *
     * @param reference the object reference. May be {@code null}
     * @return {@code true} if the object is cached
     */
    @Override
    public boolean exists(Reference reference) {
        return cache.exists(reference);
    }

    /**
     * Clears the cache.
     */
    @Override
    public void clear() {
        cache.clear();
    }
}
