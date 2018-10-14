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
import org.ehcache.spi.loaderwriter.CacheLoaderWriter;

/**
 * A manager of Ehcaches.
 *
 * @author Tim Anderson
 */
public interface EhcacheManager {

    /**
     * Creates an eternal cache.
     *
     * @param name      the cache name. Must be unique
     * @param keyType   the key type
     * @param valueType the value type
     * @return a new cache
     */
    <K, V> Cache<K, V> create(String name, Class<K> keyType, Class<V> valueType);

    /**
     * Creates an eternal cache.
     *
     * @param name        the cache name. Must be unique
     * @param maxElements the maximum number of elements
     * @param keyType     the key type
     * @param valueType   the value type
     * @return a new cache
     */
    <K, V> Cache<K, V> create(String name, long maxElements, Class<K> keyType, Class<V> valueType);

    /**
     * Creates an eternal cache.
     *
     * @param name         the cache name. Must be unique
     * @param keyType      the key type
     * @param valueType    the value type
     * @param loaderWriter the loader writer
     * @return a new cache
     */
    <K, V> Cache<K, V> create(String name, Class<K> keyType, Class<V> valueType, CacheLoaderWriter<K, V> loaderWriter);

    /**
     * Sets the maximum number of elements that a cache may hold in memory.
     *
     * @param cache       the cache
     * @param name        the cache name
     * @param maxElements the maximum number of elements
     */
    <K, V> void setMaxElements(Cache<K, V> cache, String name, long maxElements);
}
