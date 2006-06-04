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

import org.openvpms.archetype.util.TypeHelper;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


/**
 * Loads properties resources containing a mapping of short names to the
 * implementation classes that can handle them.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeHandlers {

    /**
     * Map of short names to their correspnding handler class.
     */
    private Map<String, Class> _handlers = new HashMap<String, Class>();

    /**
     * The logger.
     */
    private static final Log _log
            = LogFactory.getLog(ArchetypeHandlers.class);


    /**
     * Construct a new <code>ArchetypeHandlers</code>.
     *
     * @param name the resource name
     * @param type class the each handler must implement/extend
     */
    public ArchetypeHandlers(String name, Class type) {
        Set<URL> paths = getPaths(name);

        for (URL path : paths) {
            try {
                Properties properties = new Properties();
                properties.load(path.openStream());
                Enumeration shortNames = properties.propertyNames();
                while (shortNames.hasMoreElements()) {
                    String shortName = (String) shortNames.nextElement();
                    String className = properties.getProperty(shortName);
                    String[] matches = DescriptorHelper.getShortNames(
                            shortName, false);
                    if (matches.length == 0) {
                        _log.warn("No archetypes found matching short name="
                                + shortName + ", loaded from path=" + path);
                    } else {
                        if (_handlers.get(shortName) != null) {
                            _log.warn("Duplicate sbort name=" + shortName
                                    + " from " + path + ": ignoring");
                        } else {
                            Class clazz = getClass(className, type, path);
                            if (clazz != null) {
                                _handlers.put(shortName, clazz);
                            }
                        }
                    }
                }
            } catch (IOException exception) {
                _log.error(exception, exception);
            }
        }
    }

    /**
     * Returns a class that can handle an archetype.
     *
     * @param shortName the archetype short name
     */
    public Class getHandler(String shortName) {
        return getHandler(new String[]{shortName});
    }

    /**
     * Returns a class that can handle a set of archetypes.
     *
     * @param shortNames the archetype short names
     * @return an implemenation that supports <code>shortNames</code> or
     *         <code>null</code> if there is no match
     */
    public Class getHandler(String[] shortNames) {
        Set<String> wildcards = _handlers.keySet();
        String match = null;
        int bestDotCount = -1; // more dots in a short name, the more specific
        int bestWildCardCount = -1; // less wildcards, the more specific
        for (String wildcard : wildcards) {
            boolean found = true;
            for (String shortName : shortNames) {
                if (!TypeHelper.matches(shortName, wildcard)) {
                    found = false;
                    break;
                }
            }
            if (found) {
                if (match == null) {
                    match = wildcard;
                    bestDotCount = StringUtils.countMatches(wildcard, ".");
                    bestWildCardCount = StringUtils.countMatches(wildcard, "*");
                } else {
                    int dotCount = StringUtils.countMatches(wildcard, ".");
                    int wildcardCount = StringUtils.countMatches(wildcard, "*");
                    if (dotCount > bestDotCount ||
                            (dotCount == bestDotCount
                                    && wildcardCount < bestWildCardCount)) {
                        match = wildcard;
                    }
                }
            }
        }
        return (match != null) ? _handlers.get(match) : null;
    }

    /**
     * Returns all resources with the given name.
     *
     * @return the paths to resource with name <code>name</code>
     */
    private Set<URL> getPaths(String name) {
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
     * Loads a class.
     *
     * @param name the class name
     * @param type the type that the class must implement/extend
     * @param path the properties resource path where the class was specified
     * @return the class, or <code>null</code> if it can't be found
     */
    private Class getClass(String name, Class type, URL path) {
        for (ClassLoader loader : getClassLoaders()) {
            if (loader != null) {
                try {
                    Class clazz = loader.loadClass(name);
                    if (type.isAssignableFrom(clazz)) {
                        return clazz;
                    } else {
                        _log.error("Failed to load class: " + name
                                + ", specified in " + path
                                + ": does not extend" + type.getName());
                        return null;

                    }
                } catch (ClassNotFoundException ignore) {
                    // no-op
                }
            }
        }
        _log.error("Failed to load class: " + name + ", specified in " + path);
        return null;
    }

    /**
     * Returns a list of classloaders to locate for resources with. The list
     * will contain the context class loader and this class' loader, or this
     * class' loader if the context class loader is null or the same.
     *
     * @return a list of classloaders to locate for resources with
     */
    private ClassLoader[] getClassLoaders() {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        ClassLoader clazz = ArchetypeHandlers.class.getClassLoader();
        if (context != null && context != clazz) {
            return new ClassLoader[]{context, clazz};
        }
        return new ClassLoader[]{clazz};
    }

}
