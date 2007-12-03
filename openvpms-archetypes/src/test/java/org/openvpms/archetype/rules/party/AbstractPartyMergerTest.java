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

package org.openvpms.archetype.rules.party;

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;


/**
 * Base class for {@link PartyMerger} test classes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractPartyMergerTest extends ArchetypeServiceTest {

    /**
     * Counts all participations for a party.
     *
     * @param party the party
     * @return the no. of participations for the party
     * @throws ArchetypeServiceException for any error
     */
    protected int countParticipations(Party party) {
        ArchetypeQuery query
                = new ArchetypeQuery("participation.*", true, false);
        query.setCountResults(true);
        query.add(new ObjectRefNodeConstraint("entity",
                                              party.getObjectReference()));
        IPage<IMObject> page = getArchetypeService().get(query);
        return page.getTotalResults();
    }

}
