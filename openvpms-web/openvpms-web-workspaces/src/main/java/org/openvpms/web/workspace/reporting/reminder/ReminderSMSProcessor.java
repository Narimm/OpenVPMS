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
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.sms.Connection;
import org.openvpms.sms.ConnectionFactory;
import org.openvpms.sms.util.SMSLengthCalculator;
import org.openvpms.web.component.im.sms.SMSHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;
import org.openvpms.web.workspace.reporting.ReportingException;

import java.util.List;

import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.FailedToProcessReminder;
import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.SMSDisabled;
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
     * Determines if SMS enabled. If not, any reminder will have an error logged against it.
     */
    private final boolean smsEnabled;

    /**
     * Constructs a {@link ReminderSMSProcessor}.
     *
     * @param factory       the SMS connection factory
     * @param evaluator     the SMS template evaluator
     * @param reminderTypes the reminder types
     * @param practice      the practice
     * @param reminderRules the reminder rules
     * @param patientRules  the patient rules
     * @param practiceRules the practice rules
     * @param service       the archetype service
     * @param config        the reminder configuration
     * @param logger        the communication logger. May be {@code null}
     */
    public ReminderSMSProcessor(ConnectionFactory factory, ReminderSMSEvaluator evaluator, ReminderTypes reminderTypes,
                                Party practice, ReminderRules reminderRules, PatientRules patientRules,
                                PracticeRules practiceRules, IArchetypeService service, ReminderConfiguration config,
                                CommunicationLogger logger) {
        super(reminderTypes, reminderRules, patientRules, practice, service, config, logger);
        this.factory = factory;
        this.evaluator = evaluator;
        smsEnabled = practiceRules.isSMSEnabled(practice);
    }

    /**
     * Returns the reminder item archetype that this processes.
     *
     * @return the archetype
     */
    @Override
    public String getArchetype() {
        return ReminderArchetypes.SMS_REMINDER;
    }

    /**
     * Processes reminders.
     *
     * @param state the reminder state
     */
    @Override
    public void process(PatientReminders state) {
        if (!smsEnabled) {
            throw new ReportingException(SMSDisabled);
        }
        SMSReminders reminders = (SMSReminders) state;
        String phoneNumber = reminders.getPhoneNumber();
        try {
            Party practice = getPractice();
            String text = reminders.getText(practice);
            if (StringUtils.isEmpty(text)) {
                throw new ReportingException(SMSMessageEmpty, reminders.getSMSTemplate().getName());
            } else {
                int parts = SMSLengthCalculator.getParts(text);
                int maxParts = factory.getMaxParts();
                if (parts > maxParts) {
                    throw new ReportingException(SMSMessageTooLong, reminders.getSMSTemplate().getName(), parts,
                                                 maxParts);
                }
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
     * Returns the contact to use.
     *
     * @param customer       the reminder
     * @param matcher        the contact matcher
     * @param defaultContact the default contact, or {@code null} to select one from the customer
     * @return the contact, or {@code null} if none is found
     */
    @Override
    protected Contact getContact(Party customer, ContactMatcher matcher, Contact defaultContact) {
        Contact contact = super.getContact(customer, matcher, defaultContact);
        if (contact != null && StringUtils.isEmpty(SMSHelper.getPhone(contact))) {
            contact = null;
        }
        return contact;
    }

    /**
     * Logs reminder communications.
     *
     * @param state  the reminder state
     * @param logger the communication logger
     */
    @Override
    protected void log(PatientReminders state, CommunicationLogger logger) {
        SMSReminders reminders = (SMSReminders) state;
        String subject = Messages.get("reminder.log.sms.subject");
        Party customer = reminders.getCustomer();
        Party location = reminders.getLocation();
        Contact contact = reminders.getContact();
        String text = ((SMSReminders) state).getText();
        for (ReminderEvent event : state.getReminders()) {
            String notes = getNote(event);
            Party patient = event.getPatient();
            logger.logSMS(customer, patient, contact.getDescription(), subject, COMMUNICATION_REASON, text,
                          notes, location);
        }
    }

    /**
     * Prepares reminders for processing.
     *
     * @param reminders the reminders
     * @param groupBy   the reminder grouping policy. This determines which document template is selected
     * @param cancelled reminder items that will be cancelled
     * @param errors    reminders that can't be processed due to error
     * @param updated   acts that need to be saved on completion
     * @param resend    if {@code true}, reminders are being resent
     * @param customer  the customer, or {@code null} if there are no reminders to send
     * @param contact   the contact,  or {@code null} if there are no reminders to send
     * @param location  the practice location, or {@code null} if there are no reminders to send
     * @param template  the document template, or {@code null} if there are no reminders to send
     * @return the reminders to process
     * @throws ReportingException if the reminders cannot be prepared
     */
    @Override
    protected GroupedReminders prepare(List<ReminderEvent> reminders, ReminderType.GroupBy groupBy,
                                       List<ReminderEvent> cancelled, List<ReminderEvent> errors, List<Act> updated,
                                       boolean resend, Party customer, Contact contact, Party location,
                                       DocumentTemplate template) {
        Entity smsTemplate = null;
        if (template != null && !reminders.isEmpty()) {
            smsTemplate = template.getSMSTemplate();
            if (smsTemplate == null) {
                throw new ReportingException(TemplateMissingSMSText, template.getName());
            }
        }
        return new SMSReminders(reminders, groupBy, cancelled, errors, updated, resend, customer, contact, location,
                                template, smsTemplate, evaluator);
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
     * Returns the contact archetype.
     *
     * @return the contact archetype
     */
    @Override
    protected String getContactArchetype() {
        return ContactArchetypes.PHONE;
    }

}
