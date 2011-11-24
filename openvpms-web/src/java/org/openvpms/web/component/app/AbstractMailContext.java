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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.mail.AttachmentBrowserFactory;
import org.openvpms.web.component.mail.MailContext;

import java.util.Map;


/**
 * Abstract implementation of the {@link MailContext} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class AbstractMailContext implements MailContext {

    /**
     * The attachment browser factory. May be <tt>null</tt>
     */
    private AttachmentBrowserFactory factory;

    /**
     * Registers a factory for attachment browsers.
     *
     * @param factory the factory. May be <tt>null</tt>
     */
    public void setAttachmentBrowserFactory(AttachmentBrowserFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns a browser for documents that may be attached to mails.
     *
     * @return a browser. May be <tt>null</tt>
     */
    public Browser<Act> createAttachmentBrowser() {
        return (factory != null) ? factory.createBrowser(this) : null;
    }

    /**
     * Returns variables to be used in macro expansion.
     *
     * @return <tt>null</tt>
     */
    public Map<String, Object> getVariables() {
        return null;
    }
}
