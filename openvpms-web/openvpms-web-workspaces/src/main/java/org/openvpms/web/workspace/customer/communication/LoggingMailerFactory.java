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

package org.openvpms.web.workspace.customer.communication;

import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.Mailer;
import org.openvpms.web.component.mail.MailerFactory;

/**
 * A {@link MailerFactory} that creates {@link LoggingMailer} instances to log communication with customers.
 *
 * @author Tim Anderson
 */
public class LoggingMailerFactory extends MailerFactory {

    /**
     * The communication logger.
     */
    private final CommunicationLogger logger;

    /**
     * The practice service.
     */
    private final PracticeService service;

    /**
     * Constructs a {@link LoggingMailerFactory}.
     *
     * @param logger  the communication logger
     * @param service the practice service
     */
    public LoggingMailerFactory(CommunicationLogger logger, PracticeService service) {
        this.logger = logger;
        this.service = service;
    }

    /**
     * Creates a new {@link Mailer}.
     *
     * @param context the mail context
     * @return a new {@link Mailer}
     */
    @Override
    public Mailer create(MailContext context) {
        Mailer result;
        if (CommunicationHelper.isLoggingEnabled(service)) {
            result = new LoggingMailer(context, logger);
        } else {
            result = super.create(context);
        }
        return result;
    }
}
