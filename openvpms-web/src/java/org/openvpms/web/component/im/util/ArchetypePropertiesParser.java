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

package org.openvpms.web.component.im.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


/**
 * Property file parser for archetype handlers.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see ArchetypeHandlers
 * @see ShortNamePairArchetypeHandlers
 */
public abstract class ArchetypePropertiesParser {

    /**
     * The type all handler classes must implement.
     */
    private Class _type;

    /**
     * The path of the property file being parsed.
     */
    private URL _path;

    /**
     * The logger.
     */
    private static final Log _log
            = LogFactory.getLog(ArchetypePropertiesParser.class);

    /**
     * Constructs a new <code>ArchetypePropertiesParser</code>.
     *
     * @param type the type all handler classes must implement
     */
    public ArchetypePropertiesParser(Class type) {
        _type = type;
    }

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
                _path = path;
                Enumeration keys = properties.propertyNames();
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    String value = properties.getProperty(key).trim();
                    // trim required as Properties doesn't seem to remove
                    // trailing whitespace
                    parse(key, value);
                }
            } catch (IOException exception) {
                _log.error(exception, exception);
            }
        }
    }

    /**
     * Returns the handler class type.
     *
     * @return the handler class type
     */
    protected Class getType() {
        return _type;
    }

    /**
     * Returns the path of the property file being parsed.
     *
     * @return the property file URL
     */
    protected URL getPath() {
        return _path;
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
                    _log.error(exception, exception);
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
        ClassLoader clazz = ArchetypeHandlers.class.getClassLoader();
        if (context != null && context != clazz) {
            return new ClassLoader[]{context, clazz};
        }
        return new ClassLoader[]{clazz};
    }

    /**
     * Loads a class.
     *
     * @param name the class name
     * @return the class, or <code>null</code> if it can't be found
     */
    protected Class getClass(String name) {
        for (ClassLoader loader : getClassLoaders()) {
            if (loader != null) {
                try {
                    Class clazz = loader.loadClass(name);
                    if (_type.isAssignableFrom(clazz)) {
                        return clazz;
                    } else {
                        _log.error("Failed to load class: " + name
                                + ", specified in " + _path
                                + ": does not extend" + _type.getName());
                        return null;

                    }
                } catch (ClassNotFoundException ignore) {
                    // no-op
                }
            }
        }
        _log.error("Failed to load class: " + name + ", specified in " + _path);
        return null;
    }
}
