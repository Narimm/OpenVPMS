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

package org.openvpms.web.component.mail;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.springframework.mail.javamail.JavaMailSender;


/**
 * Default implementation of the {@link Mailer} interface.
 *
 * @author Tim Anderson
 */
public class DefaultMailer extends AbstractMailer {

    /**
     * Constructs a {@link DefaultMailer}.
     *
     * @param context  the mail context
     * @param sender   the mail service
     * @param handlers the document handlers
     */
    public DefaultMailer(MailContext context, JavaMailSender sender, DocumentHandlers handlers) {
        super(context, sender, handlers);
    }

}
