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

package org.openvpms.web.component.mail;

import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.web.resource.i18n.Messages;

/**
 * An {@link AddressFormatter} for 'to' addresses.
 * <p/>
 * This includes the type of the contact to help differentiate it from other contacts
 *
 * @author Tim Anderson
 */
public class ToAddressFormatter extends AbstractAddressFormatter {

    /**
     * Default constructor.
     */
    public ToAddressFormatter() {
        super();
    }

    /**
     * Formats an email address contact.
     *
     * @param contact the email address contact
     * @return the formatted contact
     */
    public String format(Contact contact) {
        String result;
        String nameAddress = getQualifiedNameAddress(contact);
        String type = getType(contact);
        if (type != null) {
            result = Messages.format("mail.contact.to", nameAddress, type);
        } else {
            result = nameAddress;
        }
        return result;
    }

}
