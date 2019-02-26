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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.domain.internal.practice;

import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.bean.Policies;
import org.openvpms.component.model.party.Party;
import org.openvpms.domain.internal.party.OrganisationImpl;
import org.openvpms.domain.practice.Location;
import org.openvpms.domain.practice.Practice;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link Practice}.
 *
 * @author Tim Anderson
 */
public class PracticeImpl extends OrganisationImpl implements Practice {

    /**
     * Constructs a {@link PracticeImpl}.
     *
     * @param peer    the peer to delegate to
     * @param service the archetype service
     * @param rules   the party rules
     */
    public PracticeImpl(Party peer, IArchetypeService service, PartyRules rules) {
        super(peer, service, rules);
    }

    /**
     * Returns the practice locations.
     *
     * @return the practice locations
     */
    @Override
    public List<Location> getLocations() {
        List<Location> result = new ArrayList<>();
        IMObjectBean bean = getService().getBean(getPeer());
        for (Party location : bean.getTargets("locations", Party.class, Policies.active())) {
            result.add(new LocationImpl(location, getService(), getRules()));
        }
        return result;
    }
}
