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

/**
 * Formats email addresses for display.
 *
 * @author Tim Anderson
 */
public interface AddressFormatter {

    /**
     * Returns the email address of a contact.
     *
     * @param contact the email contact. May be {@code null}
     * @return the the contact's email address, or {@code null} if none is found
     */
    String getAddress(Contact contact);

    /**
     * Formats an email address contact.
     *
     * @param contact the email address contact
     * @return the formatted contact
     */
    String format(Contact contact);
}