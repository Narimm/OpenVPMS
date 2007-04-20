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
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.Collection;
import java.util.List;


/**
 * Default implementation of the {@link ILookupService}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupService extends AbstractLookupService {

    /**
     * The default lookup node.
     */
    private static final String DEFAULT_LOOKUP = "defaultLookup"; // NON-NLS

    /**
     * Constructs a new <tt>DefaultLookupService</tt>.
     *
     * @param service the archetype service
     */
    public LookupService(IArchetypeService service) {
        super(service);
    }

    /**
     * Returns the lookup with the specified lookup archetype short name and
     * code.
     *
     * @param shortName the lookup archetype short name
     * @param code      the lookup code
     * @return the corresponding lookup or <tt>null</tt> if none is found
     */
    public Lookup getLookup(String shortName, String code) {
        return query(shortName, code);
    }

    /**
     * Returns all lookups with the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name
     * @return a collection of lookups with the specified short name
     */
    public Collection<Lookup> getLookups(String shortName) {
        return query(shortName);
    }

    /**
     * Returns the default lookup for the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name
     * @return the default lookup, or <Tt>null</tt> if none is found
     */
    public Lookup getDefaultLookup(String shortName) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, false)
                .add(new NodeConstraint(DEFAULT_LOOKUP, RelationalOp.EQ, true))
                .setMaxResults(1);
        List<IMObject> results = getService().get(query).getResults();

        return (!results.isEmpty()) ? (Lookup) results.get(0) : null;
    }

}
