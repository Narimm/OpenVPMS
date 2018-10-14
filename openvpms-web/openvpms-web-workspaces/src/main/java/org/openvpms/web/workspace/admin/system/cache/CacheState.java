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

import org.apache.commons.lang.reflect.FieldUtils;
import org.ehcache.Cache;
import org.ehcache.config.CacheRuntimeConfiguration;
import org.ehcache.config.ResourceType;
import org.ehcache.config.SizedResourcePool;
import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.core.statistics.CacheStatistics;
import org.ehcache.core.statistics.TierStatistics;
import org.ehcache.sizeof.SizeOf;
import org.ehcache.sizeof.SizeOfFilterSource;
import org.openvpms.component.business.service.cache.EhCacheable;
import org.openvpms.web.component.util.ErrorHelper;

import java.lang.reflect.Field;

/**
 * Cache state.
 * <p>
 * This assumes that the cache is heap based, with an element size.
 *
 * @author Tim Anderson
 */
class CacheState {

    /**
     * The cache name.
     */
    private final String name;

    /**
     * The cache display name.
     */
    private final String displayName;

    /**
     * The cache.
     */
    private final EhCacheable cache;

    /**
     * The statistics service.
     */
    private final StatisticsService service;

    /**
     * The cache statistics.
     */
    private CacheStatistics statistics;

    /**
     * The heap statistics.
     */
    private TierStatistics tierStatistics;

    /**
     * The cache size, in bytes.
     */
    private long size;

    /**
     * Constructs a {@link CacheState}.
     *
     * @param cache       the cache
     * @param name        the cache name
     * @param displayName the cache display name
     * @param service     the statistics service
     */
    public CacheState(EhCacheable cache, String name, String displayName, StatisticsService service) {
        this.name = name;
        this.displayName = displayName;
        this.cache = cache;
        this.service = service;
        refreshStatistics();
    }

    /**
     * Returns the cache name.
     *
     * @return the name
     */
    public String getName() {
        return name;
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
        return tierStatistics != null ? tierStatistics.getMappings() : 0;
    }

    /**
     * The maximum number of objects that can be cached.
     *
     * @return the maximum number of objects that can be cached
     */
    public long getMaxCount() {
        CacheRuntimeConfiguration configuration = cache.getCache().getRuntimeConfiguration();
        SizedResourcePool pool = configuration.getResourcePools().getPoolForResource(ResourceType.Core.HEAP);
        return pool != null ? pool.getSize() : 0;
    }

    /**
     * Returns the number of times a requested item was found in the cache.
     *
     * @return the number of times a requested item was found in the cache
     */
    public long getHits() {
        return statistics != null ? statistics.getCacheHits() : 0;
    }

    /**
     * Returns the number of times a requested item was not found in the cache.
     *
     * @return the number of times a requested element was not found in the cache
     */
    public long getMisses() {
        return statistics != null ? statistics.getCacheMisses() : 0;
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
        statistics = service.getCacheStatistics(name);
        if (statistics != null) {
            tierStatistics = statistics.getTierStatistics().get("OnHeap");
        }
        if (getCount() != 0) {
            // only calculate the cache size if it is not empty
            long newSize = -1;
            try {
                // hack to access the underlying store of the cache, in order to calculate its size.
                // TODO - clean up when Ehcache provides a cleaner API
                Cache cache = getCache();
                Field field = FieldUtils.getField(cache.getClass(), "store", true);
                if (field != null) {
                    Object store = field.get(cache);
                    if (store != null) {
                        SizeOfFilterSource filters = new SizeOfFilterSource(true);
                        SizeOf sizeOf = SizeOf.newInstance(filters.getFilters());
                        newSize = sizeOf.deepSizeOf(store);
                    }
                }
            } catch (Throwable exception) {
                ErrorHelper.show(exception);
            }
            size = newSize;
        } else {
            size = 0;
        }
    }

    /**
     * Resets the cache statistics.
     */
    public void resetStatistics() {
        if (statistics != null) {
            statistics.clear();
        }
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
    public Cache getCache() {
        return cache.getCache();
    }
}
