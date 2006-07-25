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
public class ArchetypeHandlers extends AbstractArchetypeHandlers {


    /**
     * The logger.
     */
    private static final Log _log
            = LogFactory.getLog(ArchetypeHandlers.class);

    /**
     * Map of short names to their correspnding handler class.
     */
    private Map<String, Class> _handlers = new HashMap<String, Class>();


    /**
     * Construct a new <code>ArchetypeHandlers</code>.
     *
     * @param name the resource name
     * @param type class the each handler must implement/extend
     */
    public ArchetypeHandlers(String name, Class type) {
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
    @Override
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

    class Parser extends ArchetypePropertiesParser {

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
            String[] matches = DescriptorHelper.getShortNames(key, false);
            if (matches.length == 0) {
                _log.warn("No archetypes found matching short name=" + key
                        + ", loaded from path=" + getPath());
            } else {
                if (_handlers.get(key) != null) {
                    _log.warn("Duplicate sbort name=" + key
                            + " from " + getPath() + ": ignoring");
                } else {
                    Class clazz = getClass(value);
                    if (clazz != null) {
                        _handlers.put(key, clazz);
                    }
                }
            }
        }
    }

}
