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

package org.openvpms.web.component.app;

import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.web.component.mail.MailContext;

import java.util.Collections;
import java.util.List;


/**
 * An {@link MailContext} that uses the practice location and practice's email addresses for the 'from' address.
 * The practice location and practice is sourced from the specified {@link Context}.
 *
 * @author Tim Anderson
 */
public class PracticeMailContext extends ContextMailContext {

    /**
     * Constructs a <tt>ContextMailContext</tt>.
     *
     * @param context the context
     */
    public PracticeMailContext(Context context) {
        super(context);
    }

    /**
     * Returns the available 'to' email addresses.
     *
     * @return an empty list
     */
    public List<Contact> getToAddresses() {
        return Collections.emptyList();
    }
}
