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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Prints reminders, updating a progress bar as it goes.
 *
 * @author Tim Anderson
 */
public class ReminderPrintProgressBarProcessor extends ReminderProgressBarProcessor {

    /**
     * Constructs a {@link ReminderPrintProgressBarProcessor}.
     *
     * @param query     the query
     * @param processor the email processor
     */
    public ReminderPrintProgressBarProcessor(ReminderItemSource query, final ReminderPrintProcessor processor) {
        super(query, processor, Messages.get("reporting.reminder.run.print"));
        // if a single reminder is being printed, always display the print dialog, otherwise display it if there is
        // no default printer
        if (query instanceof SingleReminderItemSource) {
            processor.setInteractiveAlways(true);
        } else {
            processor.setInteractiveAlways(false);
        }

        PrinterListener listener = new PrinterListener() {
            public void printed(String printer) {
                try {
                    setSuspend(false);
                    if (processor.isAsynchronous()) {
                        processCompleted();
                    }
                } catch (OpenVPMSException exception) {
                    processError(exception);
                    if (processor.isAsynchronous()) {
                        processCompleted();
                    }
                }
            }

            public void cancelled() {
                notifyCompleted();
            }

            public void skipped() {
                setSuspend(false);
                skip();
            }

            public void failed(Throwable cause) {
                processError(cause);
                if (processor.isAsynchronous()) {
                    processCompleted();
                }
            }
        };
        processor.setListener(listener);
    }

}
