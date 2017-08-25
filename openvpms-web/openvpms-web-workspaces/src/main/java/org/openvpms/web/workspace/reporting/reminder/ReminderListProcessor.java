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
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderConfiguration;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.archetype.rules.patient.reminder.ReminderType;
import org.openvpms.archetype.rules.patient.reminder.ReminderTypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
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
 * <p>
 * Prints all of the reminders to a report.
 *
 * @author Tim Anderson
 */
public class ReminderListProcessor extends PatientReminderProcessor {

    /**
     * The location.
     */
    private final Party location;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The printer factory.
     */
    private final IMPrinterFactory factory;

    /**
     * The printer listener.
     */
    private PrinterListener listener;


    /**
     * Constructs a {@link ReminderListProcessor}.
     *
     * @param reminderTypes the reminder types
     * @param reminderRules the reminder rules
     * @param patientRules  the patient rules
     * @param location      the practice location
     * @param practice      the practice
     * @param service       the archetype service
     * @param config        the reminder configuration
     * @param factory       the printer factory
     * @param logger        the communication logger. May be {@code null}
     * @param help          the help context
     */
    public ReminderListProcessor(ReminderTypes reminderTypes, ReminderRules reminderRules, PatientRules patientRules,
                                 Party location, Party practice, IArchetypeService service,
                                 ReminderConfiguration config, IMPrinterFactory factory, CommunicationLogger logger,
                                 HelpContext help) {
        super(reminderTypes, reminderRules, patientRules, practice, service, config, logger);
        this.location = location;
        this.help = help;
        this.factory = factory;
    }

    /**
     * Returns the reminder item archetype that this processes.
     *
     * @return the archetype
     */
    @Override
    public String getArchetype() {
        return ReminderArchetypes.LIST_REMINDER;
    }

    /**
     * Processes reminders.
     *
     * @param reminders the reminder state
     */
    @Override
    public void process(PatientReminders reminders) {
        List<Act> acts = new ArrayList<>();
        for (ReminderEvent reminder : reminders.getReminders()) {
            acts.add(reminder.getReminder());
        }
        print(acts);
    }

    /**
     * Determines if reminder processing is performed asynchronously.
     *
     * @return {@code true} if reminder processing is performed asynchronously
     */
    @Override
    public boolean isAsynchronous() {
        return true;
    }

    /**
     * Registers a listener for printer events.
     * <p>
     * This must be registered prior to processing any reminders.
     *
     * @param listener the listener
     */
    public void setListener(PrinterListener listener) {
        this.listener = listener;
    }

    /**
     * Prints reminders.
     *
     * @param reminders the reminders to print
     */
    protected void print(List<Act> reminders) {
        Context context = new LocalContext();
        context.setLocation(location);
        context.setPractice(getPractice());
        DocumentTemplateLocator locator = new ContextDocumentTemplateLocator(ReminderArchetypes.REMINDER, context);
        IMObjectReportPrinter<Act> printer = factory.createIMObjectReportPrinter(reminders, locator, context);
        InteractivePrinter iPrinter = createPrinter(printer, context);
        iPrinter.setListener(listener);
        iPrinter.print();
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
     */
    @Override
    protected PatientReminders prepare(List<ReminderEvent> reminders, ReminderType.GroupBy groupBy,
                                       List<ReminderEvent> cancelled, List<ReminderEvent> errors, List<Act> updated,
                                       boolean resend) {
        ContactMatcher matcher = createContactMatcher(ContactArchetypes.PHONE);
        for (ReminderEvent reminder : reminders) {
            Party customer = reminder.getCustomer();
            Party location = getLocation(customer);
            Contact contact = getContact(customer, matcher, reminder.getContact());
            populate(reminder, contact, location);
        }
        return new PatientReminders(reminders, groupBy, cancelled, errors, updated, resend);
    }

    /**
     * Logs reminder communications.
     *
     * @param state  the reminder state
     * @param logger the communication logger
     */
    protected void log(PatientReminders state, CommunicationLogger logger) {
        String subject = Messages.get("reminder.log.list.subject");
        for (ReminderEvent reminder : state.getReminders()) {
            Party customer = reminder.getCustomer();
            Party patient = reminder.getPatient();
            String notes = getNote(reminder);
            Party location = reminder.getLocation();
            Contact contact = reminder.getContact();
            String description = contact != null ? contact.getDescription() : "";
            logger.logPhone(customer, patient, description, subject, COMMUNICATION_REASON, null, notes, location);
        }
    }

    /**
     * Creates a new interactive printer.
     *
     * @param printer the printer to delegate to
     * @param context the context
     * @return a new interactive printer
     */
    protected InteractivePrinter createPrinter(IMObjectReportPrinter<Act> printer, Context context) {
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
