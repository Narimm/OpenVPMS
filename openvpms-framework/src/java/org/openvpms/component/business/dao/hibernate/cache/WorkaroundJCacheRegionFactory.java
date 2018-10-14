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

package org.openvpms.component.business.dao.hibernate.cache;

import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.jcache.internal.JCacheRegionFactory;
import org.hibernate.cache.spi.CacheKeysFactory;

import javax.cache.CacheManager;
import javax.cache.spi.CachingProvider;
import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * Workaround for https://hibernate.atlassian.net/browse/HHH-12531 JCache existing cache not detected.
 * This allows ehcache.xml to be read from the classpath without requiring a file:// URI.
 * Based on https://discourse.hibernate.org/t/hibernate-5-3-with-ehcache-3-5-2-configuration-problem/789/8
 *
 * @author Tim Anderson
 */
public class WorkaroundJCacheRegionFactory extends JCacheRegionFactory {

    public WorkaroundJCacheRegionFactory() {
        super();
    }

    public WorkaroundJCacheRegionFactory(CacheKeysFactory cacheKeysFactory) {
        super(cacheKeysFactory);
    }

    @Override
    protected CacheManager resolveCacheManager(SessionFactoryOptions settings, Map properties) {
        Object explicitCacheManager = properties.get("hibernate.javax.cache.cache_manager");
        if (explicitCacheManager != null) {
            return super.resolveCacheManager(settings, properties);
        } else {
            CachingProvider cachingProvider = this.getCachingProvider(properties);
            final CacheManager cacheManager;
            URI cacheManagerUri = this.getUri(properties);
// ***** begin patch ******
            URI uri = cacheManagerUri;
            URL url;
            try {
                uri.toURL();
            } catch (Exception e) {
                try {
                    url = getClassLoader(cachingProvider).getResource(cacheManagerUri.toString());
                    uri = url.toURI();
                } catch (Exception e1) {
                    throw new IllegalArgumentException("Resource not found: " + uri, e1);
                }
            }
            // ****** end patch ******
            if (cacheManagerUri != null) {
                cacheManager = cachingProvider.getCacheManager(uri, getClassLoader(cachingProvider));
            } else {
                cacheManager = cachingProvider.getCacheManager();
            }
            return cacheManager;
        }
    }
}
