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
 * Base class for functions operating on contacts.
 *
 * @author Tim Anderson
 */
public abstract class ContactFunctions {

    /**
     * The party rules.
     */
    private final PartyRules rules;

    /**
     * The contact archetype.
     */
    private final String archetype;

    /**
     * Constructs a {@link ContactFunctions}.
     *
     * @param archetype the contact archetype
     * @param rules     the party rules
     */
    public ContactFunctions(String archetype, PartyRules rules) {
        this.rules = rules;
        this.archetype = archetype;
    }

    /**
     * Returns the preferred contact for the specified party, or the first available contact, if none is preferred.
     *
     * @param party the party. May be {@code null}
     * @return the corresponding contact, or {@code null}
     */
    public Contact preferred(Party party) {
        return getContact(party, false, null);
    }

    /**
     * Returns a contact for the specified party and <em>BILLING</em> purpose.
     * If cannot find one with matching purpose returns the preferred contact.
     * If cannot find with matching purpose and preferred returns the first available.
     *
     * @param party the party. May be {@code null}
     * @return the corresponding contact, or {@code null}
     */
    public Contact billing(Party party) {
        return getContact(party, false, ContactArchetypes.BILLING_PURPOSE);
    }

    /**
     * Returns a contact for the specified party and <em>CORRESPONDENCE</em> purpose.
     * If cannot find one with matching purpose returns the preferred contact.
     * If cannot find with matching purpose and preferred returns the first available.
     *
     * @param party the party. May be {@code null}
     * @return the corresponding contact, or {@code null}
     */
    public Contact correspondence(Party party) {
        return getContact(party, false, ContactArchetypes.CORRESPONDENCE_PURPOSE);
    }

    /**
     * Returns a contact for the specified party and <em>REMINDER</em> purpose.
     * If cannot find one with matching purpose returns the preferred contact.
     * If cannot find with matching purpose and preferred returns the first available.
     *
     * @param party the party. May be {@code null}
     * @return the corresponding contact, or {@code null}
     */
    public Contact reminder(Party party) {
        return getContact(party, false, ContactArchetypes.REMINDER_PURPOSE);
    }

    /**
     * Returns a contact for the specified party and purpose.
     *
     * @param party   the party. May be {@code null}
     * @param purpose the contact purpose. May be {@code null}
     * @return the corresponding contact, or {@code null} if no contact with the purpose exists
     */
    public Contact purpose(Party party, String purpose) {
        return (party != null && purpose != null) ? getContact(party, true, purpose) : null;
    }

    /**
     * Formats a contact.
     *
     * @param contact the contact. May be {@code null}
     * @return the formatted contact. May be {@code null}
     */
    public abstract String format(Contact contact);

    /**
     * Returns a contact for the specified party and purpose.
     * If cannot find one with matching purpose returns last preferred contact.
     * If cannot find with matching purpose and preferred returns last found.
     *
     * @param party   the party. May be {@code null}
     * @param exact   if {@code true}, the contact must have the specified purpose
     * @param purpose the contact purpose. May be {@code null}
     * @return the corresponding contact, or {@code null}
     */
    protected Contact getContact(Party party, boolean exact, String purpose) {
        return (party != null) ? rules.getContact(party, archetype, exact, purpose) : null;
    }

    /**
     * Returns the party rules.
     *
     * @return the party rules
     */
    protected PartyRules getRules() {
        return rules;
    }
}