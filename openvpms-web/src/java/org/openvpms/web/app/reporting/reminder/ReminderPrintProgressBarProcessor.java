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
 *
 *  $Id$
 */
package org.openvpms.web.app.reporting.reminder;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Prints reminders, updating a progress bar as it goes.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReminderPrintProgressBarProcessor extends ReminderProgressBarProcessor {

    /**
     * The reminder printer.
     */
    private final ReminderPrintProcessor processor;

    /**
     * The events currently being printed
     */
    private List<ReminderEvent> events;

    /**
     * The mail context, used when printing interactively. May be <tt>null</tt>
     */
    private MailContext mailContext;


    /**
     * Constructs a <tt>ReminderPrintProgressBarProcessor</tt>.
     *
     * @param reminders     the reminders
     * @param groupTemplate the grouped reminder document template
     * @param statistics    the statistics
     */
    public ReminderPrintProgressBarProcessor(List<List<ReminderEvent>> reminders, DocumentTemplate groupTemplate,
                                             Statistics statistics) {
        super(reminders, statistics, Messages.get("reporting.reminder.run.print"));

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

        processor = new ReminderPrintProcessor(groupTemplate, listener, mailContext);
    }

    /**
     * Determines if reminders should always be printed interactively.
     *
     * @param interactive if <tt>true</tt>, reminders should always be printed interactively. If <tt>false</tt>,
     *                    reminders will only be printed interactively if a printer needs to be selected
     */
    public void setInteractiveAlways(boolean interactive) {
        processor.setInteractiveAlways(interactive);
    }

    /**
     * Sets the mail context, used for mailing from print dialogs.
     *
     * @param context the mail context. May be <tt>null</tt>
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
