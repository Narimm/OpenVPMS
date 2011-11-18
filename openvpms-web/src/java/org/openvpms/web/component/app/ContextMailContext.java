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

package org.openvpms.web.component.app;

import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.web.component.im.util.ContactHelper;
import org.openvpms.web.component.mail.MailContext;

import java.util.List;

/**
 * An {@link MailContext} that sources email addresses from objects held by the supplied {@link Context}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class ContextMailContext implements MailContext {

    /**
     * The context to use.
     */
    private final Context context;

    /**
     * Constructs a <tt>ContextMailContext</tt>.
     *
     * @param context the context
     */
    public ContextMailContext(Context context) {
        this.context = context;
    }

    /**
     * Returns the available 'from' email addresses.
     * <p/>
     * This implementation returns the email contacts from the current practice.
     *
     * @return the 'from' email addresses
     */
    public List<Contact> getFromAddresses() {
        return ContactHelper.getEmailContacts(context.getPractice());
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    protected Context getContext() {
        return context;
    }
}
