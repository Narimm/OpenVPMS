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

package org.openvpms.component.business.service.archetype;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.component.system.common.cache.LRUIMObjectCache;

/**
 * A read-only {@link IArchetypeService} that supports caching.
 *
 * @author Tim Anderson
 */
public class CachingReadOnlyArchetypeService extends ReadOnlyArchetypeService {

    /**
     * The cache.
     */
    private final IMObjectCache cache;

    /**
     * Constructs a {@link ReadOnlyArchetypeService} that uses an {@link LRUIMObjectCache}.
     *
     * @param maxSize the maximum size of the cache
     * @param service the service to delegate to
     */
    public CachingReadOnlyArchetypeService(int maxSize, IArchetypeService service) {
        this(new LRUIMObjectCache(maxSize, service), service);
    }

    /**
     * Constructs a {@link ReadOnlyArchetypeService}.
     *
     * @param cache   the cache
     * @param service the service to delegate to
     */
    public CachingReadOnlyArchetypeService(IMObjectCache cache, IArchetypeService service) {
        super(service);
        this.cache = cache;
    }

    /**
     * Retrieves an object given its reference.
     *
     * @param reference the object reference
     * @return the corresponding object, or {@code null} if none is found
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public IMObject get(Reference reference) {
        return cache.get(reference);
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        cache.clear();
    }
}
