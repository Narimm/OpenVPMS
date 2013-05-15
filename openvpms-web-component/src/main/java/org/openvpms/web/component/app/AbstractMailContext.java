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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.macro.Variables;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.mail.AddressFormatter;
import org.openvpms.web.component.mail.AttachmentBrowserFactory;
import org.openvpms.web.component.mail.FromAddressFormatter;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.ToAddressFormatter;


/**
 * Abstract implementation of the {@link MailContext} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractMailContext implements MailContext {

    /**
     * The attachment browser factory. May be {@code null}
     */
    private AttachmentBrowserFactory factory;

    /**
     * Registers a factory for attachment browsers.
     *
     * @param factory the factory. May be {@code null}
     */
    public void setAttachmentBrowserFactory(AttachmentBrowserFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns a browser for documents that may be attached to mails.
     *
     * @return a browser. May be {@code null}
     */
    public Browser<Act> createAttachmentBrowser() {
        return (factory != null) ? factory.createBrowser(this) : null;
    }

    /**
     * Returns variables to be used in macro expansion.
     *
     * @return {@code null}
     */
    public Variables getVariables() {
        return null;
    }

    /**
     * Returns a formatter to format 'from' addresses.
     *
     * @return the 'from' address formatter
     */
    public AddressFormatter getFromAddressFormatter() {
        return FromAddressFormatter.INSTANCE;
    }

    /**
     * Returns a formatter to format 'to' addresses.
     *
     * @return the 'to' address formatter
     */
    public AddressFormatter getToAddressFormatter() {
        return ToAddressFormatter.INSTANCE;
    }
}
