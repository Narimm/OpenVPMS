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

package org.openvpms.web.component.im.lookup;

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * An {@link LookupQuery} that queries all lookups for the specified archetype
 * short name.
 *
 * @author Tim Anderson
 */
public class ArchetypeLookupQuery extends AbstractLookupQuery {

    /**
     * The archetype short name.
     */
    private final String shortName;


    /**
     * Constructs an {@link ArchetypeLookupQuery}.
     *
     * @param shortName the archetype short name
     */
    public ArchetypeLookupQuery(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Returns the lookups.
     *
     * @return the lookups
     */
    public List<Lookup> getLookups() {
        Collection<Lookup> lookups = ServiceHelper.getLookupService().getLookups(shortName);
        List<Lookup> result = (lookups instanceof List) ? (List<Lookup>) lookups : new ArrayList<Lookup>(lookups);
        sort(result);
        return result;
    }
}
