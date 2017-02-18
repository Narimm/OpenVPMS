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
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderCount;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;
import org.openvpms.web.workspace.reporting.ReportingException;

import java.util.ArrayList;
import java.util.List;

import static org.openvpms.web.workspace.reporting.ReportingException.ErrorCode.ReminderMissingDocTemplate;


/**
 * Processor for patient reminder where the reminders are grouped by customer.
 *
 * @author Tim Anderson
 */
public abstract class GroupedReminderProcessor extends PatientReminderProcessor {

    /**
     * The grouped reminder template.
     */
    private final DocumentTemplate groupTemplate;

    /**
     * Constructs a {@link GroupedReminderProcessor}.
     *
     * @param groupTemplate the grouped reminder template
     * @param reminderTypes the reminder types
     * @param rules         the reminder rules
     * @param practice      the practice
     * @param service       the archetype service
     * @param config        the reminder configuration
     * @param logger        the communication logger. May be {@code null}
     */
    public GroupedReminderProcessor(DocumentTemplate groupTemplate, ReminderTypes reminderTypes,
                                    ReminderRules rules, Party practice, IArchetypeService service,
                                    ReminderConfiguration config, CommunicationLogger logger) {
        super(reminderTypes, rules, practice, service, config, logger);
        this.groupTemplate = groupTemplate;
    }

    /**
     * Processes reminders.
     *
     * @param state the reminder state
     */
    @Override
    public void process(State state) {
        List<ObjectSet> reminders = state.getReminders();
        Contact contact = (Contact) reminders.get(0).get("contact");
        process(contact, reminders);
    }

    /**
     * Completes processing.
     *
     * @param state the reminder state
     */
    @Override
    public void complete(State state) {
        for (ObjectSet reminder : state.getReminders()) {
            complete(reminder, state);
        }
        save(state);
        CommunicationLogger logger = getLogger();
        if (logger != null && !state.getReminders().isEmpty()) {
            log(state, logger);
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
        List<ObjectSet> result = new ArrayList<>();
        ObjectSet set = reminders.get(0);
        Contact contact = getContact(set);
        if (contact != null) {
            for (ObjectSet reminder : reminders) {
                reminder.set("contact", contact);
            }
            result.addAll(reminders);
        } else {
            noContact(reminders, updated);
            errors.addAll(reminders);
        }
        return result;
    }

    /**
     * Invoked when a customer doesn't have a contact.
     * Flags each reminder item as being in error.
     *
     * @param reminders the reminder sets
     * @param toSave    the updated reminder items
     */
    protected void noContact(List<ObjectSet> reminders, List<Act> toSave) {
        String message = Messages.format("reporting.reminder.nocontact",
                                         DescriptorHelper.getDisplayName(getContactArchetype(), getService()));
        for (ObjectSet event : reminders) {
            Act item = updateItem(event, ReminderItemStatus.ERROR, message);
            toSave.add(item);
        }
    }

    /**
     * Processes a list of reminders.
     * <p/>
     * The reminders all belong to the same customer.
     * <p/>
     * This implementation delegates to {@link #process(Contact, List, DocumentTemplate)}.
     *
     * @param contact   the contact to send to
     * @param reminders the reminders
     */
    protected void process(Contact contact, List<ObjectSet> reminders) {
        DocumentTemplate template;

        populate(reminders);

        if (reminders.size() > 1) {
            template = groupTemplate;
        } else {
            ObjectSet first = reminders.get(0);
            ReminderType reminderType = getReminderType(first);
            if (reminderType == null) {
                throw new IllegalStateException("Cannot determine reminder type");
            }
            int reminderCount = first.getInt("reminderCount");
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
        process(contact, reminders, template);
    }

    /**
     * Returns the contact to use.
     *
     * @param reminder the reminder set
     * @return the contact, or {@code null} if none is found
     */
    protected Contact getContact(ObjectSet reminder) {
        return getContact(reminder, createContactMatcher());
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

    /**
     * Processes a list of reminders.
     *
     * @param contact   the contact to send to
     * @param reminders the reminders
     * @param template  the document template to use
     */
    protected abstract void process(Contact contact, List<ObjectSet> reminders, DocumentTemplate template);


    /**
     * Logs reminder communications.
     *
     * @param state  the reminder state
     * @param logger the communication logger
     */
    protected abstract void log(State state, CommunicationLogger logger);

}
