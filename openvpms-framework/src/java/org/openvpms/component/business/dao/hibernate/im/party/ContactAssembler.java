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

package org.openvpms.component.business.dao.hibernate.im.party;

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.SetAssembler;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupDO;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;

import java.util.Set;


/**
 * Assembles {@link Contact}s from {@link ContactDO}s and vice-versa.
 *
 * @author Tim Anderson
 */
public class ContactAssembler extends IMObjectAssembler<Contact, ContactDO> {

    /**
     * Assembles sets of lookups.
     */
    private static final SetAssembler<Lookup, LookupDO> LOOKUPS
            = SetAssembler.create(Lookup.class, LookupDO.class, true);


    /**
     * Constructs a {@link ContactAssembler}.
     */
    public ContactAssembler() {
        super(org.openvpms.component.model.party.Contact.class, Contact.class, ContactDO.class, ContactDOImpl.class);
    }

    /**
     * Assembles a data object from an object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param state   the data object state
     * @param context the assembly context
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void assembleDO(ContactDO target, Contact source, DOState state,
                              Context context) {
        super.assembleDO(target, source, state, context);
        target.setActiveStartTime(source.getActiveStartTime());
        target.setActiveEndTime(source.getActiveEndTime());
        PartyDO party = null;
        DOState partyState = getDO(source.getParty(), context);
        if (partyState != null) {
            party = (PartyDO) partyState.getObject();
            state.addState(partyState);
        }
        target.setParty(party);

        LOOKUPS.assembleDO(target.getClassifications(), (Set<Lookup>) (Set) source.getClassifications(), state,
                           context);
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void assembleObject(Contact target, ContactDO source, Context context) {
        super.assembleObject(target, source, context);
        target.setActiveStartTime(source.getActiveStartTime());
        target.setActiveEndTime(source.getActiveEndTime());
        target.setParty(getObject(source.getParty(), Party.class, context));

        LOOKUPS.assembleObject((Set<Lookup>) (Set) target.getClassifications(), source.getClassifications(), context);
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected Contact create(ContactDO object) {
        return new Contact();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected ContactDO create(Contact object) {
        return new ContactDOImpl();
    }
}
