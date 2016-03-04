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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.app;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.system.common.util.Variables;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.mail.AddressFormatter;
import org.openvpms.web.component.mail.MailContext;

import java.util.List;

/**
 * A {@link MailContext} that delegates to another.
 *
 * @author Tim Anderson
 */
public class DelegatingMailContext implements MailContext {

    /**
     * The context to delegate to.
     */
    private final MailContext context;

    /**
     * Constructs an {@link DelegatingMailContext}.
     *
     * @param context the context to delegate to
     */
    public DelegatingMailContext(MailContext context) {
        this.context = context;
    }

    /**
     * Returns the available 'from' email addresses.
     *
     * @return the 'from' email addresses
     */
    @Override
    public List<Contact> getFromAddresses() {
        return context.getFromAddresses();
    }

    /**
     * Returns the available 'to' email addresses.
     *
     * @return the 'to' email addresses
     */
    @Override
    public List<Contact> getToAddresses() {
        return context.getToAddresses();
    }

    /**
     * Returns a browser for documents that may be attached to mails.
     *
     * @return a new browser. May be {@code null}
     */
    @Override
    public Browser<Act> createAttachmentBrowser() {
        return context.createAttachmentBrowser();
    }

    /**
     * Returns the object to evaluate macros against.
     *
     * @return the object to evaluate macros against. May be {@code null}
     */
    @Override
    public Object getMacroContext() {
        return context.getMacroContext();
    }

    /**
     * Returns variables to be used in macro expansion.
     *
     * @return variables to use in macro expansion. May be {@code null}
     */
    @Override
    public Variables getVariables() {
        return context.getVariables();
    }

    /**
     * Returns a formatter to format 'from' addresses.
     *
     * @return the 'from' address formatter
     */
    @Override
    public AddressFormatter getFromAddressFormatter() {
        return context.getFromAddressFormatter();
    }

    /**
     * Returns a formatter to format 'to' addresses.
     *
     * @return the 'to' address formatter
     */
    @Override
    public AddressFormatter getToAddressFormatter() {
        return context.getToAddressFormatter();
    }
}
