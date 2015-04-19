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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.archetype;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Loads properties resources containing a mapping of short names to the
 * implementation classes that can handle them.
 *
 * @author Tim Anderson
 */
public class ArchetypeHandlers<T> extends AbstractArchetypeHandlers<T> {

    /**
     * The type that each handler must implement/extend.
     */
    private final Class<T> type;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ArchetypeHandlers.class);

    /**
     * Map of short names to their corresponding handlers.
     */
    private Map<String, ArchetypeHandler<T>> handlers = new HashMap<String, ArchetypeHandler<T>>();

    /**
     * Map of handlers not associated with any short name. These
     * can only be returned by class name.
     */
    private Map<String, ArchetypeHandler<T>> anonymousHandlers = new HashMap<String, ArchetypeHandler<T>>();


    /**
     * Construct an {@link ArchetypeHandlers}.
     *
     * @param name the resource name
     * @param type class the each handler must implement/extend
     */
    public ArchetypeHandlers(String name, Class<T> type) {
        this(name, null, type);
    }

    /**
     * Construct an {@link ArchetypeHandlers}.
     *
     * @param name         the resource name
     * @param fallbackName the fallback resource name. May be {@code null}
     * @param type         class the each handler must implement/extend
     */
    public ArchetypeHandlers(String name, String fallbackName, Class<T> type) {
        this.type = type;
        if (fallbackName != null) {
            read(fallbackName, false);
        }
        read(name, true);
    }

    /**
     * Returns a handler that can handle an archetype.
     *
     * @param shortName the archetype short name
     * @return an implementation that supports {@code shortName} or {@code null} if there is no match
     */
    @Override
    public ArchetypeHandler<T> getHandler(String shortName) {
        return getHandler(new String[]{shortName});
    }

    /**
     * Returns a handler that can handle a set of archetypes.
     *
     * @param shortNames the archetype short names
     * @return a handler that supports {@code shortNames} or {@code null} if there is no match
     */
    public ArchetypeHandler<T> getHandler(String[] shortNames) {
        ArchetypeHandler<T> result = null;
        Set<String> wildcards = handlers.keySet();

        // generate a map of matching wildcards, keyed on short name
        Map<String, String> matches = new HashMap<String, String>();
        for (String wildcard : wildcards) {
            for (String shortName : shortNames) {
                if (TypeHelper.matches(shortName, wildcard)) {
                    String match = matches.get(shortName);
                    if (match == null) {
                        matches.put(shortName, wildcard);
                    } else if (moreSpecific(wildcard, match)) {
                        matches.put(shortName, wildcard);
                    }
                }
            }
        }
        if (matches.size() == shortNames.length) {
            // found a match for each short name. Make sure the implementation
            // class is the same, with the same configuration
            for (String match : matches.values()) {
                ArchetypeHandler<T> handler = handlers.get(match);
                if (result == null) {
                    result = handler;
                } else if (!result.getType().equals(handler.getType())) {
                    result = null;
                    break;
                } else if (!ObjectUtils.equals(result.getProperties(), handler.getProperties())) {
                    result = null;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns an anonymous handler.
     *
     * @param type the implementation class type
     */
    public ArchetypeHandler<T> getHandler(Class<T> type) {
        return anonymousHandlers.get(type.getName());
    }

    /**
     * Reads a handler configuration resource.
     *
     * @param name    the resource name
     * @param replace if {@code true}, replace any existing handler
     */
    private void read(String name, boolean replace) {
        if (name.endsWith(".properties")) {
            readProperties(name, replace);
        } else if (name.endsWith(".xml")) {
            readXML(name, replace);
        } else {
            readProperties(name + ".properties", replace);
            readXML(name + ".xml", replace);
        }
    }

    /**
     * Determines if one short name is more specific than another.
     * A short name is more specific than another if resolves fewer archetypes.
     *
     * @param shortName1 the first short name
     * @param shortName2 the second short name
     * @return {@code true} if shortName1 is more specific than shortName2
     */
    private boolean moreSpecific(String shortName1, String shortName2) {
        String[] matches1 = DescriptorHelper.getShortNames(shortName1);
        String[] matches2 = DescriptorHelper.getShortNames(shortName2);
        return matches1.length < matches2.length;
    }

    /**
     * Registers a handler for a particular short name.
     *
     * @param shortName  the archetype short name. May be {@code null}
     * @param type       the implementation class
     * @param properties the configuration properties. May be {@code null}
     * @param path       the resource path
     * @param replace    if {@code true} the handler can replace any existing handler
     */
    private void addHandler(String shortName, Class<T> type, Map<String, Object> properties, String path,
                            boolean replace) {
        if (!StringUtils.isEmpty(shortName)) {
            String[] matches = DescriptorHelper.getShortNames(shortName, false);
            if (matches.length == 0) {
                log.warn("No archetypes found matching short name=" + shortName + ", loaded from path=" + path);
            } else {
                if (!replace && handlers.get(shortName) != null) {
                    log.warn("Duplicate sbort name=" + shortName + " from " + path + ": ignoring");
                } else {
                    ArchetypeHandler<T> handler = new ArchetypeHandler<T>(shortName, type, properties);
                    handlers.put(shortName, handler);
                }
            }
        } else {
            // got an anonymous handler
            String name = type.getName();
            if (!replace && anonymousHandlers.get(name) != null) {
                log.warn("Duplicate anonymous handler=" + name
                         + " from " + path + ": ignoring");
            } else {
                ArchetypeHandler<T> handler = new ArchetypeHandler<T>(null, type, properties);
                anonymousHandlers.put(name, handler);
            }
        }
    }

    /**
     * Loads all XML resources with the specified name.
     *
     * @param name    the resource name
     * @param replace if {@code true}, replace any existing handler
     */
    private void readXML(String name, boolean replace) {
        XMLConfigReader reader = new XMLConfigReader(replace);
        reader.read(name);
    }

    /**
     * Loads all property resources with the specified name.
     *
     * @param name    the resource name
     * @param replace if {@code true}, replace any existing handler
     */
    private void readProperties(String name, boolean replace) {
        Reader parser = new Reader(replace);
        parser.read(name);
    }

    /**
     * Reads handlers from a .properties file.
     */
    private class Reader extends PropertiesReader {

        /**
         * Determines if existing handlers should be replaced.
         */
        private final boolean replace;

        /**
         * Constructs a {@link Reader}.
         *
         * @param replace if {@code true}, replace any existing handler
         */
        public Reader(boolean replace) {
            this.replace = replace;
        }

        /**
         * Parse a property file entry.
         *
         * @param key   the property key
         * @param value the property value
         * @param path  the path the property came from
         */
        @SuppressWarnings("unchecked")
        protected void parse(String key, String value, String path) {
            String[] properties = value.split(",");
            if (properties.length == 0) {
                log.warn("Invalid properties for short name=" + key + ", loaded from path=" + path);
            } else {
                Class<T> clazz = (Class<T>) getClass(properties[0], type, path);
                if (clazz != null) {
                    Map<String, Object> config = new HashMap<String, Object>();
                    for (int i = 1; i < properties.length; ++i) {
                        String[] pair = properties[i].split("=");
                        config.put(pair[0], pair[1]);
                    }
                    addHandler(key, clazz, config, path, replace);
                }
            }
        }
    }

    /**
     * Reads handlers from XML using {@code XStream}.
     */
    private class XMLConfigReader extends ConfigReader {

        /**
         * Determines if existing handlers should be replaced.
         */
        private final boolean replace;

        /**
         * The stream.
         */
        private XStream stream;

        public XMLConfigReader(boolean replace) {
            this.replace = replace;
            stream = new XStream();
            stream.alias("handler", Handler.class);
            stream.alias("handlers", Handlers.class);
            stream.addImplicitCollection(Handlers.class, "handlers");
        }

        /**
         * Reads the configuration at the specified path.
         *
         * @param path the path to read
         */
        @SuppressWarnings("unchecked")
        protected void read(URL path) {
            try {
                Handlers handlers = (Handlers) stream.fromXML(path.openStream());
                for (Handler handler : handlers) {
                    Class<T> clazz = (Class<T>) getClass(handler.getType(), type, path.toString());
                    if (clazz != null) {
                        addHandler(handler.getShortName(), clazz, handler.getProperties(), path.toString(), replace);
                    }
                }
            } catch (Throwable exception) {
                log.error(exception, exception);
            }
        }
    }

    /**
     * Helper to deserialize handlers from XML.
     */
    public static class Handlers implements Iterable<Handler> {

        /**
         * The handlers.
         */
        private List<Handler> handlers = new ArrayList<Handler>();

        /**
         * Adds a handler.
         *
         * @param handler the handler to add
         */
        public void add(Handler handler) {
            handlers.add(handler);
        }

        /**
         * Returns an iterator over a set of elements of type T.
         *
         * @return an Iterator.
         */
        public Iterator<Handler> iterator() {
            return handlers.iterator();
        }
    }

    /**
     * Helper to deserialize a handler from XML.
     */
    public static class Handler {

        private String shortName;
        private String type;
        private Map<String, Object> properties;

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }
    }

}
