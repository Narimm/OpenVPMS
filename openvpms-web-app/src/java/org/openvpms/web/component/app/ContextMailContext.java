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
import org.openvpms.macro.Variables;
import org.openvpms.web.component.im.contact.ContactHelper;
import org.openvpms.web.component.macro.MacroVariables;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;

/**
 * An {@link MailContext} that sources email addresses from objects held by the supplied {@link Context}.
 *
 * @author Tim Anderson
 */
public abstract class ContextMailContext extends AbstractMailContext {

    /**
     * The context to use.
     */
    private final Context context;

    /**
     * The variables.
     */
    private Variables variables;

    /**
     * Constructs a {@code ContextMailContext}.
     *
     * @param context the context
     */
    public ContextMailContext(Context context) {
        this.context = context;
    }

    /**
     * Returns the available 'from' email addresses.
     * <p/>
     * This implementation returns the email contacts from the current practice location if any, or the practice.
     *
     * @return the 'from' email addresses
     */
    public List<Contact> getFromAddresses() {
        List<Contact> result = ContactHelper.getEmailContacts(context.getLocation());
        if (result.isEmpty()) {
            result = ContactHelper.getEmailContacts(context.getPractice());
        }
        return result;
    }

    /**
     * Returns variables to be used in macro expansion.
     *
     * @return an instance of {@link MacroVariables}
     */
    @Override
    public Variables getVariables() {
        if (variables == null) {
            variables = new MacroVariables(new ReloadingContext(context), ServiceHelper.getArchetypeService(),
                                           ServiceHelper.getLookupService());
        }
        return variables;
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
