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

import net.sf.ehcache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.EhCache;
import org.hibernate.cache.Timestamper;

import java.net.URL;
import java.util.Properties;


/**
 * A <code>CacheProvider</code> for EhCache that uses the web app's ehcache.xml
 * configuration. Based on hibernates <code>EhCacheProvider</code>
 * implementation.
 * Workaround for OVPMS-418.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class WebAppEhCacheProvider implements CacheProvider {

    /**
     * The cache manager.
     */
    private CacheManager manager;

    /**
     * CacheManager.create() actually returns a singleton reference, which is
     * causing problems with users attempting to use multiple SessionFactories
     * all using the EhCacheProvider in the same classloader.  The work-around
     * is to use simple reference counting here.
     */
    private static int referenceCount = 0;

    /**
     * The logger.
     */
    private static final Log log
            = LogFactory.getLog(WebAppEhCacheProvider.class);

    private static final String DEFAULT_CLASSPATH_CONFIGURATION_FILE
            = "/ehcache.xml";
    private static final String FAILSAFE_CLASSPATH_CONFIGURATION_FILE
            = "/ehcache-failsafe.xml";

    /**
     * Builds a Cache.
     * <p/>
     * Even though this method provides properties, they are not used.
     * Properties for EHCache are specified in the ehcache.xml file.
     * Configuration will be read from ehcache.xml for a cache declaration
     * where the name attribute matches the name parameter in this builder.
     *
     * @param name       the name of the cache. Must match a cache configured in ehcache.xml
     * @param properties not used
     * @return a newly built cache will be built and initialised
     * @throws CacheException inter alia, if a cache of the same name already exists
     */
    public Cache buildCache(String name, Properties properties)
            throws CacheException {
        try {
            net.sf.ehcache.Cache cache = manager.getCache(name);
            if (cache == null) {
                log.warn("Could not find configuration [" + name
                        + "]; using defaults.");
                manager.addCache(name);
                cache = manager.getCache(name);
                log.debug("started EHCache region: " + name);
            }
            return new EhCache(cache);
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(e);
        }
    }

    /**
     * Returns the next timestamp.
     */
    public long nextTimestamp() {
        return Timestamper.next();
    }

    /**
     * Callback to perform any necessary initialization of the underlying cache
     * implementation during SessionFactory construction.
     *
     * @param properties current configuration settings.
     */
    public void start(Properties properties) throws CacheException {
        URL url;
        try {
            url = getClass().getResource(DEFAULT_CLASSPATH_CONFIGURATION_FILE);
            if (url != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Configuring ehcache from ehcache.xml found in "
                            + "the classpath: " + url);
                }
            } else {
                url = getClass().getResource(
                        FAILSAFE_CLASSPATH_CONFIGURATION_FILE);
                if (log.isWarnEnabled()) {
                    log.warn("No configuration found. Configuring ehcache from "
                            + "ehcache -failsafe.xml found in the classpath: "
                            + url);
                }
            }
            manager = CacheManager.create(url);
            referenceCount++;
        }
        catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(e);
        }
    }

    /**
     * Callback to perform any necessary cleanup of the underlying cache implementation
     * during SessionFactory.close().
     */
    public void stop() {
        if (manager != null) {
            if (--referenceCount == 0) {
                manager.shutdown();
            }
            manager = null;
        }
    }

    public boolean isMinimalPutsEnabledByDefault() {
        return false;
    }


}
