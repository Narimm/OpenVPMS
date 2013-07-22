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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.mail;

import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.resource.i18n.Messages;

/**
 * An {@link AddressFormatter} for 'from' addresses.
 *
 * @author Tim Anderson
 */
public class FromAddressFormatter extends AbstractAddressFormatter {

    /**
     * The singleton instance.
     */
    public static final AddressFormatter INSTANCE = new FromAddressFormatter();

    /**
     * Default constructor.
     */
    protected FromAddressFormatter() {
    }

    /**
     * Formats an email address contact.
     *
     * @param contact the email address contact
     * @return the formatted contact
     */
    public String format(Contact contact) {
        Party party = contact.getParty();
        return Messages.format("mail.contact.from", party.getName(), getAddress(contact));
    }
}
