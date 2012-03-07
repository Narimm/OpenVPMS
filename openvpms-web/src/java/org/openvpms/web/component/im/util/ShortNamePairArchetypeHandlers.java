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
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.util.PropertiesReader;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Loads properties resources containing a mapping of short names to the
 * implementation classes that can handle them. Each property file entry is
 * of the form:
 * <code>&lt;primaryShortName&gt;[,secondaryShortName] &lt;className&gt;</code>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ShortNamePairArchetypeHandlers<T>
        extends AbstractArchetypeHandlers<T> {

    /**
     * Map of primary short names to their correspnding handler classes.
     */
    private Map<String, Handlers<T>> handlers
            = new HashMap<String, Handlers<T>>();

    /**
     * The class that each handler must implement/extend.
     */
    private final Class type;

    /**
     * The logger.
     */
    private static final Log log
            = LogFactory.getLog(ShortNamePairArchetypeHandlers.class);


    /**
     * Construct a new <code>ShortNamePairArchetypeHandlers</code>.
     *
     * @param name the resource name
     * @param type class the each handler must implement/extend
     */
    public ShortNamePairArchetypeHandlers(String name, Class type) {
        this.type = type;
        load(name);
    }

    /**
     * Merges handlers from the specified resource.
     *
     * @param name the resource name
     */
    public void load(String name) {
        Reader parser = new Reader();
        parser.read(name);
    }

    /**
     * Returns a class that can handle an archetype.
     *
     * @param shortName the archetype short name
     * @return an implemenation that supports <code>shortName</code> or
     *         <code>null</code> if there is no match
     */
    public ArchetypeHandler<T> getHandler(String shortName) {
        return getHandler(shortName, null);
    }

    /**
     * Returns a handler that can handle a pair of short names.
     *
     * @param primary   the primary archetype short name
     * @param secondary the secondary archetype short name.
     *                  May be <code>null</code>
     * @return an implemenation that supports the short names  or
     *         <code>null</code> if there is no match
     */
    public ArchetypeHandler<T> getHandler(String primary, String secondary) {
        ArchetypeHandler<T> handler = null;
        String match = getShortName(primary, handlers.keySet());
        if (match != null) {
            Handlers<T> handlers = this.handlers.get(match);
            if (secondary != null) {
                match = getShortName(secondary, handlers.getShortNames());
                if (match != null) {
                    handler = handlers.get(match);
                }
            }
            if (handler == null) {
                // no secondary handler so fall back to the primary handler
                handler = handlers.getHandler();
            }
        }
        return handler;
    }

    /**
     * Registers the handlers for a primary short name.
     */
    private static class Handlers<T> {

        /**
         * The global handler.
         */
        private ArchetypeHandler<T> handler;

        /**
         * The handlers, keyed on short name.
         */
        private final Map<String, ArchetypeHandler<T>> handlers
                = new HashMap<String, ArchetypeHandler<T>>();

        /**
         * Sets the primary handler.
         *
         * @param handler the handler
         */
        public void setHandler(ArchetypeHandler<T> handler) {
            this.handler = handler;
        }

        /**
         * Returns the primary handler.
         *
         * @return the handler. May be <code>null</code>
         */
        public ArchetypeHandler<T> getHandler() {
            return handler;
        }

        /**
         * Adds a handler for a secondary short name.
         *
         * @param shortName the secondary short name
         * @param handler   the handler handler
         */
        public void add(String shortName, ArchetypeHandler<T> handler) {
            handlers.put(shortName, handler);
        }

        /**
         * Returns the handler for a secondary short name.
         *
         * @param shortName the secondary short name
         * @return the handler type
         */
        public ArchetypeHandler<T> get(String shortName) {
            return handlers.get(shortName);
        }

        /**
         * Returns the secondary short names for which there are registered
         * handlers.
         *
         * @return the secondary short names
         */
        public Set<String> getShortNames() {
            return handlers.keySet();
        }

    }

    /**
     * Property file parser.
     */
    private class Reader extends PropertiesReader {

        /**
         * Parse a property file entry.
         *
         * @param key   the property key
         * @param value the property value
         * @param path  the path the property came from
         */
        @SuppressWarnings("unchecked")
        protected void parse(String key, String value, String path) {
            Class<T> clazz = (Class<T>) getClass(value, type, path);
            if (clazz != null) {
                String[] pair = key.split(",");
                if (pair.length == 0 || pair.length > 2) {
                    log.error("Invalid short name pair=" + key
                            + ", loaded from path=" + path);
                } else if (pair.length == 1) {
                    addHandler(pair[0], clazz, path);
                } else {
                    addHandler(pair[0], pair[1], clazz, path);
                }
            }
        }

        /**
         * Adds a handler for a primary short name.
         *
         * @param shortName the primary short name
         * @param type      the handler type
         * @param path      the path the handler came from
         */
        private void addHandler(String shortName, Class<T> type, String path) {
            String[] matches = getShortNames(shortName, path);
            if (matches.length != 0) {
                Handlers<T> handlers = getHandlers(shortName);
                if (handlers.getHandler() != null) {
                    log.warn("Duplicate sbort name=" + shortName
                            + " from " + path + ": ignoring");

                } else {
                    ArchetypeHandler<T> handler
                            = new ArchetypeHandler<T>(shortName, type);
                    handlers.setHandler(handler);
                }
            } else {
                log.warn("Invalid archetype for handler=" + type + ", short name=" + shortName + " from "
                         + path + ": ignoring");
            }
        }

        /**
         * Adds a handler for a short name pair.
         *
         * @param primary   the primary short name
         * @param secondary the secondary short name
         * @param type      the handler type
         * @param path      the path the handler came from
         */
        private void addHandler(String primary, String secondary,
                                Class<T> type, String path) {
            String[] primaryMatches = getShortNames(primary, path);
            String[] secondaryMatches = getShortNames(secondary, path);
            if (primaryMatches.length != 0 && secondaryMatches.length != 0) {
                Handlers<T> handlers = getHandlers(primary);
                if (handlers.get(secondary) != null) {
                    log.warn("Duplicate sbort name=" + secondary
                            + " for primary short name=" + primary
                            + " from " + path + ": ignoring");
                } else {
                    ArchetypeHandler<T> handler
                            = new ArchetypeHandler<T>(secondary, type);
                    handlers.add(secondary, handler);
                }
            }
        }

        /**
         * Returns the archetype short names matching a short name.
         *
         * @param shortName the short name
         * @param path      the path the handler came from
         * @return a list of short names matching <code>shprtName</code>
         */
        private String[] getShortNames(String shortName, String path) {
            String[] matches = DescriptorHelper.getShortNames(shortName, false);
            if (matches.length == 0) {
                log.warn("No archetypes found matching short name="
                        + shortName + ", loaded from path=" + path);
            }
            return matches;
        }

        /**
         * Returns the handlers for a short name.
         *
         * @param shortName the short names
         * @return the handlers for <code>shortName</code>
         */
        private Handlers<T> getHandlers(String shortName) {
            Handlers<T> result = handlers.get(shortName);
            if (result == null) {
                result = new Handlers<T>();
                handlers.put(shortName, result);
            }
            return result;
        }
    }
}
