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


import org.ehcache.Cache;
import org.ehcache.core.Ehcache;

/**
 * Interface for services backed by an {@link Ehcache} that:
 * <ul>
 * <li>expose the cache, for statistics purposes</li>
 * <li>have additional requirements to support cache clearing</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public interface EhCacheable {

    /**
     * Returns the underlying cache.
     *
     * @return the underlying cache
     */
    Cache getCache();

    /**
     * Clears cached data, including the underlying cache.
     */
    void clear();
}
