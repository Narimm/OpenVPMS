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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.load;


/**
 * Provides a singleton instance of a {@link LookupNameCache}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupNameCacheHelper {

    /**
     * A reference to the lookup cache.
     */
    private static LookupNameCache cache;


    /**
     * Registers the singleton cache.
     *
     * @param cache the cache
     */
    public LookupNameCacheHelper(LookupNameCache cache) {
        LookupNameCacheHelper.cache = cache;
    }

    /**
     * Returns the singleton cache.
     *
     * @return the cache, or <tt>null</tt> if none is registered
     */
    public static LookupNameCache getCache() {
        return cache;
    }
}
