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
    private Map<String, Handlers<T>> _handlers
            = new HashMap<String, Handlers<T>>();

    /**
     * The logger.
     */
    private static final Log _log
            = LogFactory.getLog(ShortNamePairArchetypeHandlers.class);


    /**
     * Construct a new <code>ShortNamePairArchetypeHandlers</code>.
     *
     * @param name the resource name
     * @param type class the each handler must implement/extend
     */
    public ShortNamePairArchetypeHandlers(String name, Class type) {
        Parser parser = new Parser(type);
        parser.parse(name);
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
        String match = getShortName(primary, _handlers.keySet());
        if (match != null) {
            Handlers<T> handlers = _handlers.get(match);
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
        private ArchetypeHandler<T> _handler;

        /**
         * The handlers, keyed on short name.
         */
        private final Map<String, ArchetypeHandler<T>> _handlers
                = new HashMap<String, ArchetypeHandler<T>>();

        /**
         * Sets the primary handler.
         *
         * @param handler the handler
         */
        public void setHandler(ArchetypeHandler<T> handler) {
            _handler = handler;
        }

        /**
         * Returns the primary handler.
         *
         * @return the handler. May be <code>null</code>
         */
        public ArchetypeHandler<T> getHandler() {
            return _handler;
        }

        /**
         * Adds a handler for a secondary short name.
         *
         * @param shortName the secondary short name
         * @param handler   the handler handler
         */
        public void add(String shortName, ArchetypeHandler<T> handler) {
            _handlers.put(shortName, handler);
        }

        /**
         * Returns the handler for a secondary short name.
         *
         * @param shortName the secondary short name
         * @return the handler type
         */
        public ArchetypeHandler<T> get(String shortName) {
            return _handlers.get(shortName);
        }

        /**
         * Returns the secondary short names for which there are registered
         * handlers.
         *
         * @return the secondary short names
         */
        public Set<String> getShortNames() {
            return _handlers.keySet();
        }

    }

    /**
     * Property file parser.
     */
    private class Parser extends ArchetypePropertiesParser {

        /**
         * Constructs a new <code>Parser</code>.
         *
         * @param type the type all handler classes must implement
         */
        public Parser(Class type) {
            super(type);
        }

        /**
         * Parse a property file entry.
         *
         * @param key   the property key
         * @param value the property value
         */
        protected void parse(String key, String value) {
            Class<T> clazz = (Class<T>) getClass(value);
            if (clazz != null) {
                String[] pair = key.split(",");
                if (pair.length == 0 || pair.length > 2) {
                    _log.error("Invalid short name pair=" + key
                            + ", loaded from path=" + getPath());
                } else if (pair.length == 1) {
                    addHandler(pair[0], clazz);
                } else {
                    addHandler(pair[0], pair[1], clazz);
                }
            }
        }

        /**
         * Adds a handler for a primary short name.
         *
         * @param shortName the primary short name
         * @param type      the handler type
         */
        private void addHandler(String shortName, Class<T> type) {
            String[] matches = getShortNames(shortName);
            if (matches.length != 0) {
                Handlers<T> handlers = getHandlers(shortName);
                if (handlers.getHandler() != null) {
                    _log.warn("Duplicate sbort name=" + shortName
                            + " from " + getPath() + ": ignoring");

                } else {
                    ArchetypeHandler<T> handler = new ArchetypeHandler<T>(type);
                    handlers.setHandler(handler);
                }
            }
        }

        /**
         * Adds a handler for a short name pair.
         *
         * @param primary   the primary short name
         * @param secondary the secondary short name
         * @param type      the handler type
         */
        private void addHandler(String primary, String secondary,
                                Class<T> type) {
            String[] primaryMatches = getShortNames(primary);
            String[] secondaryMatches = getShortNames(secondary);
            if (primaryMatches.length != 0 && secondaryMatches.length != 0) {
                Handlers<T> handlers = getHandlers(primary);
                if (handlers.get(secondary) != null) {
                    _log.warn("Duplicate sbort name=" + secondary
                            + " for primary short name=" + primary
                            + " from " + getPath() + ": ignoring");
                } else {
                    handlers.add(secondary, new ArchetypeHandler<T>(type));
                }
            }
        }

        /**
         * Returns the archetype short names matching a short name.
         *
         * @param shortName the short name
         * @return a list of short names matching <code>shprtName</code>
         */
        private String[] getShortNames(String shortName) {
            String[] matches = DescriptorHelper.getShortNames(shortName, false);
            if (matches.length == 0) {
                _log.warn("No archetypes found matching short name="
                        + shortName + ", loaded from path=" + getPath());
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
            Handlers<T> handlers = _handlers.get(shortName);
            if (handlers == null) {
                handlers = new Handlers<T>();
                _handlers.put(shortName, handlers);
            }
            return handlers;
        }
    }
}
