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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
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
import java.util.Set;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class ConfigReader {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ConfigReader.class);


    /**
     * Reads all configuration resources with the specified name.
     * If there are multiple resources with the same name, they will be merged.
     *
     * @param name the configuration name
     */
    public void read(String name) {
        Set<URL> paths = getPaths(name);
        for (URL path : paths) {
            read(path);
        }
    }

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
     * Reads the configuration at the specified path.
     *
     * @param path the path to read
     */
    protected abstract void read(URL path);

    /**
     * Helper to load a class, verifying that it is an instance of the specified
     * type.
     *
     * @param name the class name
     * @param type the expected type
     * @param path the path, for error reporting purposes
     * @return the class, or <code>null</code> if it can't be found
     */
    protected Class getClass(String name, Class type, String path) {
        for (ClassLoader loader : getClassLoaders()) {
            if (loader != null) {
                try {
                    Class clazz = loader.loadClass(name);
                    if (type.isAssignableFrom(clazz)) {
                        return clazz;
                    } else {
                        log.error("Failed to load class: " + name
                                  + ", specified in " + path
                                  + ": does not extend" + type.getName());
                        return null;

                    }
                } catch (ClassNotFoundException ignore) {
                    // no-op
                }
            }
        }
        log.error("Failed to load class: " + name + ", specified in " + path);
        return null;
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
        ClassLoader clazz = PropertiesReader.class.getClassLoader();
        if (context != null && context != clazz) {
            return new ClassLoader[]{context, clazz};
        }
        return new ClassLoader[]{clazz};
    }
}
