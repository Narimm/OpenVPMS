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

package org.openvpms.web.workspace.patient.insurance.claim;

import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.contact.ContactHelper;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.customer.CustomerMailContext;

import java.util.Collections;
import java.util.List;

/**
 * A {@link MailContext} for emailing claims to insurers.
 *
 * @author Tim Anderson
 */
public class InsurerMailContext extends CustomerMailContext {

    /**
     * The preferred contact.
     */
    private final Contact preferred;

    /**
     * The contacts.
     */
    private final List<Contact> contacts;


    /**
     * Constructs a {@link InsurerMailContext} that registers an attachment browser.
     *
     * @param context the context
     * @param help    the help context
     */
    public InsurerMailContext(Context context, HelpContext help) {
        super(context, help);
        Party supplier = context.getSupplier();
        if (supplier != null) {
            contacts = ContactHelper.getEmailContacts(supplier);
        } else {
            contacts = Collections.emptyList();
        }
        preferred = (!contacts.isEmpty()) ? contacts.get(0) : null;
    }

    /**
     * Returns the preferred to address.
     *
     * @return the preferred to address. May be {@code null}
     */
    @Override
    public Contact getPreferredToAddress() {
        return preferred;
    }

    /**
     * Collects to available 'to' email addresses.
     *
     * @param addresses the list to collect addresses in
     */
    @Override
    protected void getToAddresses(List<Contact> addresses) {
        super.getToAddresses(addresses);
        addresses.addAll(contacts);
    }

}
