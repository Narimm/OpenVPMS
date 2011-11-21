/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.component.mail;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.contact.ContactHelper;

import java.util.List;

/**
 * An {@link MailContext} that returns 'from' addresses from the practice location or practice, and 'to' addresses from
 * the specified party.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class PartyMailContext implements MailContext {

    /**
     * The practice location.
     */
    private final Party location;

    /**
     * The practice.
     */
    private final Party practice;

    /**
     * The party to get the 'to' addresses.
     */
    private final Party to;

    /**
     * Constructs a <tt>PartyMailContext</tt>.
     *
     * @param location the practice location. May be <tt>null</tt>
     * @param practice the practice. May be <tt>null</tt>
     * @param to       the party to
     */
    public PartyMailContext(Party location, Party practice, Party to) {
        this.location = location;
        this.practice = practice;
        this.to = to;
    }

    /**
     * Returns the available 'from' email addresses.
     *
     * @return the 'from' email addresses
     */
    public List<Contact> getFromAddresses() {
        List<Contact> result = ContactHelper.getEmailContacts(location);
        if (result.isEmpty()) {
            result = ContactHelper.getEmailContacts(practice);
        }
        return result;
    }

    /**
     * Returns the available ''to' email addresses.
     *
     * @return the 'to' email addresses
     */
    public List<Contact> getToAddresses() {
        return ContactHelper.getEmailContacts(to);
    }

    /**
     * Returns a browser for documents that may be attached to mails.
     *
     * @return <tt>null</tt>
     */
    public Browser<Act> createAttachmentBrowser() {
        return null;
    }
}
