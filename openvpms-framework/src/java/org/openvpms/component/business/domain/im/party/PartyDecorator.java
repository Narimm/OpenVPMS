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

package org.openvpms.component.business.domain.im.party;

import org.openvpms.component.model.party.Contact;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.business.domain.im.common.EntityDecorator;

import java.util.Set;

/**
 * Decorator for {@link Party}.
 *
 * @author Tim Anderson
 */
public class PartyDecorator extends EntityDecorator implements Party {

    /**
     * Constructs a {@link PartyDecorator}.
     *
     * @param peer the peer to delegate to
     */
    public PartyDecorator(Party peer) {
        super(peer);
    }

    /**
     * Returns the contacts.
     *
     * @return the contacts
     */
    @Override
    public Set<Contact> getContacts() {
        return getPeer().getContacts();
    }

    /**
     * Add the {@link Contact} to this party
     *
     * @param contact contact to add
     */
    @Override
    public void addContact(Contact contact) {
        getPeer().addContact(contact);
    }

    /**
     * Remove the {@link Contact} from this party.
     *
     * @param contact the contact to remove
     */
    @Override
    public void removeContact(Contact contact) {
        getPeer().removeContact(contact);
    }

    /**
     * Returns the peer.
     *
     * @return the peer
     */
    @Override
    protected Party getPeer() {
        return (Party) super.getPeer();
    }
}
