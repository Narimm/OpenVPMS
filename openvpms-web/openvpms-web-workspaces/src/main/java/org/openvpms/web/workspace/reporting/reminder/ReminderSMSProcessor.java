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

package org.openvpms.web.workspace.reporting.reminder;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.ContactMatcher;
import org.openvpms.archetype.rules.party.SMSMatcher;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypeCache;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.sms.Connection;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.web.component.im.sms.SMSHelper;
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
 * Sends reminders via SMS.
 *
 * @author Tim Anderson
 */
public class ReminderSMSProcessor extends GroupedReminderProcessor {

    /**
     * The SMS connection factory.
     */
    private final ConnectionFactory factory;

    /**
     * The template evaluator.
     */
    private final ReminderSMSEvaluator evaluator;

    /**
     * Constructs a {@link ReminderSMSProcessor}.
     *
     * @param factory       the SMS connection factory
     * @param evaluator     the SMS template evaluator
     * @param groupTemplate the grouped reminder template
     * @param reminderTypes the reminder types
     * @param rules         the reminder rules
     * @param practice      the practice
     * @param service       the archetype service
     * @param config        the reminder configuration
     * @param logger        the communication logger. May be {@code null}
     */
    public ReminderSMSProcessor(ConnectionFactory factory, ReminderSMSEvaluator evaluator,
                                DocumentTemplate groupTemplate, ReminderTypeCache reminderTypes, ReminderRules rules,
                                Party practice, IArchetypeService service, ReminderConfiguration config,
                                CommunicationLogger logger) {
        super(groupTemplate, reminderTypes, rules, practice, service, config, logger);
        this.factory = factory;
        this.evaluator = evaluator;
    }

    /**
     * Determines if reminder processing is performed asynchronously.
     *
     * @return {@code true} if reminder processing is performed asynchronously
     */
    @Override
    public boolean isAsynchronous() {
        return false;
    }

    /**
     * Logs reminder communications.
     *
     * @param state  the reminder state
     * @param logger the communication logger
     */
    @Override
    protected void log(State state, CommunicationLogger logger) {
        String subject = Messages.get("reminder.log.sms.subject");
        for (ObjectSet set : state.getReminders()) {
            String notes = getNote(set);
            Party customer = getCustomer(set);
            Party patient = getPatient(set);
            Contact contact = (Contact) set.get("contact");
            Party location = (Party) set.get("location");
            String text = set.getString("text");
            logger.logSMS(customer, patient, contact.getDescription(), subject, COMMUNICATION_REASON, text,
                          notes, location);
        }
    }

    /**
     * Prepares reminders for processing.
     * <p/>
     * This:
     * <ul>
     * <li>filters out any reminders that can't be processed due to missing data</li>
     * <li>adds meta-data for subsequent calls to {@link #process}</li>
     * </ul>
     *
     * @param reminders the reminders
     * @param updated   acts that need to be saved on completion
     * @param errors    reminders that can't be processed due to error
     * @return the reminders to process
     */
    @Override
    protected List<ObjectSet> prepare(List<ObjectSet> reminders, List<Act> updated, List<ObjectSet> errors) {
        reminders = super.prepare(reminders, updated, errors);
        if (!reminders.isEmpty()) {
            ObjectSet reminder = reminders.get(0);
            Party customer = getCustomer(reminder);
            Party location = getCustomerLocation(customer);
            for (ObjectSet set : reminders) {
                set.set("location", location);
            }
        }
        return reminders;
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
     * @return a new contact matcher
     */
    @Override
    protected ContactMatcher createContactMatcher() {
        return new SMSMatcher(CONTACT_PURPOSE, false, getService());
    }

    /**
     * Processes a list of reminder events.
     *
     * @param contact   the contact to send to
     * @param reminders the events
     * @param template  the document template to use. May be {@code null}
     */
    @Override
    protected void process(Contact contact, List<ObjectSet> reminders, DocumentTemplate template) {
        if (template == null) {
            throw new ReportingException(ReminderMissingDocTemplate);
        }
        Entity smsTemplate = template.getSMSTemplate();
        if (smsTemplate == null) {
            throw new ReportingException(TemplateMissingSMSText, template.getName());
        }
        String phoneNumber = SMSHelper.getPhone(contact);

        ObjectSet first = reminders.get(0);
        Party customer = getCustomer(first);
        Party location = (Party) first.get("location");

        if (StringUtils.isEmpty(phoneNumber)) {
            throw new ReportingException(FailedToProcessReminder, "Contact has no phone number for customer=" +
                                                                  customer.getName() + " (" + customer.getId() + ")");
        } else {
            try {
                Act reminder = getReminder(first);
                Party patient = getPatient(first);
                String text = evaluator.evaluate(smsTemplate, reminder, customer, patient, location, getPractice());
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
            } catch (ReportingException exception) {
                throw exception;
            } catch (Throwable exception) {
                throw new ReportingException(FailedToProcessReminder, exception, exception.getMessage());
            }
        }
    }
}
