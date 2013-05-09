/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.reporting.reminder;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;


/**
 * Prints reminders, updating a progress bar as it goes.
 *
 * @author Tim Anderson
 */
public class ReminderPrintProgressBarProcessor extends ReminderProgressBarProcessor {

    /**
     * The reminder printer.
     */
    private final ReminderPrintProcessor processor;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The events currently being printed
     */
    private List<ReminderEvent> events;

    /**
     * The mail context, used when printing interactively. May be {@code null}
     */
    private MailContext mailContext;


    /**
     * Constructs a {@code ReminderPrintProgressBarProcessor}.
     *
     * @param reminders     the reminders
     * @param groupTemplate the grouped reminder document template
     * @param statistics    the statistics
     * @param context       the context
     * @param help          the help context
     */
    public ReminderPrintProgressBarProcessor(List<List<ReminderEvent>> reminders, DocumentTemplate groupTemplate,
                                             Statistics statistics, Context context, HelpContext help) {
        super(reminders, statistics, Messages.get("reporting.reminder.run.print"));
        this.context = context;

        PrinterListener listener = new PrinterListener() {
            public void printed(String printer) {
                try {
                    setSuspend(false);
                    processCompleted(events);
                } catch (OpenVPMSException exception) {
                    processError(exception, events);
                }
            }

            public void cancelled() {
                notifyCompleted();
            }

            public void skipped() {
                setSuspend(false);
                skip(events);
            }

            public void failed(Throwable cause) {
                processError(cause, events);
            }
        };

        processor = new ReminderPrintProcessor(groupTemplate, listener, context, mailContext, help);
    }

    /**
     * Determines if reminders should always be printed interactively.
     *
     * @param interactive if {@code true}, reminders should always be printed interactively. If {@code false},
     *                    reminders will only be printed interactively if a printer needs to be selected
     */
    public void setInteractiveAlways(boolean interactive) {
        processor.setInteractiveAlways(interactive);
    }

    /**
     * Sets the mail context, used for mailing from print dialogs.
     *
     * @param context the mail context. May be {@code null}
     */
    public void setMailContext(MailContext context) {
        mailContext = context;
    }

    /**
     * Processes a set of reminder events.
     *
     * @param events the reminder events to process
     * @throws OpenVPMSException if the events cannot be processed
     */
    protected void process(List<ReminderEvent> events) {
        super.process(events);
        this.events = events;
        processor.process(events);
        if (processor.isInteractive()) {
            // need to process this print asynchronously, so suspend
            setSuspend(true);
        }
    }
}
