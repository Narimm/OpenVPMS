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

package org.openvpms.web.component.im.query;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openvpms.web.component.im.util.DescriptorHelper;


/**
 * A factory for {@link Query} instances. The factory is configured to return
 * specific {@link Query} implementations based on the supplied criteria, with
 * {@link DefaultQuery} returned if no implementation matches.
 * <p/>
 * The factory is configured using a <em>queryfactory.properties</em> file,
 * located in the class path. The file contains pairs of archetype short names
 * and their corresponding query implementations. Short names may be wildcarded
 * e.g:
 * <p/>
 * <table> <tr><td>classification.*</td><td>org.openvpms.web.component.im.query.AutoQuery</td></tr>
 * <tr><td>lookup.*</td><td>org.openvpms.web.component.im.query.AutoQuery</td></tr>
 * <tr><td>party.patient*</td><td>org.openvpms.web.component.im.query.PatientQuery</td></tr>
 * <tr><td>party.organisation*</td>org.openvpms.web.component.im.query.AutoQuery</td></tr>
 * <tr><td>party.supplier*</td>org.openvpms.web.component.im.query.AutoQuery</td></tr>
 * </table>
 * <p/>
 * Multiple <em>queryfactory.properties</em> may be used.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public final class QueryFactory {

    /**
     * Query property
     */
    private static Map<String, Class> _queries;

    /**
     * The logger.
     */
    private static final Log _log = LogFactory.getLog(QueryFactory.class);

    /**
     * Prevent construction.
     */
    private QueryFactory() {
    }

    /**
     * Construct a new {@link Query}. Query implementations must provide at
     * least constructor accepting the following arguments, invoked in the
     * order: <ul> <li>(String refModelName, String entityName, String
     * conceptName)</li> <li>(String[] shortNames)</li> <li>default
     * constructor</li> </ul>
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @return a new query
     */
    public static Query create(String refModelName, String entityName,
                               String conceptName) {
        Query result;
        String[] shortNames = DescriptorHelper.getShortNames(
                refModelName, entityName, conceptName);
        Class clazz = getQueryClass(shortNames);
        try {
            try {
                Constructor constructor = clazz.getConstructor(
                        String.class, String.class, String.class);
                result = (Query) constructor.newInstance(
                        refModelName, entityName, conceptName);
            } catch (NoSuchMethodException exception) {
                result = create(clazz, shortNames);
            }
        } catch (Throwable throwable) {
            _log.error(throwable, throwable);
            result = new DefaultQuery(refModelName, entityName, conceptName);
        }
        return result;
    }

    /**
     * Construct a new {@link Query}. Query implementations must provide at
     * least one constructor accepting the following arguments, invoked in the
     * order: <ul> <li>(String[] shortNames)</li> <li>default constructor</li>
     * </ul>
     *
     * @param shortNames the archetype short names to query on. May contain
     *                   wildcards
     * @return a new query
     */
    public static Query create(String[] shortNames) {
        shortNames = DescriptorHelper.getShortNames(shortNames);
        Class clazz = getQueryClass(shortNames);
        return create(clazz, shortNames);
    }

    /**
     * Constructs a query implementation, using the <em>(String[]
     * shortNames)</em> constructor; or the default constructor if it doesn't
     * exist.
     *
     * @param clazz      the {@link Query} implementation
     * @param shortNames the archerype short names to query on
     * @return a new query implementation
     */
    private static Query create(Class clazz, String[] shortNames) {
        Query result;
        try {
            try {
                Constructor constructor = clazz.getConstructor(String[].class);
                result = (Query) constructor.newInstance((Object[]) shortNames);
            } catch (NoSuchMethodException exception) {
                Constructor constructor = clazz.getConstructor();
                result = (Query) constructor.newInstance();
            }
        } catch (Throwable throwable) {
            _log.error(throwable, throwable);
            result = new DefaultQuery(shortNames);
        }
        return result;
    }

    /**
     * Returns an {@link Query} implementation that can query a set of short
     * names
     *
     * @param shortNames the short names
     * @return a query implemenation that supports <code>shortNames</code> or
     *         <code>DefaultQuery</code> if there is no such implementation
     */
    private static Class getQueryClass(String[] shortNames) {
        Map<String, Class> queries = getQueries();
        Set<String> wildcards = queries.keySet();
        String match = null;
        int bestDotCount = -1; // more dots in a short name, the more specific
        int bestWildCardCount = -1; // less wildcards, the more specific
        for (String wildcard : wildcards) {
            boolean found = true;
            for (String shortName : shortNames) {
                if (!DescriptorHelper.matches(shortName, wildcard)) {
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
        return (match != null) ? queries.get(match) : DefaultQuery.class;
    }

    /**
     * Returns the map of short names to {@link Query} classes, loading them
     * from the <em>queryfactory.properties</em> resources if necessary.
     *
     * @return the map of short names to {@link Query} classes
     */
    private synchronized static Map<String, Class> getQueries() {
        if (_queries == null) {
            _queries = new HashMap<String, Class>();

            Set<URL> paths = getPaths();

            for (URL path : paths) {
                try {
                    Properties properties = new Properties();
                    properties.load(path.openStream());
                    Enumeration names = properties.propertyNames();
                    while (names.hasMoreElements()) {
                        String name = (String) names.nextElement();
                        String className = properties.getProperty(name);
                        if (_queries.get(name) != null) {
                            _log.warn("Duplicate sbort name=" + name
                                      + " from " + path + ": ignoring");
                        } else {
                            Class clazz = getClass(className);
                            if (clazz != null) {
                                _queries.put(name, clazz);
                            }
                        }
                    }
                } catch (IOException exception) {
                    _log.error(exception, exception);
                }
            }
        }
        return _queries;
    }

    /**
     * Returns the paths of the <em>queryfactory.properties</em> resources.
     *
     * @return the paths <em>queryfactory.properties</em> resources.
     */
    private static Set<URL> getPaths() {
        Set<URL> paths = new HashSet<URL>();
        for (ClassLoader loader : getClassLoaders()) {
            if (loader != null) {
                try {
                    Enumeration<URL> urls
                            = loader.getResources("queryfactory.properties");
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
     * @return the class, or <code>null</code> if it can't be found
     */
    private static Class getClass(String name) {
        for (ClassLoader loader : getClassLoaders()) {
            if (loader != null) {
                try {
                    Class clazz = loader.loadClass(name);
                    if (Query.class.isAssignableFrom(clazz)) {
                        return clazz;
                    }
                } catch (ClassNotFoundException ignore) {
                }
            }
        }
        _log.error("Failed to load class: " + name);
        return null;
    }

    /**
     * Returns a list of classloaders to locate for resources with. The list
     * will contain the context class loader and this class' loader, or this
     * class' loader if the context class loader is null or the same.
     *
     * @return a list of classloaders to locate for resources with
     */
    private static ClassLoader[] getClassLoaders() {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        ClassLoader clazz = QueryFactory.class.getClassLoader();
        if (context != null && context != clazz) {
            return new ClassLoader[]{context, clazz};
        }
        return new ClassLoader[]{clazz};
    }
}
