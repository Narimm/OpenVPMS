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

/**
 * Location contact functions.
 *
 * @author Tim Anderson
 */
public class AddressFunctions extends ContactFunctions {

    /**
     * Constructs an {@link AddressFunctions}.
     *
     * @param rules the party rules
     */
    public AddressFunctions(PartyRules rules) {
        super(ContactArchetypes.LOCATION, rules);
    }

    /**
     * Formats a contact.
     *
     * @param contact the contact. May be {@code null}
     * @return the formatted contact. May be {@code null}
     */
    @Override
    public String format(Contact contact) {
        return format(contact, false);
    }

    /**
     * Formats a contact.
     *
     * @param contact    the contact. May be {@code null}
     * @param singleLine if {@code true}, return the address as a single line
     * @return the formatted contact. May be {@code null}
     */
    public String format(Contact contact, boolean singleLine) {
        return (contact != null) ? getRules().formatAddress(contact, singleLine) : null;
    }

}
