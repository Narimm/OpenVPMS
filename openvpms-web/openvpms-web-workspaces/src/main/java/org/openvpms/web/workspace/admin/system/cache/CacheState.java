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

package org.openvpms.web.workspace.admin.system.cache;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Statistics;
import org.openvpms.component.business.service.cache.EhCacheable;

/**
 * Cache state.
 *
 * @author Tim Anderson
 */
class CacheState {

    /**
     * The cache display name.
     */
    private final String displayName;

    /**
     * The cache.
     */
    private final EhCacheable cache;

    /**
     * The current statistics.
     */
    private Statistics statistics;

    /**
     * The cache size, in bytes.
     */
    private long size;

    /**
     * Constructs a {@link CacheState}.
     *
     * @param cache       the cache
     * @param displayName the cache display name
     */
    public CacheState(EhCacheable cache, String displayName) {
        this.displayName = displayName;
        this.cache = cache;
        refreshStatistics();
    }

    /**
     * Returns the cache name.
     *
     * @return the name
     */
    public String getName() {
        return cache.getCache().getName();
    }

    /**
     * Returns the cache display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the number of objects in the cache.
     *
     * @return the number of objects in the cache
     */
    public long getCount() {
        return statistics.getObjectCount();
    }

    /**
     * The maximum number of objects that can be cached.
     *
     * @return the maximum number of objects that can be cached
     */
    public long getMaxCount() {
        return cache.getCache().getCacheConfiguration().getMaxEntriesLocalHeap();
    }

    /**
     * Returns the number of times a requested item was found in the cache.
     *
     * @return the number of times a requested item was found in the cache
     */
    public long getHits() {
        return statistics.getCacheHits();
    }

    /**
     * Returns the number of times a requested item was not found in the cache.
     *
     * @return the number of times a requested element was not found in the cache
     */
    public long getMisses() {
        return statistics.getCacheMisses();
    }

    /**
     * Returns the size of the cache.
     *
     * @return the size of the cache, in bytes
     */
    public long getSize() {
        return size;
    }

    /**
     * Returns the cache use.
     *
     * @return the cache use, as a percentage
     */
    public int getUse() {
        long max = getMaxCount();
        long count = getCount();
        return (max != 0) ? (int) Math.round(100.0 * count / max) : 0;
    }

    /**
     * Refreshes the cache statistics.
     */
    public void refreshStatistics() {
        statistics = cache.getCache().getStatistics();
        try {
            size = cache.getCache().calculateInMemorySize();
        } catch (Throwable exception) {
            size = -1;
        }
    }

    /**
     * Resets the cache statistics.
     */
    public void resetStatistics() {
        statistics.clearStatistics();
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Returns the underlying cache.
     *
     * @return the underlying cache
     */
    public Ehcache getCache() {
        return cache.getCache();
    }
}
