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

package org.openvpms.domain.internal.party;

import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.component.business.domain.im.party.PartyDecorator;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.party.Contact;
import org.openvpms.component.model.party.Party;
import org.openvpms.domain.party.Address;
import org.openvpms.domain.party.ContactableParty;
import org.openvpms.domain.party.Email;
import org.openvpms.domain.party.Phone;

/**
 * Default implementation of {@link ContactableParty}.
 *
 * @author Tim Anderson
 */
public class ContactablePartyImpl extends PartyDecorator implements ContactableParty {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The party rules.
     */
    private final PartyRules rules;

    /**
     * Constructs a {@link ContactablePartyImpl}.
     *
     * @param peer the peer to delegate to
     */
    public ContactablePartyImpl(Party peer, IArchetypeService service, PartyRules rules) {
        super(peer);
        this.service = service;
        this.rules = rules;
    }

    /**
     * Returns the address.
     *
     * @return the address. May be {@code null}
     */
    @Override
    public Address getAddress() {
        Contact contact = rules.getAddressContact(getPeer(), null);
        return contact != null ? new AddressImpl(contact, service, rules) : null;
    }

    /**
     * Returns the phone.
     *
     * @return the phone. May be {@code null}
     */
    @Override
    public Phone getPhone() {
        Contact contact = rules.getTelephoneContact(getPeer());
        return contact != null ? new PhoneImpl(contact, service) : null;
    }

    /**
     * Returns the email address.
     *
     * @return the email address. May be {@code null}
     */
    @Override
    public Email getEmail() {
        Contact contact = rules.getEmailContact(getPeer());
        return contact != null ? new EmailImpl(contact, service) : null;
    }

    /**
     * Returns an address with the given purpose.
     * <p>
     * If it cannot find the specified purpose, it uses the preferred location contact or
     * any location contact if there is no preferred.
     *
     * @param purpose the purpose
     * @return the address. May be {@code null}
     */
    protected Address getAddress(String purpose) {
        Contact contact = rules.getAddressContact(getPeer(), purpose);
        return contact != null ? new AddressImpl(contact, service, rules) : null;
    }

    /**
     * Returns a phone with the given purpose.
     * <p>
     * This will return a phone contact with the specified purpose, or any phone contact if there is none.
     *
     * @param purpose the purpose
     * @return the phone. May be {@code null}
     */
    protected Phone getPhone(String purpose) {
        Contact contact = rules.getTelephoneContact(getPeer(), purpose);
        return contact != null ? new PhoneImpl(contact, service) : null;
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
     * Returns the rules.
     *
     * @return the rules
     */
    protected PartyRules getRules() {
        return rules;
    }

}
