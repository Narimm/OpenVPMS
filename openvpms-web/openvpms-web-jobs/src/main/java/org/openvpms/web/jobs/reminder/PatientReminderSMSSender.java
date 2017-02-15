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

package org.openvpms.web.jobs.reminder;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.ContactMatcher;
import org.openvpms.archetype.rules.party.SMSMatcher;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypeCache;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.sms.Connection;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.sms.SMSHelper;
import org.openvpms.web.component.im.sms.SMSTemplateEvaluator;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;
import org.openvpms.web.workspace.reporting.ReportingException;

import java.util.Date;
import java.util.List;

import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.FailedToProcessReminder;
import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.ReminderMissingDocTemplate;
import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.SMSMessageEmpty;
import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.SMSMessageTooLong;
import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.TemplateMissingSMSText;

/**
 * Sends patient reminders via SMS.
 *
 * @author Tim Anderson
 */
public class PatientReminderSMSSender extends PatientReminderSender {

    /**
     * The SMS connection factory.
     */
    private final ConnectionFactory factory;

    /**
     * The template evaluator.
     */
    private final SMSTemplateEvaluator evaluator;

    /**
     * Constructs a {@link PatientReminderSMSSender}.
     *
     * @param factory       the SMS connection factory
     * @param evaluator     the SMS template evaluator
     * @param groupTemplate the grouped reminder template
     * @param reminderTypes the reminder types
     * @param practice      the practice
     * @param service       the archetype service
     * @param config        the reminder configuration
     * @param logger        the communication logger. May be {@code null}
     */
    public PatientReminderSMSSender(ConnectionFactory factory, SMSTemplateEvaluator evaluator,
                                    DocumentTemplate groupTemplate, ReminderTypeCache reminderTypes, Party practice,
                                    IArchetypeService service, ReminderConfiguration config,
                                    CommunicationLogger logger) {
        super(groupTemplate, reminderTypes, practice, service, config, logger);
        this.factory = factory;
        this.evaluator = evaluator;
    }

    /**
     * Returns the contact archetype.
     *
     * @return the contact archetype
     */
    @Override
    protected String getContactArchetype() {
        return ContactArchetypes.PHONE;
    }

    /**
     * Returns the date from which a reminder item should be cancelled.
     *
     * @param startTime the item start time
     * @param config    the reminder configuration
     * @return the date when the item should be cancelled
     */
    @Override
    protected Date getCancelDate(Date startTime, ReminderConfiguration config) {
        return config.getSMSCancelDate(startTime);
    }

    /**
     * Creates a contact matcher to locate the contact to send to.
     *
     * @param useReminderContact if {@code true}, the contact must have REMINDER purpose
     * @param service            the archetype service
     * @return a new contact matcher
     */
    @Override
    protected ContactMatcher createContactMatcher(boolean useReminderContact, IArchetypeService service) {
        return new SMSMatcher("PURPOSE", useReminderContact, service);
    }

    /**
     * Processes a list of reminder events.
     *
     * @param contact   the contact to send to
     * @param events    the events
     * @param shortName the report archetype short name, used to select the document template if none specified
     * @param template  the document template to use. May be {@code null}
     * @param logger    the communication logger. May be {@code null}
     */
    @Override
    protected void process(Contact contact, List<ObjectSet> events, String shortName, DocumentTemplate template,
                           CommunicationLogger logger) {
        if (template == null) {
            throw new ReportingException(ReminderMissingDocTemplate);
        }
        Entity smsTemplate = template.getSMSTemplate();
        if (smsTemplate == null) {
            throw new ReportingException(TemplateMissingSMSText, template.getName());
        }
        String phoneNumber = SMSHelper.getPhone(contact);

        ObjectSet event = events.get(0);
        Context context = createContext(event);
        Party customer = context.getCustomer();

        if (StringUtils.isEmpty(phoneNumber)) {
            throw new ReportingException(FailedToProcessReminder, "Contact has no phone number for customer=" +
                                                                  customer.getName() + " (" + customer.getId() + ")");
        } else {
            try {
                String text = evaluator.evaluate(smsTemplate, event, context);
                if (StringUtils.isEmpty(text)) {
                    throw new ReportingException(SMSMessageEmpty, smsTemplate.getName());
                } else if (text.length() > 160) {
                    throw new ReportingException(SMSMessageTooLong, smsTemplate.getName(), text.length());
                }
                Connection connection = factory.createConnection();
                try {
                    connection.send(phoneNumber, text);
                } finally {
                    connection.close();
                }
                if (logger != null) {
                    String subject = Messages.get("reminder.log.sms.subject");
                    Party location = context.getLocation();
                    for (ObjectSet set : events) {
                        String notes = getNote(set);
                        Party patient = (Party) set.get("patient");
                        logger.logSMS(customer, patient, contact.getDescription(), subject, PATIENT_REMINDER, text,
                                      notes, location);
                    }
                }
            } catch (ReportingException exception) {
                throw exception;
            } catch (Throwable exception) {
                throw new ReportingException(FailedToProcessReminder, exception, exception.getMessage());
            }
        }
    }

}
