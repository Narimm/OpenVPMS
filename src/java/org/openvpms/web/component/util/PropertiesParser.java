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

package org.openvpms.web.component.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


/**
 * Property file parser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class PropertiesParser {

    /**
     * The path of the property file being parsed.
     */
    private URL path;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PropertiesParser.class);


    /**
     * Parse all property files from the classpath matching the specified
     * name.
     *
     * @param name the property file name
     */
    public void parse(String name) {
        Set<URL> paths = getPaths(name);

        for (URL path : paths) {
            try {
                Properties properties = new Properties();
                properties.load(path.openStream());
                this.path = path;
                Enumeration keys = properties.propertyNames();
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    String value = properties.getProperty(key).trim();
                    // trim required as Properties doesn't seem to remove
                    // trailing whitespace
                    parse(key, value);
                }
            } catch (IOException exception) {
                log.error(exception, exception);
            }
        }
    }

    /**
     * Returns the path of the property file being parsed.
     *
     * @return the property file URL
     */
    protected URL getPath() {
        return path;
    }

    /**
     * Parse a property file entry.
     *
     * @param key   the property key
     * @param value the property value
     */
    protected abstract void parse(String key, String value);

    /**
     * Returns all resources with the given name.
     *
     * @return the paths to resource with name <code>name</code>
     */
    protected Set<URL> getPaths(String name) {
        Set<URL> paths = new HashSet<URL>();
        for (ClassLoader loader : getClassLoaders()) {
            if (loader != null) {
                try {
                    Enumeration<URL> urls = loader.getResources(name);
                    while (urls.hasMoreElements()) {
                        paths.add(urls.nextElement());
                    }
                } catch (IOException exception) {
                    log.error(exception, exception);
                }
            }
        }
        return paths;
    }

    /**
     * Returns a list of classloaders to locate for resources with. The list
     * will contain the context class loader and this class' loader, or this
     * class' loader if the context class loader is null or the same.
     *
     * @return a list of classloaders to locate for resources with
     */
    protected ClassLoader[] getClassLoaders() {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        ClassLoader clazz = PropertiesParser.class.getClassLoader();
        if (context != null && context != clazz) {
            return new ClassLoader[]{context, clazz};
        }
        return new ClassLoader[]{clazz};
    }
}
