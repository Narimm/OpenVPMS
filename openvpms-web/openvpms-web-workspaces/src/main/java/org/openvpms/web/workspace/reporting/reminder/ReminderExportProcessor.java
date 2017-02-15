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

import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.rules.party.ContactMatcher;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderExporter;
import org.openvpms.archetype.rules.patient.reminder.ReminderItemStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypeCache;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.echo.servlet.DownloadServlet;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Processor that exports reminders using the {@link ReminderExporter}.
 *
 * @author Tim Anderson
 */
public class ReminderExportProcessor extends PatientReminderProcessor {

    /**
     * The practice location.
     */
    private final Party location;

    /**
     * Constructs a {@link ReminderExportProcessor}.
     *
     * @param reminderTypes the reminder types
     * @param rules         the reminder rules
     * @param location      the current practice location
     * @param practice      the practice
     * @param service       the archetype service
     * @param config        the reminder configuration
     * @param logger        the communication logger. May be {@code null}
     */
    public ReminderExportProcessor(ReminderTypeCache reminderTypes, ReminderRules rules, Party location, Party practice,
                                   IArchetypeService service, ReminderConfiguration config,
                                   CommunicationLogger logger) {
        super(reminderTypes, rules, practice, service, config, logger);
        this.location = location;
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
     * Processes reminders.
     *
     * @param state the reminder state
     */
    @Override
    public void process(State state) {
        List<ObjectSet> reminders = state.getReminders();
        List<ReminderEvent> events = new ArrayList<>();
        for (ObjectSet reminder : reminders) {
            events.add(createEvent(reminder));
        }
        ReminderExporter exporter = ServiceHelper.getBean(ReminderExporter.class);
        Document document = exporter.export(events);
        DownloadServlet.startDownload(document);
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
        List<ObjectSet> toProcess = new ArrayList<>();
        ContactMatcher matcher = createContactMatcher(ContactArchetypes.LOCATION);
        for (ObjectSet reminder : reminders) {
            Contact contact = getContact(reminder, matcher);
            if (contact != null) {
                reminder.set("contact", contact);
                toProcess.add(reminder);
            } else {
                String message = Messages.format("reporting.reminder.nocontact", DescriptorHelper.getDisplayName(
                        ContactArchetypes.LOCATION, getService()));
                Act item = updateItem(reminder, ReminderItemStatus.ERROR, message);
                updated.add(item);
                errors.add(reminder);
            }
        }
        return toProcess;
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
        return config.getEmailCancelDate(startTime);
    }

    /**
     * Completes processing.
     *
     * @param state the reminder state
     */
    @Override
    public void complete(State state) {
        List<ObjectSet> reminders = state.getReminders();
        for (ObjectSet reminder : reminders) {
            complete(reminder, state);
        }
        save(state);
        CommunicationLogger logger = getLogger();
        if (logger != null && !reminders.isEmpty()) {
            String subject = Messages.get("reminder.log.export.subject");
            for (ObjectSet reminder : reminders) {
                Contact contact = (Contact) reminder.get("contact");
                String notes = getNote(reminder);
                logger.logMail(getCustomer(reminder), getPatient(reminder), contact.getDescription(),
                               subject, COMMUNICATION_REASON, null, notes, location);
            }
        }
    }

    private ReminderEvent createEvent(ObjectSet reminder) {
        return new ReminderEvent(ReminderEvent.Action.EXPORT, getReminder(reminder),
                                 getReminderType(reminder), getPatient(reminder),
                                 getCustomer(reminder), (Contact) reminder.get("contact"), null);
    }

}
