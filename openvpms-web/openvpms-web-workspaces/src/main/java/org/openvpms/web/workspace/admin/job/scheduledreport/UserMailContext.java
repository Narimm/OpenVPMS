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

package org.openvpms.web.workspace.admin.job.scheduledreport;

import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextMailContext;
import org.openvpms.web.component.im.contact.ContactHelper;
import org.openvpms.web.component.mail.MailContext;

import java.util.List;

/**
 * An {@link MailContext} that uses the user, practice location and practice's email addresses for the 'from' address.
 *
 * @author Tim Anderson
 */
public class UserMailContext extends ContextMailContext {

    /**
     * Constructs an {@link UserMailContext}.
     *
     * @param context the context
     */
    public UserMailContext(Context context) {
        super(context);
    }

    /**
     * Returns the available 'from' email addresses.
     * <p>
     * This implementation returns the email contacts from the current user, practice location and practice.
     *
     * @return the 'from' email addresses
     */
    @Override
    public List<Contact> getFromAddresses() {
        Context context = getContext();
        List<Contact> result = ContactHelper.getEmailContacts(context.getLocation());
        result.addAll(ContactHelper.getEmailContacts(context.getPractice()));
        return result;
    }

    /**
     * Returns the available 'to' email addresses.
     *
     * @return the 'to' email addresses
     */
    @Override
    public List<Contact> getToAddresses() {
        Context context = getContext();
        List<Contact> result = ContactHelper.getEmailContacts(context.getUser());
        result.addAll(ContactHelper.getEmailContacts(context.getLocation()));
        result.addAll(ContactHelper.getEmailContacts(context.getPractice()));
        return result;
    }
}
