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
import org.ehcache.CacheManager;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.core.config.store.StoreStatisticsConfiguration;
import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.spi.loaderwriter.CacheLoaderWriter;
import org.springframework.beans.factory.DisposableBean;

/**
 * Abstract implementation of {@link EhcacheManager}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractEhcacheManager implements EhcacheManager, DisposableBean {

    /**
     * Default maximum no. of elements.
     */
    private final long defaultMaxElements;

    /**
     * The underlying cache manager.
     */
    private final CacheManager cacheManager;

    /**
     * Constructs an {@link AbstractEhcacheManager}.
     *
     * @param statistics the statistics service, or {@code null} if statistics aren't to be collected
     */
    public AbstractEhcacheManager(StatisticsService statistics) {
        this(statistics, 100);
    }

    /**
     * Constructs an {@link AbstractEhcacheManager}.
     *
     * @param statistics         the statistics service, or {@code null} if statistics aren't to be collected
     * @param defaultMaxElements the default maximum no. of elements
     */
    public AbstractEhcacheManager(StatisticsService statistics, long defaultMaxElements) {
        this.defaultMaxElements = defaultMaxElements;
        CacheManagerBuilder<CacheManager> builder = CacheManagerBuilder.newCacheManagerBuilder();
        if (statistics != null) {
            builder = builder.using(statistics);
        }
        cacheManager = builder.build(true);
    }

    /**
     * Creates an eternal cache.
     *
     * @param name      the cache name. Must be unique
     * @param keyType   the key type
     * @param valueType the value type
     * @return a new cache
     */
    @Override
    public <K, V> Cache<K, V> create(String name, Class<K> keyType, Class<V> valueType) {
        return create(name, getMaxElements(name), keyType, valueType);
    }

    /**
     * Creates an eternal cache.
     *
     * @param name         the cache name. Must be unique
     * @param keyType      the key type
     * @param valueType    the value type
     * @param loaderWriter the loader writer
     * @return a new cache
     */
    @Override
    public <K, V> Cache<K, V> create(String name, Class<K> keyType, Class<V> valueType,
                                     CacheLoaderWriter<K, V> loaderWriter) {
        return create(name, getMaxElements(name), loaderWriter, keyType, valueType);
    }

    /**
     * Creates an eternal cache.
     *
     * @param name        the cache name. Must be unique
     * @param maxElements the maximum number of elements
     * @param keyType     the key type
     * @param valueType   the value type
     * @return a new cache
     */
    @Override
    public <K, V> Cache<K, V> create(String name, long maxElements, Class<K> keyType, Class<V> valueType) {
        return create(name, maxElements, null, keyType, valueType);
    }

    /**
     * Sets the maximum number of elements that a cache may hold in memory.
     *
     * @param cache       the cache
     * @param name        the cache name
     * @param maxElements the maximum number of elements
     */
    @Override
    public <K, V> void setMaxElements(Cache<K, V> cache, String name, long maxElements) {
        ResourcePools pools = ResourcePoolsBuilder.newResourcePoolsBuilder()
                .heap(maxElements, EntryUnit.ENTRIES).build();
        cache.getRuntimeConfiguration().updateResourcePools(pools);
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     */
    @Override
    public void destroy() {
        cacheManager.close();
    }

    /**
     * Returns the maximum number of elements for the named cache.
     *
     * @param name the cache name
     * @return the maximum elements, or {@code <=0} to use the default
     */
    protected long getMaxElements(String name) {
        return defaultMaxElements;
    }

    /**
     * Returns the default maximum number of elements.
     *
     * @return the default maximum number
     */
    protected long getDefaultMaxElements() {
        return defaultMaxElements;
    }

    /**
     * Creates a cache.
     *
     * @param name         the cache name
     * @param maxElements  the maximum no. of the elements. If {@code <= 0}, the default will be used
     * @param loaderWriter the loader writer. May be {@code null}
     * @param keyType      the key type
     * @param valueType    the value type
     * @return a new cache
     */
    protected <K, V> Cache<K, V> create(String name, long maxElements, CacheLoaderWriter<K, V> loaderWriter,
                                        Class<K> keyType, Class<V> valueType) {
//        CacheConfiguration config = cache.getCacheConfiguration();
//        if (config.getSizeOfPolicyConfiguration() == null) {
//            // register a configuration to limit warnings.
//            SizeOfPolicyConfiguration policyConfiguration = new SizeOfPolicyConfiguration()
//                    .maxDepth(1000000)
//                    .maxDepthExceededBehavior(SizeOfPolicyConfiguration.MaxDepthExceededBehavior.CONTINUE);
//            config.sizeOfPolicy(policyConfiguration);
//        }
        if (maxElements <= 0) {
            maxElements = getDefaultMaxElements();
        }
        ResourcePoolsBuilder heap = ResourcePoolsBuilder.newResourcePoolsBuilder().heap(maxElements, EntryUnit.ENTRIES);
        CacheConfigurationBuilder<K, V> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                keyType, valueType, heap)
                .add(new StoreStatisticsConfiguration(true)); // explicitly enable statistics
        if (loaderWriter != null) {
            builder = builder.withLoaderWriter(loaderWriter);
        }
        return cacheManager.createCache(name, builder);
    }

}
