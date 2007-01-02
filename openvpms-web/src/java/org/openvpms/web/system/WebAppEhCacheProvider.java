/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.system;

import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.EhCacheProvider;

import java.util.Properties;


/**
 * Extends <code>EhCacheProvider</code> to set the context class loader
 * so that EhCache can find the web apps' ehcache.xml configuration.
 * Workaround for OVPMS-418.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class WebAppEhCacheProvider extends EhCacheProvider {

    /**
     * Configure the cache.
     *
     * @param regionName the name of the cache region
     * @param properties configuration settings
     * @throws CacheException
     */
    public Cache buildCache(String regionName, Properties properties)
            throws CacheException {
        ClassLoader current = setClassLoader();
        try {
            return super.buildCache(regionName, properties);
        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
    }

    /**
     * Callback to perform any necessary initialization of the underlying cache
     * implementation during SessionFactory construction.
     *
     * @param properties current configuration settings.
     */
    public void start(Properties properties) throws CacheException {
        ClassLoader current = setClassLoader();
        try {
            super.start(properties);
        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
    }

    /**
     * Sets the context class loader to the class loader of this class.
     *
     * @return the existing class loader
     */
    private ClassLoader setClassLoader() {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(
                getClass().getClassLoader());
        return current;
    }

}
