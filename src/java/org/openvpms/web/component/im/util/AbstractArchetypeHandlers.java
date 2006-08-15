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
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.Collection;


/**
 * Provides a mapping between archetype short names and the classes that can
 * handle them.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractArchetypeHandlers<T> {

    /**
     * Returns a handler that can handle an archetype.
     *
     * @param shortName the archetype short name
     * @return an implemenation that supports <code>shortName</code> or
     *         <code>null</code> if there is no match
     */
    public abstract ArchetypeHandler<T> getHandler(String shortName);

    /**
     * Finds the most specific wildcarded short name from a collection of
     * wildcarded short names that matches the supplied short name.
     *
     * @param shortName the shortName
     * @param wildcards the wildcarded short names
     * @return the most specific short name, or <code>null</code> if none
     *         can be found
     */
    protected String getShortName(String shortName,
                                  Collection<String> wildcards) {
        return getShortName(new String[]{shortName}, wildcards);
    }

    /**
     * Finds the most specific wildcarded short name from a collection of
     * wildcarded short names that matches the supplied short names.
     *
     * @param shortNames the short names
     * @param wildcards  the wildcarded short names
     * @return the most specific short name, or <code>null</code> if none
     *         can be found
     */
    protected String getShortName(String[] shortNames,
                                  Collection<String> wildcards) {
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
        return match;
    }

}
