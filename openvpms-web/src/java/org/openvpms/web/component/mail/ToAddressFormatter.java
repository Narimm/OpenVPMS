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
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.resource.util.Messages;

/**
 * An {@link AddressFormatter} for 'to' addresses.
 * <p/>
 * This includes the type of the contact to help differentiate it from other contacts
 *
 * @author Tim Anderson
 */
public class ToAddressFormatter extends AbstractAddressFormatter {

    /**
     * The singleton instance.
     */
    public static final AddressFormatter INSTANCE = new ToAddressFormatter();

    /**
     * Default constructor.
     */
    protected ToAddressFormatter() {

    }

    /**
     * Formats an email address contact.
     *
     * @param contact the email address contact
     * @return the formatted contact
     */
    public String format(Contact contact) {
        Party party = contact.getParty();
        String type = DescriptorHelper.getDisplayName(party);
        return Messages.get("mail.contact.to", party.getName(), getAddress(contact), type);
    }
}
