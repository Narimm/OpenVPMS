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
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.print.InteractivePrinter;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;

import java.util.ArrayList;
import java.util.List;


/**
 * Processor for <em>act.patientReminderItemList</em> reminders.
 * <p/>
 * Prints all of the reminders to a report.
 *
 * @author Tim Anderson
 */
public class ReminderListProcessor extends PatientReminderProcessor {

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The printer listener.
     */
    private PrinterListener listener;


    /**
     * Constructs a {@link ReminderListProcessor}.
     *
     * @param reminderTypes the reminder types
     * @param rules         the reminder rules
     * @param practice      the practice
     * @param service       the archetype service
     * @param config        the reminder configuration
     * @param logger        the communication logger. May be {@code null}
     * @param context       the context
     * @param help          the help context
     */
    public ReminderListProcessor(ReminderTypes reminderTypes, ReminderRules rules, Party practice,
                                 IArchetypeService service, ReminderConfiguration config, CommunicationLogger logger,
                                 Context context, HelpContext help) {
        super(reminderTypes, rules, practice, service, config, logger);
        this.context = context;
        this.help = help;
    }

    /**
     * Returns the reminder item archetype that this processes.
     *
     * @return the archetype
     */
    @Override
    public String getArchetype() {
        return ReminderArchetypes.EXPORT_REMINDER;
    }

    /**
     * Registers a listener for printer events.
     * <p/>
     * This must be registered prior to processing any reminders.
     *
     * @param listener the listener
     */
    public void setListener(PrinterListener listener) {
        this.listener = listener;
    }

    /**
     * Processes reminders.
     *
     * @param state the reminder state
     */
    @Override
    public void process(State state) {
        List<Act> acts = new ArrayList<>();
        for (ObjectSet reminder : state.getReminders()) {
            acts.add(getReminder(reminder));
        }
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(ReminderArchetypes.REMINDER, context);
        IMObjectReportPrinter<Act> printer = new IMObjectReportPrinter<>(acts, locator, context);
        InteractivePrinter iPrinter = createPrinter(printer);
        iPrinter.setListener(listener);
        iPrinter.print();
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
        ContactMatcher matcher = createContactMatcher(ContactArchetypes.PHONE);
        for (ObjectSet reminder : reminders) {
            Contact contact = getContact(reminder, matcher);
            reminder.set("contact", contact);
        }
        return reminders;
    }

    /**
     * Logs reminder communications.
     *
     * @param state  the reminder state
     * @param logger the communication logger
     */
    protected void log(State state, CommunicationLogger logger) {
        String subject = Messages.get("reminder.log.list.subject");
        for (ObjectSet reminder : state.getReminders()) {
            populate(reminder);
            Party customer = getCustomer(reminder);
            if (customer != null) {
                Party patient = getPatient(reminder);
                String notes = getNote(reminder);
                Party location = getLocation(customer);
                Contact contact = (Contact) reminder.get("contact");
                String description = contact != null ? contact.getDescription() : "";
                logger.logPhone(customer, patient, description, subject, COMMUNICATION_REASON, null, notes, location);
            }
        }
    }

    /**
     * Creates a new interactive printer.
     *
     * @param printer the printer to delegate to
     * @return a new interactive printer
     */
    protected InteractivePrinter createPrinter(IMObjectReportPrinter<Act> printer) {
        return createPrinter(Messages.get("reporting.reminder.list.print.title"), printer, context, help);
    }

    /**
     * Creates a new interactive printer.
     *
     * @param title   the dialog title
     * @param printer the printer to delegate to
     * @param context the context
     * @param help    the help context
     * @return a new interactive printer
     */
    protected InteractivePrinter createPrinter(String title, IMObjectReportPrinter<Act> printer, Context context,
                                               HelpContext help) {
        return new InteractiveIMPrinter<>(title, printer, true, context, help);
    }

}
