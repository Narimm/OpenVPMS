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

package org.openvpms.web.component.mail;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.system.common.util.Variables;
import org.openvpms.web.component.im.query.MultiSelectBrowser;

import java.util.List;


/**
 * Context information to pass to the mail editor.
 *
 * @author Tim Anderson
 */
public interface MailContext {

    /**
     * Returns the available 'from' email addresses.
     *
     * @return the 'from' email addresses
     */
    List<Contact> getFromAddresses();

    /**
     * Returns the available 'to' email addresses.
     *
     * @return the 'to' email addresses
     */
    List<Contact> getToAddresses();

    /**
     * Returns the preferred to address.
     *
     * @return the preferred to address. May be {@code null}
     */
    Contact getPreferredToAddress();

    /**
     * Returns a browser for documents that may be attached to mails.
     *
     * @return a new browser. May be {@code null}
     */
    MultiSelectBrowser<Act> createAttachmentBrowser();

    /**
     * Returns the object to evaluate macros against.
     *
     * @return the object to evaluate macros against. May be {@code null}
     */
    Object getMacroContext();

    /**
     * Returns variables to be used in macro expansion.
     *
     * @return variables to use in macro expansion. May be {@code null}
     */
    Variables getVariables();

    /**
     * Returns a formatter to format 'from' addresses.
     *
     * @return the 'from' address formatter
     */
    AddressFormatter getFromAddressFormatter();

    /**
     * Returns a formatter to format 'to' addresses.
     *
     * @return the 'to' address formatter
     */
    AddressFormatter getToAddressFormatter();
}