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

package org.openvpms.web.workspace.reporting.reminder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.practice.MailServer;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.object.Reference;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.mail.EmailAddress;
import org.openvpms.web.component.mail.Mailer;
import org.openvpms.web.component.mail.MailerFactory;
import org.openvpms.web.component.service.DefaultMailService;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.reporting.ReportingException;
import org.openvpms.web.workspace.reporting.email.PracticeEmailAddresses;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.NoMailServer;

/**
 * Creates a {@link Mailer}s for sending reminders.
 *
 * @author Tim Anderson
 */
class ReminderMailerFactory {

    /**
     * The practice.
     */
    private final Party practice;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The mailer factory.
     */
    private final MailerFactory factory;

    /**
     * The practice email addresses.
     */
    private final PracticeEmailAddresses addresses;

    /**
     * Mail server setting ids, keyed on practice location.
     */
    private final Map<Party, Long> settingsByLocation = new HashMap<>();

    /**
     * Mail server settings, keyed on id.
     */
    private final Map<Long, MailServer> settingsById = new HashMap<>();

    /**
     * Mail senders, keyed on mail server settings id.
     */
    private final Map<Long, JavaMailSender> senders = new HashMap<>();

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ReminderMailerFactory.class);

    /**
     * Constructs a {@link ReminderMailerFactory}.
     *
     * @param practice      the practice
     * @param practiceRules the practice rules
     * @param service       the archetype service
     * @param factory       the mailer factory
     * @throws ReportingException if there is no practice mail server or email contact
     */
    public ReminderMailerFactory(Party practice, PracticeRules practiceRules, IArchetypeService service,
                                 MailerFactory factory) {
        this.practice = practice;
        this.service = service;
        this.factory = factory;
        List<Party> locations = practiceRules.getLocations(practice);
        addresses = new PracticeEmailAddresses(practice, locations, "REMINDER", service);
        if (addMailSettings(practice) == null) {
            throw new ReportingException(NoMailServer, practice.getName());
        }
        for (Party location : locations) {
            addMailSettings(location);
        }
    }

    /**
     * Creates a {@link Mailer} for sending reminders.
     * <p>
     * This will use the mail server settings and REMINDER email address associated with the supplied practice location.
     * <p>
     * If the location has no mail server settings or REMINDER email address, the practice details will
     * <p>
     * If there is no REMINDER email contact at the location, it falls back to the practice email address.<br/>
     * If there are no mail server settings at the location, it falls back to the practice mail server settings.<br/>
     * The from address in the returned mailer will be that of the location REMINDER email contact, or the practice
     * email address, if none is found.
     *
     * @param location the practice location
     * @param context  the context
     * @return a new mailer, with the {@code from} address populated
     * @throws ReportingException if no mail server settings can be found, or there is no reminder contact
     */
    public Mailer create(Party location, Context context) {
        MailServer settings;
        EmailAddress from;
        settings = getMailSettings(location);
        from = addresses.getAddress(location);
        if (from == null || settings == null) {
            // no reminder contact at the practice location. Fall back to the practice
            from = addresses.getPracticeAddress();
            settings = getMailSettings(practice);
        }
        Mailer mailer = create(context, settings);
        mailer.setFrom(from.toString(true));
        return mailer;
    }

    /**
     * Creates a mailer for the specified context and mail server.
     *
     * @param context  the context
     * @param settings the mail server settings
     * @return a new mailer
     */
    protected Mailer create(Context context, MailServer settings) {
        JavaMailSender sender = getMailSender(settings);
        return factory.create(new CustomerMailContext(context), sender);
    }

    /**
     * Returns a mail service that uses the supplied mail server settings.
     *
     * @param settings the settings
     * @return a mail service
     */
    protected JavaMailSender getMailSender(MailServer settings) {
        Long id = settings.getId();
        JavaMailSender result = senders.get(id);
        if (result == null) {
            result = new DefaultMailService(settings);
            senders.put(id, result);
        }
        return result;
    }

    /**
     * Adds mail settings for the specified location if it has a location email address and mail settings.
     *
     * @param location the practice or practice location
     * @return the settings, or {@code null} if none are found
     */
    private MailServer addMailSettings(Party location) {
        MailServer settings = null;
        Reference reference = service.getBean(location).getTargetRef("mailServer");
        if (reference != null) {
            if (addresses.getAddress(location) != null) {
                settings = settingsById.get(reference.getId());
                if (settings == null) {
                    Entity entity = (Entity) service.get(reference, true);
                    if (entity != null) {
                        settings = new MailServer(entity, service);
                        settingsById.put(entity.getId(), settings);
                    }
                }
                if (settings != null) {
                    settingsByLocation.put(location, settings.getId());
                }
            } else {
                log.warn("Not using mail server for location=" + location.getName()
                         + ": location has no reminder email contact");
            }
        }
        return settings;
    }

    /**
     * Returns the mail server settings for the specified location.
     *
     * @param location the practice or practice location
     * @return the mail server settings, or {@code null} if none are found
     */
    private MailServer getMailSettings(Party location) {
        MailServer result = null;
        Long id = settingsByLocation.get(location);
        if (id != null) {
            result = settingsById.get(id);
        }
        return result;
    }

}
