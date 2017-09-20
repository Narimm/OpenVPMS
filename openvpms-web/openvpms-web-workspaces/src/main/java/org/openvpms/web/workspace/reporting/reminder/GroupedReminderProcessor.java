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

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.party.ContactMatcher;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderCount;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;
import org.openvpms.web.workspace.reporting.ReportingException;

import java.util.ArrayList;
import java.util.List;

import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.NoCustomerGroupedReminderTemplate;
import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.NoPatientGroupedReminderTemplate;
import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.ReminderMissingDocTemplate;


/**
 * Processor for patient reminder where the reminders are grouped by customer.
 *
 * @author Tim Anderson
 */
public abstract class GroupedReminderProcessor extends PatientReminderProcessor {

    /**
     * Constructs a {@link GroupedReminderProcessor}.
     *
     * @param reminderTypes the reminder types
     * @param reminderRules the reminder rules
     * @param patientRules  the patient rules
     * @param practice      the practice
     * @param service       the archetype service
     * @param config        the reminder configuration
     * @param logger        the communication logger. May be {@code null}
     */
    public GroupedReminderProcessor(ReminderTypes reminderTypes, ReminderRules reminderRules, PatientRules patientRules,
                                    Party practice, IArchetypeService service, ReminderConfiguration config,
                                    CommunicationLogger logger) {
        super(reminderTypes, reminderRules, patientRules, practice, service, config, logger);
    }

    /**
     * Prepares reminders for processing.
     * <p>
     * This:
     * <ul>
     * <li>filters out any reminders that can't be processed due to missing data</li>
     * <li>adds meta-data for subsequent calls to {@link #process}</li>
     * </ul>
     *
     * @param reminders the reminders to prepare
     * @param groupBy   the reminder grouping policy. This determines which document template is selected
     * @param cancelled reminder items that will be cancelled
     * @param errors    reminders that can't be processed due to error
     * @param updated   acts that need to be saved on completion
     * @param resend    if {@code true}, reminders are being resent
     * @return the reminders to process
     * @throws ReportingException if the reminders cannot be prepared
     */
    @Override
    protected PatientReminders prepare(List<ReminderEvent> reminders, ReminderType.GroupBy groupBy,
                                       List<ReminderEvent> cancelled, List<ReminderEvent> errors, List<Act> updated,
                                       boolean resend) {
        DocumentTemplate template = null;
        List<ReminderEvent> toProcess = new ArrayList<>();
        Party customer = null;
        Contact contact = null;
        Party location = null;
        if (!reminders.isEmpty()) {
            ReminderEvent event = reminders.get(0);
            customer = event.getCustomer();
            contact = getContact(customer, createContactMatcher(), event.getContact());
            if (contact != null) {
                location = getLocation(customer);
                toProcess.addAll(reminders);
                populate(reminders, contact, location);
                template = getTemplate(toProcess, groupBy);
            } else {
                noContact(reminders, updated);
                errors.addAll(reminders);
            }
        }
        return prepare(toProcess, groupBy, cancelled, errors, updated, resend, customer, contact, location, template);
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
    protected GroupedReminders prepare(List<ReminderEvent> reminders, ReminderType.GroupBy groupBy,
                                       List<ReminderEvent> cancelled, List<ReminderEvent> errors, List<Act> updated,
                                       boolean resend, Party customer, Contact contact, Party location,
                                       DocumentTemplate template) {
        return new GroupedReminders(reminders, groupBy, cancelled, errors, updated, resend, customer, contact, location,
                                    template);
    }

    /**
     * Invoked when a customer doesn't have a contact.
     * Flags each reminder item as being in error.
     *
     * @param reminders the reminder sets
     * @param toSave    the updated reminder items
     */
    protected void noContact(List<ReminderEvent> reminders, List<Act> toSave) {
        String message = Messages.format("reporting.reminder.nocontact",
                                         DescriptorHelper.getDisplayName(getContactArchetype(), getService()));
        for (ReminderEvent event : reminders) {
            Act item = updateItem(event, ReminderItemStatus.ERROR, message);
            toSave.add(item);
        }
    }

    /**
     * Returns the document template to use for the specified reminders and grouping policy.
     *
     * @param reminders the reminders
     * @param groupBy   the reminder grouping policy
     * @return the document template
     * @throws ReportingException if the document template cannot be located
     */
    protected DocumentTemplate getTemplate(List<ReminderEvent> reminders, ReminderType.GroupBy groupBy) {
        DocumentTemplate template;
        if (reminders.size() > 1) {
            if (groupBy == ReminderType.GroupBy.CUSTOMER) {
                template = getConfig().getCustomerGroupedReminderTemplate();
                if (template == null) {
                    throw new ReportingException(NoCustomerGroupedReminderTemplate);
                }
            } else if (groupBy == ReminderType.GroupBy.PATIENT) {
                template = getConfig().getPatientGroupedReminderTemplate();
                if (template == null) {
                    throw new ReportingException(NoPatientGroupedReminderTemplate);
                }
            } else {
                throw new IllegalArgumentException("Multiple reminders specified for incorrect groupBy: " + groupBy);
            }
        } else {
            ReminderEvent first = reminders.get(0);
            ReminderType reminderType = getReminderType(first);
            if (reminderType == null) {
                throw new IllegalStateException("Cannot determine reminder type");
            }
            int reminderCount = first.getReminderCount();
            ReminderCount count = reminderType.getReminderCount(reminderCount);
            if (count == null) {
                throw new ReportingException(ReportingException.ErrorCode.NoReminderCount, reminderType.getName(),
                                             reminderCount);
            }
            template = count.getTemplate();
            if (template == null) {
                throw new ReportingException(ReminderMissingDocTemplate);
            }
        }
        return template;
    }

    /**
     * Creates a new contact matcher.
     *
     * @return a new contact matcher
     */
    protected ContactMatcher createContactMatcher() {
        return createContactMatcher(getContactArchetype());
    }

    /**
     * Returns the contact archetype.
     *
     * @return the contact archetype
     */
    protected abstract String getContactArchetype();

}
