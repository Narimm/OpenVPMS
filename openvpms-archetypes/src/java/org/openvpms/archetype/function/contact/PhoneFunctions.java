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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.contact;

import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;

/**
 * Phone contact functions.
 *
 * @author Tim Anderson
 */
public class PhoneFunctions extends ContactFunctions {

    /**
     * Constructs a {@link ContactFunctions}.
     *
     * @param rules the party rules
     */
    public PhoneFunctions(PartyRules rules) {
        super(ContactArchetypes.PHONE, rules);
    }

    /**
     * Returns a home phone contact for the specified party.
     *
     * @param party the party. May be {@code null}
     * @return the home phone contact. May be {@code null}
     */
    public Contact home(Party party) {
        return getContact(party, false, ContactArchetypes.HOME_PURPOSE);
    }

    /**
     * Returns a work phone contact for the specified party.
     *
     * @param party the party. May be {@code null}
     * @return the work phone contact. May be {@code null}
     */
    public Contact work(Party party) {
        return getContact(party, false, ContactArchetypes.WORK_PURPOSE);
    }

    /**
     * Returns a mobile phone contact for the specified party.
     *
     * @param party the party. May be {@code null}
     * @return the mobile phone contact. May be {@code null}
     */
    public Contact mobile(Party party) {
        return getContact(party, true, ContactArchetypes.MOBILE_PURPOSE);
    }

    /**
     * Formats a contact.
     *
     * @param contact the contact. May be {@code null}
     * @return the formatted contact. May be {@code null}
     */
    @Override
    public String format(Contact contact) {
        return (contact != null) ? getRules().formatPhone(contact, false) : null;
    }

}
