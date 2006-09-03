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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Loads properties resources containing a mapping of short names to the
 * implementation classes that can handle them.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ArchetypeHandlers<T> extends AbstractArchetypeHandlers<T> {


    /**
     * The logger.
     */
    private static final Log _log
            = LogFactory.getLog(ArchetypeHandlers.class);

    /**
     * Map of short names to their corresponding handlers.
     */
    private Map<String, ArchetypeHandler<T>> _handlers
            = new HashMap<String, ArchetypeHandler<T>>();


    /**
     * Construct a new <code>ArchetypeHandlers</code>.
     *
     * @param name the resource name
     * @param type class the each handler must implement/extend
     */
    public ArchetypeHandlers(String name, Class<T> type) {
        Parser parser = new Parser(type);
        parser.parse(name);
    }

    /**
     * Returns a handler that can handle an archetype.
     *
     * @param shortName the archetype short name
     * @return an implemenation that supports <code>shortName</code> or
     *         <code>null</code> if there is no match
     */
    @Override
    public ArchetypeHandler<T> getHandler(String shortName) {
        return getHandler(new String[]{shortName});
    }

    /**
     * Returns a handler that can handle a set of archetypes.
     *
     * @param shortNames the archetype short names
     * @return a handler that supports <code>shortNames</code> or
     *         <code>null</code> if there is no match
     */
    public ArchetypeHandler<T> getHandler(String[] shortNames) {
        ArchetypeHandler<T> result = null;
        Set<String> wildcards = _handlers.keySet();

        // generate a map of matching wildcards, keyed on short name
        Map<String, String> matches = new HashMap<String, String>();
        for (String wildcard : wildcards) {
            for (String shortName : shortNames) {
                if (TypeHelper.matches(shortName, wildcard)) {
                    String match = matches.get(shortName);
                    if (match == null) {
                        matches.put(shortName, wildcard);
                    } else {
                        if (moreSpecific(wildcard, match)) {
                            matches.put(shortName, wildcard);
                        }
                    }
                    matches.put(shortName, wildcard);
                }
            }
        }
        if (matches.size() == shortNames.length) {
            // found a match for each short name. Make sure the implementation
            // class is the same, with the same configuration
            for (String match : matches.values()) {
                ArchetypeHandler<T> handler = _handlers.get(match);
                if (result == null) {
                    result = handler;
                } else if (!result.getType().equals(handler.getType())) {
                    result = null;
                    break;
                } else if (!ObjectUtils.equals(result.getProperties(),
                                               handler.getProperties())) {
                    result = null;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Determines if one short name is more specific than another.
     * A short name is more specific than another if it has:
     * <ul>
     * <li>more dots</li>
     * <li>the same no of dots, but fewer wildcards</li>
     * </ul>
     *
     * @param shortName1 the first short name
     * @param shortName2 the second short name
     * @return <code>true</code> if shortName1 is more specific than shortName2
     */
    private boolean moreSpecific(String shortName1, String shortName2) {
        boolean result = false;
        int dotCount1 = StringUtils.countMatches(shortName1, ".");
        int wildcardCount1 = StringUtils.countMatches(shortName1, "*");
        int dotCount2 = StringUtils.countMatches(shortName2, ".");
        int wildCardCount2 = StringUtils.countMatches(shortName2, "*");
        if (dotCount1 > dotCount2 ||
                (dotCount1 == dotCount2 && wildcardCount1 < wildCardCount2)) {
            result = true;
        }
        return result;
    }

    class Parser extends ArchetypePropertiesParser {

        /**
         * Constructs a new <code>Parser</code>.
         *
         * @param type the type all handler classes must implement
         */
        public Parser(Class<T> type) {
            super(type);
        }

        /**
         * Parse a property file entry.
         *
         * @param key   the property key
         * @param value the property value
         */
        @SuppressWarnings("unchecked")
        protected void parse(String key, String value) {
            String[] matches = DescriptorHelper.getShortNames(key, false);
            if (matches.length == 0) {
                _log.warn("No archetypes found matching short name=" + key
                        + ", loaded from path=" + getPath());
            } else {
                if (_handlers.get(key) != null) {
                    _log.warn("Duplicate sbort name=" + key
                            + " from " + getPath() + ": ignoring");
                } else {
                    String[] properties = value.split(",");
                    if (properties.length == 0) {
                        _log.warn("Invalid properties for short name=" + key
                                + ", loaded from path=" + getPath());
                    }
                    Class<T> clazz = (Class<T>) getClass(properties[0]);
                    Map<String, String> config = new HashMap<String, String>();
                    for (int i = 1; i < properties.length; ++i) {
                        String[] pair = properties[i].split("=");
                        config.put(pair[0], pair[1]);
                    }
                    if (clazz != null) {
                        _handlers.put(key,
                                      new ArchetypeHandler<T>(clazz, config));
                    }
                }
            }
        }
    }

}
