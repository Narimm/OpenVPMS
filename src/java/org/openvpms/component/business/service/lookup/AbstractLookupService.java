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

package org.openvpms.component.business.service.lookup;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;

import java.util.Collection;
import java.util.List;


/**
 * Abstract implementation of the {@link ILookupService}, that
 * delegates to the {@link IArchetypeService}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractLookupService implements ILookupService {

    /**
     * The archetype service
     */
    private final IArchetypeService service;


    /**
     * Constructs a new <tt>AbstractLookupService</tt>.
     *
     * @param service the archetype service
     */
    public AbstractLookupService(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Returns the lookup with the specified lookup archetype short name and
     * code.
     *
     * @param shortName the lookup archetype short name
     * @param code      the lookup code
     * @return the corresponding lookup or <tt>null</tt> if none is found
     */
    protected Lookup query(String shortName, String code) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true);
        query.add(new NodeConstraint("code", code)); // NON-NLS
        query.setMaxResults(1);
        List<IMObject> results = service.get(query).getResults();
        return (!results.isEmpty()) ? (Lookup) results.get(0) : null;
    }

    /**
     * Returns all lookups with the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name
     * @return a collection of lookups with the specified short name
     */
    @SuppressWarnings("unchecked")
    protected Collection<Lookup> query(String shortName) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        List results = service.get(query).getResults();
        return (List<Lookup>) results;
    }


}
