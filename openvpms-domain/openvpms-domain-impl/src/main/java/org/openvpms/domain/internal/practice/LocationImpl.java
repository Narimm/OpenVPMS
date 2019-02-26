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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.domain.internal.practice;

import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.party.Party;
import org.openvpms.domain.internal.party.OrganisationImpl;
import org.openvpms.domain.practice.Location;

/**
 * Default implementation of {@link Location}.
 *
 * @author Tim Anderson
 */
public class LocationImpl extends OrganisationImpl implements Location {

    /**
     * Constructs a {@link LocationImpl}.
     *
     * @param peer    the peer to delegate to
     * @param service the archetype service
     * @param rules   the rules
     */
    public LocationImpl(Party peer, IArchetypeService service, PartyRules rules) {
        super(peer, service, rules);
    }
}
