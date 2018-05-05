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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.user;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.jxpath.Functions;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.util.Collections;
import java.util.Map;

/**
 * An implementation of {@link UserFunctions} that caches user names to improve performance.
 *
 * @author Tim Anderson
 */
public class CachingUserFunctions extends UserFunctions {

    /**
     * The cache of formatted names.
     */
    private final Map<String, String> cache;

    /**
     * Constructs a {@link CachingUserFunctions}.
     *
     * @param service         the archetype service
     * @param practiceService the practice service
     * @param lookups         the lookup service
     * @param functions       functions that may be invoked by the expressions
     * @param cacheSize       the cache size
     */
    public CachingUserFunctions(IArchetypeService service, PracticeService practiceService, ILookupService lookups,
                                Functions functions, int cacheSize) {
        super(service, practiceService, lookups, functions);
        cache = Collections.synchronizedMap(new LRUMap<String, String>(cacheSize));
    }

    /**
     * Formats the name of a user, according to the specified style.
     *
     * @param user  the user. May be {@code null}
     * @param style the style. One of 'short', 'medium' or 'long'
     * @return the formatted name, or {@code null} if no user is specified
     */
    @Override
    public String format(User user, String style) {
        String result = null;
        if (user != null) {
            String key = getKey(user.getId(), style);
            result = cache.get(key);
            if (result == null) {
                result = super.format(user, style);
                cache.put(key, result);
            }
        }
        return result;
    }

    /**
     * Formats the name of a user, according to the specified style.
     *
     * @param id    the user id
     * @param style the style. One of 'short', 'medium' or 'long'
     * @return the formatted name, or {@code null} if no user can be found with the id
     */
    @Override
    public String formatById(long id, String style) {
        String key = getKey(id, style);
        String result = cache.get(key);
        if (result == null) {
            result = super.formatById(id, style);
            cache.put(key, result);
        }
        return result;
    }

    /**
     * Generates a key for a formatted user name.
     *
     * @param id    the user identifier
     * @param style the format style
     * @return a concatenation of the id and style
     */
    private String getKey(long id, String style) {
        return id + style;
    }
}
