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
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.print.ObjectSetReportPrinter;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.StaticDocumentTemplateLocator;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.customer.communication.CommunicationLogger;

import java.util.List;


/**
 * Prints reminders.
 *
 * @author Tim Anderson
 */
public class ReminderPrintProcessor extends GroupedReminderProcessor {

    /**
     * Determines if a print dialog is being displayed.
     */
    private boolean interactive;

    /**
     * Determines if the print dialog should always be displayed.
     */
    private boolean alwaysInteractive;

    /**
     * The printer to fallback to, if none is specified by the document templates. This is selected once.
     */
    private String fallbackPrinter;

    /**
     * The listener for printer events.
     */
    private PrinterListener listener;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * Constructs a {@link ReminderPrintProcessor}.
     *
     * @param help          the help context
     * @param reminderTypes the reminder types
     * @param rules         the reminder rules
     * @param practice      the practice
     * @param service       the archetype service
     * @param config        the reminder configuration
     * @param logger        the communication logger. May be {@code null}
     */
    public ReminderPrintProcessor(HelpContext help, ReminderTypes reminderTypes, ReminderRules rules,
                                  Party practice, IArchetypeService service, ReminderConfiguration config,
                                  CommunicationLogger logger) {
        super(reminderTypes, rules, practice, service, config, logger);
        this.help = help;
    }

    /**
     * Returns the reminder item archetype that this processes.
     *
     * @return the archetype
     */
    @Override
    public String getArchetype() {
        return ReminderArchetypes.PRINT_REMINDER;
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
     * Determines if reminder processing is performed asynchronously.
     *
     * @return {@code true} if reminder processing is performed asynchronously
     */
    @Override
    public boolean isAsynchronous() {
        return alwaysInteractive || interactive;
    }

    /**
     * Determines if reminders should always be printed interactively.
     *
     * @param interactive if {@code true}, reminders should always be printed interactively. If {@code false},
     *                    reminders will only be printed interactively if a printer needs to be selected
     */
    public void setInteractiveAlways(boolean interactive) {
        alwaysInteractive = interactive;
    }

    /**
     * Processes reminders.
     *
     * @param reminders the reminder state
     */
    @Override
    public void process(PatientReminders reminders) {
        GroupedReminders groupedReminders = (GroupedReminders) reminders;
        DocumentTemplateLocator locator = new StaticDocumentTemplateLocator(groupedReminders.getTemplate());
        List<ObjectSet> sets = reminders.getReminders();
        Context context = reminders.createContext(getPractice());
        if (sets.size() > 1) {
            IMPrinter<ObjectSet> printer = new ObjectSetReportPrinter(sets, locator, context);
            print(printer, context);
        } else {
            Act reminder = getReminder(sets.get(0));
            IMPrinter<Act> printer = new IMObjectReportPrinter<>(reminder, locator, context);
            print(printer, context);
        }
    }

    /**
     * Returns the contact archetype.
     *
     * @return the contact archetype
     */
    @Override
    protected String getContactArchetype() {
        return ContactArchetypes.LOCATION;
    }

    /**
     * Logs reminder communications.
     *
     * @param state  the reminder state
     * @param logger the communication logger
     */
    @Override
    protected void log(PatientReminders state, CommunicationLogger logger) {
        GroupedReminders reminders = (GroupedReminders) state;
        Party customer = reminders.getCustomer();
        Contact contact = reminders.getContact();
        Party location = reminders.getLocation();
        String subject = Messages.get("reminder.log.mail.subject");
        for (ObjectSet reminder : state.getReminders()) {
            String notes = getNote(reminder);
            Party patient = getPatient(reminder);
            logger.logMail(customer, patient, contact.getDescription(), subject, COMMUNICATION_REASON, null, notes,
                           location);
        }
    }

    /**
     * Invoked when reminders are printed.
     *
     * @param printer the printer
     */
    protected void onPrinted(String printer) {
        if (fallbackPrinter == null) {
            fallbackPrinter = printer;
        }
        if (listener != null) {
            listener.printed(printer);
        }
    }

    /**
     * Invoked when printing is cancelled.
     */
    protected void onPrintCancelled() {
        if (listener != null) {
            listener.cancelled();
        }
    }

    /**
     * Invoked when printing is skipped.
     */
    protected void onPrintSkipped() {
        if (listener != null) {
            listener.skipped();
        }
    }

    /**
     * Invoked when printing fails.
     */
    protected void onPrintFailed(Throwable cause) {
        if (listener != null) {
            listener.failed(cause);
        }
    }

    /**
     * Performs a print.
     * <p/>
     * If a printer is configured, the print will occur in the background, otherwise a print dialog will be popped up.
     *
     * @param printer the printer
     * @param context the context
     */
    private <T> void print(IMPrinter<T> printer, Context context) {
        final InteractiveIMPrinter<T> iPrinter = new InteractiveIMPrinter<>(printer, context, help);
        String printerName = printer.getDefaultPrinter();
        if (printerName == null) {
            printerName = fallbackPrinter;
        }
        interactive = alwaysInteractive || printerName == null;
        iPrinter.setInteractive(interactive);
        iPrinter.setMailContext(new CustomerMailContext(context, help));

        iPrinter.setListener(new PrinterListener() {
            @Override
            public void printed(String printer) {
                onPrinted(printer);
            }

            @Override
            public void cancelled() {
                onPrintCancelled();
            }

            @Override
            public void skipped() {
                onPrintSkipped();
            }

            @Override
            public void failed(Throwable cause) {
                onPrintFailed(cause);
            }
        });
        iPrinter.print(printerName);
    }

}
