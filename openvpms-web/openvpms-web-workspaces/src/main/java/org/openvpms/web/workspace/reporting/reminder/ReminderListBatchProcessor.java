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

import org.openvpms.web.component.print.PrinterListener;


/**
 * Lists reminders for printing.
 *
 * @author Tim Anderson
 */
public class ReminderListBatchProcessor extends AbstractReminderBatchProcessor {

    /**
     * Constructs an {@link ReminderListBatchProcessor}.
     *
     * @param query     the query
     * @param processor the reminder processor
     */
    public ReminderListBatchProcessor(ReminderItemSource query, ReminderListProcessor processor) {
        super(query, processor, "reporting.reminder.run.list");
        processor.setListener(new PrinterListener() {
            public void printed(String printer) {
                try {
                    completed();
                } catch (Throwable error) {
                    notifyError(error);
                }
            }

            public void cancelled() {
                notifyCompleted();
            }

            public void skipped() {
                notifyCompleted();
            }

            public void failed(Throwable cause) {
                notifyError(cause);
            }
        });
    }
}
