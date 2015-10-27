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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.print.PrinterListener;
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
     * The events currently being printed
     */
    private List<ReminderEvent> events;

    /**
     * Constructs a {@link ReminderPrintProgressBarProcessor}.
     *
     * @param reminders  the reminders
     * @param processor  the reminder print processor
     * @param statistics the statistics
     */
    public ReminderPrintProgressBarProcessor(List<List<ReminderEvent>> reminders, ReminderPrintProcessor processor,
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
        this.processor = processor;
        processor.setListener(listener);
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
