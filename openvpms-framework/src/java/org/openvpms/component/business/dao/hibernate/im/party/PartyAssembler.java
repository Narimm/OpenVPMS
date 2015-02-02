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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.party;

import org.openvpms.component.business.domain.im.party.Party;


/**
 * Assembles {@link Party} instances from {@link PartyDO} instances and vice-versa.
 *
 * @author Tim Anderson
 */
public class PartyAssembler extends AbstractPartyAssembler<Party, PartyDO> {

    /**
     * Constructs a {@link PartyAssembler}.
     */
    public PartyAssembler() {
        super(Party.class, PartyDO.class, PartyDOImpl.class);
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected Party create(PartyDO object) {
        return new Party();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected PartyDO create(Party object) {
        return new PartyDOImpl();
    }
}
