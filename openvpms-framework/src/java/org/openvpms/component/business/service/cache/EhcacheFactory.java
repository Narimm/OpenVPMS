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

package org.openvpms.component.business.service.cache;

import net.sf.ehcache.Ehcache;

/**
 * A factory for Ehcaches.
 *
 * @author Tim Anderson
 */
public interface EhcacheFactory {

    /**
     * Creates an eternal cache.
     *
     * @param name the cache name. Must be unique
     * @return a new cache
     */
    Ehcache create(String name);

    /**
     * Sets the maximum number of elements that a cache may hold in memory.
     *
     * @param cache       the cache
     * @param maxElements the maximum number of elements
     */
    void setMaxElements(Ehcache cache, long maxElements);
}
