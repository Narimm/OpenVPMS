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

package org.openvpms.web.workspace.customer.communication;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.mail.DefaultMailer;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.Mailer;
import org.openvpms.web.component.mail.MailerFactory;
import org.openvpms.web.component.service.CurrentLocationMailService;
import org.openvpms.web.component.service.MailService;
import org.openvpms.web.system.ServiceHelper;

/**
 * A {@link MailerFactory} that creates {@link LoggingMailer} instances to log communication with customers.
 *
 * @author Tim Anderson
 */
public class LoggingMailerFactory implements MailerFactory {

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The communication logger.
     */
    private final CommunicationLogger logger;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link LoggingMailerFactory}.
     *
     * @param handlers        the document handlers
     * @param logger          the communication logger
     * @param practiceService the practice service
     * @param service         the archetype service
     */
    public LoggingMailerFactory(DocumentHandlers handlers, CommunicationLogger logger,
                                PracticeService practiceService, IArchetypeService service) {
        this.handlers = handlers;
        this.logger = logger;
        this.practiceService = practiceService;
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
        if (CommunicationHelper.isLoggingEnabled(practiceService, service)) {
            result = createLoggingMailer(context, logger);
        } else {
            result = createMailer(context);
        }
        return result;
    }

    /**
     * Creates a logging mailer.
     *
     * @param context the mail context
     * @param logger  the logger
     * @return a new {@link LoggingMailer}
     */
    protected LoggingMailer createLoggingMailer(MailContext context, CommunicationLogger logger) {
        // need to lazily access the mail service, as it is bound to the user session
        return new LoggingMailer(context, getMailService(), getDocumentHandlers(), logger);
    }

    /**
     * Creates a mailer.
     *
     * @param context the mail context
     * @return a new {@link Mailer}
     */
    protected Mailer createMailer(MailContext context) {
        return new DefaultMailer(context, getMailService(), getDocumentHandlers());
    }

    /**
     * Returns the mail service.
     * <p>
     * This implementation returns the {@link CurrentLocationMailService} which is bound to the user session.
     *
     * @return the mail service
     */
    protected MailService getMailService() {
        return ServiceHelper.getBean(CurrentLocationMailService.class);
    }

    /**
     * Returns the document handlers.
     */
    protected DocumentHandlers getDocumentHandlers() {
        return handlers;
    }

}
