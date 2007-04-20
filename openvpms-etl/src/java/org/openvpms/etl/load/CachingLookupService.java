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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.resources.Messages;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.lookup.AbstractLookupService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A lookup service that caches lookups.
 * Note that if a lookup is modified, it needs to be explicitly added to the
 * cache. This could be improved by adding a post-invocation rule on the
 * archetype service for the {@link IArchetypeService#save} methods to
 * update the cache automatically.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CachingLookupService extends AbstractLookupService {

    /**
     * A map of lookup short names to their corresponding codes and lookups.
     */
    private final Map<String, Map<String, Lookup>> lookupsByArchetype
            = new HashMap<String, Map<String, Lookup>>();

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(CachingLookupService.class);

    /**
     * Logging messages.
     */
    private static final Messages messages
            = Messages.getMessages("messages.properties"); // NON-NLS


    /**
     * Constructs a new <tt>CachingLookupService</tt>.
     *
     * @param service the archetype service
     */
    public CachingLookupService(IArchetypeService service) {
        super(service);
    }

    /**
     * Returns the lookup with the specified lookup archetype short name and
     * code.
     *
     * @param shortName the lookup archetype short name. May contain wildcards
     * @param code      the lookup code
     * @return the corresponding lookup or <tt>null</tt> if none is found
     */
    public Lookup getLookup(String shortName, String code) {
        String[] shortNames = getShortNames(shortName);
        for (String name : shortNames) {
            Map<String, Lookup> lookups = get(name);
            Lookup lookup = lookups.get(code);
            if (lookup != null) {
                return lookup;
            }
        }
        return null;
    }

    /**
     * Returns all lookups with the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name. May contain wildcards
     * @return a collection of lookups with the specified short name
     */
    @SuppressWarnings("unchecked")
    public Collection<Lookup> getLookups(String shortName) {
        String[] shortNames = getShortNames(shortName);
        List<Lookup> result = new ArrayList<Lookup>();
        for (String name : shortNames) {
            Map<String, Lookup> lookups = get(name);
            result.addAll(lookups.values());
        }
        return result;
    }

    /**
     * Returns the default lookup for the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name. May contain wildcards
     * @return the default lookup, or <Tt>null</tt> if none is found
     */
    public Lookup getDefaultLookup(String shortName) {
        String[] shortNames = getShortNames(shortName);
        for (String name : shortNames) {
            for (Lookup lookup : getLookups(name)) {
                if (lookup.isDefaultLookup()) {
                    return lookup;
                }
            }
        }
        return null;
    }

    /**
     * Adds a lookup to the cache. If the lookup already exists, it will be
     * replaced.
     *
     * @param lookup the lookup to add
     */
    public synchronized void add(Lookup lookup) {
        String shortName = lookup.getArchetypeId().getShortName();
        Map<String, Lookup> lookups = get(shortName);
        lookups.put(lookup.getCode(), lookup);
    }

    /**
     * Returns all lookups for the specified short name, keyed on lookup code.
     *
     * @param shortName the lookup short name
     * @return the lookups, keyed on code.
     */
    private synchronized Map<String, Lookup> get(String shortName) {
        Map<String, Lookup> lookups = lookupsByArchetype.get(shortName);
        if (lookups == null) {
            lookups = new HashMap<String, Lookup>();
            Collection<Lookup> list = query(shortName);
            for (Lookup lookup : list) {
                String code = lookup.getCode();
                if (lookups.containsKey(code)) {
                    log.warn(messages.getMessage("DuplicateLookup",
                                                 new Object[]{code, shortName}));
                } else {
                    lookups.put(code, lookup);
                }
            }
            lookupsByArchetype.put(shortName, lookups);
        }
        return lookups;
    }

    /**
     * Expands a wildcarded short name.
     *
     * @param shortName the short name
     * @return all short names matching the wildcarded short name
     */
    private String[] getShortNames(String shortName) {
        if (shortName.contains("*")) {
            return DescriptorHelper.getShortNames(shortName, getService());
        }
        return new String[]{shortName};
    }

}
