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

package org.openvpms.web.component.mail;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.web.component.service.MailService;

/**
 * Factory for {@link Mailer} instances.
 *
 * @author Tim Anderson
 */
public class DefaultMailerFactory implements MailerFactory {

    /**
     * The mail service.
     */
    private final MailService service;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * Constructs a {@link DefaultMailerFactory}.
     *
     * @param service  the mail service
     * @param handlers the document handlers
     */
    public DefaultMailerFactory(MailService service, DocumentHandlers handlers) {
        this.service = service;
        this.handlers = handlers;
    }

    /**
     * Creates a new {@link Mailer}.
     *
     * @param context the mail context
     * @return a new {@link Mailer}
     */
    @Override
    public Mailer create(MailContext context) {
        return new DefaultMailer(context, service, handlers);
    }

}
